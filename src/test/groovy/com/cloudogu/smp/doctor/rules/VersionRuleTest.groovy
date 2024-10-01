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

package com.cloudogu.smp.doctor.rules

import com.cloudogu.smp.PackageJson
import com.cloudogu.smp.ScmPropertyHelper
import com.cloudogu.smp.SmpExtension
import com.cloudogu.smp.doctor.Context
import com.cloudogu.smp.doctor.Result
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Path

import static org.assertj.core.api.Assertions.assertThat

class VersionRuleTest {

  private Project project
  private File packageJsonFile
  private SmpExtension extension

  @BeforeEach
  void createDependencies(@TempDir Path directory){
    project = ProjectBuilder.builder().build()
    packageJsonFile = directory.resolve("package.json").toFile()
    extension = new SmpExtension(ScmPropertyHelper.create("2.7.0")) {}
  }

  PackageJson createPackageJson(String version = null) {
    if (version != null) {
      packageJsonFile << """
      {
        "version": "${version}"
      }
      """
    }
    return new PackageJson(packageJsonFile)
  }

  @Test
  void shouldReturnOk() {
    project.version = "2.0.0"
    PackageJson packageJson = createPackageJson("2.0.0")

    Result result = validate(packageJson)
    assertThat(result.getType()).isEqualTo(Result.Type.OK)
  }

  private Result validate(PackageJson packageJson) {
    return new VersionRule().validate(new Context(project, extension, packageJson))
  }

  @Test
  void shouldReturnOkWithoutPackageJsonVersion() {
    project.version = "2.0.0"
    PackageJson packageJson = createPackageJson()

    Result result = validate(packageJson)
    assertThat(result.getType()).isEqualTo(Result.Type.OK)
  }

  @Test
  void shouldWarnWithFixable() {
    project.version = "2.0.0"
    PackageJson packageJson = createPackageJson("1.0.0")

    Result result = validate(packageJson)
    assertThat(result.getType()).isEqualTo(Result.Type.ERROR);
    assertThat(result.isFixable()).isTrue();
  }

  @Test
  void shouldFix() {
    project.version = "2.0.0"
    PackageJson packageJson = createPackageJson("1.0.0")

    Result result = validate(packageJson)
    result.fix()

    packageJson = new PackageJson(packageJsonFile)
    assertThat(packageJson.getVersion()).isEqualTo("2.0.0")
  }

}
