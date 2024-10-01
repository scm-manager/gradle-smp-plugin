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

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

class LicenseTasks {

  static void configure(Project project) {
    File licenseFile = findLicenseFile(project)
    if (licenseFile == null) {
      project.tasks.register("license", LicenseFileMissingTask) {
        outputs.upToDateWhen { false }
      }
    } else {
      project.plugins.apply("org.scm-manager.license")
      configureTasks(project, licenseFile)
    }
  }

  private static File findLicenseFile(Project project) {
    File licenseHeaderFile = new File(project.rootDir, "LICENSE-HEADER.txt")
    if (licenseHeaderFile.exists()) {
      return licenseHeaderFile
    }
    File licenseFile = new File(project.rootDir, "LICENSE.txt")
    if (licenseFile.exists()) {
      return licenseFile
    }
    return null
  }

  private static void configureTasks(Project project, File licenseFile) {
    project.tasks.register("license") {
      dependsOn 'checkLicenses'
    }

    project.license {
      header licenseFile
      newLine = true
      ignoreNewLine = true
      lineEnding = "\n"

      exclude "**/*.mustache"
      exclude "**/*.json"
      exclude "**/*.ini"
      exclude "**/mockito-extensions/*"
      exclude "**/*.txt"
      exclude "**/*.md"

      tasks {
        gradle {
          files.from("build.gradle", "settings.gradle", "gradle.properties")
        }
        docker {
          files.from("Dockerfile", "docker-compose.yml")
        }
        ui {
          files.from("src/main/js", "src/test/e2e")
        }
        etc {
          files.from("src/main/conf/logging.xml")
        }
      }
    }
  }

  static class LicenseFileMissingTask extends DefaultTask {

    @TaskAction
    void execute() {
      println "No LICENSE.txt or LICENSE-HEADER.txt found"
      println "In order to use the license check, please create a LICENSE.txt or LICENSE-HEADER.txt in the root of the project"
    }

  }

}
