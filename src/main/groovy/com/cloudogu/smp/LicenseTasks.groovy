package com.cloudogu.smp

import com.hierynomus.gradle.license.tasks.LicenseCheck
import org.gradle.api.Project

class LicenseTasks {

    static void configure(Project project) {
        File licenseFile = new File(project.rootDir, "LICENSE.txt")

        project.tasks.register("licenseBuild", LicenseCheck) {
            source = project.fileTree(dir: ".").include("build.gradle", "settings.gradle")
            enabled = licenseFile.exists()
        }

        project.tasks.register("licenseUI", LicenseCheck) {
            source = project.fileTree(dir: "src/main/js")
            enabled = licenseFile.exists()
        }

        project.tasks.getByName("licenseMain").configure {
            enabled = licenseFile.exists()
        }

        project.tasks.getByName("licenseTest").configure {
            enabled = licenseFile.exists()
        }

        project.tasks.getByName("license").configure {
            dependsOn("licenseBuild", "licenseUI")
            enabled = licenseFile.exists()
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

}
