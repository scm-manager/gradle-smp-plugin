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

import com.cloudogu.smp.PackageJson
import com.cloudogu.smp.doctor.Context
import com.cloudogu.smp.doctor.Result

class MissingPostInstallRule extends PackageJsonRule {

  @Override
  Result validate(Context context, PackageJson packageJson) {
    if (context.getExtension().core) {
      return Result.ok("core plugins need no postinstall")
    }

    Closure<Void> fix = {
      packageJson.modify {
        if (it.scripts == null) {
          it.scripts = ["postinstall": "plugin-scripts postinstall"]
        } else {
          it.scripts.postinstall = "plugin-scripts postinstall"
        }
      }
    }

    Optional<String> optionalScript = packageJson.getScript("postinstall")
    if (!optionalScript.isPresent()) {
      return Result.warn("no postinstall script defined").withFix(fix).build()
    }

    String script = optionalScript.get()
    if (!"plugin-scripts postinstall".equals(script)) {
      return Result.warn("plugins should use 'plugin-scripts postinstall' as postinstall script").withFix(fix).build()
    }

    return Result.ok("package.json uses post install of plugin-scripts")
  }

}
