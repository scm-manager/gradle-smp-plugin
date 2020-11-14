#!groovy
pipeline {

  agent {
    docker {
      image 'scmmanager/java-build:11.0.9_11.1'
      label 'docker'
    }
  }

  stages {

    stage('Check') {
      steps {
        sh './gradlew check'
        junit 'build/test-results/*/*.xml'
      }
    }

    stage('Build') {
      steps {
        sh './gradlew build'
        archiveArtifacts artifacts: 'build/libs/*.jar'
      }
    }

  }
}
