package com.cloudogu.smp

import org.gradle.api.Project

class Tests {

  static boolean shouldIgnoreTestFailures(Project project) {
    def property = project.ext.getProperty('ignoreTestFailures')
    if (property instanceof Boolean) {
      return property
    } else if (property instanceof String) {
      return Boolean.parseBoolean(property)
    } else {
      throw new IllegalArgumentException(
        'failed to parse property ignoreTestFailures, only String or boolean is allowed'
      )
    }
  }

}
