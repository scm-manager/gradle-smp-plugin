package com.cloudogu.smp

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.Dependency
import org.gradle.api.attributes.*
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet

class Dependencies {

  static Iterable<File> createPackagingClasspath(Project project) {
    FileCollection runtimeClasspath = project.getConvention().getPlugin(JavaPluginConvention.class)
      .getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME).getRuntimeClasspath()
    Configuration plugins = project.getConfigurations().getByName("plugin")
    Configuration optionalPlugin = project.getConfigurations().getByName("optionalPlugin")
    runtimeClasspath -= plugins
    runtimeClasspath - optionalPlugin
  }

  static Set<Dependency> runtimeDependencies(Project project) {
    def runtime = project.configurations.implementation.allDependencies
    runtime -= project.configurations.scmCoreDependency.allDependencies
    runtime -= project.configurations.plugin.allDependencies
    runtime -= project.configurations.optionalPlugin.allDependencies
    runtime
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
    def libraryElementsStrategy = project.dependencies.attributesSchema.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE)
    libraryElementsStrategy.compatibilityRules.add(SMPLibraryElementsCompatibilityRule)
    libraryElementsStrategy.disambiguationRules.add(SMPLibraryDisambiguationRule)

    project.dependencies.components.all(SmpVariantRule)

    configureRepositories(project)
    createConfigurations(project)
    project.afterEvaluate {
      configureDependencies(project, extension)
    }
  }

  private static void createConfigurations(Project project) {
    // We define our own configuration container that we are able to use all dependencies for compilation,
    // but remove the core dependencies before packaging
    // https://github.com/gradle/gradle/blob/master/subprojects/plugins/src/main/java/org/gradle/api/plugins/WarPlugin.java#L72
    ConfigurationContainer configurationContainer = project.getConfigurations()
    Configuration coreDependency = configurationContainer
      .create("scmCoreDependency")
      .setVisible(false)
      .setDescription("Additional classpath for libraries which are provided from scm code.")

    Configuration pluginDependency = configurationContainer
      .create("plugin")
      .setVisible(false)
      .setDescription("Plugin dependencies.")
      .extendsFrom(coreDependency)

    pluginDependency.canBeConsumed = true

    configurationContainer.getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME).extendsFrom(pluginDependency)

    Configuration optionalPlugin = configurationContainer
      .create("optionalPlugin")
      .setVisible(false)
      .setDescription("Optional plugin dependencies.")
      .extendsFrom(coreDependency)

    configurationContainer.getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME).extendsFrom(optionalPlugin)

    Configuration runtimeScmElements = project.configurations.create("runtimePluginElements")
    runtimeScmElements.canBeResolved = true
    runtimeScmElements.canBeConsumed = true
    runtimeScmElements.attributes {
      it.attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage, Usage.JAVA_RUNTIME))
      it.attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category, Category.LIBRARY))
      it.attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named(Bundling, Bundling.EXTERNAL))
      it.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements, "smp"))

      runtimeScmElements.extendsFrom(pluginDependency, optionalPlugin)
    }
  }

  private static void configureDependencies(Project project, SmpExtension extension) {
    // gradle has xerces on it classpath, which breaks our annotation processor
    // so we force jdk build in for now
    // @see https://stackoverflow.com/questions/53299280/java-and-xerces-cant-find-property-xmlconstants-access-external-dtd
    System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
      "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");

    project.dependencies {
      // we enforce the dependency versions from scm-manager root pom dependency management
      if (extension.core) {
        scmCoreDependency enforcedPlatform(project.project(':'))
        scmCoreDependency project.project(':scm-core')
        scmCoreDependency project.project(':scm-test')
        annotationProcessor project.project(':scm-annotation-processor')
      } else {
        scmCoreDependency enforcedPlatform("sonia.scm:scm:${extension.scmVersion}")
        scmCoreDependency "sonia.scm:scm-core:${extension.scmVersion}"
        testImplementation "sonia.scm:scm-test:${extension.scmVersion}"
        annotationProcessor "sonia.scm:scm-annotation-processor:${extension.scmVersion}"
      }

      // is provided in scm-core
      scmCoreDependency "javax.ws.rs:javax.ws.rs-api:2.1.1"
      scmCoreDependency "io.swagger.core.v3:swagger-annotations:2.1.1"
      scmCoreDependency 'javax.servlet:javax.servlet-api:3.1.0'

      scmCoreDependency 'org.projectlombok:lombok:1.18.12'
      scmCoreDependency 'org.mapstruct:mapstruct-jdk8:1.3.1.Final'

      // register annotation processors
      annotationProcessor 'org.mapstruct:mapstruct-processor:1.3.1.Final'
      annotationProcessor 'org.projectlombok:lombok:1.18.12'

      // resteasy test dependencies
      testImplementation "org.jboss.resteasy:resteasy-core:4.5.3.Final"
      testImplementation "org.jboss.resteasy:resteasy-core-spi:4.5.3.Final"
      testImplementation "org.jboss.resteasy:resteasy-jackson2-provider:4.5.3.Final"

      // test engine
      testAnnotationProcessor 'org.projectlombok:lombok:1.18.12'

      testImplementation 'org.junit.jupiter:junit-jupiter-api:5.6.2'
      testImplementation 'org.junit.jupiter:junit-jupiter-params:5.6.2'
      testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.6.2'

      testImplementation 'org.assertj:assertj-core:3.16.1'
      testImplementation 'org.mockito:mockito-core:1.3.1.Final'
      testImplementation 'org.mockito:mockito-junit-jupiter:1.3.1.Final'
    }
  }

  private static void configureRepositories(Project project) {
    project.repositories {
      mavenLocal()
      maven {
        url "https://packages.scm-manager.org/repository/public/"
      }
    }
  }

  private static class SMPLibraryElementsCompatibilityRule implements AttributeCompatibilityRule<LibraryElements> {
    @Override
    void execute(CompatibilityCheckDetails<LibraryElements> details) {
      if (details.consumerValue.name == "smp" && details.producerValue.name == LibraryElements.JAR) {
        // accept JARs for libraries that do not have JPIs so that we do not fail.
        // Non-JPI files will be filtered out later if needed (e.g. by the TestDependenciesTask)
        details.compatible()
      }
    }
  }

  private static class SMPLibraryDisambiguationRule implements AttributeDisambiguationRule<LibraryElements> {

    @Override
    void execute(MultipleCandidatesDetails<LibraryElements> details) {
      if (details.consumerValue.name == "smp") {
        details.candidateValues.each {
          if (it.name == "smp") {
            details.closestMatch(it)
          }
        }
      }
    }
  }
}
