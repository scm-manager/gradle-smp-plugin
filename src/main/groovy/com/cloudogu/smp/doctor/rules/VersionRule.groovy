package com.cloudogu.smp.doctor.rules

import com.cloudogu.smp.doctor.Context
import com.cloudogu.smp.doctor.Result
import com.cloudogu.smp.doctor.Rule

class VersionRule implements Rule {
  @Override
  Result validate(Context context) {
    String smpVersion = context.getProject().version
    String packageJsonVersion = context.getPackageJson().getVersion()
    if (packageJsonVersion == null) {
      return Result.ok("package.json has no version, which is fine")
    }

    if (smpVersion.equals(packageJsonVersion)) {
      return Result.ok("version of gradle.properties and package.json are equal")
    } else {
      return Result.error("version of gradle.properties and package.json are not equal")
        .withFix {
          context.getPackageJson().modify {
            it.version = smpVersion
          }
        }.build()
    }
  }
}
