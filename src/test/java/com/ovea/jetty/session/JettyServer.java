/**
 * Copyright (C) 2011 Ovea <dev@ovea.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
