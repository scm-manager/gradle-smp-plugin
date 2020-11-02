package com.cloudogu.smp.doctor.rules

import com.cloudogu.smp.SmpExtension
import com.cloudogu.smp.doctor.Context
import com.cloudogu.smp.doctor.Result
import com.cloudogu.smp.doctor.Rule

class NameRule implements Rule {

  @Override
  Result validate(Context context) {
    SmpExtension extension = context.getExtension()
    String extensionName = extension.getName(context.getProject())
    String packageJsonName = context.getPackageJson().getName()

    int slash = packageJsonName.indexOf('/')
    if (slash > 0) {
      packageJsonName = packageJsonName.substring(slash + 1)
    }

    if (extensionName.equals(packageJsonName)) {
      return Result.ok("gradle and package.json names are equal")
    }

    return Result.error("gradle and package.json names differ")
      .withFix {
        context.getPackageJson().modify {
          it.name = "@scm-manager/${extensionName}"
        }
      }
      .build()
  }
}
