package com.cloudogu.smp

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import groovy.json.JsonOutput


class WriteServerConfigTask extends DefaultTask {

  private SmpExtension extension
  private File outputFile
  private Closure<File> webappResolver = {
    def coordinates = "sonia.scm:scm-webapp:${extension.scmVersion}@war"
    def dependency = project.dependencies.create(coordinates)
    def configuration = project.configurations.detachedConfiguration(dependency)
    configuration.resolve()
    return configuration.files.first()
  }

  @Nested
  SmpExtension getExtension() {
    return extension
  }

  void setExtension(SmpExtension extension) {
    this.extension = extension
  }

  @OutputFile
  File getOutputFile() {
    return outputFile
  }

  void setOutputFile(File outputFile) {
    this.outputFile = outputFile
  }

  @Internal
  Closure<File> getWebappResolver() {
    return webappResolver
  }

  void setWebappResolver(Closure<File> webappResolver) {
    this.webappResolver = webappResolver
  }

  @TaskAction
  void write() {
    File directory = outputFile.getParentFile()
    if(!directory.exists() && !directory.mkdirs()) {
      throw new IllegalStateException("failed to create parent directory for server configuration: ${directory}")
    }
    if (outputFile.exists() && !outputFile.delete()) {
      throw new IllegalStateException("failed to delete existing server configuration: ${outputFile}")
    }

    if (extension.serverConfiguration.warFile == null) {
      webappResolver.delegate = {
        it.project = project
      }

      extension.serverConfiguration.warFile = webappResolver.call()
    }
    extension.serverConfiguration.home = extension.getScmHome(project)

    outputFile << JsonOutput.toJson(extension.serverConfiguration)
  }
}
