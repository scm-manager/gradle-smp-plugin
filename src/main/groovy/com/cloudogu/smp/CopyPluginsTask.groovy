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

package com.cloudogu.smp

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.Directory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class CopyPluginsTask extends DefaultTask {

  @Classpath
  final Property<Configuration> configuration = project.objects.property(Configuration)

  @Input
  final Property<File> home = project.objects.property(File)

  @OutputDirectory
  final Provider<Directory> pluginsDirectory = home.map {
    project.layout.projectDirectory.dir("${it.path}/plugins")
  }

  @Internal
  final Provider<Map<String, String>> lookup = configuration.map {
    it.resolvedConfiguration
      .resolvedArtifacts
      .findAll {
        it.extension == "smp"
      }
      .collectEntries { [(it.file.name): it.name + '.smp'] } as Map<String, String>
  }

  @TaskAction
  void execute() {
    File directory = pluginsDirectory.get().asFile
    if (!directory.exists() && !directory.mkdirs()) {
      throw new GradleException("failed to create plugin directory: " + directory);
    }
    def withoutVersion = lookup.get()
    project.copy {
      into(directory)
      from(configuration) {
        include('*.smp')
        rename {
          withoutVersion[it as String]
        }
      }
    }
  }
}
