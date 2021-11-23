package com.cloudogu.smp

import com.moowork.gradle.node.task.NodeTask
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.process.JavaExecSpec

import java.util.stream.Collectors

class RunTask extends DefaultTask {

  @Nested
  SmpExtension extension

  @Internal
  PackageJson packageJson

  @Input
  List<Action<JavaExecSpec>> execSpecActions = []

  @Input
  @Option(option = 'debug-jvm', description = 'Start ScmServer suspended and listening on debug port (default: 5005)')
  boolean debugJvm = false

  @Input
  @Option(option = 'debug-wait', description = 'Wait until a debugger has connected')
  boolean debugWait = false

  @Input
  @Option(option = 'debug-port', description = 'Port for debugger')
  String debugPort = "5005"

  @TaskAction
  void exec() {
    List<Closure<Void>> actions = new ArrayList<>()
    actions.add(createBackend())
    if (packageJson.hasScript("watch")) {
      actions.add(createFrontend())
    }
    def threads = start(actions)
    wait(threads)
  }

  private static void wait(List<Thread> threads) {
    for (Thread thread : threads) {
      thread.join()
    }
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
      project.javaexec { jes ->
        jes.mainClass.set(ScmServer.name)
        jes.args(extension.getServerConfigurationFile(project))
        jes.environment("NODE_ENV", "development")
        jes.classpath(project.buildscript.configurations.classpath)
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
    }
  }

  void execSpec(Action<JavaExecSpec> action) {
    execSpecActions.add(action)
  }

  private Closure<Void> createFrontend() {
    def name = extension.getName(project)
    def home = new File(extension.getScmHome(project), "plugins/${name}/webapp/assets")
    if (!home.exists()) {
      home.mkdirs()
    }
    def frontend = project.tasks.create("boot-frontend", NodeTask) {
      args = ['run', 'watch']
      environment = [
        "BUNDLE_OUTPUT": home.absolutePath,
        "NODE_ENV"     : "development"
      ]
    }
    return {
      frontend.exec()
    }
  }
}
