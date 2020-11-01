package com.cloudogu.smp

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import static org.assertj.core.api.Assertions.*

import java.nio.file.Path

class PluginJsonTest {

  private File packageJsonFile

  @BeforeEach
  void setUpFile(@TempDir Path directory) {
    packageJsonFile = directory.resolve("package.json").toFile()
  }

  @Nested
  class ExistsTests {

    @Test
    void shouldReturnFalseIfPackageJsonDoesNotExists() {
      PackageJson packageJson = new PackageJson(packageJsonFile)
      assertThat(packageJson.exists()).isFalse()
    }

    @Test
    void shouldReturnTrueIfPackageJsonExists() {
      packageJsonFile << "{}"
      PackageJson packageJson = new PackageJson(packageJsonFile)
      assertThat(packageJson.exists()).isTrue()
    }

  }

  @Nested
  class HasScriptTests {

    @Test
    void shouldReturnFalseIfScriptsDoesNotExists() {
      packageJsonFile << "{}"
      PackageJson packageJson = new PackageJson(packageJsonFile)
      assertThat(packageJson.hasScript("build")).isFalse()
    }

    @Test
    void shouldReturnFalseIfScriptDoesNotExists() {
      packageJsonFile << """
      {
        "scripts": {}
      }
      """
      PackageJson packageJson = new PackageJson(packageJsonFile)
      assertThat(packageJson.hasScript("build")).isFalse()
    }

    @Test
    void shouldReturnTueIfScriptExists() {
      packageJsonFile << """
      {
        "scripts": {
          "build": "echo build it"
        }
      }
      """
      PackageJson packageJson = new PackageJson(packageJsonFile)
      assertThat(packageJson.hasScript("build")).isTrue()
    }
  }
}
