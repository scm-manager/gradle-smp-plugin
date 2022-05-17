package com.cloudogu.smp.doctor.rules

import com.cloudogu.smp.PackageJson
import com.cloudogu.smp.ScmPropertyHelper
import com.cloudogu.smp.SmpExtension
import com.cloudogu.smp.doctor.Context
import com.cloudogu.smp.doctor.Result
import com.cloudogu.smp.doctor.Rule
import groovy.xml.MarkupBuilder
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import static org.assertj.core.api.Assertions.assertThat
import static org.junit.jupiter.api.Assertions.assertThrows

class MinScmVersionRuleTest {

  private Project project
  private File directory

  @BeforeEach
  void createDependencies(@TempDir Path directory){
    project = ProjectBuilder.builder().build()
    this.directory = directory.toFile()
  }

  @Test
  void shouldReturnOkWithoutDependencies() {
    Result result = validate("2.0.0")
    assertOk(result)
  }

  @Test
  void shouldReturnOkIfScmVersionIsHighEnough() {
    Result result = validate("2.8.0",
      smp("scm-mail-plugin", "2.0.0"),
      smp("scm-code-editor-plugin", "2.8.0")
    )
    assertOk(result)
  }

  @Test
  void shouldIgnorePluginsWithoutVersion() {
    Result result = validate("2.8.0",
      smp("scm-mail-plugin", null),
      smp("scm-code-editor-plugin", null)
    )
    assertOk(result, "no", "plugin", "found")
  }

  @Test
  void shouldReturnErrorIfScmVersionIsTooLow() {
    Result result = validate("2.7.9",
      smp("scm-mail-plugin", "2.0.0"),
      smp("scm-code-editor-plugin", "2.8.0")
    )
    assertError(result, "2.7.9", "scm-code-editor-plugin", "2.8.0")
  }

  @Test
  void shouldThrowExceptionForDependencyWithoutName() {
    Rule rule = rule(smp(null, "2.0.0"))
    Context context = context("2.11.1")
    assertThrows(GradleException, {
      rule.validate(context)
    })
  }

  private void assertOk(Result result, String... messageParts) {
    assertThat(result.getType()).isEqualTo(Result.Type.OK)
    if (messageParts.length > 0) {
      assertThat(result.getMessage()).contains(messageParts)
    }
  }

  private void assertError(Result result, String scmVersion, String plugin, String requiredVersion) {
    assertThat(result.getType()).isEqualTo(Result.Type.ERROR)
    assertThat(result.getMessage())
      .contains(scmVersion)
      .contains(plugin)
      .contains(requiredVersion)
  }

  private File smp(String pluginName, String minVersion) {
    File file = new File(directory, pluginName + ".smp")
    ZipOutputStream zos = new ZipOutputStream(file.newOutputStream(), StandardCharsets.UTF_8)
    zos.putNextEntry(new ZipEntry("META-INF/scm/plugin.xml"))

    def writer = new StringWriter()
    def xml = new MarkupBuilder(writer)
    xml.plugin() {
      if (pluginName != null) {
        information {
          name(pluginName)
        }
      }
      if (minVersion != null) {
        conditions {
          'min-version'(minVersion)
        }
      }
    }

    zos.write(writer.toString().getBytes(StandardCharsets.UTF_8))
    zos.closeEntry()
    zos.close()

    return file
  }

  private Result validate(String scmVersion, File... files) {
    return rule(files).validate(context(scmVersion))
  }

  private Context context(String scmVersion) {
    SmpExtension extension = new SmpExtension(ScmPropertyHelper.create(scmVersion)) {}
    return new Context(project, extension, new PackageJson(new File(directory, "package.json")))
  }

  private Rule rule(File... files) {
    return new MinScmVersionRule({
      return files.toList().toSet()
    })
  }

}
