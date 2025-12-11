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

import com.moowork.gradle.node.yarn.YarnTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Path

import static org.assertj.core.api.Assertions.assertThat

class UiTasksTest {

  private File directory
  private Project project
  private SmpExtension extension

  @BeforeEach
  void setUpProject(@TempDir Path directoryPath) {
    this.directory = directoryPath.toFile()
    this.project = ProjectBuilder.builder().withProjectDir(directory).build()
    this.extension = new SmpExtension(ScmPropertyHelper.create("3.0.0")) {}
  }

  @Test
  void shouldNotRegisterUiTasksWithoutPackageJson() {
    configure()

    assertThat(project.tasks.findByPath("yarn_install")).isNull()
  }

  @Test
  void shouldRegisterYarnInstall() {
    new File(directory, "package.json") << "{}"

    configure()

    assertThat(project.tasks.getByName("yarn_install")).isNotNull()
  }

  @Test
  void shouldRegisterTypeCheckTask() {
    project.tasks.register("check")
    new File(directory, "package.json") << """
    {
      "scripts": {
        "typecheck": "tsc"
      }
    }
    """

    configure()

    assertThat(project.tasks.getByName("ui-typecheck")).isInstanceOf(YarnTask)
  }

  @Test
  void shouldRegisterBundleTask() {
    new File(directory, "package.json") << """
    {
      "scripts": {
        "build": "webpack"
      }
    }
    """

    configure()

    assertThat(project.tasks.getByName("ui-bundle")).isInstanceOf(YarnTask)
  }

  @Test
  void shouldRegisterUiTestTask() {
    project.tasks.register('test')
    new File(directory, 'package.json') << '''
    {
      "scripts": {
        "test": "jest"
      }
    }
    '''

    configure()

    def task = project.tasks.getByName('ui-test')
    assertThat(task).isInstanceOf(YarnTask)
  }

  @Test
  void shouldRegisterUiTestTaskFor4_0() {
    this.extension = new SmpExtension(ScmPropertyHelper.create("4.0.0")) {}
    project.tasks.register('test')
    new File(directory, 'package.json') << '''
    {
      "scripts": {
        "test": "vitest"
      }
    }
    '''

    configure()

    def task = project.tasks.getByName('ui-test')
    assertThat(task).isInstanceOf(YarnTask)
  }

  @Test
  void shouldRegisterUiDeployTask() {
    project.tasks.register("publish")
    new File(directory, "package.json") << """
    {
      "scripts": {
        "deploy": "ui-scripts deploy"
      }
    }
    """

    configure()

    assertThat(project.tasks.getByName("ui-deploy")).isInstanceOf(YarnTask)
  }

  private void configure() {
    PackageJson packageJson = new PackageJson(project)
    UiTasks.configure(project, extension, packageJson)
  }

}
