package com.cloudogu.smp.doctor


import org.gradle.api.GradleException
import org.junit.jupiter.api.Test

import static org.assertj.core.api.Assertions.assertThat
import static org.junit.jupiter.api.Assertions.assertThrows

class DoctorValidateTaskTest extends DoctorTaskTestBase {

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
  }

  @Test
  void shouldFailOnError() {
    Rules rules = Rules.of(
      rule(Result.error("Everything is bad").build())
    )

    assertThrows(GradleException) {
      execute(rules)
    }
  }

  private String execute(Rules rules) {
    return execute(rules, DoctorValidateTask)
  }

}
