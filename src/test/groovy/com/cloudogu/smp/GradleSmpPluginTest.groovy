package com.cloudogu.smp

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

import static org.assertj.core.api.Assertions.assertThat
import static org.junit.jupiter.api.Assertions.assertThrows

class GradleSmpPluginTest {

  @Test
  void shouldApplyPlugin() {
    Project project = ProjectBuilder.builder().build()
    project.version = "2.4.0"

    def plugin = new GradleSmpPlugin()
    plugin.apply(project)

    assertThat(project.tasks.size()).isGreaterThan(20)
  }

  @Test
  void shouldFailWithoutVersion() {
    Project project = ProjectBuilder.builder().build()

    def plugin = new GradleSmpPlugin()
    def ex = assertThrows(GradleException.class, {
      plugin.apply(project)
    })
    assertThat(ex.message)
      .contains("version")
      .contains("gradle.properties")
  }

}
