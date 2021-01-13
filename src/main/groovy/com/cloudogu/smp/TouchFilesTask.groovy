package com.cloudogu.smp

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import java.time.Instant

class TouchFilesTask extends DefaultTask {

  @Input
  Object directory

  @Input
  String extension

  @Input
  long timestamp

  TouchFilesTask() {
    timestamp = Instant.now().toEpochMilli()
    // this task should run always
    outputs.upToDateWhen {
      false
    }
  }

  @TaskAction
  public void execute() {
    if (directory instanceof File) {
      touchDirectory(directory)
    } else if (directory instanceof String) {
      touchDirectory(new File((String) directory))
    }
  }

  private void touchDirectory(File file) {
    if (file.exists()) {
      touch(file)
    }
  }

  private void touch(File file) {
    if (file.isDirectory()) {
      for (File child : file.listFiles()) {
        touch(child)
      }
    } else if (file.getName().endsWith(".${extension}")) {
      file.setLastModified(timestamp)
    }
  }

}
