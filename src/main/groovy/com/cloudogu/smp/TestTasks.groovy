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
import org.gradle.testing.jacoco.plugins.JacocoPlugin

class TestTasks {

  static void configure(Project project) {
    if (Environment.isCI()) {
      project.plugins.apply(JacocoPlugin)

      project.jacocoTestReport {
        reports {
          xml.enabled true
        }
      }

      project.tasks.register("update-test-timestamp", TouchFilesTask) {
        directory = new File(project.buildDir, "test-results")
        extension = "xml"
      }

      project.tasks.getByName("test").configure {
        dependsOn "update-test-timestamp"
      }
    }

    project.test {
      useJUnitPlatform()
      if (Environment.isCI()) {
        ignoreFailures = true
        finalizedBy project.jacocoTestReport
      }
    }
  }

}
