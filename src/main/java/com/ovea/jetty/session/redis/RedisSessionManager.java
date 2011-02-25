package com.ovea.jetty.session.redis;

import com.ovea.jetty.session.SessionManagerSkeleton;
import org.eclipse.jetty.server.session.AbstractSessionManager;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class RedisSessionManager extends SessionManagerSkeleton {

    @Override
    protected void addSession(AbstractSessionManager.Session session) {
    }

    @Override
    public AbstractSessionManager.Session getSession(String idInCluster) {
        return null;
    }

    @Override
    protected AbstractSessionManager.Session newSession(HttpServletRequest request) {
        return null;
    }

    @Override
    protected boolean removeSession(String idInCluster) {
        return false;
    }

}
