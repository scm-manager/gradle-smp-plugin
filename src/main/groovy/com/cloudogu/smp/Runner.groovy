package com.cloudogu.smp

import java.nio.file.Paths

class Runner {

  static void main(String[] args) {
    def webapp = System.getProperty("scm.webapp")
    def home = System.getProperty("scm.home")

    ScmServer server = ScmServer.builder(Paths.get(webapp), Paths.get(home))
      .withContextPath("/scm")
      .withBackground(false)
      .withHeaderSize(16384)
      .withDisableCorePlugins(false)
      .withPort(8081)
      .withStage("DEVELOPMENT")
      .build()

    server.start()
  }
}
