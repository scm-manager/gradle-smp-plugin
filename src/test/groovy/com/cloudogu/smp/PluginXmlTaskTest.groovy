package com.cloudogu.smp

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Path

import static org.assertj.core.api.Assertions.*

class PluginXmlTaskTest {

  @Test
  void shouldCreatePluginXml(@TempDir Path temp) {
    File packageJson = temp.resolve("package.json").toFile()
    packageJson << "{}"
    File moduleXml = temp.resolve("module.xml").toFile()
    moduleXml << """
      <?xml version="1.0" encoding="UTF-8" standalone="no"?>
      <module>
        <subscriber>
          <class>com.cloudogu.sample.events.EventListener</class>
        </subscriber>
      </module>
    """.trim().stripIndent()
    File pluginXml = temp.resolve("plugin.xml").toFile()
    SmpExtension extension = new SmpExtension()
    extension.name = "scm-sample-plugin"
    extension.displayName = "Sample Plugin"
    extension.version = "2.4.0"
    extension.category = "Sample"
    extension.author = "Cloudogu GmbH"
    extension.scmVersion = "2.7.0"
    extension.conditions {
      os = "Linux"
      arch = "arm"
    }
    extension.depends {
      on 'sonia.scm.plugins:scm-mail-plugin:2.1.0'
      optional 'sonia.scm.plugins:scm-editor-plugin:2.0.0'
      optional 'sonia.scm.plugins:scm-landingpage-plugin:1.0.0'
    }

    Project project = ProjectBuilder.builder().build()
    def task = project.task("plugin-xml", type: PluginXmlTask) {
      it.extension = extension
      it.moduleXml = moduleXml
      it.pluginXml = pluginXml
      it.packageJson = new PackageJson(packageJson)
    }
    task.write()

    XmlSlurper slurper = new XmlSlurper()
    def plugin = slurper.parse(pluginXml)

    assertThat(plugin["scm-version"]).isEqualTo("2")
    assertThat(plugin.subscriber["class"]).isEqualTo("com.cloudogu.sample.events.EventListener")

    assertThat(plugin.information.name).isEqualTo("scm-sample-plugin")
    assertThat(plugin.information.displayName).isEqualTo("Sample Plugin")
    assertThat(plugin.information.version).isEqualTo("2.4.0")
    assertThat(plugin.information.category).isEqualTo("Sample")
    assertThat(plugin.information.author).isEqualTo("Cloudogu GmbH")

    assertThat(plugin.resources.script).isEqualTo("assets/scm-sample-plugin.bundle.js")

    assertThat(plugin.conditions["min-version"]).isEqualTo("2.7.0")
    assertThat(plugin.conditions.os).isEqualTo("Linux")
    assertThat(plugin.conditions.arch).isEqualTo("arm")

    assertThat(plugin.dependencies.dependency).isEqualTo("scm-mail-plugin")
    assertThat(plugin.dependencies.dependency.@version).isEqualTo("2.1.0")

    assertThat(plugin["optional-dependencies"].dependency[0]).isEqualTo("scm-editor-plugin")
    assertThat(plugin["optional-dependencies"].dependency[0].@version).isEqualTo("2.0.0")
    assertThat(plugin["optional-dependencies"].dependency[1]).isEqualTo("scm-landingpage-plugin")
    assertThat(plugin["optional-dependencies"].dependency[1].@version).isEqualTo("1.0.0")
  }

  @Test
  void shouldNotGenerateResourceWithoutPackageJson(@TempDir Path temp) {
    File packageJson = temp.resolve("package.json").toFile()
    File moduleXml = temp.resolve("module.xml").toFile()
    moduleXml << """
      <?xml version="1.0" encoding="UTF-8" standalone="no"?>
      <module>
      </module>
    """.trim().stripIndent()
    File pluginXml = temp.resolve("plugin.xml").toFile()
    SmpExtension extension = new SmpExtension()
    extension.name = "scm-sample-plugin"

    Project project = ProjectBuilder.builder().build()
    def task = project.task("plugin-xml", type: PluginXmlTask) {
      it.extension = extension
      it.moduleXml = moduleXml
      it.pluginXml = pluginXml
      it.packageJson = new PackageJson(packageJson)
    }
    task.write()

    XmlSlurper slurper = new XmlSlurper()
    def plugin = slurper.parse(pluginXml)

    assertThat(plugin.resources.script.size()).isEqualTo(0)
  }

}
