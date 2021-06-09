#!groovy

def setCategory(comment) {

    

	if (comment.startsWith("TM_MIGRATE") | comment.startsWith("TM_MIGRATETEST")) {

	    return "migration"

	}

	else {

	    return "services"

	}

}


def checkoutCommitHash() {

    checkout scm

    if(!env.BUILD_COMMIT_HASH) {

        env.BUILD_COMMIT_HASH = getCommitHash()

    }

    sh "git checkout $BUILD_COMMIT_HASH"

}


def getCommitHash() {

    def gitCommit = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()

    return gitCommit

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

		echo "before stage preparation"

			

		stage("Preparation") {

		    checkout scm

			mavenHome = tool(name: 'maven 3.6', type: 'maven');

			

			//def GitUils = load("pipeline/GitUtils.groovy")

			echo "after gitutils"

			def commitHash = getCommitHash()

			echo "after commithash" 
			echo "Commit hash $commitHash"
			sh "git diff $commitHash"
			def changeLogSets = currentBuild.changeSets

			echo "after changesets"
			echo "changeset $changeLogSets"

			def PRComment = "services"

			echo "PRComment printed is $PRComment"

			

			category = setCategory(PRComment)

			

			for(changeSet in changeLogSets ) {

				for(entry in changeSet.items) {

					echo "${entry.commitId} by ${entry.author} on ${new Date(entry.timestamp)}: ${entry.msg}"

					def files = new ArrayList(entry.affectedFiles)

					for (file in files) {

						echo "  ${file.editType.name} ${file.path}"

						def tokens = file.path.split('/')

						echo "Tokens printed is $tokens"

						def parentFolder = tokens[0]

						echo "Parent folder printed is $parentFolder"

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

			if (category == "services"){

				currentModules = serviceFoldersChanged

				echo "currentmodules printed is $currentModules"

				}

			else{

				currentModules = migrationServiceFoldersChanged

				echo "currentmodules printed is $currentModules"

				}

		}

		/*withEnv([

                'MAVEN_HOME=' + mavenHome,

                "PATH=${mavenHome}/bin:${env.PATH}"

		])*/  
		
		

			stage ("Build") {

				for(module in currentModules) {

					dir(module) {

						def mvnHome = tool name: 'maven 3.6', type: 'maven' 
                        sh "${mvnHome}/bin/mvn package"
						

					}

					

				}
				
				
				}



			

                /*stage('SonarQube analysis') {
				//def mvnHome = tool name: 'maven 3.6', type: 'maven'
                withSonarQubeEnv('sonar-6') {
                // requires SonarQube Scanner for Maven 3.2+
               
                        sh "${mvnHome}/bin/mvn sonar:sonar"
						
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar'
                }
             }*/
			
			
		
    
    
    

   
    /*    stage('Preparation') {
			
			def mavenHome
            def deploymentUtils
                checkout scm
                deploymentUtils = load('pipeline/utils/DeploymentUtils.groovy')
                mavenHome = tool(name: 'maven-3.5.0', type: 'maven');
            }
            withEnv([
                    'MAVEN_HOME=' + mavenHome,
                    "PATH=${mavenHome}/bin:${env.PATH}"
            ]) 
                stage("dev-deploy"){
                    dir(APP_BASE){
                        sh "'${mavenHome}/bin/mvn' clean install"
                    }
                    deploymentUtils.appPackageDeploy(DEPLOYMENT, CF_ORG, CF_SPACE,APP_BASE, APP_NAME, APP_NAME_PREFIX)
                }
                currentBuild.result = "SUCCESS" */
				
				
				
         
    }
	}
    

catch(error) {

currentBuild.result = "FAILURE"

	throw err

 }
 
 

