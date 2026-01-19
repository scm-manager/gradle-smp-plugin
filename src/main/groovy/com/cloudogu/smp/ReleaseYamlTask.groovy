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

import groovy.yaml.YamlBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import java.security.MessageDigest

class ReleaseYamlTask extends DefaultTask {

  @Nested
  final Property<SmpExtension> extension = project.objects.property(SmpExtension)
  private String pluginName
  private String pluginVersion
  private File releaseYaml
  private File smp

  @Input
  String getPluginVersion() {
    return pluginVersion
  }

  void setPluginVersion(String pluginVersion) {
    this.pluginVersion = pluginVersion
  }

  @Input
  String getPluginName() {
    return pluginName
  }

  void setPluginName(String pluginName) {
    this.pluginName = pluginName
  }

  @InputFile
  File getSmp() {
    return smp
  }

  void setSmp(File smp) {
    this.smp = smp
  }

  @OutputFile
  File getReleaseYaml() {
    return releaseYaml
  }

  void setReleaseYaml(File releaseYaml) {
    this.releaseYaml = releaseYaml
  }

  @TaskAction
  void write() {
    if (releaseYaml.exists() && !releaseYaml.delete()) {
      throw new IllegalStateException("could not delete existing release yaml: ${releaseYaml}")
    }

    String chksum = checksum()
    String downloadUrl = createDownloadUrl()

    def pluginDeps = project.configurations.getByName("plugin").dependencies.collect { dep ->
      dep.name
    }

    def optionalPluginDeps = project.configurations.getByName("optionalPlugin").dependencies.collect { dep ->
      dep.name
    }

    SmpExtension ext = extension.get()
    def scm4Compatible = ext.isScm4Compatible ? ext.isScm4Compatible : Integer.parseInt(ext.scmVersion.get().split("\\.")[0]) >= 4

    def release = new YamlBuilder()
    release {
      plugin pluginName
      tag pluginVersion
      date new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'")
      url downloadUrl
      checksum chksum
      isScm4Compatible scm4Compatible
      if (!pluginDeps.isEmpty()) {
        dependencies pluginDeps
      }
      if (!optionalPluginDeps.isEmpty()) {
        optionalDependencies optionalPluginDeps
      }
      conditions {
        minVersion ext.getScmVersion().get()
        if (ext.pluginConditions.os != null) {
          os ext.pluginConditions.os
        }
        if (ext.pluginConditions.arch != null) {
          arch ext.pluginConditions.arch
        }
      }
    }

    releaseYaml << release.toString()
  }

  private String createDownloadUrl() {
    String groupIdPath = extension.get().group.replaceAll("\\.", "/")
    return "https://packages.scm-manager.org/repository/plugin-releases/${groupIdPath}/${pluginName}/${pluginVersion}/${pluginName}-${pluginVersion}.smp"
  }

  private String checksum() {
    def digest = MessageDigest.getInstance("SHA-256")

    def buffer = new byte[16384]
    def len

    def inputStream = smp.newInputStream()
    while ((len = inputStream.read(buffer)) > 0) {
      digest.update(buffer, 0, len)
    }
    inputStream.close()

    def shasum = digest.digest()
    def result = ""
    for (byte b : shasum) {
      result += toHex(b)
    }
    return result
  }

  private static String hexChr(int b) {
    return Integer.toHexString(b & 0xF)
  }

  private static String toHex(int b) {
    return hexChr((b & 0xF0) >> 4) + hexChr(b & 0x0F)
  }

}
