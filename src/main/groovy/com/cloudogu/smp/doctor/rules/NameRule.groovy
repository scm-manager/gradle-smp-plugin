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

package com.cloudogu.smp.doctor.rules

import com.cloudogu.smp.SmpExtension
import com.cloudogu.smp.PackageJson
import com.cloudogu.smp.doctor.Context
import com.cloudogu.smp.doctor.Result

class NameRule extends PackageJsonRule {

  @Override
  Result validate(Context context, PackageJson packageJson) {
    SmpExtension extension = context.getExtension()
    String extensionName = extension.getName(context.getProject())
    String packageJsonName = packageJson.getName()

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
