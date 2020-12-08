package com.cloudogu.smp

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.util.VersionNumber

class VersionTasks {

  static void configure(Project project) {
    project.tasks.register("setVersion", SetVersionTask) {
      it.group = "help"
      it.description = "Set version for the plugin e.g.: setVersion --newVersion=x.y.z)"
    }
    project.tasks.register("setVersionToNextSnapshot", SetVersionToNextSnapshot) {
      it.group = "help"
      it.description = "Set version of the plugin to the next snapshot version"
    }
  }

  private static void setVersion(Project project, String newVersion) {
    Properties properties = new Properties()

    File propertiesFile = new File(project.rootDir, 'gradle.properties')
    propertiesFile.withInputStream { stream ->
      properties.load(stream)
    }

    if (properties.version == newVersion) {
      println "project uses already version ${newVersion}"
      return
    }

    println "set version from ${properties.version} to ${newVersion}"

    properties.version = newVersion
    propertiesFile.withOutputStream { stream ->
      properties.store(stream, 'gradle properties')
    }
  }

  static class SetVersionTask extends DefaultTask {

    @Option(option = "newVersion", description = "Sets new version for project")
    String newVersion

    @TaskAction
    void execute() {
      if (!newVersion?.trim()) {
        throw new GradleException("newVersion option is required e.g.: --newVersion=x.y.z")
      }
      setVersion(project, newVersion)
    }
  }

  static class SetVersionToNextSnapshot extends DefaultTask {

    @TaskAction
    void execute() {
      VersionNumber v = VersionNumber.parse(project.version)
      String version = "${v.major}.${v.minor}.${v.micro + 1}-SNAPSHOT"
      setVersion(project, version)
    }
  }

}
