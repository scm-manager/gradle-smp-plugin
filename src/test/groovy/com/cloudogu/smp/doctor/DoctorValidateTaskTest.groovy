package com.cloudogu.smp.doctor


import org.gradle.api.GradleException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Path

import static org.assertj.core.api.Assertions.assertThat
import static org.junit.jupiter.api.Assertions.assertThrows

class DoctorValidateTaskTest extends DoctorTaskTestBase {

  private File markerFile

  @BeforeEach
  void prepareProject(@TempDir Path temp) {
    markerFile = temp.resolve("marker").toFile()
  }

  @Test
  void shouldPrintRuleResults() {
    Rules rules = Rules.of(
      rule(Result.ok("Everything is fine")),
      rule(Result.warn("Somethings are not so good, but with a little help").withFix {}.build())
    )

    String output = execute(rules)

    assertThat(output)
      .contains("INFO")
      .contains("Everything is fine")
      .contains("WARN")
      .contains("little help")
      .contains("fixable")
    assertThat(markerFile).exists()
  }

  @Test
  void shouldFailOnError() {
    Rules rules = Rules.of(
      rule(Result.error("Everything is bad").build())
    )

    assertThrows(GradleException) {
      execute(rules)
    }

    assertThat(markerFile).doesNotExist()
  }

  @Override
  void prepareTask(def task) {
    task.outputMarker = markerFile
  }

  private String execute(Rules rules) {
    return execute(rules, DoctorValidateTask)
  }

}
