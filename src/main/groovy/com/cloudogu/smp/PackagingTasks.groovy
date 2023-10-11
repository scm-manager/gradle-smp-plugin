package com.cloudogu.smp

import io.swagger.v3.plugins.gradle.tasks.ResolveTask
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.bundling.War

class PackagingTasks {

  static PublishArtifact configure(Project project, PackageJson packageJson, SmpExtension extension) {
    PublishArtifact artifact = registerSmpTasks(project, extension)
    registerPluginXml(project, packageJson, extension)

    Configuration smpArtifacts = project.configurations.create('smp')
      .setDescription('smp variant of plugin')
      .attributes { it ->
        it.attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category, Category.LIBRARY))
        it.attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named(Bundling, Bundling.EXTERNAL))
        it.attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage, Usage.JAVA_RUNTIME))
        it.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements, "smp"))
      }
      .extendsFrom(
        project.configurations.getByName("plugin"),
        project.configurations.getByName("optionalPlugin")
      )
    smpArtifacts.canBeResolved = false
    smpArtifacts.canBeConsumed = true

    project.afterEvaluate {
      registerOpenApiSpecGenerator(project, extension)

      project.artifacts.add('smp', project.tasks.getByName('smp'))
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
      from("build/openapi")
      from("src/main/webapp") {
        into "webapp"
      }

      dependsOn("classes", "openapi")
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
      mustRunAfter("smp")
    }

    new LazyPublishArtifact(smp)
  }

  private static void registerPluginXml(Project project, PackageJson packageJson, SmpExtension extension) {
    project.tasks.getByName('classes').configure {
      dependsOn("plugin-xml")
    }

    project.tasks.register("plugin-xml", PluginXmlTask) {
      it.extension.set(extension)
      it.moduleXml.set(project.layout.buildDirectory.file("classes/java/main/META-INF/scm/module.xml").map({ f ->
        if (f.asFile.exists()) {
          return f
        }
        return null
      }))
      it.pluginXml.set(project.layout.buildDirectory.file("smp/META-INF/scm/plugin.xml"))
      it.packageJson.set(packageJson.exists() ? packageJson : null)

      it.mustRunAfter("compileJava")
    }

    project.tasks.getByName("jar").configure {
      exclude("**/module.xml")
      from "build/smp"
    }
  }

  private static boolean needsJackson(String version) {
    String[] versionParts = version.split("[.-]")
    if (versionParts.length < 2) {
      return false
    }
    def majorVersion = versionParts[0] as int
    def minorVersion = versionParts[1] as int
    def patchVersion = versionParts[2] as int
    return majorVersion == 2 && (minorVersion > 39 || minorVersion == 39 && patchVersion > 1)
  }

  private static void registerOpenApiSpecGenerator(Project project, SmpExtension extension) {
    final Configuration config = project.configurations.create("swaggerDeps")
      .setVisible(false)

    config.defaultDependencies(new Action<DependencySet>() {
      public void execute(DependencySet dependencies) {
        dependencies.add(project.getDependencies().create("org.apache.commons:commons-lang3:3.7"))
        dependencies.add(project.getDependencies().create("io.swagger.core.v3:swagger-jaxrs2:2.1.7"))
        dependencies.add(project.getDependencies().create("jakarta.ws.rs:jakarta.ws.rs-api:2.1.6"))
        dependencies.add(project.getDependencies().create("jakarta.servlet:jakarta.servlet-api:5.0.0"))
        if (needsJackson(extension.scmVersion.get())) {
          dependencies.add(project.getDependencies().create("com.fasterxml.jackson.core:jackson-core:2.13.4"))
        }
      }
    })

    project.tasks.register("openapi", ResolveTask) {
      outputFileName = 'openapi'
      outputFormat = 'JSONANDYAML'
      prettyPrint = 'TRUE'
      classpath = project.sourceSets.main.runtimeClasspath
      resourcePackages = extension.openApiSpec.packages
      outputDir = new File(project.buildDir, "openapi/classes/META-INF/scm")
      skip = extension.openApiSpec.packages.isEmpty()

      setBuildClasspath(config)

      dependsOn("classes")
      mustRunAfter("classes")
    }
  }

}
