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
          sh 'mvn verify --batch-mode -Divy.engine.list.url=http://zugprobldmas/job/Trunk_All/'
          archiveArtifacts '*/target/*.jar'
      }
    }
  }
}
