pipeline {
    environment {
        dockerrepo = 'docker-repo.andewil.com'
        imagename = "docker-repo.andewil.com/sitemonitor/server"
        registryCredential = 'bamboouploader'
        dockerImage = ''
        BUILD_VERSION = "0.0.${BUILD_NUMBER}"
    }

    tools{
        maven 'maven3'
        jdk 'OpenJDK11'
    }

    agent any

    stages {
        stage('initialization') {
            steps {
                sh 'java -version'
                sh 'mvn --version'
                sh 'echo HOME=${HOME}'
                sh 'echo PATH=${PATH}'
                sh 'echo M2_HOME=${M2_HOME}'
                echo 'BUILD_VERSION=$BUILD_VERSION'
            }
        }
        stage('build') {
            steps {
                sh 'mvn -B -DskipTests clean compile versions:set -DnewVersion=${BUILD_VERSION}'
                sh 'mvn -DskipTests package'
            }
        }

        stage('docker image DEV') {
            when {
                branch 'dev'
            }
            steps {
                sh 'docker build -t $imagename:dev .'
                sh 'docker push $imagename:dev'
            }

        }
        stage('docker image MASTER') {
            when {
                branch 'master'
            }
            steps {
                sh 'docker build -t $imagename:$BUILD_VERSION .'
                sh 'docker build -t $imagename:latest .'
                sh 'docker push $imagename:$BUILD_VERSION'
                sh 'docker push $imagename:latest'
            }
        }
        stage('deploy') {
            when {
                branch 'master'
            }
            steps {
                sh 'sh /var/jenkins_home/deployscripts/deploy-sitemonitor-server.sh'
            }
        }
     }

    post {
        success {
            sh '/var/jenkins_home/deployscripts/send-message.sh Jenkins "Job <b>$JOB_NAME</b> was completed and artifacts were deployed. Build version: <b>$BUILD_VERSION</b>" '
        }
        failure {
            sh '/var/jenkins_home/deployscripts/send-message.sh Jenkins "Job <b>$JOB_NAME</b> failed. Build version: <b>$BUILD_VERSION</b>" '
        }
    }
}
