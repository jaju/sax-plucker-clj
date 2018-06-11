pipeline {
  agent any
  stages {
    stage('test') {
      steps {
        sh 'lein test'
      }
    }
    stage('uberjar') {
      steps {
        sh 'lein uberjar'
      }
    }
    stage('deploy') {
      steps {
        sh 'lein install'
      }
    }
  }
  environment {
    PATH = '/usr/local/bin:/usr/bin:/bin:$PATH'
  }
}