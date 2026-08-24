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

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Path

import static org.assertj.core.api.Assertions.assertThat

class TestTasksTest {

  /** This test simulates the environment on Jenkins */
  @Test
  void shouldApplyPluginOnJenkins(@TempDir Path projectDir) {
    projectDir.resolve("gradle.properties").toFile() << "version=2.0.0"
    projectDir.resolve("settings.gradle").toFile() << ""
    projectDir.resolve("build.gradle").toFile() << """
      plugins {
        id('org.scm-manager.smp')
      }
      """.stripIndent()

    Map<String, String> environment = new HashMap<>(System.getenv())
    environment.put("JENKINS_URL", "http://localhost")
    environment.put("BUILD_ID", "test-build")

    def result = GradleRunner.create()
      .withPluginClasspath()
      .withArguments("help")
      .withProjectDir(projectDir.toFile())
      .withEnvironment(environment)
      .build()

    assertThat(result.output).contains("BUILD SUCCESSFUL")
  }
}
