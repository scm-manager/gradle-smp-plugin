package com.cloudogu.smp

import com.moowork.gradle.node.yarn.YarnTask
import com.moowork.gradle.node.NodeExtension
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.language.base.plugins.LifecycleBasePlugin

class UiTasks {

  static void configure(Project project, PackageJson packageJson, SmpExtension extension) {
    if (packageJson.exists()) {
      setupNodeEnv(project)
      registerYarnInstall(project)

      if (packageJson.hasScript("typecheck")) {
        registerUITypeCheck(project)
      }
      if (packageJson.hasScript("build")) {
        registerUIBuild(project)
      }
      if (packageJson.hasScript("test")) {
        registerUITest(project)
      }
      if (packageJson.hasScript("deploy")) {
        registerUIDeploy(project, extension)
      }
    }
  }

  private static void setupNodeEnv(Project project) {
    project.plugins.apply("com.github.node-gradle.node")
    def nodeExt = NodeExtension.get(project)
    nodeExt.setDownload(true)
  }

  private static void registerYarnInstall(Project project) {
    project.tasks.getByName('yarn_install').configure {
      inputs.file("package.json")
      inputs.file("yarn.lock")
      outputs.dir("node_modules")

      description = "Install ui dependencies"
    }
  }

  private static void registerUITypeCheck(Project project) {
    project.tasks.register("ui-typecheck", YarnTask) {
      inputs.file("package.json")
      inputs.file("yarn.lock")
      inputs.dir("src/main/js")

      args = ['run', 'typecheck']
      dependsOn("yarn_install")

      group = LifecycleBasePlugin.VERIFICATION_GROUP
      description = "Run typecheck"
    }

    project.tasks.getByName("check").configure {
      dependsOn("ui-typecheck")
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

    project.afterEvaluate {
      project.tasks.getByName("smp").configure {
        dependsOn("ui-bundle")
      }
    }
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

    project.tasks.getByName("test").configure {
      dependsOn("ui-tests")
    }
  }

  private static void registerUIDeploy(Project project, SmpExtension extension) {
    project.tasks.register("ui-deploy", YarnTask) {
      inputs.file("package.json")
      inputs.file("yarn.lock")
      inputs.dir("src/main/js")

      args = ['run', 'deploy', extension.version]
      dependsOn("yarn_install")

      group = "publishing"
      description = "Run ui tests"
    }

    project.tasks.getByName("publish").configure {
      dependsOn("ui-deploy")
    }
  }

}
