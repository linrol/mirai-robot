pipeline {

	agent any
	
	stages {
	
		stage('git clone'){
			steps {
				script{
					git branch: 'master', credentialsId: 'githubId', url: 'https://github.com/linrol/mirai-robot.git'
					echo "code git clone success"
				}
			}
	  }
	    
	  stage('maven build'){
	    steps {
			  script{
				  dir("./"){
				    sh "mvn clean package -Dmaven.test.skip=true"
				  }
			  }
		  }
	  }
		
		stage('server deploy run'){
		  steps {
			  script {
				  withEnv(['JENKINS_NODE_COOKIE=background_job']) {
	          sh "cp -r ./target/*.jar ./run.sh /root/web/app/mirai/"
	          sh "chmod +x /root/web/app/mirai/run.sh && cd /root/web/app/mirai/ && ./run.sh restart"
          }
        }
      }
		}
				
	}

}
