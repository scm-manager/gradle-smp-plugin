package com.cloudogu.smp

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional

class SmpExtension implements Serializable {

  @Input
  String scmVersion = "2.0.0"

  @Input
  String group = "sonia.scm.plugins"

  @Input
  @Optional
  String name

  @Input
  @Optional
  String displayName

  @Input
  @Optional
  String description

  @Input
  @Optional
  String author

  @Input
  @Optional
  String category

  @Input
  @Optional
  String avatarUrl

  @Input
  boolean core = false

  @Nested
  Conditions pluginConditions = new Conditions()

  @Nested
  OpenApiSpec openApiSpec = new OpenApiSpec()

  @Nested
  ScmServerConfiguration serverConfiguration = new ScmServerConfiguration()

  def conditions(Closure<Void> closure) {
    closure.delegate = pluginConditions
    closure.call()
  }

  def openapi(Closure<Void> closure) {
    closure.delegate = openApiSpec
    closure.call()
  }

  def run(def closure) {
    closure.delegate = serverConfiguration
    closure.call()
  }

  File getScmHome(Project project) {
    if (project.hasProperty("scm.home")) {
      return new File(project.getProperty("scm.home"))
    }
    def home = serverConfiguration.getHome()
    if (home != null) {
      return new File(home)
    }
    return new File(project.buildDir, "scm-home")
  }

  String getName(Project project) {
    if (name != null) {
      return name
    }
    return project.name
  }

  File getServerConfigurationFile(Project project) {
    return new File(project.buildDir, "server" + File.separator + "config.json")
  }

  class Conditions {
    @Input
    @Optional
    String os

    @Input
    @Optional
    String arch
  }

  class OpenApiSpec {

    @Input
    @Optional
    Set<String> packages = new LinkedHashSet<>()

  }

}
