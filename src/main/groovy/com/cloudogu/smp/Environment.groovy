/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.smp

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
