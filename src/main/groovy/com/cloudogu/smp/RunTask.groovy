package com.cloudogu.smp

import com.moowork.gradle.node.yarn.YarnTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction

import java.util.stream.Collectors

class RunTask extends DefaultTask {

  @Nested
  SmpExtension extension

  @Nested
  PackageJson packageJson

  @TaskAction
  void exec() {
    List<Closure<Void>> actions = new ArrayList<>();
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
    def webapp = resolveWebApp()
    def backend = project.tasks.create("boot-backend", JavaExec) {
      main ScmServer.class.name
      environment "NODE_ENV", "development"
      systemProperty "scm.webapp", webapp.toString()
      systemProperty "scm.home", extension.getScmHome(project)
      classpath project.buildscript.configurations.classpath
    }
    return {
      backend.exec()
    }
  }

  private Closure<Void> createFrontend() {
    def name = extension.getName(project)
    def home = new File(extension.getScmHome(project), "plugins/${name}/webapp/assets")
    if (!home.exists()) {
      home.mkdirs()
    }
    def frontend = project.tasks.create("boot-frontend", YarnTask) {
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

  private File resolveWebApp() {
    def extension = project.extensions.getByType(SmpExtension)
    def coordinates = "sonia.scm:scm-webapp:${extension.scmVersion}@war"
    def dependency = project.dependencies.create(coordinates)
    def configuration = project.configurations.detachedConfiguration(dependency)
    configuration.resolve()
    return configuration.files.first()
  }
}
