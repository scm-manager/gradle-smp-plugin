package com.cloudogu.smp.doctor.rules

import com.cloudogu.smp.PackageJson
import com.cloudogu.smp.doctor.Context
import com.cloudogu.smp.doctor.Result
import com.cloudogu.smp.doctor.Rule

abstract class PackageJsonRule implements Rule {

  @Override
  Result validate(Context context) {
    PackageJson packageJson = context.getPackageJson()
    if (!packageJson.exists()) {
      return Result.ok('no package.json found')
    }
    return validate(context, packageJson)
  }

  abstract Result validate(Context context, PackageJson packageJson)

}
