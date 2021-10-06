# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
