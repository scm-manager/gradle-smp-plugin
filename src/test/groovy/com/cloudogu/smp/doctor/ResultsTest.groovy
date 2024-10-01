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
