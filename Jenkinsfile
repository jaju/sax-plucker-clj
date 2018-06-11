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
    PATH = '/usr/local/bin:/usr/bin:/bin:$PATH'
  }
}