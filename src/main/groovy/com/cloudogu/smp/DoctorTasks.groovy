package com.cloudogu.smp

import com.cloudogu.smp.doctor.DoctorFixTask
import com.cloudogu.smp.doctor.DoctorValidateTask
import org.gradle.api.Project
import org.gradle.language.base.plugins.LifecycleBasePlugin

class DoctorTasks {

  static void configure(Project project, SmpExtension extension, PackageJson packageJson) {
    project.tasks.register("validate", DoctorValidateTask) {
      it.description = "Validates build configuration."
      it.group = LifecycleBasePlugin.VERIFICATION_GROUP

      it.extension = extension
      it.packageJson = packageJson
    }

    project.tasks.getByName("check").configure {
      it.dependsOn("validate")
    }

    project.tasks.register("fix", DoctorFixTask) {
      it.description = "Fixes broken build configuration."
      it.group = LifecycleBasePlugin.VERIFICATION_GROUP

      it.extension = extension
      it.packageJson = packageJson
    }
  }

}
