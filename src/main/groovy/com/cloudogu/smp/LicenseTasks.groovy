package com.cloudogu.smp

import com.hierynomus.gradle.license.tasks.LicenseCheck
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

class LicenseTasks {

  static void configure(Project project) {
    File licenseFile = new File(project.rootDir, "LICENSE.txt")
    if (licenseFile.exists()) {
      project.plugins.apply("com.github.hierynomus.license")
      configureTasks(project, licenseFile)
    } else {
      project.tasks.register("license", LicenseFileMissingTask) {
        outputs.upToDateWhen { false }
      }
    }
  }

  private static void configureTasks(Project project, File licenseFile) {
    project.tasks.register("licenseBuild", LicenseCheck) {
      source = project.fileTree(dir: ".").include("build.gradle", "settings.gradle", "gradle.properties")
      enabled = true
    }

    project.tasks.register("licenseUI", LicenseCheck) {
      source = project.fileTree(dir: "src/main/js")
      enabled = true
    }

    project.tasks.getByName("licenseMain").configure {
      enabled = true
    }

    project.tasks.getByName("licenseTest").configure {
      enabled = true
    }

    project.tasks.getByName("license").configure {
      dependsOn("licenseBuild", "licenseUI")
      enabled = true
    }

    project.license {
      header licenseFile
      strictCheck true

      mapping {
        tsx = 'SLASHSTAR_STYLE'
        ts = 'SLASHSTAR_STYLE'
        java = 'SLASHSTAR_STYLE'
        gradle = 'SLASHSTAR_STYLE'
      }

      exclude "**/*.mustache"
      exclude "**/*.json"
      exclude "**/*.ini"
      exclude "**/mockito-extensions/*"
      exclude "**/*.txt"
      exclude "**/*.md"
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
