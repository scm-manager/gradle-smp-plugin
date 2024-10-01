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

package com.cloudogu.smp.doctor

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Path

import static org.assertj.core.api.Assertions.assertThat
import static org.junit.jupiter.api.Assertions.assertThrows

class ValidatePluginJsonTaskTest {

  private Project project
  private File localeDirectory
  private File markerFile

  @BeforeEach
  void prepareProject(@TempDir Path temp) {
    project = ProjectBuilder.builder().build()
    localeDirectory = temp.resolve("locales").toFile()
    markerFile = temp.resolve("marker").toFile()
  }

  @Test
  void shouldFailWithCorruptJson() throws IOException {
    writeJsonForLanguage("en", "{\"valid\": true}")
    writeJsonForLanguage("de", "{invalid: true}")

    assertFail(task(localeDirectory))
  }

  @Test
  void shouldSucceedWithValidJson() throws IOException {
    writeJsonForLanguage("en", "{\"valid\": true}")
    writeJsonForLanguage("de", "{\"valid\": \"too\"}")

    assertSuccess(task(localeDirectory))
  }

  @Test
  void shouldHandleMissingLocalesDirectory() {
    assertSuccess(task(null))
  }

  @Test
  void shouldHandleMissingLanguageDirectory() throws IOException {
    localeDirectory.mkdirs()

    assertSuccess(task(localeDirectory))
  }

  @Test
  void shouldNotFailIfExecutedTwice() throws IOException {
    localeDirectory.mkdirs()

    def task = task(localeDirectory)
    assertSuccess(task)
    assertSuccess(task)
  }

  void assertSuccess(ValidatePluginsJsonTask t) {
    t.execute()
    assertThat(markerFile).exists()
  }

  private void assertFail(ValidatePluginsJsonTask t) {
    assertThrows(GradleException, {
      t.execute()
    })
    assertThat(markerFile).doesNotExist()
  }

  private ValidatePluginsJsonTask task(File directory) {
    return project.task("validatePluginJson", type: ValidatePluginsJsonTask) {
      it.localeDirectory = directory
      it.outputMarker = markerFile
    }
  }

  private void writeJsonForLanguage(String language, String json) throws IOException {
    File directory = new File(localeDirectory, language)
    directory.mkdirs()

    File file = new File(directory, "plugins.json")
    file << json
  }
}
