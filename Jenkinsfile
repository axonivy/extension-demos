pipeline {
  agent {
    dockerfile true
  }

  options {
    buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '10'))
  }

  triggers {
    cron '@midnight'
  }

  stages {
    stage('build') {
      steps {
        script {
          maven cmd: 'clean deploy -Divy.engine.list.url=https://jenkins.ivyteam.io/job/core_product/job/master/'
          archiveArtifacts '*/target/*.jar'
        }
      }
    }
  }
}
