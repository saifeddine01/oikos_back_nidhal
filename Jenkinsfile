pipeline {
    agent any
    stages {
        stage('Test') {
            steps {
                echo 'Testing'
                sh '''
                export JAVA_HOME=/root/oikos/oikos-backend/oikos-backend/jdk-15.0.2+7
                mvn clean -Dmaven.test.skip=true
                mvn test
                cd search
                mvn clean -Dmaven.test.skip=true
                '''
            }
        }
        stage('Build') {
            steps {
                echo 'Building'
                sh '''
                export JAVA_HOME=/root/oikos/oikos-backend/oikos-backend/jdk-15.0.2+7
                mvn package -Dmaven.test.skip=true
                cd search
                mvn package -Dmaven.test.skip=true
                cd ..
                docker-compose down --remove-orphans
                docker-compose up -d --build oikos-backend oikos-search
                '''
            }
        }
    }
    post {
        always {
            junit 'target/surefire-reports/**/*.xml'
        }
    }
}
