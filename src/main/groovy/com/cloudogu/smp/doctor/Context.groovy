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
import org.gradle.api.Project

class Context {

  private final Project project
  private final SmpExtension extension
  private final PackageJson packageJson

  Context(Project project, SmpExtension extension, PackageJson packageJson) {
    this.project = project
    this.extension = extension
    this.packageJson = packageJson
  }

  Project getProject() {
    return project
  }

  SmpExtension getExtension() {
    return extension
  }

  PackageJson getPackageJson() {
    return packageJson
  }
}
