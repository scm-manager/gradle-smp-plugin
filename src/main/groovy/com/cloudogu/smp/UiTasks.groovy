package com.cloudogu.smp

import com.moowork.gradle.node.yarn.YarnTask
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.language.base.plugins.LifecycleBasePlugin

class UiTasks {

    static void configure(Project project) {
        registerYarnInstall(project)
        registerUIBuild(project)
        registerUITest(project)
    }


    private static void registerUITest(Project project) {
        project.tasks.register("ui-tests", YarnTask) {
            inputs.file("package.json")
            inputs.file("yarn.lock")
            inputs.dir("src/main/js")

            outputs.dir("target/jest-reports")

            args = ['run', 'test']
            dependsOn("yarn_install")

            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Run ui tests"
        }
    }

    private static void registerUIBuild(Project project) {
        project.tasks.register("ui-bundle", YarnTask) {
            inputs.file("package.json")
            inputs.file("yarn.lock")
            inputs.dir("src/main/js")

            outputs.dir("build/webapp/assets")

            args = ['run', 'build']
            dependsOn("yarn_install")

            group = BasePlugin.BUILD_GROUP
            description = "Assembles the plugin ui bundle"
        }
    }

    private static void registerYarnInstall(Project project) {
        project.tasks.getByName('yarn_install').configure {
            inputs.file("package.json")
            inputs.file("yarn.lock")
            outputs.dir("node_modules")

            description = "Install ui dependencies"
        }
    }

}
