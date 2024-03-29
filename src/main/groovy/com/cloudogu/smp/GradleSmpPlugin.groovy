/*
 * This Groovy source file was generated by the Gradle 'init' task.
 */
package com.cloudogu.smp

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion

class GradleSmpPlugin implements Plugin<Project> {

  int determineJavaVersion(String version) {
    String[] versionParts = version.split("\\.")
    if (versionParts.length < 2) {
      return 8
    }
    def majorVersion = versionParts[0] as int
    def minorVersion = versionParts[1] as int
    if (majorVersion == 2 && minorVersion < 35) {
      return 8
    } else {
      return 11
    }
  }

  void apply(Project project) {
    if (!project.version?.trim() || "unspecified".equals(project.version) ) {
      throw new GradleException("version is missing in gradle.properties")
    }

    project.plugins.apply(JavaLibraryPlugin)
    project.plugins.apply(MavenPublishPlugin)
    project.plugins.apply(com.cloudogu.changelog.GradlePlugin)

    def extension = project.extensions.create("scmPlugin", SmpExtension, project)
    def jvmVersion = extension.scmVersion.map({ v -> determineJavaVersion(v) })

    project.java {
      toolchain {
        languageVersion = JavaLanguageVersion.of(11)
      }
    }

    project.tasks.withType(JavaCompile) {
      options.release = jvmVersion
      options.encoding = 'UTF-8'
    }

    def packageJson = new PackageJson(project)

    AnalysisTasks.configure(project, extension, packageJson)
    DoctorTasks.configure(project, extension, packageJson)
    LicenseTasks.configure(project)
    Dependencies.configure(project, extension)
    UiTasks.configure(project, packageJson)
    TestTasks.configure(project)

    def artifact = PackagingTasks.configure(project, packageJson, extension)
    PublishingTasks.configure(project, extension, artifact)
    RunTasks.configure(project, packageJson, extension)
    VersionTasks.configure(project, extension)
  }

}
