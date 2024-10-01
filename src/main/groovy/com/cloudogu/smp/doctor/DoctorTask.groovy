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

import com.cloudogu.smp.PackageJson
import com.cloudogu.smp.SmpExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction

abstract class DoctorTask extends DefaultTask {

  private SmpExtension extension
  private PackageJson packageJson
  private PrintStream outputStream = System.out
  
  private Rules rules = Rules.all()

  @Nested
  SmpExtension getExtension() {
    return extension
  }

  void setExtension(SmpExtension extension) {
    this.extension = extension
  }

  @Nested
  @Optional
  PackageJson getPackageJson() {
    return packageJson
  }

  void setPackageJson(PackageJson packageJson) {
    this.packageJson = packageJson
  }

  void setOutputStream(PrintStream outputStream) {
    this.outputStream = outputStream
  }

  @Internal
  PrintStream getOutputStream() {
    return outputStream
  }

  @Internal
  Rules getRules() {
    return rules
  }

  void setRules(Rules rules) {
    this.rules = rules
  }

  @TaskAction
  void execute() {
    if (packageJson == null) {
      packageJson = new PackageJson(project)
    }
    def results = rules.validate(new Context(project, extension, packageJson))
    execute(results)
  }

  protected void log(Result.Type type, String message) {
    String formattedMessage = format(type, message)
    outputStream.println formattedMessage
  }

  private static String format(Result.Type type, String message) {
    if (type == Result.Type.OK) {
      return "[INFO ] Rule passed: " + message
    } else {
      String messageWithPrefix = "Rule failed: " + message;
      if (type == Result.Type.WARN) {
        return "[WARN ] " + messageWithPrefix
      } else {
        return "[ERROR] " + messageWithPrefix
      }
    }
  }

  abstract void execute(Results results)
}
