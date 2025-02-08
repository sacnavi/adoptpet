pipeline {
    agent any
    tools {
        jdk 'JDK17'
    }

    stages {
        stage('Cloning code') {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: '*/feat/jenkinsfile']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'ricp-git', url: 'https://github.com/sacnavi/adoptpet.git']]])
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