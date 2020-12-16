package com.cloudogu.smp

import org.gradle.api.Project

class TestTasks {

  static void configure(Project project) {
    if (!project.ext.has('ignoreTestFailures')) {
      project.ext.ignoreTestFailures = false
    }

    project.afterEvaluate {
      project.test {
        useJUnitPlatform()
        ignoreFailures = Tests.shouldIgnoreTestFailures(project)
      }
    }
  }

}
