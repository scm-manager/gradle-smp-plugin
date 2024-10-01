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

import com.cloudogu.smp.PackageJson
import com.cloudogu.smp.ScmPropertyHelper
import com.cloudogu.smp.SmpExtension
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Path

class DoctorTaskTestBase {

  protected Project project

  @BeforeEach
  void setUpProject(@TempDir Path directory) {
    project = ProjectBuilder.builder().withProjectDir(directory.toFile()).build()
  }

  protected String execute(Rules rules, def type) {
    ByteArrayOutputStream output = new ByteArrayOutputStream()
    PrintStream stream = new PrintStream(output, true)

    def task = project.task("doctor", type: type) {
      it.extension = new SmpExtension(ScmPropertyHelper.create("2.7.0")) {}
      it.packageJson = new PackageJson(project)
      it.outputStream = stream
      it.rules = rules
      prepareTask(it)
    }
    task.execute()

    return output.toString()
  }

  protected void prepareTask(def task) {

  }

  protected Rule rule(Result result) {
    return new PredefinedResultRule(result)
  }

  private class PredefinedResultRule implements Rule {

    private final Result result

    PredefinedResultRule(Result result) {
      this.result = result
    }

    @Override
    Result validate(Context context) {
      return result
    }
  }

}
