package com.cloudogu.smp.doctor

import com.cloudogu.smp.PackageJson
import com.cloudogu.smp.SmpExtension
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Path

class DoctorTaskTestBase {

  protected Project project

  @BeforeEach
  void setUpProject(@TempDir Path directory) {
    project = ProjectBuilder.builder().withProjectDir(directory.toFile()).build()
  }

  protected String execute(Rules rules, def type) {
    ByteArrayOutputStream output = new ByteArrayOutputStream()
    PrintStream stream = new PrintStream(output, true)

    def task = project.task("doctor", type: type) {
      it.extension = new SmpExtension()
      it.packageJson = new PackageJson(project)
      it.outputStream = stream
      it.rules = rules
      prepareTask(it)
    }
    task.execute()

    return output.toString()
  }

  protected void prepareTask(def task) {

  }

  protected Rule rule(Result result) {
    return new PredefinedResultRule(result)
  }

  private class PredefinedResultRule implements Rule {

    private final Result result

    PredefinedResultRule(Result result) {
      this.result = result
    }

    @Override
    Result validate(Context context) {
      return result
    }
  }

}
