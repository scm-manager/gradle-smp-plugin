package com.cloudogu.smp

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.publish.maven.MavenPublication
import static com.cloudogu.smp.Dependencies.*

class PublishingTasks {

  static void configure(Project project, SmpExtension extension, PublishArtifact smp) {
    project.afterEvaluate {
      configurePublishing(project, extension, smp)
    }
  }

  private static void configurePublishing(Project project, SmpExtension extension, PublishArtifact smp) {
    project.java {
      withJavadocJar()
      withSourcesJar()
    }

    project.publishing {
      publications {
        mavenJava(MavenPublication) {
          groupId = extension.group
          artifactId = extension.getName(project)
          version = project.version

          from project.components.java
          artifact smp

          pom {
            packaging = "smp"
            description = extension.description
          }

          pom.withXml {
            def rootNode = asNode()
            rootNode.remove(rootNode.get('dependencies'))
            def dependenciesNode = rootNode.appendNode('dependencies')

            Set<Dependency> runtime = runtimeDependencies(project)

            def provided = project.configurations.scmCoreDependency.allDependencies
              .findAll { dep ->
                return !(dep.group.equals("sonia.scm") && dep.name.equals("scm"))
              }

            appendDependencies(dependenciesNode, provided, 'provided')
            appendDependencies(dependenciesNode, runtime)
            appendDependencies(dependenciesNode, project.configurations.plugin.dependencies)
            appendDependencies(dependenciesNode, project.configurations.optionalPlugin.dependencies, null, true)
          }
        }
      }
      repositories {
        maven {
          // TODO package.scm-manager.org
          url = "${project.buildDir}/repo"
        }
      }
    }
  }

}
