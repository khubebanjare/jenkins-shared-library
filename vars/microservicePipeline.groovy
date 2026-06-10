def call(Map config = [:]) {

    pipeline {

        agent any

        environment {
            SERVICE_NAME = config.serviceName
            IMAGE_NAME   = config.imageName
        }

        stages {

            stage('Checkout') {
                steps {
                    checkout scm
                }
            }

            stage('Build') {
                steps {
                    sh './gradlew clean build'
                }
            }

            stage('Unit Test') {
                steps {
                    sh './gradlew test'
                }
            }

            stage('JaCoCo Coverage') {
                steps {
                    retry(2) {
                        timeout(time: 10, unit: 'MINUTES') {
                            sh './gradlew :jacocoTestReport --no-daemon'
                        }
                    }
                }
            }

            stage('Mutation Testing') {
                steps {
                    retry(2) {
                        timeout(time: 15, unit: 'MINUTES') {
                            sh './gradlew :pitest --no-daemon'
                        }
                    }
                }
            }

            stage('SonarQube') {
                steps {
                    sh './gradlew sonar'
                }
            }

            stage('Docker Build') {
                steps {
                    sh """
                    docker build \
                    -t ${IMAGE_NAME}:${BUILD_NUMBER} .
                    """
                }
            }

            stage('Push Docker Image') {
                steps {
                    sh """
                    docker push ${IMAGE_NAME}:${BUILD_NUMBER}
                    """
                }
            }

            stage('Deploy') {
                steps {
                    echo "Deploying ${SERVICE_NAME}"
                }
            }
        }
    }
}