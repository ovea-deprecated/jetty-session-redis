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
package com.ovea.jetty.session.redis;

import com.ovea.jetty.session.Serializer;
import com.ovea.jetty.session.SessionManagerSkeleton;
import com.ovea.jetty.session.serializer.XStreamSerializer;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.TransactionBlock;
import redis.clients.jedis.exceptions.JedisException;

import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class RedisSessionManager extends SessionManagerSkeleton<RedisSessionManager.RedisSession> {

    final static Logger LOG = Log.getLogger("org.eclipse.jetty.server.session");
    private static final String[] FIELDS = {"id", "created", "accessed", "lastNode", "expiryTime", "lastSaved", "lastAccessed", "maxIdle", "cookieSet", "attributes"};

    private final JedisExecutor jedisExecutor;
    private final Serializer serializer;

    private long saveIntervalSec = 20; //only persist changes to session access times every 20 secs

    public RedisSessionManager(JedisPool jedisPool) {
        this(jedisPool, new XStreamSerializer());
    }

    public RedisSessionManager(String jndiName) {
        this(jndiName, new XStreamSerializer());
    }

    public RedisSessionManager(JedisPool jedisPool, Serializer serializer) {
        this.serializer = serializer;
        this.jedisExecutor = new PooledJedisExecutor(jedisPool);
    }

    public RedisSessionManager(final String jndiName, Serializer serializer) {
        this.serializer = serializer;
        this.jedisExecutor = new JedisExecutor() {
            JedisExecutor delegate;

            @Override
            public <V> V execute(JedisCallback<V> cb) {
                if (delegate == null) {
                    try {
                        InitialContext ic = new InitialContext();
                        JedisPool jedisPool = (JedisPool) ic.lookup(jndiName);
                        delegate = new PooledJedisExecutor(jedisPool);
                    } catch (Exception e) {
                        throw new IllegalStateException("Unable to find instance of " + JedisExecutor.class.getName() + " in JNDI location " + jndiName + " : " + e.getMessage(), e);
                    }
                }
                return delegate.execute(cb);
            }
        };
    }

    public void setSaveInterval(long sec) {
        saveIntervalSec = sec;
    }

    @Override
    public void doStart() throws Exception {
        serializer.start();
        super.doStart();
    }

    @Override
    public void doStop() throws Exception {
        super.doStop();
        serializer.stop();
    }

    @Override
    protected RedisSession loadSession(final String clusterId, final RedisSession current) {
        long now = System.currentTimeMillis();
        RedisSession loaded;
        if (current == null) {
            LOG.debug("[RedisSessionManager] loadSession - No session found in cache, loading id={}", clusterId);
            loaded = loadFromStore(clusterId, current);
        } else if (current.requestStarted()) {
            LOG.debug("[RedisSessionManager] loadSession - Existing session found in cache, loading id={}", clusterId);
            loaded = loadFromStore(clusterId, current);
        } else {
            loaded = current;
        }
        if (loaded == null) {
            LOG.debug("[RedisSessionManager] loadSession - No session found in Redis for id={}", clusterId);
            if (current != null)
                current.invalidate();
        } else if (loaded == current) {
            LOG.debug("[RedisSessionManager] loadSession - No change found in Redis for session id={}", clusterId);
            return loaded;
        } else if (!loaded.lastNode.equals(getSessionIdManager().getWorkerName()) || current == null) {
            //if the session in the database has not already expired
            if (loaded.expiryTime > now) {
                //session last used on a different node, or we don't have it in memory
                loaded.changeLastNode(getSessionIdManager().getWorkerName());
            } else {
                LOG.debug("[RedisSessionManager] loadSession - Loaded session has expired, id={}", clusterId);
                loaded = null;
            }
        }
        return loaded;
    }

    private RedisSession loadFromStore(final String clusterId, final RedisSession current) {
        List<String> redisData = jedisExecutor.execute(new JedisCallback<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) {
                final String key = RedisSessionIdManager.REDIS_SESSION_KEY + clusterId;
                if (current == null) {
                    return jedis.exists(key) ? jedis.hmget(key, FIELDS) : null;
                } else {
                    String val = jedis.hget(key, "lastSaved");
                    if (val == null) {
                        // no session in store
                        return Collections.emptyList();
                    }
                    if (current.lastSaved != Long.parseLong(val)) {
                        // session has changed - reload
                        return jedis.hmget(key, FIELDS);
                    } else {
                        // session dit not changed in cache since last save
                        return null;
                    }
                }
            }
        });
        if (redisData == null) {
            // case where session has not been modified
            return current;
        }
        if (redisData.isEmpty() || redisData.get(0) == null) {
            // no session found in redis (no data)
            return null;
        }
        Map<String, String> data = new HashMap<String, String>();
        for (int i = 0; i < FIELDS.length; i++)
            data.put(FIELDS[i], redisData.get(i));
        String attrs = data.get("attributes");
        //noinspection unchecked
        return new RedisSession(data, attrs == null ? new HashMap<String, Object>() : serializer.deserialize(attrs, Map.class));
    }

    @Override
    protected void storeSession(final RedisSession session) {
        if (!session.redisMap.isEmpty()) {
            final Map<String, String> toStore = session.redisMap.containsKey("attributes") ?
                session.redisMap :
                new TreeMap<String, String>(session.redisMap);
            if (toStore.containsKey("attributes"))
                toStore.put("attributes", serializer.serialize(session.getSessionAttributes()));
            LOG.debug("[RedisSessionManager] storeSession - Storing session id={}", session.getClusterId());
            jedisExecutor.execute(new JedisCallback<Object>() {
                @Override
                public Object execute(Jedis jedis) {
                    session.lastSaved = System.currentTimeMillis();
                    toStore.put("lastSaved", "" + session.lastSaved);
                    return jedis.multi(new TransactionBlock() {
                        @Override
                        public void execute() throws JedisException {
                            final String key = RedisSessionIdManager.REDIS_SESSION_KEY + session.getClusterId();
                            super.hmset(key, toStore);
                            super.expireAt(key, session.expiryTime / 1000);
                        }
                    });
                }
            });
            session.redisMap.clear();
        }
    }

    @Override
    protected RedisSession newSession(HttpServletRequest request) {
        return new RedisSession(request);
    }

    @Override
    protected void deleteSession(final RedisSession session) {
        LOG.debug("[RedisSessionManager] deleteSession - Deleting from Redis session id={}", session.getClusterId());
        jedisExecutor.execute(new JedisCallback<Object>() {
            @Override
            public Object execute(Jedis jedis) {
                return jedis.del(RedisSessionIdManager.REDIS_SESSION_KEY + session.getClusterId());
            }
        });
    }

    final class RedisSession extends SessionManagerSkeleton.SessionSkeleton {

        private final Map<String, String> redisMap = new TreeMap<String, String>();

        private long expiryTime;
        private long lastSaved;
        private String lastNode;
        private final ThreadLocal<Boolean> firstAccess = new ThreadLocal<Boolean>() {
            @Override
            protected Boolean initialValue() {
                return true;
            }
        };

        private RedisSession(HttpServletRequest request) {
            super(request);
            lastNode = getSessionIdManager().getWorkerName();
            int maxidle = getMaxInactiveInterval() * 1000;
            expiryTime = maxidle < 0 ? 0 : System.currentTimeMillis() + maxidle;
            // new session so prepare redis map accordingly
            redisMap.put("id", getClusterId());
            redisMap.put("context", getCanonicalizedContext());
            redisMap.put("virtualHost", getVirtualHost());
            redisMap.put("created", "" + getCreationTime());
            redisMap.put("lastNode", lastNode);
            redisMap.put("lastAccessed", "" + getLastAccessedTime());
            redisMap.put("accessed", "" + getAccessed());
            redisMap.put("expiryTime", "" + expiryTime);
            redisMap.put("maxIdle", "" + maxidle);
            redisMap.put("cookieSet", "" + getCookieSetTime());
            redisMap.put("attributes", "");
        }

        RedisSession(Map<String, String> redisData, Map<String, Object> attributes) {
            super(parseLong(redisData.get("created")), parseLong(redisData.get("accessed")), redisData.get("id"));
            lastNode = redisData.get("lastNode");
            expiryTime = parseLong(redisData.get("expiryTime"));
            lastSaved = parseLong(redisData.get("lastSaved"));
            super.setMaxInactiveInterval(parseInt(redisData.get("maxIdle")) / 1000);
            setCookieSetTime(parseLong(redisData.get("cookieSet")));
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                super.doPutOrRemove(entry.getKey(), entry.getValue());
            }
            super.access(parseLong(redisData.get("lastAccessed")));
        }

        public void changeLastNode(String lastNode) {
            this.lastNode = lastNode;
            redisMap.put("lastNode", lastNode);
        }

        @Override
        public void setAttribute(String name, Object value) {
            super.setAttribute(name, value);
            redisMap.put("attributes", "");
        }

        @Override
        public void removeAttribute(String name) {
            super.removeAttribute(name);
            redisMap.put("attributes", "");
        }

        public final Map<String, Object> getSessionAttributes() {
            Map<String, Object> attrs = new LinkedHashMap<String, Object>();
            for (String key : super.getNames()) {
                attrs.put(key, super.doGet(key));
            }
            return attrs;
        }

        @Override
        protected boolean access(long time) {
            boolean ret = super.access(time);
            firstAccess.remove();
            int maxidle = getMaxInactiveInterval() * 1000;
            expiryTime = maxidle < 0 ? 0 : time + maxidle;
            // prepare serialization
            redisMap.put("lastAccessed", "" + getLastAccessedTime());
            redisMap.put("accessed", "" + getAccessed());
            redisMap.put("expiryTime", "" + expiryTime);
            return ret;
        }

        @Override
        public void setMaxInactiveInterval(int secs) {
            super.setMaxInactiveInterval(secs);
            // prepare serialization
            redisMap.put("maxIdle", "" + (secs * 1000));
        }

        @Override
        protected void cookieSet() {
            super.cookieSet();
            // prepare serialization
            redisMap.put("cookieSet", "" + getCookieSetTime());
        }

        @Override
        protected void complete() {
            super.complete();
            if (!redisMap.isEmpty()
                && (redisMap.size() != 3
                || !redisMap.containsKey("lastAccessed")
                || !redisMap.containsKey("accessed")
                || !redisMap.containsKey("expiryTime")
                || getAccessed() - lastSaved >= saveIntervalSec * 1000)) {
                try {
                    willPassivate();
                    storeSession(this);
                    didActivate();
                } catch (Exception e) {
                    LOG.warn("[RedisSessionManager] complete - Problem persisting changed session data id=" + getId(), e);
                } finally {
                    redisMap.clear();
                }
            }
        }

        public boolean requestStarted() {
            boolean first = firstAccess.get();
            if (first)
                firstAccess.set(false);
            return first;
        }
    }
}
