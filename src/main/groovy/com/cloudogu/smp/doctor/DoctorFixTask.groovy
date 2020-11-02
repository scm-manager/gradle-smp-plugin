package com.cloudogu.smp.doctor

class DoctorFixTask extends DoctorTask {
  @Override
  void execute(Results results) {
    Results fixable = results.fixable()
    if (fixable.isEmpty()) {
      outputStream.println "[INFO ] nothing to fix"
    } else {
      fixable.each {result ->
        result.fix()
        outputStream.println "[INFO ] ${result.message} [fixed]"
      }
    }
  }
}
