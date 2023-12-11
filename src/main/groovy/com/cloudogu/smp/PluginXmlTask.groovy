package com.cloudogu.smp

import groovy.xml.DOMBuilder
import groovy.xml.XmlUtil
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
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

  @Nested
  final Property<SmpExtension> extension = project.objects.property(SmpExtension)

  @Optional
  @InputFile
  @PathSensitive(PathSensitivity.RELATIVE)
  final RegularFileProperty moduleXml = project.objects.fileProperty()

  @Nested
  @Optional
  final Property<PackageJson> packageJson = project.objects.property(PackageJson)

  @Input
  final Property<String> pluginName = project.objects.property(String).convention(extension.map({ ext ->
    ext.getName(project)
  }))

  @Input
  final String getPluginVersion() {
    project.version.toString()
  }

  @Classpath
  final Configuration getPluginDependencies() {
    project.configurations.getByName("plugin")
  }

  @Classpath
  final Configuration getOptionalPluginDependencies() {
    project.configurations.getByName("optionalPlugin")
  }

  @OutputFile
  final RegularFileProperty pluginXml = project.objects.fileProperty()

  @TaskAction
  void write() {
    File pluginXmlFile = pluginXml.get().getAsFile()
    if (!pluginXmlFile.getParentFile().exists()) {
      pluginXmlFile.getParentFile().mkdirs()
    }

    PackageJson pkgJson = packageJson.getOrNull()

    def xml = new NodeBuilder()
    SmpExtension ext = extension.get()

    RegularFile moduleXmlFile = moduleXml.getOrNull();
    def scmVersion = ext.scmVersion.get();
    def output = xml.plugin {
      'scm-version'(scmVersion.split("\\.")[0])
      if (ext.childFirstClassloader) {
        "child-first-classloader"(ext.childFirstClassloader)
      }
      information {
        createNode('name', pluginName.get())
        version(pluginVersion)
        if (ext.displayName != null) {
          displayName(ext.displayName)
        }
        if (ext.description != null) {
          // we have to use explicit 'createNode' here, because 'description()' exists as a method in task
          createNode('description', ext.description)
        }
        if (ext.category != null) {
          category(ext.category)
        }
        if (ext.author != null) {
          author(ext.author)
        }
        if (ext.avatarUrl != null) {
          avatarUrl(ext.avatarUrl)
        }

      }
      conditions {
        'min-version'(scmVersion)
        if (ext.pluginConditions.os != null) {
          os(ext.pluginConditions.os)
        }
        if (ext.pluginConditions.arch != null) {
          arch(ext.pluginConditions.arch)
        }
      }
      resources {
        if (pkgJson != null && pkgJson.hasScript('build')) {
          script("assets/${pluginName.get()}.bundle.js")
        }
      }
      // we use name/artifactid as dependency
      dependencies {
        pluginDependencies.dependencies.forEach { dep ->
          dependency(version: dep.version, dep.name)
        }
      }
      'optional-dependencies' {
        optionalPluginDependencies.dependencies.forEach { dep ->
          dependency(version: dep.version, dep.name)
        }
      }
    }

    if (moduleXmlFile != null) {
      def module = new XmlParser().parse(moduleXmlFile.getAsFile())
      module.each { node ->
        output.append node
      }
    }

    def document = DOMBuilder.parse(new StringReader(XmlUtil.serialize(output)))
    TransformerFactory.newInstance().newTransformer().with {
      setOutputProperty(OutputKeys.INDENT, 'yes')
      setOutputProperty(OutputKeys.STANDALONE, 'no')

      transform(new DOMSource(document), new StreamResult(pluginXmlFile))
    }
  }
}
