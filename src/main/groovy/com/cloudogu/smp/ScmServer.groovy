package com.cloudogu.smp

import com.google.common.collect.Lists
import org.eclipse.jetty.server.*
import org.eclipse.jetty.server.handler.ContextHandlerCollection
import org.eclipse.jetty.webapp.WebAppContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path

import static com.google.common.base.Preconditions.checkArgument

final class ScmServer {

  private static final Logger LOG = LoggerFactory.getLogger(ScmServer.class)

  private final Path warFile
  private final Path scmHome

  private int port = 8081

  private String contextPath = "/scm"
  private boolean disableCorePlugins = false
  private String stage = "DEVELOPMENT"
  private int headerSize = 16384

  private Server server

    private ScmServer(Path warFile, Path scmHome) {
        this.warFile = warFile
        this.scmHome = scmHome
    }

    void start() throws Exception {
        LOG.info("start scm-server at port {}", port)

        System.setProperty("scm.home", scmHome.toString())
        if (disableCorePlugins) {
            LOG.info("disable core plugin extraction")
            System.setProperty("sonia.scm.boot.disable-core-plugin-extraction", "true")
        }

        LOG.info("set stage {}", stage)
        System.setProperty("scm.stage", stage)

        server = new Server()
        server.addConnector(createServerConnector(server))

        ContextHandlerCollection col = new ContextHandlerCollection()
        col.setHandlers([
            createScmContext()
        ] as Handler[])
        server.setHandler(col)

        server.start()
        LOG.info("scm-server is now accessible at http://localhost:{}{}", port, contextPath);
    }

    private WebAppContext createScmContext() {
        WebAppContext warContext = new WebAppContext()

        warContext.setContextPath(contextPath)
        warContext.setExtractWAR(true)
        warContext.setWar(warFile.toString())

        return warContext
    }

    private ServerConnector createServerConnector(Server server) throws MalformedURLException {
        ServerConnector connector = new ServerConnector(server)
        HttpConfiguration cfg = new HttpConfiguration()

        cfg.setRequestHeaderSize(headerSize)
        cfg.setResponseHeaderSize(headerSize)

        connector.setConnectionFactories([new HttpConnectionFactory(cfg)])
        connector.setPort(port)

        return connector
    }

    static ScmServerBuilder builder(Path warFile, Path scmHome) {
        return new ScmServerBuilder(warFile, scmHome);
    }

    static class ScmServerBuilder {

        private final ScmServer scmServer;

        ScmServerBuilder(Path warFile, Path scmHome) {
            this.scmServer = new ScmServer(warFile, scmHome);
        }

        ScmServerBuilder withDisableCorePlugins(boolean disableCorePlugins) {
            scmServer.disableCorePlugins = disableCorePlugins;
            return this;
        }

        ScmServerBuilder withContextPath(String contextPath) {
            scmServer.contextPath = contextPath;
            return this;
        }

        ScmServerBuilder withStage(String stage) {
            scmServer.stage = stage;
            return this;
        }

        ScmServerBuilder withPort(int port) {
            scmServer.port = port;
            return this;
        }

        ScmServerBuilder withHeaderSize(int headerSize) {
            checkArgument(headerSize >= 1024, "header buffer must as least 1024");
            checkArgument(headerSize <= 65536, "header buffer must be smaller than 65536");
            scmServer.headerSize = headerSize;
            return this;
        }

        ScmServer build() {
            return scmServer;
        }
    }

}
