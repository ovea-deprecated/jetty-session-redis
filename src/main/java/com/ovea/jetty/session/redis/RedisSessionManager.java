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
import com.ovea.jetty.session.serializer.JsonSerializer;
import org.eclipse.jetty.util.log.Log;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.TransactionBlock;
import redis.clients.jedis.exceptions.JedisException;

import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.Long.parseLong;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class RedisSessionManager extends SessionManagerSkeleton<RedisSessionManager.RedisSession> {

    private static final String[] FIELDS = {"id", "created", "accessed", "lastNode", "expiryTime", "lastSaved", "lastAccessed", "maxIdle", "cookieSet", "attributes"};

    private final JedisExecutor jedisExecutor;
    private final Serializer serializer;

    private long saveIntervalSec = 60; //only persist changes to session access times every 60 secs

    public RedisSessionManager(JedisPool jedisPool) {
        this(jedisPool, new JsonSerializer());
    }

    public RedisSessionManager(String jndiName) {
        this(jndiName, new JsonSerializer());
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

    public long getSaveInterval() {
        return saveIntervalSec;

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
            Log.debug("No session ", clusterId);
            loaded = loadSession(clusterId);
        } else if (now - current.lastSaved >= saveIntervalSec * 1000) {
            Log.debug("Old session", clusterId);
            loaded = loadSession(clusterId);
        } else {
            loaded = current;
        }
        if (loaded == null) {
            Log.debug("No session in database matching id={}", clusterId);
        } else if (!loaded.lastNode.equals(getIdManager().getWorkerName()) || current == null) {
            //if the session in the database has not already expired
            if (loaded.expiryTime > now) {
                //session last used on a different node, or we don't have it in memory
                loaded.changeLastNode(getIdManager().getWorkerName());
            } else {
                Log.debug("expired session", clusterId);
                loaded = null;
            }
        }
        return loaded;
    }

    private RedisSession loadSession(final String clusterId) {
        Log.debug("Load distributed session with id {}", clusterId);
        List<String> redisData = jedisExecutor.execute(new JedisCallback<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) {
                return jedis.hmget(RedisSessionIdManager.REDIS_SESSION_KEY + clusterId, FIELDS);
            }
        });
        Map<String, String> data = new HashMap<String, String>();
        for (int i = 0; i < FIELDS.length; i++)
            data.put(FIELDS[i], redisData.get(i));
        String attrs = data.get("attributes");
        //noinspection unchecked
        return data.get("id") == null ? null : new RedisSession(data, attrs == null ? new HashMap<String, Object>() : serializer.deserialize(attrs, Map.class));
    }

    @Override
    protected void storeSession(final RedisSession session) {
        final Map<String, String> toStore = session.redisMap.containsKey("attributes") ?
                session.redisMap :
                new TreeMap<String, String>(session.redisMap);
        if (toStore.containsKey("attributes"))
            toStore.put("attributes", serializer.serialize(session.getAttributes()));
        jedisExecutor.execute(new JedisCallback<Object>() {
            @Override
            public Object execute(Jedis jedis) {
                return jedis.multi(new TransactionBlock() {
                    @Override
                    public void execute() throws JedisException {
                        super.hmset(RedisSessionIdManager.REDIS_SESSION_KEY + session.getClusterId(), toStore);
                        super.expireAt(RedisSessionIdManager.REDIS_SESSION_KEY + session.getClusterId(), session.expiryTime / 1000);
                    }
                });
            }
        });
        session.saved();
    }

    @Override
    protected RedisSession newSession(HttpServletRequest request) {
        return new RedisSession(request);
    }

    @Override
    protected void deleteSession(final RedisSession session) {
        jedisExecutor.execute(new JedisCallback<Object>() {
            @Override
            public Object execute(Jedis jedis) {
                return jedis.del(session.getClusterId());
            }
        });
    }

    final class RedisSession extends SessionManagerSkeleton.SessionSkeleton {

        private static final long serialVersionUID = -1232252425814928262L;

        private final Map<String, String> redisMap = new TreeMap<String, String>();

        private long expiryTime;
        private long lastSaved;
        private String lastNode;

        private RedisSession(HttpServletRequest request) {
            super(request);
            lastNode = getIdManager().getWorkerName();
            expiryTime = _maxIdleMs < 0 ? 0 : System.currentTimeMillis() + _maxIdleMs;
            // new session so prepare redis map accordingly
            redisMap.put("id", _clusterId);
            redisMap.put("context", getCanonicalizedContext());
            redisMap.put("virtualHost", getVirtualHost());
            redisMap.put("created", "" + _created);
            redisMap.put("lastNode", lastNode);
            redisMap.put("lastAccessed", "" + _lastAccessed);
            redisMap.put("accessed", "" + _accessed);
            redisMap.put("expiryTime", "" + expiryTime);
            redisMap.put("maxIdle", "" + _maxIdleMs);
            redisMap.put("cookieSet", "" + _cookieSet);
            redisMap.put("lastSaved", "" + lastSaved);
            redisMap.put("attributes", "");
        }

        RedisSession(Map<String, String> redisData, Map<String, Object> attributes) {
            super(parseLong(redisData.get("created")), parseLong(redisData.get("accessed")), redisData.get("id"));
            lastNode = redisData.get("lastNode");
            expiryTime = parseLong(redisData.get("expiryTime"));
            lastSaved = parseLong(redisData.get("lastSaved"));
            _lastAccessed = parseLong(redisData.get("lastAccessed"));
            _maxIdleMs = parseLong(redisData.get("maxIdle"));
            _cookieSet = parseLong(redisData.get("cookieSet"));
            _attributes.putAll(attributes);
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

        public Map<String, Object> getAttributes() {
            return _attributes;
        }

        @Override
        protected void access(long time) {
            super.access(time);
            expiryTime = _maxIdleMs < 0 ? 0 : time + _maxIdleMs;
            // prepare serialization
            redisMap.put("lastAccessed", "" + _lastAccessed);
            redisMap.put("accessed", "" + _accessed);
            redisMap.put("expiryTime", "" + expiryTime);
        }

        @Override
        public void setMaxInactiveInterval(int secs) {
            super.setMaxInactiveInterval(secs);
            // prepare serialization
            redisMap.put("maxIdle", "" + _maxIdleMs);
        }

        @Override
        protected void cookieSet() {
            super.cookieSet();
            // prepare serialization
            redisMap.put("cookieSet", "" + _cookieSet);
        }

        @Override
        protected void complete() {
            super.complete();
            try {
                if (!redisMap.isEmpty()
                        && (redisMap.size() != 3
                        || !redisMap.containsKey("lastAccessed")
                        || !redisMap.containsKey("accessed")
                        || !redisMap.containsKey("expiryTime")
                        || _accessed - lastSaved >= getSaveInterval() * 1000)) {
                    willPassivate();
                    storeSession(this);
                    didActivate();
                }
            } catch (Exception e) {
                Log.warn("Problem persisting changed session data id=" + getId(), e);
            } finally {
                redisMap.clear();
            }
        }

        public void saved() {
            lastSaved = System.currentTimeMillis();
            redisMap.put("lastSaved", "" + lastSaved);
        }
    }
}
