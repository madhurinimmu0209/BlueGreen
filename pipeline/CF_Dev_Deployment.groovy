//Deployment to CloudFoundry
node("ccone-slave") {

	def mavenHome

	def deploymentUtils
	try {
//Stage preparation

		stage('Preparation') {

// Checking out SCM
			checkout scm
            //Loading utilities file
			deploymentUtils = load('pipeline/utils/DeploymentUtils.groovy')

			mavenHome = tool(name: 'maven-3.5.0', type: 'maven');

		}

		withEnv([

			'MAVEN_HOME=' + mavenHome,

			"PATH=${mavenHome}/bin:${env.PATH}"

		]) {
		
		// Starting deployment

			stage("dev-deploy"){

				dir(APP_BASE){

					sh "'${mavenHome}/bin/mvn' clean install"

				}
				//Loading CF_Details properties file
				Properties props = new Properties()
				File propsFile = new File('C://Users/mkoneti/eclipse-workspace/bluegreentestv1/pipeline/properties/CF_Details.properties')
				props.load(propsFile.newDataInputStream())
			
				// Invoking deployment script
				deploymentUtils.runDeployScript(
						props.getProperty('DATACENTER'),
						props.getProperty('CF_ORG'),
						props.getProperty('CF_SPACE'),
						props.getProperty('APP_BASE'),
						props.getProperty('APP_NAME'),
						props.getProperty('APP_NAME_PREFIX')
						)

			}

			currentBuild.result = "SUCCESS"

		}

	}

	catch (error) {

		currentBuild.result = "FAILURE"

		throw error

	}

}
