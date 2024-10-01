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

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Path

import static org.assertj.core.api.Assertions.*
import static org.junit.jupiter.api.Assertions.assertThrows

class VersionTasksTest {

  private Project project
  private File gradleProperties

  @BeforeEach
  void setUpProject(@TempDir Path directoryPath) {
    File directory = directoryPath.toFile()
    project = ProjectBuilder.builder()
      .withProjectDir(directory)
      .build()

    String projectVersion = "1.2.3"
    project.version = projectVersion

    gradleProperties = directoryPath.resolve("gradle.properties").toFile()
    gradleProperties << "version = ${projectVersion}"
  }

  @Test
  void shouldSetVersion() {
    def task = project.task("setVersion", type: VersionTasks.SetVersionTask) {
      newVersion = "2.0.0"
    }
    task.execute()
    assertThat(version).isEqualTo("2.0.0")
  }

  @Test
  void shouldFailWithoutNewVersion() {
    def task = project.task("setVersion", type: VersionTasks.SetVersionTask)
    assertThrows(GradleException.class, {
      task.execute()
    })
  }

  @Test
  void shouldSetVersionToNextSnapshot() {
    def task = project.task("setVersionToNextSnapshot", type: VersionTasks.SetVersionToNextSnapshot)
    task.execute()
    assertThat(version).isEqualTo("1.2.4-SNAPSHOT")
  }

  private String getVersion() {
    Properties properties = new Properties()
    gradleProperties.withInputStream {
      properties.load(it)
    }
    properties.version
  }

}
