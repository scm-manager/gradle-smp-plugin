package com.cloudogu.smp

import groovy.xml.DOMBuilder
import groovy.xml.XmlUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class PluginXmlTask extends DefaultTask {

    private SmpExtension extension
    private File moduleXml
    private File pluginXml

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

    @InputFile
    File getModuleXml() {
        return moduleXml
    }

    void setModuleXml(File moduleXml) {
        this.moduleXml = moduleXml
    }

    @TaskAction
    void write() {
        if (!pluginXml.getParentFile().exists()) {
            pluginXml.getParentFile().mkdirs()
        }

        def xml = new NodeBuilder()
        def module = new XmlParser().parse(moduleXml)

        def pluginName = extension.getName(project)
        def output = xml.plugin {
            'scm-version'('2')
            information {
                name(pluginName)
                version(extension.version)
                if (extension.displayName != null) {
                    displayName(extension.displayName)
                }
                if (extension.description != null) {
                    description(extension.description)
                }
                if (extension.category != null) {
                    category(extension.category)
                }
                if (extension.author != null) {
                    author(extension.author)
                }
            }
            conditions {
                'min-version'(extension.scmVersion)
            }
            resources {
                script("assets/${pluginName}.bundle.js")
            }
            // we use name/artifactid as dependency
            dependencies {
                extension.dependencies.forEach {
                    def dep = project.dependencies.create(it)
                    dependency(verions: dep.version, dep.name)
                }
            }
            'optional-dependencies' {
                extension.optionalDependencies.forEach {
                    def dep = project.dependencies.create(it)
                    dependency(verions: dep.version, dep.name)
                }
            }
        }

        module.each {node ->
            output.append node
        }

        def document = DOMBuilder.parse( new StringReader( XmlUtil.serialize( output ) ) )
        TransformerFactory.newInstance().newTransformer().with {
            setOutputProperty( OutputKeys.INDENT, 'yes' )
            setOutputProperty( OutputKeys.STANDALONE, 'no' )

            transform( new DOMSource(document), new StreamResult(pluginXml) )
        }
    }
}
