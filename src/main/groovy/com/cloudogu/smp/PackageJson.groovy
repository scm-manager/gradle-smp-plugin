package com.cloudogu.smp

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.Project
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

class PackageJson {

  private File file
  private Object packageJson

  PackageJson(Project project) {
    this(new File(project.rootDir, "package.json"))
  }

  PackageJson(File file) {
    this.file = file
    if (file.exists()) {
      parse()
    }
  }

  private void parse() {
    JsonSlurper slurper = new JsonSlurper()
    packageJson = slurper.parse(file)
  }

  @Internal
  String getName() {
    if (packageJson != null) {
      return packageJson.name
    }
  }

  @Internal
  String getVersion() {
    if (packageJson != null) {
      return packageJson.version
    }
  }

  @InputFile
  @PathSensitive(PathSensitivity.RELATIVE)
  File getFile() {
    return file
  }

  String getDependencyVersion(String dependency) {
    if (packageJson != null) {
      return packageJson.dependencies[dependency]
    }
  }

  boolean exists() {
    return packageJson != null
  }

  Optional<String> getScript(String script) {
    if (packageJson != null) {
      def scripts = packageJson.scripts
      if (scripts != null) {
        return Optional.ofNullable(scripts[script])
      }
    }
    Optional.empty()
  }

  boolean hasScript(String script) {
    return getScript(script).isPresent()
  }

  void modify(Closure<Void> modifier) {
    modifier.call(packageJson)
    file.setText(JsonOutput.prettyPrint(JsonOutput.toJson(packageJson)))
    parse()
  }
}
