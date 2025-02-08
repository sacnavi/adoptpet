pipeline {
  agent any

  environment {
    DOCKERHUB_CREDENTIALS = credentials('ricp-docker')
    DOCKER_IMAGE = 'sacnavi/adoptpetsvc:v7'
  }

  tools {
    jdk 'JDK17'
  }

  stages {
    stage('Cloning code') {
      steps {
        checkout([$class: 'GitSCM', branches: [
          [name: '*/feat/jenkinsfile']
        ], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [
          [credentialsId: 'ricp-git', url: 'https://github.com/sacnavi/adoptpet.git']
        ]])
        sh 'ls -la'
      }
    }

    stage('Compiling code') {
      steps {
        sh './mvnw clean compile'
        sh 'ls -la'
      }
    }
    /*
            stage('Running tests') {
                steps {
                    sh './mvnw test'
                }
                post {
                    always {
                        junit 'target/surefire-reports/*.xml' // Informar resultados de JUnit
                    }
                }
            }
    */
    stage('Packaging application') {
      steps {
        sh './mvnw package -Dmaven.test.skip=true'
      }
    }

    stage('Building image') {
      steps {
        sh 'sudo docker build -t ${DOCKER_IMAGE} .'
      }
    }

    stage('Login & Push') {
      steps {
        sh ''
        '
        echo $DOCKERHUB_CREDENTIALS_PSW | sudo docker login - u $DOCKERHUB_CREDENTIALS_USR--password - stdin docker.io
        sudo docker push $ {
          DOCKER_IMAGE
        }
        ''
        '
      }
    }
  }

  post {
    success {
      echo 'Image built successfully'
    }
    failure {
      echo 'There were some errors, look at console output for details.'
    }
  }
}