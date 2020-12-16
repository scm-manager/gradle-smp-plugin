package com.cloudogu.smp

import org.gradle.api.Project

class Tests {

  private static final String PROPERTY = 'ignoreTestFailures'

  static boolean shouldIgnoreTestFailures(Project project) {
    if (!project.ext.hasProperty(PROPERTY)) {
      return false
    }
    def property = project.ext.getProperty(PROPERTY)
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
