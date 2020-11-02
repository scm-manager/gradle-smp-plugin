package com.cloudogu.smp.doctor

import com.cloudogu.smp.PackageJson
import com.cloudogu.smp.SmpExtension
import org.gradle.api.Project

class Context {

  private final Project project
  private final SmpExtension extension
  private final PackageJson packageJson

  Context(Project project, SmpExtension extension, PackageJson packageJson) {
    this.project = project
    this.extension = extension
    this.packageJson = packageJson
  }

  Project getProject() {
    return project
  }

  SmpExtension getExtension() {
    return extension
  }

  PackageJson getPackageJson() {
    return packageJson
  }
}
