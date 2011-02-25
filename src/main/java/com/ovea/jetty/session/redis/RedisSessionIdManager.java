package com.ovea.jetty.session.redis;

import com.ovea.jetty.session.SessionIdManagerSkeleton;
import redis.clients.jedis.Jedis;


/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class RedisSessionIdManager extends SessionIdManagerSkeleton {

    private static final String REDIS_SESSIONS_KEY = "jetty-sessions";

    private JedisExecutor jedisExecutor;

    @Override
    protected void deleteClusterId(final String clusterId) {
        jedisExecutor.execute(new JedisCallback<Object>() {
            @Override
            public Object execute(Jedis jedis) {
                return jedis.srem(REDIS_SESSIONS_KEY, clusterId);
            }
        });
    }

    @Override
    protected void storeClusterId(final String clusterId) {
        jedisExecutor.execute(new JedisCallback<Object>() {
            @Override
            public Object execute(Jedis jedis) {
                return jedis.sadd(REDIS_SESSIONS_KEY, clusterId);
            }
        });
    }

    @Override
    protected boolean hasClusterId(final String clusterId) {
        return jedisExecutor.execute(new JedisCallback<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) {
                return jedis.sismember(REDIS_SESSIONS_KEY, clusterId);
            }
        });
    }
}
