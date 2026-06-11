def call() {

    pipeline {

        agent any

        stages {

            stage('Initialize') {
                steps {
                    script {

                        env.SERVICE_NAME = env.JOB_NAME.replace('-pipeline', '')
                        env.IMAGE_NAME = "khubebanjare/${env.SERVICE_NAME}"

                        echo "================================="
                        echo "JOB_NAME      = ${env.JOB_NAME}"
                        echo "SERVICE_NAME  = ${env.SERVICE_NAME}"
                        echo "IMAGE_NAME    = ${env.IMAGE_NAME}"
                        echo "BRANCH_NAME   = ${env.BRANCH_NAME}"
                        echo "================================="
                    }
                }
            }

            stage('Checkout') {
                steps {
                    checkout scm
                }
            }

            stage('Build') {
                steps {
                    dir(env.SERVICE_NAME) {
                        sh '''
                            chmod +x gradlew
                            ./gradlew clean build
                        '''
                    }
                }
            }

            stage('Test') {
                steps {
                    dir(env.SERVICE_NAME) {
                        sh './gradlew test'
                    }
                }
            }
        }

        post {
            success {
                echo 'Pipeline completed successfully'
            }

            failure {
                echo 'Pipeline failed'
            }

            always {
                cleanWs()
            }
        }
    }
}