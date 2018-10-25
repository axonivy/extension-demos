pipeline {
  agent {
    docker {
      image 'maven:3.5.4-jdk-8'
    }
  }

  options {
    buildDiscarder(logRotator(artifactNumToKeepStr: '10'))
  }

  triggers {
    cron '@midnight'
  }

  stages {
    stage('build') {
      steps {
          sh 'mvn verify -Divy.engine.list.url=http://zugprobldmas/job/Trunk_All/'
      }
    }
  }
}
