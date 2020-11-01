package com.cloudogu.smp

import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Sync
import static com.cloudogu.smp.Dependencies.*

class RunTasks {

    static void configure(Project project, PackageJson packageJson, SmpExtension extension) {
        project.tasks.register("copy-dependencies", Copy) {
            extension.dependencies.each { dependencyString ->
                def files = resolveSmp(project, dependencyString)
                from(files)
            }

            extension.optionalDependencies.each { dependencyString ->
                def files = resolveSmp(project, dependencyString)
                from(files)
            }

            destinationDir = new File(extension.getScmHome(project), "plugins")
        }

        project.tasks.register("prepare-home", Sync) {
            createPackagingClasspath(project).each { file ->
                if (file.name.endsWith(".jar")) {
                    from(file) {
                        into("lib")
                    }
                } else if (file.name == "main") {
                    from(file) {
                        into("classes")
                        exclude("**/module.xml")
                    }
                } else {
                    println "WARNING: unknown classpath entry ${file}"
                }
            }

            from "build/smp"
            from("build/resources/main/META-INF") {
                into "META-INF"
            }
            from("src/main/webapp") {
                into "webapp"
            }

            destinationDir = new File(extension.getScmHome(project), "plugins/${extension.getName(project)}")
            dependsOn("classes", "copy-dependencies")
        }

        project.tasks.register("run", RunTask) {
          description = "Run SCM-Manager with the plugin installed"
          it.extension = extension
          it.packageJson = packageJson
          // run always
          outputs.upToDateWhen { false }
          dependsOn("prepare-home", "yarn_install")
        }
    }

}
