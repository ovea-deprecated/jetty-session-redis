package com.ovea.jetty.session;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.session.AbstractSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public abstract class SessionIdManagerSkeleton extends AbstractSessionIdManager {

    // for a session id in the whole jetty, each webapp can have different sessions for the same id
    private final ConcurrentMap<String, Object> sessions = new ConcurrentHashMap<String, Object>();
    private final Server server;

    protected SessionIdManagerSkeleton(Server server) {
        this.server = server;
    }

    @Override
    protected void doStart() throws Exception {
        sessions.clear();
        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        sessions.clear();
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
        String clusterId = ((SessionManagerSkeleton.Session) session).getClusterId();
        storeClusterId(clusterId);
        sessions.putIfAbsent(clusterId, Void.class);
    }

    @Override
    public final void removeSession(HttpSession session) {
        String clusterId = ((SessionManagerSkeleton.Session) session).getClusterId();
        sessions.remove(clusterId);
        deleteClusterId(clusterId);
    }

    @Override
    public final void invalidateAll(String clusterId) {
        sessions.remove(clusterId);
        deleteClusterId(clusterId);
        Handler[] contexts = server.getChildHandlersByClass(ContextHandler.class);
        for (int i = 0; contexts != null && i < contexts.length; i++) {
            SessionHandler sessionHandler = ((ContextHandler) contexts[i]).getChildHandlerByClass(SessionHandler.class);
            if (sessionHandler != null) {
                SessionManager manager = sessionHandler.getSessionManager();
                if (manager != null && manager instanceof SessionManagerSkeleton)
                    ((SessionManagerSkeleton) manager).invalidateSession(clusterId);
            }
        }
    }

    protected abstract void deleteClusterId(String clusterId);

    protected abstract void storeClusterId(String clusterId);

    protected abstract boolean hasClusterId(String clusterId);

}
