package com.cloudogu.smp.doctor

import org.gradle.api.GradleException

class DoctorValidateTask extends DoctorTask {
  @Override
  void execute(Results results) {
    results.each {result ->
      String message = result.getMessage()
      if (result.isFixable()) {
        message += " [fixable]"
      }
      log(result.getType(), message)
    }

    if (results.hasError()) {
      throw new GradleException("At least one of the doctor rules failed")
    }
  }
}
