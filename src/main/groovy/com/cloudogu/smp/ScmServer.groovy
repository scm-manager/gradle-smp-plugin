package com.cloudogu.smp

import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.google.common.collect.Sets
import org.eclipse.jetty.server.*
import org.eclipse.jetty.server.handler.ContextHandlerCollection
import org.eclipse.jetty.util.component.AbstractLifeCycle
import org.eclipse.jetty.util.component.LifeCycle
import org.eclipse.jetty.webapp.WebAppContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path

import static com.google.common.base.Preconditions.checkArgument

final class ScmServer {

    private static final Logger LOG = LoggerFactory.getLogger(ScmServer.class);

    private final Path warFile;
    private final Path scmHome;

    private int port = 8081;

    private String contextPath = "/scm";
    private boolean disableCorePlugins = false;
    private String loggingConfiguration;
    private String stage = "DEVELOPMENT";
    private String stopKey = "stop";
    private int stopPort = 8005;
    private boolean background = false;
    private int headerSize = 16384;

    private Set<ScmServerListener> listeners = Sets.newHashSet();

    private ScmServer(Path warFile, Path scmHome) {
        this.warFile = warFile;
        this.scmHome = scmHome;
    }

    private Server server;

    void start() throws Exception {
        LOG.info("start scm-server at port {}", port);

        System.setProperty("scm.home", scmHome.toString());
        if (disableCorePlugins) {
            LOG.info("disable core plugin extraction");
            System.setProperty("sonia.scm.boot.disable-core-plugin-extraction", "true");
        }

        LOG.info("set stage {}", stage);
        System.setProperty("scm.stage", stage);

        // System.setProperty("livereload.url", "${contextPath}/assets/whm.bundle.js");

        if (!Strings.isNullOrEmpty(loggingConfiguration)) {
            System.setProperty("logback.configurationFile", loggingConfiguration);
        }

        server = new Server();

        URL baseURL = createBaseURL();
        server.addConnector(createServerConnector(server, baseURL));

        ContextHandlerCollection col = new ContextHandlerCollection();
        col.setHandlers([
                createScmContext() //,
                // createLiveReloadContext(server)
        ] as Handler[]);
        server.setHandler(col);

        // startStopMonitor(server);
        // startReadyNotifier(baseURL);
        server.start();

        LOG.info("scm-server is now accessible at http://localhost:{}{}", port, contextPath);
        LOG.info("livereload is available at ws://localhost:{}/livereload", port);

        if (!background) {
            server.join();
        }
    }

    void stop() {
        server.stop();
    }

    /*
    private void startReadyNotifier(URL baseURL) throws MalformedURLException {
        new Thread(new ReadyNotifier(listeners, baseURL, createReadinessURL())).start();
    }

    private void startStopMonitor(Server server) {
        new StopMonitorThread(server, stopPort, stopKey).start();
    }

    private URL createReadinessURL() throws MalformedURLException {
        return new URL("http://localhost:" + port + contextPath + "/api/v2");
    }
    */

    private WebAppContext createScmContext() {
        WebAppContext warContext = new WebAppContext();

        warContext.setContextPath(contextPath);
        warContext.setExtractWAR(true);
        warContext.setWar(warFile.toString());

        return warContext;
    }

    /*private ServletContextHandler createLiveReloadContext(Server server) throws ServletException, DeploymentException {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setServer(server);

        ServerContainer wsc = WebSocketServerContainerInitializer.configureContext(context);
        wsc.addEndpoint(LiveReloadEndPoint.class);

        context.addServlet(LiveReloadScriptServlet.class, "/livereload.js");

        return context;
    }*/

    private ServerConnector createServerConnector(Server server, URL baseURL) throws MalformedURLException {
        ServerConnector connector = new ServerConnector(server);
        HttpConfiguration cfg = new HttpConfiguration();

        cfg.setRequestHeaderSize(headerSize);
        cfg.setResponseHeaderSize(headerSize);

        List<ConnectionFactory> factories = Lists.newArrayList();

        factories.add(new HttpConnectionFactory(cfg));
        connector.setConnectionFactories(factories);

        connector.addLifeCycleListener(new AbstractLifeCycle.AbstractLifeCycleListener() {
            @Override
            void lifeCycleStarted(LifeCycle event) {
                for (ScmServerListener listener : listeners) {
                    LOG.info("call started listener {} with url {}", listener.getClass(), baseURL);
                    try {
                        listener.started(baseURL);
                    } catch (IOException ex) {
                        LOG.warn("listener failed to start: " + listener.getClass(), ex);
                    }
                }
            }

            @Override
            void lifeCycleStopped(LifeCycle event) {
                for (ScmServerListener listener : listeners) {
                    try {
                        listener.stopped(baseURL);
                    } catch (IOException ex) {
                        LOG.warn("listener failed to stop: " + listener.getClass(), ex);
                    }
                }
            }
        });

        connector.setPort(port);

        return connector;
    }

    private URL createBaseURL() throws MalformedURLException {
        return new URL("http://localhost:" + port + contextPath);
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

        ScmServerBuilder withBackground(boolean background) {
            scmServer.background = background;
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

        ScmServerBuilder withStopPort(int port) {
            scmServer.stopPort = port;
            return this;
        }

        ScmServerBuilder withLoggingConfiguration(String loggingConfiguration) {
            scmServer.loggingConfiguration = loggingConfiguration;
            return this;
        }

        ScmServerBuilder withStopKey(String key) {
            scmServer.stopKey = key;
            return this;
        }

        ScmServerBuilder withHeaderSize(int headerSize) {
            checkArgument(headerSize >= 1024, "header buffer must as least 1024");
            checkArgument(headerSize <= 65536, "header buffer must be smaller than 65536");
            scmServer.headerSize = headerSize;
            return this;
        }

        ScmServerBuilder withListener(ScmServerListener scmServerListener) {
            scmServer.listeners.add(scmServerListener);
            return this;
        }

        ScmServer build() {
            return scmServer;
        }
    }

}
