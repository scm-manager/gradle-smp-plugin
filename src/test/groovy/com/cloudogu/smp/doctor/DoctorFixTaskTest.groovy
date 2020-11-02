package com.cloudogu.smp.doctor

import org.junit.jupiter.api.Test
import static org.assertj.core.api.Assertions.assertThat

class DoctorFixTaskTest extends DoctorTaskTestBase {

  @Test
  void shouldPrintNothingWithoutFixableResults() {
    Rules rules = Rules.of(
      rule(Result.ok("Everything is find")),
      rule(Result.warn("Not so fine but acceptable").build()),
    )

    String output = execute(rules)

    assertThat(output).contains("nothing")
  }

  @Test
  void shouldFixRuleResult() {
    FixableRule fixableRule = new FixableRule()
    Rules rules = Rules.of(
      rule(Result.ok("Everything is find")),
      fixableRule
    )

    String output = execute(rules)

    assertThat(output)
      .contains("Lets have a warn")
      .contains("[fixed]")
    assertThat(fixableRule.fixed).isTrue()
  }

  private String execute(Rules rules) {
    return execute(rules, DoctorFixTask)
  }

  private class FixableRule implements Rule {

    private boolean fixed = false

    @Override
    Result validate(Context context) {
      return Result.warn("Lets have a warn")
        .withFix {
          fixed = true
        }
        .build()
    }
  }

}
