package com.cloudogu.smp

import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class WriteServerConfigTask extends DefaultTask {

  private SmpExtension extension
  private File outputFile
  private Configuration configuration

  @InputFiles
  Configuration getConfiguration() {
    return configuration
  }

  void setConfiguration(Configuration configuration) {
    this.configuration = configuration
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
      ResolvedArtifact artifact = configuration.resolvedConfiguration.resolvedArtifacts.find {
        return it.name == "scm-webapp" && it.extension == "war"
      }
      extension.serverConfiguration.warFile = artifact.file
    }
    extension.serverConfiguration.home = extension.getScmHome(project)

    outputFile << JsonOutput.toJson(extension.serverConfiguration)
  }
}
