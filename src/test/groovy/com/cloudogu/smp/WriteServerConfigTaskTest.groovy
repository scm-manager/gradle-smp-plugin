package com.cloudogu.smp

import groovy.json.JsonSlurper
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Path

import static org.assertj.core.api.Assertions.*

class WriteServerConfigTaskTest {

  @Test
  @Tag("slow")
  void shouldResolveWebapp(@TempDir Path directory) {
    SmpExtension extension = new SmpExtension()
    extension.scmVersion = "2.0.0"
    extension.home = directory.resolve("home")

    Project project = ProjectBuilder.builder().build()
    project.repositories {
      maven {
        url "https://packages.scm-manager.org/repository/public/"
      }
    }

    def outputFile = directory.resolve("server-config.json").toFile()
    def task = project.task("write-server-config", type: WriteServerConfigTask) {
      it.extension = extension
      it.outputFile = outputFile
    }
    task.write()

    def json = new JsonSlurper().parse(outputFile)
    assertThat(new File(json['warFile'] as String)).exists()
  }

  @Test
  void shouldWriteServerConfig(@TempDir Path directory) {
    SmpExtension extension = new SmpExtension()
    extension.scmVersion = "2.0.0"
    def home = directory.resolve("home").toString()
    extension.serverConfiguration.home = home

    def webapp = directory.resolve("scm-webapp.war")
    def loggingConf = directory.resolve("logging.xml").toString()
    extension.serverConfiguration.loggingConfiguration = loggingConf

    Project project = ProjectBuilder.builder().build()

    def outputFile = directory.resolve("server-config.json").toFile()
    def task = project.task("write-server-config", type: WriteServerConfigTask) {
      it.extension = extension
      it.outputFile = outputFile
      it.webappResolver = {
        return webapp.toFile()
      }
    }
    task.write()

    def json = new JsonSlurper().parse(outputFile)
    assertThat(json["openBrowser"] as boolean).isTrue()
    assertThat(json["port"] as int).isEqualTo(8081)
    assertThat(json["disableCorePlugins"] as boolean).isFalse()
    assertThat(json["home"]).isEqualTo(home)
    assertThat(json["headerSize"] as int).isEqualTo(16384)
    assertThat(json["warFile"] as String).isEqualTo(webapp.toString())
    assertThat(json["loggingConfiguration"] as String).isEqualTo(loggingConf)
    assertThat(json["stage"] as String).isEqualTo("DEVELOPMENT")
    assertThat(json["contextPath"] as String).isEqualTo("/scm")
  }
}
