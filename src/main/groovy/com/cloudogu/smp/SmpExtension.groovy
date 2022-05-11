package com.cloudogu.smp

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional

abstract class SmpExtension implements Serializable {

  @Input
  final Property<String> scmVersion

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

  @Input
  Map<String,String> sonarProperties = [:]

  @Nested
  Conditions pluginConditions = new Conditions()

  @Nested
  OpenApiSpec openApiSpec = new OpenApiSpec()

  @Nested
  ScmServerConfiguration serverConfiguration = new ScmServerConfiguration()

  SmpExtension(Project project) {
    this.scmVersion = project.objects.property(String).convention("2.0.0")
  }

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

  def sonar(def closure) {
    closure.delegate = new Sonar()
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

  class Sonar {

    public void property(String key, String value) {
      sonarProperties.put(key, value)
    }

  }

}
