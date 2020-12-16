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
        property 'sonar.sources', 'src/main/java,src/main/js'
        property 'sonar.tests', 'src/test/java,src/main/js'
        property 'sonar.junit.reportPaths', 'build/test-results/test/,build/jest-reports/'
        property 'sonar.test.inclusions', 'src/**/*.test.ts,src/**/*.test.js,src/**/*.test.tsx,src/**/*.test.jsx,src/**/*Test.java,src/**/*ITCase.java'
        property 'sonar.nodejs.executable', ".gradle/nodejs/node-v${Environment.NODE_VERSION}-${Environment.CI_OS}-${Environment.CI_ARCH}/bin/node"
      }
    }
  }
}
