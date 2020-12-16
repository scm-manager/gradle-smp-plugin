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

  @BeforeEach
  void setUpProject(@TempDir Path directoryPath) {
    this.directory = directoryPath.toFile()
    this.project = ProjectBuilder.builder().withProjectDir(directory).build()
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
    project.tasks.register('check')
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
    UiTasks.configure(project, packageJson)
  }

}
