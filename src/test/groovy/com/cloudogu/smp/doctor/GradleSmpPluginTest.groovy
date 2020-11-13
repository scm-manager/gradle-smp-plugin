package com.cloudogu.smp.doctor

import com.cloudogu.smp.GradleSmpPlugin
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

import static org.assertj.core.api.Assertions.*

class GradleSmpPluginTest {

  @Test
  void shouldApplyPlugin() {
    Project project = ProjectBuilder.builder().build()

    def plugin = new GradleSmpPlugin()
    plugin.apply(project)

    assertThat(project.tasks.size()).isGreaterThan(20)
  }

}
