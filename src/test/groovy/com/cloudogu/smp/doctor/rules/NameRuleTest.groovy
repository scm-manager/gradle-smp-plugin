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

class NameRuleTest {

  private Project project
  private File packageJsonFile

  @BeforeEach
  void createDependencies(@TempDir Path directory){
    project = ProjectBuilder.builder().build()
    packageJsonFile = directory.resolve("package.json").toFile()
  }

  @Test
  void shouldReturnOk() {
    Result result = validate("heart-of-gold", "heart-of-gold")
    assertThat(result.getType()).isEqualTo(Result.Type.OK);
  }

  @Test
  void shouldReturnOkForNameWithScope() {
    Result result = validate("heart-of-gold", "@hitchhiker/heart-of-gold")
    assertThat(result.getType()).isEqualTo(Result.Type.OK)
  }

  @Test
  void shouldReturnFixableError() {
    Result result = validate("heart-of-gold", "puzzle-42")

    assertThat(result.getType()).isEqualTo(Result.Type.ERROR);
    assertThat(result.isFixable()).isTrue();
  }

  @Test
  void shouldFix() {
    Result result = validate("heart-of-gold", "puzzle-42")
    result.fix()

    PackageJson packageJson = new PackageJson(packageJsonFile)
    assertThat(packageJson.getName()).isEqualTo("@scm-manager/heart-of-gold")
  }


  private Result validate(String extensionName, String packageJsonName) {
    SmpExtension extension = extension(extensionName)
    PackageJson packageJson = createPackageJson(packageJsonName)
    return new NameRule().validate(new Context(project, extension, packageJson))
  }

  private SmpExtension extension(String name) {
    SmpExtension extension = new SmpExtension(ScmPropertyHelper.create("2.7.0")) {}
    extension.setName(name)
    return extension
  }

  private PackageJson createPackageJson(String name = null) {
    if (name != null) {
      packageJsonFile << """
      {
        "name": "${name}"
      }
      """
    }
    return new PackageJson(packageJsonFile)
  }

}
