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

import org.gradle.api.Project
import org.gradle.api.tasks.Sync

import static com.cloudogu.smp.Dependencies.createPackagingClasspath

class RunTasks {

  static void configure(Project project, PackageJson packageJson, SmpExtension extension) {
    project.tasks.register("copy-plugins", CopyPluginsTask) {
      it.configuration = project.configurations.getByName("runtimePluginElements")
      it.home = extension.getScmHome(project)
    }

    project.tasks.register("prepare-home", Sync) {
      createPackagingClasspath(project).each { file ->
        if (file.name.endsWith(".jar")) {
          from(file) {
            into("lib")
          }
        } else if (file.name == "main") {
          from(file) {
            into("classes")
            exclude("**/module.xml")
          }
        } else {
          println "WARNING: unknown classpath entry ${file}"
        }
      }

      from "build/smp"
      from("build/resources/main/META-INF") {
        into "META-INF"
      }
      from("src/main/webapp") {
        into "webapp"
      }

      destinationDir = new File(extension.getScmHome(project), "plugins/${extension.getName(project)}")
      dependsOn("classes", "copy-plugins")
    }

    project.tasks.register("write-server-config", WriteServerConfigTask) {
      description = "Writes the configuration for run task"
      it.extension = extension
      it.configuration = project.configurations.getByName("scmServer")
      it.outputFile = extension.getServerConfigurationFile(project)
    }

    project.afterEvaluate {
      if (!extension.core) {
        // We register run only on none core plugins,
        // because core plugins should only be used from the root project.
        project.tasks.register("run", RunTask) {
          group = "Run"
          description = "Run SCM-Manager with the plugin installed"
          it.extension = extension
          it.packageJson = packageJson
          // run always
          outputs.upToDateWhen { false }
          dependsOn("prepare-home", "write-server-config")
          if (packageJson.exists()) {
            dependsOn("yarn_install")
          }
        }
      }
    }
  }

}
