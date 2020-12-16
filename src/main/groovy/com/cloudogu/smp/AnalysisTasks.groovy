package com.cloudogu.smp

import org.gradle.api.Project

class AnalysisTasks {

  static void configure(Project project) {
    if (Environment.isCI()) {
      configureSonarQube(project)
    }
  }

  static void configureSonarQube(Project project) {
    project.plugins.apply('org.sonarqube')
    project.sonarqube {
      properties {
        property 'sonar.javascript.lcov.reportPaths', 'build/jest-reports/coverage/lcov.info'
      }
    }
  }
}
