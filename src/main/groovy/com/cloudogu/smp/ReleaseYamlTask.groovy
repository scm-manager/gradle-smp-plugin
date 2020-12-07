package com.cloudogu.smp

import groovy.yaml.YamlBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import java.security.MessageDigest

class ReleaseYamlTask extends DefaultTask {

  private SmpExtension extension
  private File releaseYaml
  private File smp

  @Nested
  SmpExtension getExtension() {
    return extension
  }

  void setExtension(SmpExtension extension) {
    this.extension = extension
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

    String name = extension.getName(project)
    String chksum = checksum()
    String downloadUrl = createDownloadUrl(name)

    def pluginDeps = project.configurations.getByName("plugin").dependencies.collect { dep ->
      dep.name
    }

    def optionalPluginDeps = project.configurations.getByName("optionalPlugin").dependencies.collect { dep ->
      dep.name
    }

    def release = new YamlBuilder()
    release {
      plugin name
      tag extension.version
      date new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'")
      url downloadUrl
      checksum chksum
      if (!pluginDeps.isEmpty()) {
        dependencies pluginDeps
      }
      if (!optionalPluginDeps.isEmpty()) {
        optionalDependencies optionalPluginDeps
      }
      conditions {
        minVersion extension.scmVersion
        if (extension.pluginConditions.os != null) {
          os extension.pluginConditions.os
        }
        if (extension.pluginConditions.arch != null) {
          arch extension.pluginConditions.arch
        }
      }
    }

    releaseYaml << release.toString()
  }

  private String createDownloadUrl(String name) {
    String groupIdPath = extension.group.replaceAll("\\.", "/")
    return "https://packages.scm-manager.org/repository/plugin-releases/${groupIdPath}/${name}/${extension.version}/${name}-${extension.version}.smp"
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
