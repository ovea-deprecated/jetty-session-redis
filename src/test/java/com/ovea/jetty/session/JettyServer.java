package com.ovea.jetty.session;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;

import java.io.File;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class JettyServer {

    final Server server;

    public JettyServer(String webappRoot) {
        try {
            server = new Server();

            XmlConfiguration configuration = new XmlConfiguration(new File(webappRoot + "/WEB-INF/jetty-server.xml").toURI().toURL());
            configuration.configure(server);

            HandlerCollection contexts = server.getChildHandlerByClass(ContextHandlerCollection.class);
            WebAppContext webapp = new WebAppContext(webappRoot, webappRoot);
            contexts.addHandler(webapp);

            configuration = new XmlConfiguration(new File(webappRoot + "/WEB-INF/jetty-web.xml").toURI().toURL());
            configuration.configure(webapp);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void start() throws Exception {
        server.start();
    }

    public void stop() throws Exception {
        if (server != null)
            server.stop();
    }
}
