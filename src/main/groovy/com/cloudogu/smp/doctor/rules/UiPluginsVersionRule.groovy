package com.cloudogu.smp.doctor.rules

import com.cloudogu.smp.PackageJson
import com.cloudogu.smp.doctor.Context
import com.cloudogu.smp.doctor.Result
import com.cloudogu.smp.doctor.Rule

class UiPluginsVersionRule extends PackageJsonRule {
  @Override
  Result validate(Context context, PackageJson packageJson) {
    String extensionVersion = context.getExtension().getScmVersion()
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
