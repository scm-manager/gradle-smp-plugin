package com.cloudogu.smp

import org.gradle.api.Project

class Environment {

  static final String NODE_VERSION = "21.6.2"
  static final String YARN_VERSION = "1.22.15"

  static final String CI_OS = "linux"
  static final String CI_ARCH = "x64"

  static boolean isCI() {
    return isEnvAvailable("JENKINS_URL") && isEnvAvailable("BUILD_ID")
  }

  private static boolean isEnvAvailable(String key) {
    String value = System.getenv(key)
    return value != null && !value.trim().isEmpty()
  }

}
