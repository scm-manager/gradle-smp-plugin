package com.cloudogu.smp

import groovy.xml.DOMBuilder
import groovy.xml.XmlUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

@CacheableTask
class PluginXmlTask extends DefaultTask {

  private SmpExtension extension
  private File moduleXml
  private File pluginXml
  private PackageJson packageJson

  private String pluginName
  private String pluginVersion

  @Input
  String getPluginVersion() {
    return pluginVersion
  }

  void setPluginVersion(String pluginVersion) {
    this.pluginVersion = pluginVersion
  }

  @Input
  String getPluginName() {
    return pluginName
  }

  void setPluginName(String pluginName) {
    this.pluginName = pluginName
  }

  @Nested
  SmpExtension getExtension() {
    return extension
  }

  void setExtension(SmpExtension extension) {
    this.extension = extension
  }

  @OutputFile
  File getPluginXml() {
    return pluginXml
  }

  void setPluginXml(File pluginXml) {
    this.pluginXml = pluginXml
  }

  @Optional
  @InputFile
  @PathSensitive(PathSensitivity.RELATIVE)
  File getModuleXml() {
    return moduleXml
  }

  void setModuleXml(File moduleXml) {
    this.moduleXml = moduleXml
  }

  @Nested
  @Optional
  PackageJson getPackageJson() {
    return packageJson
  }

  void setPackageJson(PackageJson packageJson) {
    this.packageJson = packageJson
  }

  @TaskAction
  void write() {
    if (!pluginXml.getParentFile().exists()) {
      pluginXml.getParentFile().mkdirs()
    }

    def xml = new NodeBuilder()

    def output = xml.plugin {
      'scm-version'('2')
      information {
        name(pluginName)
        version(pluginVersion)

        if (extension.displayName != null) {
          displayName(extension.displayName)
        }
        if (extension.description != null) {
          // we have to use explicit 'createNode' here, because 'description()' exists as a method in task
          createNode('description', extension.description)
        }
        if (extension.category != null) {
          category(extension.category)
        }
        if (extension.author != null) {
          author(extension.author)
        }
        if (extension.avatarUrl != null) {
          avatarUrl(extension.avatarUrl)
        }
      }
      conditions {
        'min-version'(extension.scmVersion)
        if (extension.pluginConditions.os != null) {
          os(extension.pluginConditions.os)
        }
        if (extension.pluginConditions.arch != null) {
          arch(extension.pluginConditions.arch)
        }
      }
      resources {
        if (packageJson != null && packageJson.hasScript('build')) {
          script("assets/${pluginName}.bundle.js")
        }
      }
      // we use name/artifactid as dependency
      dependencies {
        project.configurations.getByName("plugin").dependencies.forEach { dep ->
          dependency(version: dep.version, dep.name)
        }
      }
      'optional-dependencies' {
        project.configurations.getByName("optionalPlugin").dependencies.forEach { dep ->
          dependency(version: dep.version, dep.name)
        }
      }
    }

    if (moduleXml) {
      def module = new XmlParser().parse(moduleXml)
      module.each { node ->
        output.append node
      }
    }

    def document = DOMBuilder.parse(new StringReader(XmlUtil.serialize(output)))
    TransformerFactory.newInstance().newTransformer().with {
      setOutputProperty(OutputKeys.INDENT, 'yes')
      setOutputProperty(OutputKeys.STANDALONE, 'no')

      transform(new DOMSource(document), new StreamResult(pluginXml))
    }
  }
}
