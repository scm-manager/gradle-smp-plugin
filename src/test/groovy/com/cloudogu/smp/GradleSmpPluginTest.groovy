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
import org.junit.jupiter.api.Test

import static org.assertj.core.api.Assertions.assertThat
import static org.junit.jupiter.api.Assertions.assertThrows

class GradleSmpPluginTest {

  @Test
  void shouldApplyPlugin() {
    Project project = ProjectBuilder.builder().build()
    project.version = "2.4.0"

    def plugin = new GradleSmpPlugin()
    plugin.apply(project)

    assertThat(project.tasks.size()).isGreaterThan(20)
  }

  @Test
  void shouldFailWithoutVersion() {
    Project project = ProjectBuilder.builder().build()

    def plugin = new GradleSmpPlugin()
    def ex = assertThrows(GradleException.class, {
      plugin.apply(project)
    })
    assertThat(ex.message)
      .contains("version")
      .contains("gradle.properties")
  }

}
