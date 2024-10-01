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

class UiPluginsVersionRule extends PackageJsonRule {
  @Override
  Result validate(Context context, PackageJson packageJson) {
    String extensionVersion = context.getExtension().getScmVersion().get()
    String uiPluginsVersion = packageJson.getDependencyVersion("@scm-manager/ui-plugins")
      if (uiPluginsVersion != null) {
        if (extensionVersion.equals(uiPluginsVersion)) {
          return Result.ok("scm version is equal with @scm-manager/ui-plugins")
        } else if (extensionVersion.endsWith("SNAPSHOT")) {
          return Result.ok("gradle uses scm snapshot, no need to match @scm-manager/ui-plugins")
        } else {
          return Result.warn("@scm-manager/ui-plugins is not equal with scm version")
            .withFix {
              context.packageJson.modify {
                if (it.dependencies == null) {
                  it.dependencies = ["@scm-manager/ui-plugins": extensionVersion]
                } else {
                  it.dependencies["@scm-manager/ui-plugins"] = extensionVersion
                }
              }
            }
            .build()
        }
      } else {
        return Result.ok("plugin has no dependency to @scm-manager/ui-plugins")
      }
  }
}
