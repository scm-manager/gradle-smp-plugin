package com.cloudogu.smp


import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Path

import static org.assertj.core.api.Assertions.assertThat

class PackageJsonTest {

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
    void shouldReturnFalseIfPackageJsonDoesNotExists() {
      PackageJson packageJson = new PackageJson(packageJsonFile)
      assertThat(packageJson.hasScript("build")).isFalse()
    }

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

  @Nested
  class GetVersionTests {

    @Test
    void shouldReturnNullIfPackageJsonDoesNotExists() {
      PackageJson packageJson = new PackageJson(packageJsonFile)
      assertThat(packageJson.getVersion()).isNull()
    }

    @Test
    void shouldReturnNullWithoutVersion() {
      packageJsonFile << "{}"
      PackageJson packageJson = new PackageJson(packageJsonFile)
      assertThat(packageJson.getVersion()).isNull()
    }

    @Test
    void shouldReturnVersion() {
      packageJsonFile << """
      {
        "version": "1.0.0"
      }
      """
      PackageJson packageJson = new PackageJson(packageJsonFile)
      assertThat(packageJson.getVersion()).isEqualTo("1.0.0")
    }
  }

  @Nested
  class GetNameTests {

    @Test
    void shouldReturnNullIfPackageJsonDoesNotExists() {
      PackageJson packageJson = new PackageJson(packageJsonFile)
      assertThat(packageJson.getName()).isNull()
    }

    @Test
    void shouldReturnNullWithoutName() {
      packageJsonFile << "{}"
      PackageJson packageJson = new PackageJson(packageJsonFile)
      assertThat(packageJson.getName()).isNull()
    }

    @Test
    void shouldReturnName() {
      packageJsonFile << """
      {
        "version": "@scm-manager/scm-super-plugin"
      }
      """
      PackageJson packageJson = new PackageJson(packageJsonFile)
      assertThat(packageJson.getVersion()).isEqualTo("@scm-manager/scm-super-plugin")
    }
  }

  @Nested
  class ModifyTests {

    @Test
    void shouldModifyPackageJson() {
      packageJsonFile << """
      {
        "version": "1.0.0"
      }
      """
      PackageJson packageJson = new PackageJson(packageJsonFile)
      packageJson.modify { it ->
        it.version = "2.0.0"
      }
      assertThat(packageJson.getVersion()).isEqualTo("2.0.0")
    }

  }
}
