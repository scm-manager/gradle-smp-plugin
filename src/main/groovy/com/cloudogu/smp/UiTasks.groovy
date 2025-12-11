/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.smp

import com.moowork.gradle.node.yarn.YarnTask
import com.moowork.gradle.node.NodeExtension
import org.gradle.api.Project
import org.gradle.api.GradleException
import org.gradle.api.plugins.BasePlugin
import org.gradle.language.base.plugins.LifecycleBasePlugin

class UiTasks {

  static void configure(Project project, SmpExtension extension, PackageJson packageJson) {
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
        registerUITest(project, extension)
      }
      if (packageJson.hasScript("deploy")) {
        registerUIDeploy(project)
      }
    }
  }

  private static void setupNodeEnv(Project project) {
    project.plugins.apply("com.github.node-gradle.node")
    def nodeExt = NodeExtension.get(project)
    nodeExt.setDownload(true)
    nodeExt.setVersion(Environment.NODE_VERSION)
    nodeExt.setYarnVersion(Environment.YARN_VERSION)
  }

  private static void registerYarnInstall(Project project) {
    project.tasks.getByName('yarn_install').configure {
      inputs.file('package.json')
      inputs.file( project.rootProject.file('yarn.lock') )
      outputs.dir( project.rootProject.file('node_modules') )

      description = "Install ui dependencies"
    }
  }

  private static void registerUITypeCheck(Project project) {
    File marker = new File(project.buildDir, 'tmp/ui-typecheck/marker')
    project.tasks.register("ui-typecheck", YarnTask) {
      inputs.file("package.json")
      inputs.file( project.rootProject.file('yarn.lock') )
      inputs.dir("src/main/js")
      outputs.file(marker)

      args = ['run', 'typecheck']
      dependsOn("yarn_install")

      group = LifecycleBasePlugin.VERIFICATION_GROUP
      description = "Run typecheck"

      doLast {
        File directory = marker.getParentFile()
        if (!directory.exists() && !directory.mkdirs()) {
          throw new GradleException("failed to create directory ${directory}")
        }
        if (!marker.exists() && !marker.createNewFile()) {
          throw new GradleException("failed to create marker file ${marker}")
        }
      }
    }

    project.tasks.getByName("check").configure {
      dependsOn("ui-typecheck")
    }
  }

  private static void registerUIBuild(Project project) {
    project.tasks.register("ui-bundle", YarnTask) {
      inputs.file("package.json")
      inputs.file( project.rootProject.file('yarn.lock') )
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

  private static void registerUITest(Project project, SmpExtension smpExtension) {
    def testRunner = testsRunWith(smpExtension.scmVersion.get())
    if (Environment.isCI()) {
      project.tasks.register("update-ui-test-timestamp", TouchFilesTask) {
        directory = new File(project.buildDir, "${testRunner}-reports")
        extension = "xml"
      }
    }

    project.tasks.register("ui-test", YarnTask) {
      inputs.file("package.json")
      inputs.file( project.rootProject.file('yarn.lock') )
      inputs.dir("src/main/js")

      outputs.dir("build/${testRunner}-reports")

      args = ['run', 'test']
      ignoreExitValue = Environment.isCI()

      if (Environment.isCI()) {
        dependsOn("yarn_install", "update-ui-test-timestamp")
      } else {
        dependsOn("yarn_install")
      }

      group = LifecycleBasePlugin.VERIFICATION_GROUP
      description = "Run ui tests"
    }

    project.tasks.getByName("test").configure {
      dependsOn("ui-test")
    }
  }

  private static String testsRunWith(String version) {
    String[] versionParts = version.split("[.-]")
    def majorVersion = versionParts[0] as int
    return majorVersion >= 4 ? "vite" : "jest"
  }

  private static void registerUIDeploy(Project project) {
    project.tasks.register("ui-deploy", YarnTask) {
      inputs.file("package.json")
      inputs.file( project.rootProject.file('yarn.lock') )
      inputs.dir("src/main/js")

      args = ['run', 'deploy', project.version]
      dependsOn("yarn_install")

      group = "publishing"
      description = "Run ui tests"
    }

    project.tasks.getByName("publish").configure {
      dependsOn("ui-deploy")
    }
  }

}
