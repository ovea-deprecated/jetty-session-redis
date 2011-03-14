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

import org.testatoo.container.Container;
import org.testatoo.container.ContainerConfiguration;
import org.testatoo.container.TestatooContainer;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class JettyServer {

    final Container container;

    public JettyServer(String webappRoot) {
        container = ContainerConfiguration.create()
                .webappRoot(webappRoot)
                .set("jetty.conf", webappRoot + "/WEB-INF/jetty-server.xml")
                .set("jetty.env", webappRoot + "/WEB-INF/jetty-web.xml")
                .buildContainer(TestatooContainer.JETTY);
    }

    public void start() throws Exception {
        if (container != null)
            container.start();
    }

    public void stop() throws Exception {
        if (container != null)
            container.stop();
    }
}
