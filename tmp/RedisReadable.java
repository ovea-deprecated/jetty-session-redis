package net.playtouch.jaxspot.module.session.serializer.redis;

import redis.clients.jedis.Jedis;

/**
 * @author Mathieu Carbou
 */
abstract class RedisReadable<V> extends RedisInvoker {
    protected RedisReadable(RedisCommand command, Object key, Object... args) {
        super(command, key, args);
    }

    abstract V execute(Jedis jedis);
}
