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

class UiPluginsVersionRuleTest {

  private Project project
  private File packageJsonFile

  @BeforeEach
  void createDependencies(@TempDir Path directory){
    project = ProjectBuilder.builder().build()
    packageJsonFile = directory.resolve("package.json").toFile()
  }

  @Test
  void shouldReturnOkWithoutUiPlugins() {
    Result result = validate("2.0.0")
    assertThat(result.getType()).isEqualTo(Result.Type.OK)
  }

  @Test
  void shouldReturnOkWithEqualVersion() {
    Result result = validate("2.0.0", "2.0.0")
    assertThat(result.getType()).isEqualTo(Result.Type.OK)
  }

  @Test
  void shouldReturnOkForSnapshotVersion() {
    Result result = validate("2.0.0-SNAPSHOT", "latest")
    assertThat(result.getType()).isEqualTo(Result.Type.OK)
  }

  @Test
  void shouldReturnFixableWarn() {
    Result result = validate("2.0.0", "1.0.0")
    assertThat(result.getType()).isEqualTo(Result.Type.WARN)
    assertThat(result.isFixable()).isTrue()
  }

  @Test
  void shouldFix() {
    Result result = validate("2.0.0", "1.0.0")
    result.fix()

    PackageJson packageJson = new PackageJson(packageJsonFile)
    assertThat(packageJson.getDependencyVersion("@scm-manager/ui-plugins")).isEqualTo("2.0.0");
  }

  private Result validate(String scmVersion, String uiPluginsVersion = null) {
    SmpExtension extension = extension(scmVersion)
    PackageJson packageJson = createPackageJson(uiPluginsVersion)
    return new UiPluginsVersionRule().validate(new Context(project, extension, packageJson))
  }

  private SmpExtension extension(String scmVersion) {
    SmpExtension extension = new SmpExtension(ScmPropertyHelper.create(scmVersion)) {}
    return extension
  }

  private PackageJson createPackageJson(String uiPluginsVersion = null) {
    if (uiPluginsVersion != null) {
      packageJsonFile << """
      {
        "dependencies": {
          "@scm-manager/ui-plugins": "${uiPluginsVersion}"
        }
      }
      """
    }
    return new PackageJson(packageJsonFile)
  }
}
