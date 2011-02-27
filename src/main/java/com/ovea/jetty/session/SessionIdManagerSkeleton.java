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

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.session.AbstractSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.log.Log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public abstract class SessionIdManagerSkeleton extends AbstractSessionIdManager {

    // for a session id in the whole jetty, each webapp can have different sessions for the same id
    private final ConcurrentMap<String, Object> sessions = new ConcurrentHashMap<String, Object>();

    private final Server server;

    private long scavengerInterval = 60 * 1000; // 1min
    private ScheduledFuture<?> scavenger;
    private ScheduledExecutorService executorService;

    protected SessionIdManagerSkeleton(Server server) {
        this.server = server;
    }

    public final void setScavengerInterval(long scavengerInterval) {
        this.scavengerInterval = scavengerInterval;
    }

    @Override
    protected final void doStart() throws Exception {
        sessions.clear();
        if (scavenger != null) {
            scavenger.cancel(true);
            scavenger = null;
        }
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
        if (scavengerInterval > 0) {
            executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = Executors.defaultThreadFactory().newThread(r);
                    t.setName("RedisSessionIdManager-ScavengerThread");
                    return t;
                }
            });
            scavenger = executorService.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    if (!sessions.isEmpty()) {
                        try {
                            final List<String> expired = scavenge(new ArrayList<String>(sessions.keySet()));
                            for (String clusterId : expired)
                                sessions.remove(clusterId);
                            forEachSessionManager(new SessionManagerCallback() {
                                @Override
                                public void execute(SessionManagerSkeleton sessionManager) {
                                    sessionManager.expire(expired);
                                }
                            });
                        } catch (Exception e) {
                            Log.warn("Scavenger thread failure: " + e.getMessage(), e);
                        }
                    }
                }
            }, scavengerInterval, scavengerInterval, TimeUnit.MILLISECONDS);
        }
        super.doStart();
    }

    @Override
    protected final void doStop() throws Exception {
        sessions.clear();
        if (scavenger != null) {
            scavenger.cancel(true);
            scavenger = null;
        }
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
        super.doStop();
    }

    @Override
    public final String getClusterId(String nodeId) {
        int dot = nodeId.lastIndexOf('.');
        return dot > 0 ? nodeId.substring(0, dot) : nodeId;
    }

    @Override
    public final String getNodeId(String clusterId, HttpServletRequest request) {
        String worker = request == null ? null : (String) request.getAttribute("org.eclipse.http.ajp.JVMRoute");
        if (worker != null)
            return clusterId + '.' + worker;
        if (_workerName != null)
            return clusterId + '.' + _workerName;
        return clusterId;
    }

    @Override
    public final boolean idInUse(String clusterId) {
        return sessions.containsKey(clusterId) || hasClusterId(clusterId);
    }

    @Override
    public final void addSession(HttpSession session) {
        String clusterId = getClusterId(session.getId());
        storeClusterId(clusterId);
        sessions.putIfAbsent(clusterId, Void.class);
    }

    @Override
    public final void removeSession(HttpSession session) {
        String clusterId = getClusterId(session.getId());
        if (sessions.containsKey(clusterId)) {
            sessions.remove(clusterId);
            deleteClusterId(clusterId);
        }
    }

    @Override
    public final void invalidateAll(final String clusterId) {
        if (sessions.containsKey(clusterId)) {
            sessions.remove(clusterId);
            deleteClusterId(clusterId);
            forEachSessionManager(new SessionManagerCallback() {
                @Override
                public void execute(SessionManagerSkeleton sessionManager) {
                    sessionManager.invalidateSession(clusterId);
                }
            });
        }
    }

    protected abstract void deleteClusterId(String clusterId);

    protected abstract void storeClusterId(String clusterId);

    protected abstract boolean hasClusterId(String clusterId);

    protected abstract List<String> scavenge(List<String> clusterIds);

    private void forEachSessionManager(SessionManagerCallback callback) {
        Handler[] contexts = server.getChildHandlersByClass(ContextHandler.class);
        for (int i = 0; contexts != null && i < contexts.length; i++) {
            SessionHandler sessionHandler = ((ContextHandler) contexts[i]).getChildHandlerByClass(SessionHandler.class);
            if (sessionHandler != null) {
                SessionManager manager = sessionHandler.getSessionManager();
                if (manager != null && manager instanceof SessionManagerSkeleton)
                    callback.execute((SessionManagerSkeleton) manager);
            }
        }
    }

    /**
     * @author Mathieu Carbou (mathieu.carbou@gmail.com)
     */
    private static interface SessionManagerCallback {
        void execute(SessionManagerSkeleton sessionManager);
    }
}
