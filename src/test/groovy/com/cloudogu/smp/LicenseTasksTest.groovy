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
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Path

import static org.assertj.core.api.Assertions.assertThat

class LicenseTasksTest {

  @Test
  void shouldRegisterLicenseFileMissingTask(@TempDir Path directory) {
    Project project = ProjectBuilder.builder().withProjectDir(directory.toFile()).build()

    LicenseTasks.configure(project)

    assertThat(project.tasks.getByName("license")).isInstanceOf(LicenseTasks.LicenseFileMissingTask)
  }

  @Test
  void shouldConfigureLicenseTasksWithLicenseHeaderFile(@TempDir Path directory) {
    def licenseFile = directory.resolve("LICENSE-HEADER.txt").toFile()
    licenseFile << "Awesome License"
    Project project = ProjectBuilder.builder().withProjectDir(directory.toFile()).build()
    project.plugins.apply(JavaPlugin)

    LicenseTasks.configure(project)

    def tasks = project.tasks.asList().collect {
      return it.name
    }

    assertThat(tasks).contains(
      "license", "checkLicenses", "checkLicenseCustomUi", "checkLicenseCustomGradle"
    )
  }

  @Test
  void shouldConfigureLicenseTasksWithLicenseFile(@TempDir Path directory) {
    def licenseFile = directory.resolve("LICENSE.txt").toFile()
    licenseFile << "Awesome License"
    Project project = ProjectBuilder.builder().withProjectDir(directory.toFile()).build()
    project.plugins.apply(JavaPlugin)

    LicenseTasks.configure(project)

    def tasks = project.tasks.asList().collect {
      return it.name
    }

    assertThat(tasks).contains(
      "license", "checkLicenses", "checkLicenseCustomUi", "checkLicenseCustomGradle"
    )
  }
}
