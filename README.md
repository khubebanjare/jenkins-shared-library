# jenkins-shared-library
Centralized Jenkins Shared Library providing reusable CI/CD pipeline components for Spring Boot microservices, including build automation, testing, code quality analysis, Docker image creation, security scanning, and Kubernetes deployment.

## Features

- Gradle Build Automation
- Unit & Integration Testing
- JaCoCo Code Coverage
- PIT Mutation Testing
- SonarQube Analysis
- Docker Build & Push
- Kubernetes Deployment
- Security Scanning
- OpenTelemetry Integration
- Grafana Monitoring Support
- Environment-Specific Deployments (Dev, QA, UAT, Prod)

## Supported Services

- auth-service
- wallet-service
- company-service
- employee-service
- department-service
- api-gateway
- config-server

## Usage

```groovy
@Library('shared-library') _

microservicePipeline(
    serviceName: 'auth-service',
    imageName: 'khubebanjare/auth-service'
)
