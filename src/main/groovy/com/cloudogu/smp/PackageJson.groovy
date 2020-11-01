package com.cloudogu.smp

import groovy.json.JsonSlurper
import org.gradle.api.Project
import org.gradle.api.tasks.InputFile

class PackageJson {

  @InputFile
  private File file
  private Object packageJson

  PackageJson(Project project) {
    this(new File(project.rootDir, "package.json"))
  }

  PackageJson(File file) {
    this.file = file
    if (file.exists()) {
      JsonSlurper slurper = new JsonSlurper()
      packageJson = slurper.parse(file)
    }
  }

  boolean exists() {
    return packageJson != null
  }

  boolean hasScript(String script) {
    if (packageJson != null) {
      def scripts = packageJson.scripts
      if (scripts != null) {
        return scripts[script] != null
      }
    }
    return false
  }
}
