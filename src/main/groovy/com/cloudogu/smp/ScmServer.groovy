package com.cloudogu.smp

import com.google.common.base.Strings
import groovy.json.JsonSlurper
import org.eclipse.jetty.server.HttpConfiguration
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.util.component.AbstractLifeCycle
import org.eclipse.jetty.util.component.LifeCycle
import org.eclipse.jetty.webapp.WebAppContext

import java.awt.Desktop

final class ScmServer {

  private final ScmServerConfiguration configuration
  private Server server

  private ScmServer(ScmServerConfiguration configuration) {
    this.configuration = configuration
  }

  void start() throws Exception {
    info("start scm-server at port %s", configuration.port)

    // gradle has xerces on it classpath, which breaks our annotation processor
    // so we force jdk build in for now
    // @see https://stackoverflow.com/questions/53299280/java-and-xerces-cant-find-property-xmlconstants-access-external-dtd
    System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
      "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl")

    System.setProperty("scm.home", configuration.home)
    if (configuration.disableCorePlugins) {
      info("disable core plugin extraction")
      System.setProperty("sonia.scm.boot.disable-core-plugin-extraction", "true")
    }

    if (!Strings.isNullOrEmpty(configuration.loggingConfiguration)) {
      System.setProperty("logback.configurationFile", configuration.loggingConfiguration);
    }

    info("set stage %s", configuration.stage)
    System.setProperty("scm.stage", configuration.stage)

    // skip first start user creation wizard
    System.setProperty("scm.initialPassword", "scmadmin")

    server = new Server()
    server.addConnector(createServerConnector(server))
    server.setHandler(createScmContext())
    server.addLifeCycleListener(new AbstractLifeCycle.AbstractLifeCycleListener() {
      @Override
      void lifeCycleStarted(LifeCycle event) {

        String endpoint = String.format("http://localhost:%d%s", configuration.port, configuration.contextPath)

        System.out.println()
        System.out.println("==> scm-server started successfully and is accessible at:")
        System.out.append("==> ").println(endpoint)
        System.out.println()

        if (configuration.openBrowser) {
          openBrowser(endpoint)
        }
      }
    })

    server.start()
  }

  private static void openBrowser(String endpoint) {
    try {
      Desktop desktop = Desktop.getDesktop()
      desktop.browse(URI.create(endpoint))
    } catch (IOException | URISyntaxException ex) {
      warn("could not open browser", ex)
    }
  }

  private static void info(String message, Object... args) {
    log("INFO", message, args)
  }

  private static void warn(String message, Exception exception) {
    log("WARN", message)
    exception.printStackTrace(System.out)
  }

  private static void log(String level, String template, Object... args) {
    System.out.println("[${level}] " + String.format(template, args))
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
    String stage = args[1]
    JsonSlurper slurper = new JsonSlurper()
    def json = slurper.parse(new File(configurationPath))
    ScmServerConfiguration configuration = new ScmServerConfiguration(json)
    if (!Strings.isNullOrEmpty(stage)) {
      configuration.setStage(stage)
    }
    ScmServer server = new ScmServer(configuration)
    server.start()
  }

}
