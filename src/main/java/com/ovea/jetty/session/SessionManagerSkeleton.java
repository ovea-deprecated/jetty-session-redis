package com.ovea.jetty.session;

import org.eclipse.jetty.server.session.AbstractSessionManager;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public abstract class SessionManagerSkeleton extends AbstractSessionManager {

    private final ConcurrentMap<String, JettySession> sessions = new ConcurrentHashMap<String, JettySession>();

    @Override
    protected void addSession(AbstractSessionManager.Session session) {
        if (isRunning()) {
            JettySession jettySession = ((JettySession) session);
            String clusterId = getClusterId(session);
            sessions.put(clusterId, jettySession);
            jettySession.willPassivate();
            storeSession(jettySession);
            jettySession.didActivate();
        }
    }

    @Override
    protected AbstractSessionManager.Session newSession(HttpServletRequest request) {
        return new JettySession(request);
    }

    @Override
    @Deprecated
    public Map getSessionMap() {
        throw new UnsupportedOperationException("deprecated");
    }

    @Override
    protected void invalidateSessions() {
        //Do nothing - we don't want to remove and
        //invalidate all the sessions because this
        //method is called from doStop(), and just
        //because this context is stopping does not
        //mean that we should remove the session from
        //any other nodes
    }

    public void invalidateSession(String clusterId) {
        //TODO
    }

    public void expire(List<String> expired) {
        //TODO
    }

    protected abstract void storeSession(JettySession jettySession);

    public class JettySession extends AbstractSessionManager.Session {
        JettySession(HttpServletRequest request) {
            super(request);
        }

        @Override
        public boolean isValid() {
            return super.isValid();
        }

        @Override
        public void willPassivate() {
            super.willPassivate();
        }

        @Override
        public void didActivate() {
            super.didActivate();
        }
    }
}
