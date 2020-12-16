package com.cloudogu.smp

import org.gradle.api.Project

class Environment {

  static boolean isCI() {
    return isEnvAvailable("JENKINS_URL") && isEnvAvailable("BUILD_ID")
  }

  private static boolean isEnvAvailable(String key) {
    String value = System.getenv(key)
    return value != null && !value.trim().isEmpty()
  }

}
