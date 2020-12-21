package com.cloudogu.smp.doctor.rules

import com.cloudogu.smp.PackageJson
import com.cloudogu.smp.doctor.Context
import com.cloudogu.smp.doctor.Result
import com.cloudogu.smp.doctor.Rule

class MissingPostInstallRule implements Rule {

  @Override
  Result validate(Context context) {
    PackageJson packageJson = context.getPackageJson()
    if (!packageJson.exists()) {
      return Result.ok("no package.json found")
    }

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
