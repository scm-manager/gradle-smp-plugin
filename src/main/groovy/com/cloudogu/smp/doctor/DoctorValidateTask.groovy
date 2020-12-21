package com.cloudogu.smp.doctor

import org.gradle.api.GradleException
import org.gradle.api.tasks.OutputFile

class DoctorValidateTask extends DoctorTask {

  private File outputMarker

  void setOutputMarker(File outputMarker) {
    this.outputMarker = outputMarker
  }

  @OutputFile
  File getOutputMarker() {
    return outputMarker
  }

  @Override
  void execute(Results results) {
    results.each {result ->
      String message = result.getMessage()
      if (result.isFixable()) {
        message += " [fixable]"
      }
      log(result.getType(), message)
    }

    if (results.hasError()) {
      throw new GradleException("At least one of the doctor rules failed")
    }

    File directory = outputMarker.getParentFile()
    if (!directory.exists() && !directory.mkdirs()) {
      throw new GradleException("Failed to create directory ${directory}")
    }

    if (!outputMarker.exists() && !outputMarker.createNewFile()) {
      throw new GradleException("Failed to create marker file ${outputMarker}")
    }
  }
}
