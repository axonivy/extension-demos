pipeline {
  agent {
    dockerfile true
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
        script {
          maven cmd: 'clean deploy -Divy.engine.list.url=https://jenkins.ivyteam.io/job/ivy-core_product/job/master/'
          archiveArtifacts '*/target/*.jar'
        }
      }
    }
  }
}
