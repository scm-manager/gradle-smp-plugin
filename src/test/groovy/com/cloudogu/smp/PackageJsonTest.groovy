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
  class ScriptTests {

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
    void shouldReturnTrueIfScriptExists() {
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

    @Test
    void shouldReturnScript() {
      packageJsonFile << """
      {
        "scripts": {
          "build": "echo build it"
        }
      }
      """
      PackageJson packageJson = new PackageJson(packageJsonFile)
      assertThat(packageJson.getScript("build")).contains("echo build it")
    }

    @Test
    void shouldReturnEmptyOptional() {
      packageJsonFile << """
      {
        "scripts": {
        }
      }
      """
      PackageJson packageJson = new PackageJson(packageJsonFile)
      assertThat(packageJson.getScript("build")).isEmpty()
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
        "name": "@scm-manager/scm-super-plugin"
      }
      """
      PackageJson packageJson = new PackageJson(packageJsonFile)
      assertThat(packageJson.getName()).isEqualTo("@scm-manager/scm-super-plugin")
    }
  }

  @Nested
  class GetDependencyVersionTests {

    @Test
    void shouldReturnNullIfPackageJsonDoesNotExists() {
      PackageJson packageJson = new PackageJson(packageJsonFile)
      assertThat(packageJson.getName()).isNull()
    }

    @Test
    void shouldReturnNullWithoutDependencies() {
      packageJsonFile << "{}"
      PackageJson packageJson = new PackageJson(packageJsonFile)
      assertThat(packageJson.getName()).isNull()
    }

    @Test
    void shouldReturnNullWithoutDependency() {
      packageJsonFile << """
      {
        "dependencies": {}
      }
      """
      PackageJson packageJson = new PackageJson(packageJsonFile)
      assertThat(packageJson.getName()).isNull()
    }

    @Test
    void shouldReturnVersion() {
      packageJsonFile << """
      {
        "dependencies": {
          "@scm-manager/scm-super-plugin": "2.0.0"        
        }
      }
      """
      PackageJson packageJson = new PackageJson(packageJsonFile)
      String version = packageJson.getDependencyVersion("@scm-manager/scm-super-plugin")
      assertThat(version).isEqualTo("2.0.0")
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
