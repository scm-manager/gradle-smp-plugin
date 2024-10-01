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

package com.cloudogu.smp

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.Project
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

class PackageJson {

  private File file
  private Object packageJson

  PackageJson(Project project) {
    this(new File(project.projectDir, "package.json"))
  }

  PackageJson(File file) {
    this.file = file
    if (file.exists()) {
      parse()
    }
  }

  private void parse() {
    JsonSlurper slurper = new JsonSlurper()
    packageJson = slurper.parse(file)
  }

  @Internal
  String getName() {
    if (packageJson != null) {
      return packageJson.name
    }
  }

  @Internal
  String getVersion() {
    if (packageJson != null) {
      return packageJson.version
    }
  }

  @InputFile
  @PathSensitive(PathSensitivity.RELATIVE)
  File getFile() {
    return file
  }

  String getDependencyVersion(String dependency) {
    if (packageJson != null) {
      return packageJson.dependencies[dependency]
    }
  }

  boolean exists() {
    return packageJson != null
  }

  Optional<String> getScript(String script) {
    if (packageJson != null) {
      def scripts = packageJson.scripts
      if (scripts != null) {
        return Optional.ofNullable(scripts[script])
      }
    }
    Optional.empty()
  }

  boolean hasScript(String script) {
    return getScript(script).isPresent()
  }

  void modify(Closure<Void> modifier) {
    modifier.call(packageJson)
    file.setText(JsonOutput.prettyPrint(JsonOutput.toJson(packageJson)))
    parse()
  }
}
