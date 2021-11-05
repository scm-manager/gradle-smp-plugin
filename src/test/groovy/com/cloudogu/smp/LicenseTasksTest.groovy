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
  void shouldConfigureLicenseTasks(@TempDir Path directory) {
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
