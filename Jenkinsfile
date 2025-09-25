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
          maven cmd: 'clean deploy' // -Divy.engine.list.url=https://product.ivyteam.io -Divy.engine.version.latest.minor=true'
          archiveArtifacts '*/target/*.jar'
        }
      }
    }
  }
}
