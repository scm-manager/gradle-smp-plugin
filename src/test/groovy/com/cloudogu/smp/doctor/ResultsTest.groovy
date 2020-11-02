package com.cloudogu.smp.doctor

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

import static org.assertj.core.api.Assertions.assertThat

class ResultsTest {

  @Nested
  class HasErrorsTests {

    @Test
    void shouldReturnTrueIfTheResultHasErrors() {
      Results results = Results.of(Result.ok("ok_one"), Result.error("danger").build(), Result.ok("ok_two"))
      assertThat(results.hasError()).isTrue()
    }

    @Test
    void shouldReturnFalseIfTheResultHasNoErrors() {
      Results results = Results.of(Result.ok("ok_one"), Result.warn("warning").build(), Result.ok("ok_two"))
      assertThat(results.hasError()).isFalse()
    }

  }

  @Nested
  class FixableTests {

    @Test
    void shouldReturnOnlyFixableResults() {
      Results results = Results.of(Result.ok("ok_one"), Result.warn("warning").withFix {}.build(), Result.ok("ok_two"))
      Results fixables = results.fixable()
      assertThat(fixables.isEmpty()).isFalse()
      assertThat(fixables).hasSize(1)
    }

    @Test
    void shouldBeEmptyWithoutFixableResults() {
      Results results = Results.of(Result.ok("ok_one"), Result.ok("ok_two"))
      Results fixables = results.fixable()
      assertThat(fixables.isEmpty()).isTrue()
    }

  }

}
