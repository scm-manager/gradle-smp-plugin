package com.cloudogu.smp

import org.gradle.api.Project
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.bundling.War

class PackagingTasks {

  static PublishArtifact configure(Project project, PackageJson packageJson, SmpExtension extension) {
    PublishArtifact artifact = registerSmpTasks(project, extension)
    registerPluginXml(project, packageJson, extension)
    project.afterEvaluate {
      registerOpenApiSpecGenerator(project, extension)
    }
    artifact
  }

  private static PublishArtifact registerSmpTasks(Project project, SmpExtension extension) {
    String name = extension.getName(project)

    def smp = project.tasks.register("smp", War) {
      description = 'Generates the SMP package'
      group = BasePlugin.BUILD_GROUP
      archiveFileName.set("${name}.smp")
      archiveExtension.set("smp")

      Dependencies.createPackagingClasspath(project).each { file ->
        if (file.name.endsWith(".jar")) {
          from(file) {
            into("lib")
          }
        } else if (file.name == "main") {
          from(file) {
            into("classes")
            exclude("**/module.xml")
          }
        } else {
          println "WARNING: unknown classpath entry ${file}"
        }
      }

      from("build/resources/main/META-INF") {
        into "META-INF"
      }

      from("build/webapp") {
        into "webapp"
      }
      from("build/smp")
      from("src/main/webapp") {
        into "webapp"
      }

      dependsOn("classes", "resolve")
    }

    project.tasks.getByName("assemble").configure {
      dependsOn("release-yaml")
    }

    project.tasks.register("release-yaml", ReleaseYamlTask) {
      it.extension = extension
      it.releaseYaml = new File(project.buildDir, "libs/release.yaml")
      it.smp = new File(project.buildDir, "libs/${name}.smp")
      it.pluginName = extension.getName(project)
      it.pluginVersion = project.version

      dependsOn("smp")
    }

    new LazyPublishArtifact(smp)
  }

  private static void registerPluginXml(Project project, PackageJson packageJson, SmpExtension extension) {
    project.tasks.getByName('classes').configure {
      dependsOn("plugin-xml")
    }

    project.tasks.register("plugin-xml", PluginXmlTask) {
      it.extension = extension
      it.moduleXml = new File(project.buildDir, "classes/java/main/META-INF/scm/module.xml")
      it.pluginXml = new File(project.buildDir, "smp/META-INF/scm/plugin.xml")
      if (packageJson.exists()) {
        it.packageJson = packageJson
      }
      it.pluginName = extension.getName(project)
      it.pluginVersion = project.version

      it.mustRunAfter("compileJava")
    }

    project.tasks.getByName("jar").configure {
      exclude("**/module.xml")
      from "build/smp"
    }
  }

  private static void registerOpenApiSpecGenerator(Project project, SmpExtension extension) {
    project.tasks.getByName("resolve") {
      outputFileName = 'openapi'
      outputFormat = 'JSONANDYAML'
      prettyPrint = 'TRUE'
      classpath = project.sourceSets.main.runtimeClasspath
      resourcePackages = extension.openApiSpec.packages
      outputDir = new File(project.buildDir, "smp/META-INF/scm")
      skip = extension.openApiSpec.packages.isEmpty()

      mustRunAfter("classes")
    }
  }

}
