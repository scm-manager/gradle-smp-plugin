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

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Path

import static org.assertj.core.api.Assertions.assertThat

class PluginXmlTaskTest {

  @Test
  void shouldCreatePluginXml(@TempDir Path temp) {
    File packageJson = temp.resolve("package.json").toFile()
    packageJson << '''{
      "scripts": {
        "build": "plugin-scripts build"
      }
    }'''
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
    SmpExtension extension = new SmpExtension(ScmPropertyHelper.create("2.7.0")) {}
    extension.displayName = "Sample Plugin"
    extension.category = "Sample"
    extension.author = "Cloudogu GmbH"
    extension.avatarUrl = '/images/avatar.png'
    extension.childFirstClassloader = true
    extension.conditions {
      os = "Linux"
      arch = "arm"
    }

    Project project = ProjectBuilder.builder().build()
    def pluginConfiguration = project.configurations.create("plugin")
    pluginConfiguration.dependencies.add(project.dependencies.create("sonia.scm.plugins:scm-mail-plugin:2.1.0"))
    def optionalPluginConfiguration = project.configurations.create("optionalPlugin")
    optionalPluginConfiguration.dependencies.add(project.dependencies.create("sonia.scm.plugins:scm-editor-plugin:2.0.0"))
    optionalPluginConfiguration.dependencies.add(project.dependencies.create("sonia.scm.plugins:scm-landingpage-plugin:1.0.0"))

    project.setProperty("version", "2.4.0")
    def task = project.task("plugin-xml", type: PluginXmlTask) {
      it.extension.set(extension)
      it.moduleXml.set(moduleXml)
      it.pluginXml.set(pluginXml)
      it.packageJson.set(new PackageJson(packageJson))
      it.pluginName.set("scm-sample-plugin")
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
    assertThat(plugin.information.avatarUrl).isEqualTo('/images/avatar.png')
    assertThat(plugin["child-first-classloader"]).isEqualTo(true)

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
    SmpExtension extension = new SmpExtension(ScmPropertyHelper.create("2.7.0")) {}
    Project project = ProjectBuilder.builder().build()
    project.configurations.create("plugin")
    project.configurations.create("optionalPlugin")
    project.setProperty("version", "2.4.0")
    def task = project.task("plugin-xml", type: PluginXmlTask) {
      it.extension.set(extension)
      it.moduleXml.set(moduleXml)
      it.pluginXml.set(pluginXml)
      it.packageJson.set(new PackageJson(packageJson))
      it.pluginName.set("scm-sample-plugin")
    }
    task.write()

    XmlSlurper slurper = new XmlSlurper()
    def plugin = slurper.parse(pluginXml)

    assertThat(plugin.resources.script.size()).isEqualTo(0)
  }

}
