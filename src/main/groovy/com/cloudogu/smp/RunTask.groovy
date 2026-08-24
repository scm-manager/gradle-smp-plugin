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

import com.google.common.base.CharMatcher
import com.google.common.base.Splitter
import com.github.gradle.node.task.NodeTask
import com.github.gradle.node.yarn.task.YarnTask
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.process.JavaExecSpec
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault

import javax.inject.Inject
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.stream.Collectors

@DisableCachingByDefault(because = "Starts long-running backend and frontend processes")
abstract class RunTask extends DefaultTask {

  @Inject
  abstract ExecOperations getExecOperations()

  @Nested
  SmpExtension extension

  @Internal
  PackageJson packageJson

  @Input
  List<Action<JavaExecSpec>> execSpecActions = []

  @Input
  @Option(option = 'debug-jvm', description = 'Start SCM server suspended and listening on debug port (default: 5005)')
  boolean debugJvm = false

  @Input
  @Option(option = 'debug-wait', description = 'Wait until a debugger has connected')
  boolean debugWait = false

  @Input
  @Option(option = 'debug-port', description = 'Port for debugger')
  String debugPort = "5005"

  @Input
  @Option(option = 'run-in-background', description = 'Start SCM server in background')
  boolean runInBackground = false

  @TaskAction
  void exec() {
    List<Closure<Void>> actions = new ArrayList<>()
    actions.add(createBackend())
    if (packageJson.hasScript("watch")) {
      actions.add(createFrontend())
    }
    def threads = start(actions)
    if (!runInBackground) {
      wait(threads)
    } else {
      waitForPortToBeOpen()
    }
  }

  private static void wait(List<Thread> threads) {
    for (Thread thread : threads) {
      thread.join()
    }
  }

  private void waitForPortToBeOpen() {
    int retries = 180
    for (int i = 0; i < retries; i++) {
      try {
        URL urlConnect = new URL("http://localhost:${extension.serverConfiguration.port}/scm/api/v2")
        URLConnection conn = (HttpURLConnection) urlConnect.openConnection()
        if (conn.getResponseCode() == 200) {
          return
        }
      } catch (IOException ex) {
        System.out.println("scm-server not reachable, retrying...")
      }
      Thread.sleep(500)
    }
    throw new GradleException("scm-server not reachable")
  }

  private static List<Thread> start(List<Closure<Void>> actions) {
    return actions.stream().map({ action ->
      Thread thread = new Thread(action)
      thread.start()
      return thread
    }).collect(Collectors.toList())
  }

  private Closure<Void> createBackend() {
    return {
      execOperations.javaexec { jes ->
        jes.mainClass.set(ScmServer.name)
        jes.args(extension.getServerConfigurationFile(project))
        jes.environment("NODE_ENV", "development")
        jes.environment("SCM_WEBAPP_HOMEDIR", extension.getScmHome(project).getAbsolutePath())
        jes.classpath(project.buildscript.configurations.classpath)
        if (extension.configFileDirectory != "") {
          println("Using config.yml from " + extension.configFileDirectory)
          jes.classpath(extension.configFileDirectory)
        } else if (!Paths.get("build/server/config.yml").toFile().exists()) {
          InputStream resource = getClass().getResourceAsStream("conf/config.yml")
          Path fallbackConfig = Files.createFile(Path.of("build/server", "config.yml"))
          Files.copy(resource, fallbackConfig, StandardCopyOption.REPLACE_EXISTING)
          jes.classpath(fallbackConfig.getParent())
        } else {
          Path fallbackConfigDir = Paths.get("build/server")
          jes.classpath(fallbackConfigDir)
        }
        if (debugJvm) {
          jes.debug = true
          jes.debugOptions {
            enabled = true
            port = Integer.parseInt(debugPort)
            server = true
            suspend = debugWait
          }
        }
        execSpecActions.each { a -> a.execute(jes) }
      }
      return null
    }
  }

  private

  void execSpec(Action<JavaExecSpec> action) {
    execSpecActions.add(action)
  }

  private Closure<Void> createFrontend() {
    def name = extension.getName(project)
    def home = new File(extension.getScmHome(project), "plugins/${name}/webapp/assets")
    if (!home.exists()) {
      home.mkdirs()
    }

    def env = [
      "BUNDLE_OUTPUT": home.absolutePath,
      "NODE_ENV"     : "development"
    ]

    def frontend
    def script = packageJson.getScript("watch").orElseThrow({ ->
      new IllegalStateException("could not find watch script in package.json")
    })
    // we call the plugin scripts directly, to avoid stop problems with yarn on windows
    if (script.startsWith("plugin-scripts")) {
      def args = Splitter.on(CharMatcher.whitespace()).omitEmptyStrings().trimResults().splitToList(script)
      frontend = project.tasks.create("boot-frontend", NodeTask) {
        it.script.set(project.layout.projectDirectory.file("node_modules/@scm-manager/plugin-scripts/bin/plugin-scripts.js"))
        it.args.set(args.subList(1, args.size()))
        it.environment.set(env)
      }
    } else {
      // if we not use plugin-scripts for our watch script we fallback to start it with yarn
      frontend = project.tasks.create("boot-frontend", YarnTask) {
        it.args.set(['run', 'watch'])
        it.environment.set(env)
      }
    }

    return {
      frontend.exec()
    }
  }
}
