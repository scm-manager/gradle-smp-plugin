package com.cloudogu.smp.doctor

import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.*

import static groovy.io.FileType.FILES

@CacheableTask
class ValidatePluginsJsonTask extends DefaultTask {

  private File localeDirectory
  private File outputMarker

  void setLocaleDirectory(File localeDirectory) {
    this.localeDirectory = localeDirectory
  }

  @Optional
  @InputDirectory
  File getLocaleDirectory() {
    return localeDirectory
  }

  void setOutputMarker(File outputMarker) {
    this.outputMarker = outputMarker
  }

  @OutputFile
  File getOutputMarker() {
    return outputMarker
  }

  @TaskAction
  void execute() {
    // null means the directory does not exists
    if (localeDirectory != null) {
      validateLocale()
    }

    File parentFile = outputMarker.getParentFile()
    if (!parentFile.exists() && !parentFile.mkdirs()) {
      throw new GradleException("failed to create directory for marker file")
    }
    if (!outputMarker.exists() && !outputMarker.createNewFile()) {
      throw new GradleException("failed to create marker file")
    }
  }

  void validateLocale() {
    localeDirectory.eachFileRecurse(FILES) {file ->
      if (file.name.endsWith(".json")) {
        validateJson(file)
      }
    }
  }

  private static void validateJson(File file) {
    try {
      // we use pretty print of validation,
      // because of https://stackoverflow.com/a/48470335/579777
      JsonOutput.prettyPrint(file.text)
    } catch (Exception ex) {
      throw new GradleException("failed to parse locale file: ${file}", ex)
    }
  }
}
