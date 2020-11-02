package com.cloudogu.smp

import groovy.json.JsonSlurper
import org.eclipse.jetty.server.HttpConfiguration
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.webapp.WebAppContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

final class ScmServer {

  private static final Logger LOG = LoggerFactory.getLogger(ScmServer.class)

  private final ScmServerConfiguration configuration
  private Server server

  private ScmServer(ScmServerConfiguration configuration) {
    this.configuration = configuration
  }

  void start() throws Exception {
    LOG.info("start scm-server at port {}", configuration.port)

    System.setProperty("scm.home", configuration.home)
    if (configuration.disableCorePlugins) {
      LOG.info("disable core plugin extraction")
      System.setProperty("sonia.scm.boot.disable-core-plugin-extraction", "true")
    }

    LOG.info("set stage {}", configuration.stage)
    System.setProperty("scm.stage", configuration.stage)

    server = new Server()
    server.addConnector(createServerConnector(server))
    server.setHandler(createScmContext())

    server.start()
    LOG.info("scm-server is now accessible at http://localhost:{}{}", configuration.port, configuration.contextPath)
  }

  private WebAppContext createScmContext() {
    WebAppContext warContext = new WebAppContext()

    warContext.setContextPath(configuration.contextPath)
    warContext.setExtractWAR(true)
    warContext.setWar(configuration.warFile)

    return warContext
  }

  private ServerConnector createServerConnector(Server server) throws MalformedURLException {
    ServerConnector connector = new ServerConnector(server)
    HttpConfiguration cfg = new HttpConfiguration()

    cfg.setRequestHeaderSize(configuration.headerSize)
    cfg.setResponseHeaderSize(configuration.headerSize)

    connector.setConnectionFactories([new HttpConnectionFactory(cfg)])
    connector.setPort(configuration.port)

    return connector
  }

  static void main(String[] args) {
    String configurationPath = args[0]
    JsonSlurper slurper = new JsonSlurper()
    def json = slurper.parse(new File(configurationPath))
    ScmServerConfiguration configuration = new ScmServerConfiguration(json)
    ScmServer server = new ScmServer(configuration)
    server.start()
  }

}
