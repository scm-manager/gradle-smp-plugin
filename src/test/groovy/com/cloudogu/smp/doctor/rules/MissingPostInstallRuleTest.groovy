package com.cloudogu.smp.doctor.rules

import com.cloudogu.smp.PackageJson
import com.cloudogu.smp.ScmPropertyHelper
import com.cloudogu.smp.SmpExtension
import com.cloudogu.smp.doctor.Context
import com.cloudogu.smp.doctor.Result
import org.assertj.core.api.AbstractComparableAssert
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Path

import static org.assertj.core.api.Assertions.assertThat

class MissingPostInstallRuleTest {

  private Project project
  private File packageJsonFile

  @BeforeEach
  void createDependencies(@TempDir Path directory){
    project = ProjectBuilder.builder().build()
    packageJsonFile = directory.resolve("package.json").toFile()
  }

  @Test
  void shouldReturnOkWithoutPackageJson() {
    Result result = validate()
    assertOk(result)
  }

  @Test
  void shouldReturnWarnWithoutScripts() {
    packageJsonFile << "{}"

    Result result = validate()
    assertWarn(result)
  }

  @Test
  void shouldReturnWarnWithoutPostinstall() {
    packageJsonFile << """
    {
      "scripts": {}
    }
    """

    Result result = validate()
    assertWarn(result)
  }

  @Test
  void shouldReturnWarnWithWrongPostinstall() {
    packageJsonFile << """
    {
      "scripts": {
        "postinstall": "ui-scripts postinstall"
      }
    }
    """

    Result result = validate()
    assertWarn(result)
  }

  @Test
  void shouldReturnOkForCorrectPostinstall() {
    packageJsonFile << """
    {
      "scripts": {
        "postinstall": "plugin-scripts postinstall"
      }
    }
    """

    Result result = validate()
    assertOk(result)
  }

  @Test
  void shouldFixWithoutScripts() {
    packageJsonFile << "{}"

    validate().fix()
    PackageJson json = new PackageJson(packageJsonFile)
    assertThat(json.getScript("postinstall")).contains("plugin-scripts postinstall")
  }

  @Test
  void shouldFixWithScripts() {
    packageJsonFile << """
    {
      "scripts": {
        "postinstall": "ui-scripts postinstall",
        "build": "ui-plugins build"
      }
    }
    """

    validate().fix()
    PackageJson json = new PackageJson(packageJsonFile)
    assertThat(json.getScript("postinstall")).contains("plugin-scripts postinstall")
    assertThat(json.getScript("build")).contains("ui-plugins build")
  }

  private void assertOk(Result result) {
    assertThat(result.getType()).isEqualTo(Result.Type.OK)
  }

  private void assertWarn(Result result) {
    assertThat(result.getType()).isEqualTo(Result.Type.WARN)
    assertThat(result.isFixable()).isTrue()
  }



  private Result validate() {
    SmpExtension extension = new SmpExtension(ScmPropertyHelper.create("2.7.0")) {}
    PackageJson packageJson = new PackageJson(packageJsonFile)
    return new MissingPostInstallRule().validate(new Context(project, extension, packageJson))
  }

}
