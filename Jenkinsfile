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

    stage('docker images build'){
      steps{
        script{
          sh '''
            #镜像ID
            IMAGE_ID=$(docker images | grep "$JOB_NAME" | awk \'{print $3}\')
            # 构建docker镜像
            if [ -n "$IMAGE_ID" ]; then
              echo "$JOB_NAME镜像id=$IMAGE_ID,skip..."
            else
              echo "$JOB_NAME镜像work dir:`pwd`,start creating"
              docker build -t $JOB_NAME .
            fi
          '''
        }
      }
    }

    stage('docker deploy run'){
      sh '''
    		#容器ID
    		CONTAINER_ID=$(docker ps -a | grep "$JOB_NAME" | awk \'{print $1}\')

    		if [[ -n "$CONTAINER_ID" ]]; then
    			echo "存在$JOB_NAME容器,容器ID=$CID,状态：$(docker inspect $JOB_NAME -f '{{.State.Status}}'),重建docker容器 ..."
    			docker stop $JOB_NAME
    	    docker rm -f $JOB_NAME
    	    docker run -d --name $JOB_NAME $JOB_NAME
    			echo "$JOB_NAME容器重建完成"
    		else
    			echo "不存在$JOB_NAME容器，docker-compose run创建容器..."
    			docker run -d --name $JOB_NAME $JOB_NAME
    			echo "$JOB_NAME容器创建完成"
    		fi
      '''
    }


	}

}
