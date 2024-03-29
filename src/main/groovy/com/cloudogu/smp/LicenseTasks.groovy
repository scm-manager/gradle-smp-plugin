package com.cloudogu.smp

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

class LicenseTasks {

  static void configure(Project project) {
    File licenseFile = new File(project.rootDir, "LICENSE.txt")
    if (licenseFile.exists()) {
      project.plugins.apply("org.scm-manager.license")
      configureTasks(project, licenseFile)
    } else {
      project.tasks.register("license", LicenseFileMissingTask) {
        outputs.upToDateWhen { false }
      }
    }
  }

  private static void configureTasks(Project project, File licenseFile) {
    project.tasks.register("license") {
      dependsOn 'checkLicenses'
    }

    project.license {
      header licenseFile
      newLine = true
      ignoreNewLine = true
      lineEnding = "\n"

      exclude "**/*.mustache"
      exclude "**/*.json"
      exclude "**/*.ini"
      exclude "**/mockito-extensions/*"
      exclude "**/*.txt"
      exclude "**/*.md"

      tasks {
        gradle {
          files.from("build.gradle", "settings.gradle", "gradle.properties")
        }
        ui {
          files.from("src/main/js")
        }
      }
    }
  }

  static class LicenseFileMissingTask extends DefaultTask {

    @TaskAction
    void execute() {
      println "No LICENSE.txt found"
      println "In order to use the license check, please create a LICENSE.txt in the root of the project"
    }

  }

}
