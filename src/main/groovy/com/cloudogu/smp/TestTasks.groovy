package com.cloudogu.smp

import org.gradle.api.Project
import org.gradle.testing.jacoco.plugins.JacocoPlugin

class TestTasks {

  static void configure(Project project) {
    if (Environment.isCI()) {
      project.plugins.apply(JacocoPlugin)

      project.jacocoTestReport {
        reports {
          xml.enabled true
        }
      }
    }

    project.test {
      useJUnitPlatform()
      if (Environment.isCI()) {
        ignoreFailures = true
        finalizedBy project.jacocoTestReport
      }
    }
  }

}
