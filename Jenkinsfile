pipeline {
  agent any
  stages {
    stage('build') {
      steps {
        sh 'lein uberjar'
      }
    }
    stage('test') {
      steps {
        sh 'lein test'
      }
    }
  }
  environment {
    PATH = '/usr/local/bin:/usr/bin:/bin:$PATH'
  }
}