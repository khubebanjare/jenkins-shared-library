def call() {

    pipeline {

        agent any

        stages {

            stage('Checkout') {
                steps {
                    checkout scm
                }
            }

            stage('Build') {
                steps {
                    sh '''
                        chmod +x gradlew
                        ./gradlew clean build -x test
                    '''
                }
            }

            stage('Test') {
                steps {
                    sh './gradlew test'
                }
            }

            stage('Print Info') {
                steps {
                    script {
                        echo "Job Name      : ${env.JOB_NAME}"
                        echo "Build Number  : ${env.BUILD_NUMBER}"
                        echo "Git Branch    : ${env.BRANCH_NAME}"
                    }
                }
            }
        }

        post {

            success {
                echo 'Build Successful'
            }

            failure {
                echo 'Build Failed'
            }

            always {
                cleanWs()
            }
        }
    }
}