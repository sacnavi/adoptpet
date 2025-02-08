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
    stage ('Initialize') {
      steps {
        sh '''
        echo "PATH = ${PATH}"
        echo "JAVA_HOME = ${JAVA_HOME}"
        echo "M2_HOME = ${M2_HOME}"
        java -version 
        '''
      }
    }
    stage('Checkout') {
      steps {
        checkout([$class: 'GitSCM', branches: [
          [name: "*/feat/jenkinsfile"]
        ], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [
          [credentialsId: 'ricp-git', url: 'https://github.com/sacnavi/adoptpet.git']
        ]])
        sh 'ls -la'
      }
    }

    stage('Compile') {
      steps {
        sh './mvnw clean compile'
        sh 'ls -la'
      }
    }
    /*
            stage('Test') {
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
    stage('Package') {
      steps {
        sh './mvnw package -Dmaven.test.skip=true'
      }
    }

    stage('Build') {
      steps {
        sh 'sudo docker build -t ${DOCKER_IMAGE} .'
      }
    }

    stage('Push') {
      steps {
        sh '''
        echo $DOCKERHUB_CREDENTIALS_PSW | sudo docker login - u $DOCKERHUB_CREDENTIALS_USR--password - stdin docker.io
        sudo docker push $ {DOCKER_IMAGE}
        '''
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