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

import com.cloudogu.smp.doctor.rules.MinScmVersionRule
import com.cloudogu.smp.doctor.rules.MissingPostInstallRule
import com.cloudogu.smp.doctor.rules.NameRule
import com.cloudogu.smp.doctor.rules.UiPluginsVersionRule
import com.cloudogu.smp.doctor.rules.VersionRule

class Rules implements Iterable<Rule> {

  private final List<Rule> rules

  private Rules(List<Rule> rules) {
    this.rules = rules
  }

  static Rules all() {
    return new Rules([
      new NameRule(),
      new VersionRule(),
      new UiPluginsVersionRule(),
      new MissingPostInstallRule(),
      new MinScmVersionRule()
    ])
  }

  static Rules of(Rule... rules) {
    return new Rules(Arrays.asList(rules))
  }

  @Override
  Iterator<Rule> iterator() {
    return rules.iterator()
  }

  Results validate(Context context) {
    List<Result> results = rules.collect {rule ->
      return rule.validate(context)
    }
    return new Results(results);
  }
}
