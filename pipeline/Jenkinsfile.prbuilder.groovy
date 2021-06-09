#!groovy

def mergePullRequest() {
	step([$class  : "ghprbPullRequestMerge", allowMergewithoutTriggerPhase : false, deleteonMerge : true,
	     disallowOwnCode : false, fallonNonMerge : true, mergeComment : "Merged" , onlyAdminsMerge : false])
}

def setCategory(comment) {
    
	if (comment.startsWith("TM_MIGRATE") | comment.startsWith("TM_MIGRATETEST")) {
	    return "migration"
	}
	else {
	    return "services"
	}
}

try {

	node() {
	
		
		def tm_services_modules = ["devtest1", "devtest2" , "devtest3"]
		def tm_migration_service_modules = ["tenant-migration"]
		def foldersChanged = []
		def serviceFoldersChanged = []
		def migrationServiceFoldersChanged = []
		def category
		def mavenHome
		def currentModules
			
		stage("Preparation") {
		
			checkout scm
			mavenHome = tool(name: 'maven 3.6', type: 'maven');
			def currentDir = pwd()
			//def GitUtils = load("${currentDir}/utils/GitUtils.groovy")
			def commitHash = GitUtils.getCommitHash()
			def changeLogSets = currentBuild.changeSets
			//def PRComment = ghprbCommentBody
			
			//category = setCategory(PRComment)
			
			for(changeSet in changeLogSets ) {
				for(entry in changeSet.items) {
					echo "${entry.commitId} by ${entry.author} on ${new Date(entry.timestamp)}: ${entry.msg}"
					def files = new ArrayList(entry.affectedFiles)
					for (file in files) {
						echo "  ${file.editType.name} ${file.path}"
						def tokens = file.path.split('/')
						def parentFolder = tokens[0]
						foldersChanged.add(parentFolder)						
					}
				
				}
			}
			
			for(folder in foldersChanged.toSet()) {
				if(tm_services_modules.contains(folder)) {
					echo "Service changed : $folder"
					serviceFoldersChanged.add(folder)
				}
				else if(tm_migration_service_modules.contains(folder)) {
					echo "Migration Service changed : $folder"
					migrationServiceFoldersChanged.add(folder)
				}
			}
			if (category == "services")
				currentModules = serviceFoldersChanged
			else
				currentModules = migrationServiceFoldersChanged
		}
		withEnv([
                'MAVEN_HOME=' + mavenHome,
                "PATH=${mavenHome}/bin:${env.PATH}"
		])  {
			stage ("Build") {
				for(module in currentModules) {
					dir(module) {
						sh "'${mavenHome}/bin/mvn' clean test"
					}
					
				}
			
			}
		}

		stage("Merge") {
			currentBuild.result = "SUCCESS"
			if ((category == "services" && ghprbCommentBody.startsWith("TM_MERGE")) | (category == "migration" && ghprbCommentBody.startsWith("TM_MIGRATE") && !ghprbCommentBody.contains("TEST"))) {
				node() {
					checkout scm
					mergePullRequest()
				}
			}
			//triggerMasterBuild(commitHash, currentModules,category)
		}
	}

}
catch(error) {

    currentBuild.result = "FAILURE"
	throw err
}


def triggerMasterBuild(commitHash, modules,category) {
    echo "Starting master build pipeline : $modules"
    build job: 'build_master',
            parameters: [
                    string(name: 'BUILD_COMMIT_HASH', value: commitHash),
                    string(name: 'APP_BASE', value: modules),
		    string(name: 'CATEGORY', value: category)
            ], propagate: false, wait: false
}
