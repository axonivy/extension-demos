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
          maven cmd: 'deploy ' +
            ' -Divy.engine.list.url=https://jenkins.ivyteam.io/job/core_product/job/release%252F8.0/ ' +
            ' -Dproject-build-plugin.version=8.0.6 '

          archiveArtifacts '*/target/*.jar'
        }
      }
    }
  }
}
