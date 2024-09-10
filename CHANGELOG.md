# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased
### Fixed
- Support src/main/conf/logging.xml

## 0.17.1 - 2024-09-10
### Fixed
- Support Dockerfile, docker-compose.yml files and src/test/e2e path
- Format properties as ISO 8859-1

## 0.17.0 - 2024-09-09
### Added
- Instead of a LICENSE.txt file one can use a LICENSE-HEADER.txt file for a shortened license

## 0.16.4 - 2024-04-22
### Changed
- Update nodejs to 21.6.2

## 0.16.3 - 2024-01-29
### Fixed
- SCM-Home Switch for Gradle `Run` Task

## 0.16.2 - 2024-01-15
### Fixed
- Resolve `scmVersion` for plugin xml by actual core dependency

## 0.16.1 - 2023-11-29
### Fixed
- Support for SCM-Manager v3 (OpenAPI and configs)

## 0.16.0 - 2023-10-12
### Added
- Support for SCM-Manager v3

## 0.15.0 - 2023-01-21
### Changed
- Upgrade changelog plugin to 0.2.0

## 0.14.0

## 0.13.0
### Changed
- Add jackson dependency for core versions greater than 2.39.2 ([#18](https://github.com/scm-manager/gradle-smp-plugin/pull/18))

## 0.12.0
### Added
- Add property to use ChildFirstClassloader in plugins ([#17](https://github.com/scm-manager/gradle-smp-plugin/pull/17))

## 0.11.1 - 2022-05-19
### fixed
- Resolve property before usage for dependencies

## 0.11.0 - 2022-05-16
### Changed
- Set target compilation level to Java 11 if core version is higher than 2.35.x

## 0.10.4 - 2022-05-09
### Fixed
- Keep plugins on rerun ([#14](https://github.com/scm-manager/gradle-smp-plugin/pull/14))
- Upgrade changelog plugin to 0.1.7

## 0.10.3 - 2021-12-22
### Fixed
- Set line ending used for license files to LF ([#12](https://github.com/scm-manager/gradle-smp-plugin/pull/12))

## 0.10.2 - 2021-12-21
### Fixed
- Stop plugin run on Windows ([#11](https://github.com/scm-manager/gradle-smp-plugin/pull/11))

### Changed
- Upgrade changelog plugin to 0.1.6

## 0.10.1 - 2021-11-29
### Fixed
- Broken publishing

### Changed
- Downgrade Gradle to 6.7.1 to be compatible with Gradle 6.7 and 6.8

## 0.10.0 - 2021-11-25
### Added
- Support for gradle 7

### Changed
- Use org.scm-manager.license for license checks
- Update gradle to 7.3

## 0.9.4 - 2021-11-10
### Changed
- Update nodejs to 16.13.0
- Update yarn to 1.22.15

## 0.9.3 - 2021-10-19
### Fixed
- Fix JAX-RS Tie dependency

## 0.9.1 - 2021-10-19
### Changed
- Bump JAX-RS Tie to version 1.0.5

## 0.9.0 - 2021-10-13
### Added
- [Conveyor](https://github.com/cloudogu/conveyor) as dependency for all scm plugin
- [JAX-RS Tie](https://github.com/cloudogu/jaxrs-tie) as dependency for all scm plugin
- Gradle Changelog plugin to support changelog flow in all scm plugins

## 0.8.5 - 2021-07-29
### Changed
- Remove core dependencies from final smp package
- Skip first start user creation wizard

### Fixed
- Fix xml parsing errors on run

## 0.8.4 - 2021-07-29
### Fixed
- Fix missing extensions in plugin.xml

## 0.8.3 - 2021-07-06
### Fixed
- Fix rewrite of server configuration on snapshots ([#6](https://github.com/scm-manager/gradle-smp-plugin/pull/6))
- Fix wrong checksum on release ([#7](https://github.com/scm-manager/gradle-smp-plugin/pull/7))

## 0.8.2 - 2021-05-06
### Fixed
- Fix sonarqube analysis for js-only projects

## 0.8.1 - 2021-05-06
### Changed
- Skip parsing and appending moduleXml content if not available ([#5](https://github.com/scm-manager/gradle-smp-plugin/pull/5))

## 0.8.0 - 2021-03-24
### Added
- Add smp variant to published gradle module ([#4](https://github.com/scm-manager/gradle-smp-plugin/pull/4))

### Changed
- Automatic refresh for snapshot dependencies ([#1](https://github.com/scm-manager/gradle-smp-plugin/pull/1))
- Force UTF-8 encoding for all java compilation tasks ([#3](https://github.com/scm-manager/gradle-smp-plugin/pull/3))

## 0.7.5 - 2021-03-08
### Changed
- Write description field ([#2](https://github.com/scm-manager/gradle-smp-plugin/pull/2))

## 0.7.4 - 2021-03-01
### Changed
- Use special sonar environment for projects without frontend

## 0.7.3 - 2021-03-01
### Changed
- Reduce dependencies for openapi spec generation

### Fixed
- Fix run task for plugin without frontend

## 0.7.2 - 2021-01-13
### Changed
- Specify directory as Input and not as InputDirectory

### Fixed
- Allow passing directory as string or file

## 0.7.1 - 2021-01-13
### Changed
- Update timestamps of test reports on ci

## 0.7.0 - 2021-01-13
### Added
- Implemented maven publishing

### Changed
- Do not register version tasks for core plugins

### Fixed
- Fix wrong location of openapi specs
- Compile test classes to java 8 to fix enforcer
- Disable run task for core plugins, to avoid conflicts

## 0.6.0 - 2021-01-06
### Added
- Support for core plugins 
- Define artifact for smp
- Add missing avatar url configuration
- Allow specification of extra sonarqube properties
- Add debugging options
- Add local maven repository to use scm-manager snapshots

### Changed
- Update gradle to 6.7.1
- Compile with Java 11 but force Java 8 as target

### Fixed
- Fix dependency resolution for core plugins
- Fix doctor tasks for core plugins
- Do not fail on javadoc errors
- Fix caching of typecheck
- Do not fail if marker already exists
- Do not fail if package.json does not exists
- Fix doctor tasks for plugins without package.json
- Fix issue with overwritten war file in server configuration

## 0.5.1 - 2020-12-16
### Added
- Set project key for sonar to group:name

## 0.5.0 - 2020-12-16
### Added
- Add sonarqube parameters for ui code analysis

## 0.4.1 - 2020-12-16
### Added
- Configure SonarQube to find ui coverage report

## 0.4.0 - 2020-12-16
### Added
- Set java compatibility of compiled groovy classes to 8
- Collect code coverage on ci server

### Changed
- Replace manuel set able property with auto detection

## 0.3.1 - 2020-12-16
### Fixed
- Fix wrong usage of extra properties

## 0.3.0 - 2020-12-16
### Added
- Add property scm.home
- Set java compatibility level to 8
- Add property ignoreTestFailures

### Changed
- Decouple java tests from ui tests

## 0.2.2 - 2020-12-15
### Added
- Add documentation for installation and configuration

### Fixed
- Fix wrong directory for jest-reports

## 0.2.1 - 2020-12-14
### Added
- Add maven publishing plugin for testing with local maven repository

### Changed
- Update jetty to v9.4.35.v20201120

### Fixed
- Fix wrong error message, use gradle.properties instead of pom.xml
- Remove gradle api dependencies from ScmServer to fix run task

## 0.2.0 - 2020-12-11
### Added
- Download node and yarn as part of the build
- Add tasks to set version and to set the version to next snapshot
- Validate locale json files
- Add MissingPostInstallRule
- Add rule to validate min scmVersion of dependencies
- Add sonarqube plugin
- Configure publishing for packages.scm-manager.org

### Changed
- Define dependency versions to support older core versions
- Set node version to 14.15.1 and yarn to 1.22.5
- Move plugin dependency declaration from extension to gradle dependencies
- Set Java compatibility to version 1.8
- Use version from gradle.properties instead of extension
- Keep comments of gradle.properties

### Fixed
- Do not fail if username or password not set for publishing
