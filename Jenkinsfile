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
        // update timestamp to avoid rerun tests again and fix junit-plugin:
        // ERROR: Test reports were found but none of them are new
        sh 'touch build/test-results/*/*.xml'
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
