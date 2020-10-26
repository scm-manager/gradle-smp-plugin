# gradle-smp-plugin

## Notes

* @scm-manager/jest-preset: collect coverage only if `COLLECT_COVERAGE` environment variable is set
* @scm-manager/jest-preset: find module root on pom.xml or build.gradle
* @scm-manager/ui-scripts: execute commands without fork, because they won't exit on `CTRL+C` with gradle
* @scm-manager/ui-scripts: add webpack-plugin-serve for live reloading 
* remove version from package.json
