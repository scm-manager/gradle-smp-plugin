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
