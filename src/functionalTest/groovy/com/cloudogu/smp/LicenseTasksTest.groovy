package com.cloudogu.smp

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import static org.assertj.core.api.Assertions.*

import java.nio.file.Path

class LicenseTasksTest {

  private File projectDir
  private GradleRunner runner

  @BeforeEach
  void setUpDependencies(@TempDir Path projectDirPath) {
    projectDir = projectDirPath.toFile()
    projectDir.mkdirs()

    runner = GradleRunner.create()
    runner.withPluginClasspath()
    runner.withArguments("license")
    runner.withProjectDir(projectDir)
  }

  @ParameterizedTest
  @ValueSource(strings = ["src/main/java/App.java", "src/test/java/AppTest.java", "src/main/js/index.js"])
  void shouldFailIfLicenseIsMissing(String filePath) {
    content "LICENSE.txt", "Awesome License"
    content "settings.gradle", """
     /*
      * Awesome License
      */"""
    content "build.gradle", """
     /*
      * Awesome License
      */
    
      plugins {
        id('org.scm-manager.smp')
      }
      """
    content filePath, ""

    def result = runner.buildAndFail()
    assertThat(result.output)
      .contains("License violations")
      .contains(filePath)
  }

  @ParameterizedTest
  @ValueSource(strings = ["src/main/java/Main.java", "src/test/java/MainTest.java", "src/main/js/index.ts"])
  void shouldNotFail(String filePath) {
    content "LICENSE.txt", "Cool License"
    content "settings.gradle", """
     /*
      * Cool License
      */"""
    content "build.gradle", """
     /*
      * Cool License
      */
    
      plugins {
        id('org.scm-manager.smp')
      }
      """
    content filePath, """
     /*
      * Cool License
      */
      """

    def result = runner.build()
    assertThat(result.output).contains("BUILD SUCCESSFUL")
  }

  @Test
  void shouldFailIfBuildFileLicenseIsMissing() {
    content "LICENSE.txt", "Super awesome License"
    content "settings.gradle", ""
    content "build.gradle", """
      plugins {
        id('org.scm-manager.smp')
      }
      """
    def result = runner.buildAndFail()
    assertThat(result.output)
      .contains("License violations")
      .contains("settings.gradle")
      .contains("build.gradle")
  }

  @ParameterizedTest
  @ValueSource(strings = ["src/main/java/Main.java", "src/test/java/MainTest.java", "src/main/js/index.ts"])
  void shouldNotFailWithoutLicenseFile(String filePath) {
    content "settings.gradle", ""
    content "build.gradle", """
      plugins {
        id('org.scm-manager.smp')
      }
      """
    content filePath, ""

    def result = runner.build()
    assertThat(result.output).contains("BUILD SUCCESSFUL")
  }

  private void content(String path, String content) {
    def file = new File(projectDir, path)
    if (!file.getParentFile().exists()) {
      file.getParentFile().mkdirs()
    }
    file << content.stripIndent().trim()
  }

}
