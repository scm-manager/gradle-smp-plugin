package com.cloudogu.smp.doctor

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
      new UiPluginsVersionRule()
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
