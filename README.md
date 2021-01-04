<p align="center">
  <a href="https://www.scm-manager.org/">
    <img alt="SCM-Manager" src="https://download.scm-manager.org/images/logo/scm-manager_logo.png" width="500" />
  </a>
</p>
<h1 align="center">
  gradle-smp-plugin
</h1>

A [Gradle](https://gradle.org/) plugin to build SCM-Manager plugins.

## Installation

To install the plugin just add the following snippet to your build.gradle file.

```groovy
plugins {
  id "org.scm-manager.smp" version "..."
}
```

The version should always be the latest available.
The latest version can be found in the [Gradle Plugin Center](https://plugins.gradle.org/plugin/org.scm-manager.smp).

## Configuration

### Version

The version of the plugin is specified in the `gradle.properties` file.

### Metadata

Most of the metadata is configured inside a `scmPlugin` block in the `build.gradle` file.
Such a block looks like the following:

```groovy
scmPlugin {
  scmVersion = "2.8.0"
  displayName = "Review"
  description = "Depict a review process with pull requests"
  author = "Cloudogu GmbH"
  category = "Workflow"

  conditions {
    os = "Linux"
  }
  
  run {
    home = "/var/lib/scm"
  }
  
  openapi {
    packages = [
      "com.cloudogu.scm.review.pullrequest.api",
      "com.cloudogu.scm.review.config.api",
      "com.cloudogu.scm.review.workflow",
    ]
  }
}
```

The following table shows the available options.

| Name | Required | Description |
| ---- | -------- | ----------- |
| scmVersion | Yes | SCM-Manager parent version |
| name | No | Name of the plugin, default the gradle project name is used |
| group | No | Maven group id, default is `sonia.scm.plugins` |
| displayName | Yes | Display name of the plugin |
| description | Yes | A short description of the plugin |
| author | Yes | Who has written the plugin |
| category | Yes | The [category](https://www.scm-manager.org/plugins/#categories) of the plugin |
| openapi.packages | No | Generate OpenApi documentation for the given packages |
| conditions.os | No | Specifies on which operating system the plugin can run |
| conditions.arch | No | Specifies on which cpu architecture the plugin can run |
| run.warFile | No | Path to the war file which is used with the `run` task |
| run.home | No | Path to the scm home directory, default is `build/scm-home` (can also be changed by passing `-Pscm.home=/path` to gradle) |
| run.port | No | Port used to start SCM-Manager, default is `8081` |
| run.contextPath | No | Context path for SCM-Manager, default is `/scm` |
| run.disableCorePlugins | No | Disable the installation of core plugin, default is `false` |
| run.stage | No | Stage of SCM-Manager runtime, default is `DEVELOPMENT` |
| run.headerSize | No | Jetty header size, default is `16384` |
| run.loggingConfiguration | No | Path to a logback configuration |
| run.openBrowser | No | Open a browser after SCM-Manager is started, default is `true` |
| sonar.property | No | Specify extra properties for SonarQube analysis (can be used multiple times) |

The blocks openapi, conditions, run and sonar are complete optional. 

### Declaring dependencies

Java dependencies are handled by the gradle [java plugin](https://docs.gradle.org/current/userguide/dependency_management_for_java_projects.html) e.g.:

```groovy
dependencies {
  // implementation is used for runtime dependencies 
  implementation "org.jasig.cas.client:cas-client-core:3.5.1"
  // and testImplementation is used for test dependencies
  testImplementation "org.jboss.resteasy:resteasy-validator-provider:4.5.8.Final"
}
```

If you want to declare a dependency to another SCM-Manager plugin a special configuration is required:

```groovy
dependencies {
  // plugin is used for a required plugin dependency 
  plugin "sonia.scm.plugins:scm-mail-plugin:2.1.0"
  // and optionalPlugin is used for an optional one
  optionalPlugin "sonia.scm.plugins:scm-editor-plugin:2.0.0"
}
```

## Tasks

The gradle-smp-plugin provides some high level tasks,
which should cover most of the daily work.

| Name | Description |
| ---- | ----------- |
| run | Starts an SCM-Manager plugin with the plugin pre installed, with livereload for the ui |
| build | Executes all checks, tests and builds the smp inclusive javadoc and source jar |
| check | Executes all registered checks and tests (java and ui) |
| test | Run all java tests |
| ui-test | Run all ui tests |
| fix | Fixes all fixable findings of the check task |
| smp | Builds the smp file, without the execution of checks and tests |
| clean | Deletes the build directory |

The plugin also defines a few tasks which are more relevant for CI servers. 

| Name | Description |
| ---- | ----------- |
| publish | Publishes the plugin to packages.scm-manager.org (requires credentials) |
| sonarqube | Executes a SonarQube analysis |
| setVersion | Sets the version to a new version |
| setVersionToNextSnapshot | Sets the version to the next snapshot version |

There many more tasks, which are executed as part of the high level tasks, 
and it should rarely be necessary to call them individually.
To see the full list of available tasks, execute the following command:

```bash
./gradlew tasks
```

## Converting from Maven

If you want to migrate an existing plugin from Maven to Gradle, 
please have a look at [smp-maven-to-gradle](https://github.com/scm-manager/smp-maven-to-gradle).

## Local development

To speed up local development the build of the gradle-smp-plugin can be combined with the one of the SCM-Manager plugin.
Doing so will use the gradle-smp-plugin directly from the source code and changes are immediately visible.
To combine the builds, we have to clone the gradle-smp-plugin and insert the path into the settings.gradle file of our SCM-Manager plugin e.g.:

```groovy
rootProject.name = 'scm-review-plugin'
includeBuild '../gradle-smp-plugin'
```

## Need help?

Looking for more guidance? Full documentation lives on our [homepage](https://www.scm-manager.org/docs/) or the dedicated pages for our [plugins](https://www.scm-manager.org/plugins/). Do you have further ideas or need support?

- **Community Support** - Contact the SCM-Manager support team for questions about SCM-Manager, to report bugs or to request features through the official channels. [Find more about this here](https://www.scm-manager.org/support/).

- **Enterprise Support** - Do you require support with the integration of SCM-Manager into your processes, with the customization of the tool or simply a service level agreement (SLA)? **Contact our development partner Cloudogu! Their team is looking forward to discussing your individual requirements with you and will be more than happy to give you a quote.** [Request Enterprise Support](https://cloudogu.com/en/scm-manager-enterprise/).
