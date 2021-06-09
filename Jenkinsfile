
node(){
   stage("git checkout"){
      checkout scm
     
   }
		
  stage("Build"){
    def mvnHome = tool name: 'MAVEN_HOME', type: 'maven'
    def directory = "devtest1"
		dir(directory){
      
            sh "${mvnHome}/bin/mvn package"
		}	
      }
	
	stage("Upload artifact to S3"){		
	  
	withCredentials([aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'AWSCreds', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
  		 s3Upload bucket: 'bucket1-madhu', file: '/var/lib/jenkins/workspace/PipelineDemo/devtest1/target/devtest1.war', path: 'project1/'
}
		
		
		
	
	}
	stage("Download artifact from S3"){		
		
		withCredentials([aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'AWSCreds', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
		s3Download(file:'devtest1.war', bucket:'bucket1-madhu', path:'project1/devtest1.war', force:true)	
			
  		 
}
		
		
		
		
		
		
	
	
	}
	
	stage("Dummy"){
	}
  
}
