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

            stage('JaCoCo Coverage') {
                steps {
                    retry(2) {
                        timeout(time: 10, unit: 'MINUTES') {
                            sh './gradlew :auth-service:jacocoTestReport --no-daemon'
                        }
                    }
                }
            }

            stage('Mutation Testing') {
                steps {
                    retry(2) {
                        timeout(time: 15, unit: 'MINUTES') {
                            sh './gradlew :auth-service:pitest --no-daemon'
                        }
                    }
                }
            }

            stage('SonarQube Analysis') {
                steps {
                    retry(2) {
                        timeout(time: 10, unit: 'MINUTES') {
                            withSonarQubeEnv('SonarQube') {
                                sh './gradlew :auth-service:sonar --no-daemon'
                            }
                        }
                    }
                }
            }
        }


        post {
            always {

                junit(
                        allowEmptyResults: true,
                        skipPublishingChecks: true,
                        testResults: '**/build/test-results/test/*.xml'
                )

                archiveArtifacts(
                        artifacts: 'auth-service/build/reports/jacoco/test/html/**',
                        allowEmptyArchive: true
                )

                archiveArtifacts(
                        artifacts: 'auth-service/build/reports/pitest/**',
                        allowEmptyArchive: true
                )

                publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'auth-service/build/reports/jacoco/test/html',
                        reportFiles: 'index.html',
                        reportName: 'JaCoCo Coverage Report'
                ])

                echo "==================================="
                echo "QUALITY REPORTS"
                echo "==================================="

                echo "JaCoCo Report:"
                echo "${env.BUILD_URL}JaCoCo_20Coverage_20Report/"

                echo "PIT Mutation Report:"
                echo "${env.BUILD_URL}artifact/auth-service/build/reports/pitest/index.html"

                echo "SonarQube Dashboard:"
                echo "http://localhost:9000/dashboard?id=auth-service"

                cleanWs()
            }
            success {
                emailext(
                        subject: "✅ SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: """
                Build Successful

                Project: ${env.JOB_NAME}
                Build Number: ${env.BUILD_NUMBER}

                Build URL:
                ${env.BUILD_URL}
            """,
                        to: "info.khube@gmail.com"
                )
            }

            failure {
                emailext(
                        subject: "❌ FAILURE: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: """
                Build Failed

                Project: ${env.JOB_NAME}
                Build Number: ${env.BUILD_NUMBER}

                Check Console Output:
                ${env.BUILD_URL}

                Possible Causes:
                - Unit Test Failure
                - JaCoCo Threshold Failure
                - PIT Mutation Failure
                - SonarQube Quality Gate Failure
                - Docker Build Failure
                - Kubernetes Deployment Failure
            """,
                        to: "info.khube@gmail.com"
                )
            }
        }
    }
}