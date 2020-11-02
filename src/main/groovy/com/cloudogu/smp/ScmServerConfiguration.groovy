package com.cloudogu.smp

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

class ScmServerConfiguration {

  private String warFile

  @Input
  @Optional
  private String home

  @Input
  @Optional
  private int port = 8081

  @Input
  @Optional
  private String contextPath = "/scm"

  @Input
  @Optional
  private boolean disableCorePlugins = false

  @Input
  @Optional
  private String stage = "DEVELOPMENT"

  @Input
  @Optional
  private int headerSize = 16384

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

  File getHome(Project project) {
    if (home != null) {
      return new File(home)
    }
    return new File(project.buildDir, "scm-home")
  }

  File getFile(Project project) {
    return new File(project.buildDir, "server" + File.separator + "config.json")
  }
}
