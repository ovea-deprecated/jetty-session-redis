package com.ovea.jetty.session.redis;

import com.ovea.jetty.session.SessionManagerSkeleton;
import org.eclipse.jetty.server.session.AbstractSessionManager;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class RedisSessionManager extends SessionManagerSkeleton {

    private Serializer serializer;

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public AbstractSessionManager.Session getSession(String idInCluster) {
        return null;
    }

    @Override
    protected boolean removeSession(String idInCluster) {
        return false;
    }

    @Override
    protected void storeSession(JettySession jettySession) {
        //TODO
    }
}
