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

import com.cloudogu.smp.doctor.DoctorFixTask
import com.cloudogu.smp.doctor.DoctorValidateTask
import com.cloudogu.smp.doctor.ValidatePluginsJsonTask
import org.gradle.api.Project
import org.gradle.language.base.plugins.LifecycleBasePlugin

class DoctorTasks {

  static void configure(Project project, SmpExtension extension, PackageJson packageJson) {
    project.tasks.register("validate", DoctorValidateTask) {
      it.description = "Validates build configuration."
      it.group = LifecycleBasePlugin.VERIFICATION_GROUP

      it.extension = extension
      if (packageJson.exists()) {
        it.packageJson = packageJson
      }
      it.outputMarker = new File(project.buildDir, "tmp/validate/marker")
    }

    File localeDirectory = new File(project.projectDir, 'src/main/resources/locales')
    project.tasks.register("validatePluginJson", ValidatePluginsJsonTask) {
      it.localeDirectory = localeDirectory.exists() ? localeDirectory : null
      it.outputMarker = new File(project.buildDir, "tmp/validatePluginJson/marker")
    }

    project.tasks.getByName("check").configure {
      it.dependsOn("validate", 'validatePluginJson')
    }

    project.tasks.register("fix", DoctorFixTask) {
      it.description = "Fixes broken build configuration."
      it.group = LifecycleBasePlugin.VERIFICATION_GROUP

      it.extension = extension
      if (packageJson.exists()) {
        it.packageJson = packageJson
      }
    }
  }

}
