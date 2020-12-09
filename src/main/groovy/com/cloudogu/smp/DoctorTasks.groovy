package com.cloudogu.smp

import com.cloudogu.smp.doctor.DoctorFixTask
import com.cloudogu.smp.doctor.DoctorValidateTask
import com.cloudogu.smp.doctor.ValidatePluginsJsonTask
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

    File localeDirectory = new File(project.rootDir, 'src/main/resources/locales')
    project.tasks.register("validatePluginJson", ValidatePluginsJsonTask) {
      it.localeDirectory = localeDirectory.exists() ? localeDirectory : null
      it.outputMarker = new File(project.buildDir, "temp/validatePluginJson/marker")
    }

    project.tasks.getByName("check").configure {
      it.dependsOn("validate", 'validatePluginJson')
    }

    project.tasks.register("fix", DoctorFixTask) {
      it.description = "Fixes broken build configuration."
      it.group = LifecycleBasePlugin.VERIFICATION_GROUP

      it.extension = extension
      it.packageJson = packageJson
    }
  }

}
