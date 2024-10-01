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

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

class ScmServerConfiguration {

  @Input
  @Optional
  private String warFile

  @Input
  @Optional
  private String home

  @Input
  private int port = 8081

  @Input
  @Optional
  private String contextPath = "/scm"

  @Input
  private boolean disableCorePlugins = false

  @Input
  @Optional
  private String stage = "DEVELOPMENT"

  @Input
  private int headerSize = 16384

  @Input
  @Optional
  private String loggingConfiguration

  @Input
  private boolean openBrowser = true

  String getWarFile() {
    return warFile
  }

  void setWarFile(String warFile) {
    this.warFile = warFile
  }

  String getHome() {
    return home
  }

  void setHome(String home) {
    this.home = home
  }

  int getPort() {
    return port
  }

  void setPort(int port) {
    this.port = port
  }

  String getContextPath() {
    return contextPath
  }

  void setContextPath(String contextPath) {
    this.contextPath = contextPath
  }

  boolean getDisableCorePlugins() {
    return disableCorePlugins
  }

  void setDisableCorePlugins(boolean disableCorePlugins) {
    this.disableCorePlugins = disableCorePlugins
  }

  String getStage() {
    return stage
  }

  void setStage(String stage) {
    this.stage = stage
  }

  int getHeaderSize() {
    return headerSize
  }

  void setHeaderSize(int headerSize) {
    this.headerSize = headerSize
  }

  String getLoggingConfiguration() {
    return loggingConfiguration
  }

  void setLoggingConfiguration(String loggingConfiguration) {
    this.loggingConfiguration = loggingConfiguration
  }

  boolean getOpenBrowser() {
    return openBrowser
  }

  void setOpenBrowser(boolean openBrowser) {
    this.openBrowser = openBrowser
  }
}
