#!groovy



def mergePullRequest() {

    step([$class: 'GhprbPullRequestMerge', 
	         allowMergeWithoutTriggerPhrase: true,
			 deleteOnMerge: true,
             disallowOwnCode: false, 
			 failOnNonMerge: true, 
			 mergeComment: 'Merged', 
			 onlyAdminsMerge: false
	     ])

}



try {



    node() {





        def mavenHome

        def utProjects = ["devtest1", "devtest2" , "devtest3"]

        stage('Preparation') {

            checkout scm

            mavenHome = tool(name: 'maven 3.6', type: 'maven');

        }

        withEnv([

                'MAVEN_HOME=' + mavenHome,

                "PATH=${mavenHome}/bin:${env.PATH}"

        ]) {

            stage('Build') {

                for (project in utProjects) {

                    dir(project) {

                        sh "'${mavenHome}/bin/mvn' clean test"

                        //sh "'${mavenHome}/bin/mvn' clean "

                    }

                }

            }

        }

        stage('Merge') {

            currentBuild.result = "SUCCESS"

            if (ghprbCommentBody.startsWith("TM_MERGE")) {

                node() {

                    checkout scm

                    mergePullRequest()

                }

            }

        }



    }



}

catch (error) {

    currentBuild.result = "FAILURE"

    throw error

}