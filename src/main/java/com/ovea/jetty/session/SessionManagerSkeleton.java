package com.ovea.jetty.session;

import org.eclipse.jetty.server.session.AbstractSessionManager;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public abstract class SessionManagerSkeleton extends AbstractSessionManager {
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

    public class Session extends AbstractSessionManager.Session {
        Session(HttpServletRequest request) {
            super(request);
        }

        @Override
        public boolean isValid() {
            return super.isValid();
        }

        @Override
        public String getClusterId() {
            return super.getClusterId();
        }
    }
}
