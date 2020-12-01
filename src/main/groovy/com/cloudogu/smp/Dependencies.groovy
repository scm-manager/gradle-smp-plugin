package com.cloudogu.smp

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet

class Dependencies {

  static Set<File> resolveSmp(Project project, String dependencyString) {
    String coordinates = dependencyString
    if (!dependencyString.endsWith("@smp")) {
      coordinates = dependencyString + "@smp"
    }
    def dependency = project.dependencies.create(coordinates)
    def configuration = project.configurations.detachedConfiguration(dependency)
    configuration.resolve()
    configuration.files
  }

  static Iterable<File> createPackagingClasspath(Project project) {
    FileCollection runtimeClasspath = project.getConvention().getPlugin(JavaPluginConvention.class)
      .getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME).getRuntimeClasspath()
    Configuration scmPluginDependency = project.getConfigurations().getByName("scmPluginDependency")
    runtimeClasspath - scmPluginDependency
  }

  static Set<Dependency> runtimeDependencies(Project project) {
    def runtime = project.configurations.implementation.allDependencies
    runtime -= project.configurations.scmCoreDependency.allDependencies
    runtime -= project.configurations.scmPluginDependency.allDependencies
    runtime
  }

  static Iterable<Dependency> createDependencies(Project project, Collection<String> dependencyStrings) {
    dependencyStrings.collect { dep ->
      project.dependencies.create(dep)
    }
  }

  static void appendDependencies(Node dependenciesNode, Iterable<Dependency> dependencies, String scope = null, boolean optional = false) {
    dependencies.forEach { dep ->
      def dependencyNode = dependenciesNode.appendNode('dependency')
      dependencyNode.appendNode('groupId', dep.group)
      dependencyNode.appendNode('artifactId', dep.name)
      if (dep.version != null && !dep.version.isEmpty()) {
        dependencyNode.appendNode('version', dep.version)
      }
      if (scope != null) {
        dependencyNode.appendNode('scope', 'provided')
      }
      if (optional) {
        dependencyNode.appendNode('optional', true)
      }
    }
  }

  static void configure(Project project, SmpExtension extension) {
    configureRepositories(project)
    project.afterEvaluate {
      configureDependencies(project, extension)
    }
  }

  private static void configureDependencies(Project project, SmpExtension extension) {
    // gradle has xerces on it classpath, which breaks our annotation processor
    // so we force jdk build in for now
    // @see https://stackoverflow.com/questions/53299280/java-and-xerces-cant-find-property-xmlconstants-access-external-dtd
    System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
      "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");

    // We define our own configuration container that we are able to use all dependencies for compilation,
    // but remove the core dependencies before packaging
    // https://github.com/gradle/gradle/blob/master/subprojects/plugins/src/main/java/org/gradle/api/plugins/WarPlugin.java#L72
    ConfigurationContainer configurationContainer = project.getConfigurations()
    Configuration coreDependency = configurationContainer
      .create("scmCoreDependency")
      .setVisible(false)
      .setDescription("Additional classpath for libraries which are provided from scm code.")

    Configuration pluginDependency = configurationContainer
      .create("scmPluginDependency")
      .setVisible(false)
      .setDescription("Plugin dependencies.")
      .extendsFrom(coreDependency)

    configurationContainer.getByName(JavaPlugin.COMPILE_CONFIGURATION_NAME).extendsFrom(pluginDependency)

    project.dependencies {
      // we enforce the dependency versions from scm-manager root pom dependency management
      // TODO at all dependencies from here to dependencyManagement of core
      // for older version we could add those manually based on the scm version
      scmCoreDependency enforcedPlatform("sonia.scm:scm:${extension.scmVersion}")

      scmCoreDependency "sonia.scm:scm-core:${extension.scmVersion}"

      // is provided in scm-core
      scmCoreDependency "javax.ws.rs:javax.ws.rs-api:2.1.1"
      scmCoreDependency "io.swagger.core.v3:swagger-annotations:2.1.1"
      // TODO define in dependencyManagement
      scmCoreDependency 'javax.servlet:javax.servlet-api:3.1.0'

      scmCoreDependency 'org.projectlombok:lombok:1.18.12'
      scmCoreDependency 'org.mapstruct:mapstruct-jdk8:1.3.1.Final'

      // register annotation processors
      // TODO because it is defined as provided in dependencyManagement?
      annotationProcessor 'org.mapstruct:mapstruct-processor:1.3.1.Final'
      // TODO because it is defined as provided in dependencyManagement?
      annotationProcessor 'org.projectlombok:lombok:1.18.12'
      annotationProcessor "sonia.scm:scm-annotation-processor:${extension.scmVersion}"

      // test dependencies
      testImplementation "sonia.scm:scm-test:${extension.scmVersion}"

      // resteasy test dependencies
      testImplementation "org.jboss.resteasy:resteasy-core:4.5.3.Final"
      testImplementation "org.jboss.resteasy:resteasy-core-spi:4.5.3.Final"
      testImplementation "org.jboss.resteasy:resteasy-jackson2-provider:4.5.3.Final"

      // test engine
      // TODO because it is defined as provided in dependencyManagement?
      testAnnotationProcessor 'org.projectlombok:lombok:1.18.12'

      testImplementation 'org.junit.jupiter:junit-jupiter-api:5.6.2'
      testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.6.2'

      testImplementation 'org.assertj:assertj-core:3.16.1'
      testImplementation 'org.mockito:mockito-core:1.3.1.Final'
      testImplementation 'org.mockito:mockito-junit-jupiter:1.3.1.Final'

      // we have to add both smp and jar,
      // because the smp (default artifact) knows the dependencies and the jar knows the plugin classes
      extension.dependencies.forEach { dep ->
        scmPluginDependency "${dep}"
        scmPluginDependency "${dep}@jar"
      }

      extension.optionalDependencies.forEach { dep ->
        scmPluginDependency "${dep}"
        scmPluginDependency "${dep}@jar"
      }
    }
  }

  private static void configureRepositories(Project project) {
    project.repositories {
      maven {
        url "https://packages.scm-manager.org/repository/public/"
      }
    }
  }
}
