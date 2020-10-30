/*
 * This Groovy source file was generated by the Gradle 'init' task.
 */
package com.cloudogu.smp

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin

class GradleSmpPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.plugins.apply(JavaLibraryPlugin)
        project.plugins.apply("com.github.node-gradle.node")
        project.plugins.apply("io.swagger.core.v3.swagger-gradle-plugin")
        project.plugins.apply("com.github.hierynomus.license")
        project.plugins.apply(MavenPublishPlugin)

        def extension = project.extensions.create("scmPlugin", SmpExtension)

        LicenseTasks.configure(project)
        Dependencies.configure(project, extension)
        UiTasks.configure(project)
        TestTasks.configure(project)

        def artifact = PackagingTasks.configure(project, extension)
        PublishingTasks.configure(project, extension, artifact)
        RunTasks.configure(project, extension)
    }

}