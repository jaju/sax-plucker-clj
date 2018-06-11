pipeline {
  agent any
  stages {
    stage('build') {
      steps {
        sh 'lein uberjar'
      }
    }
  }
  environment {
    PATH = '/usr/local/bin:$PATH'
  }
}