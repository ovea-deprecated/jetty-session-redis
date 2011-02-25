package net.playtouch.jaxspot.module.session.serializer.redis;

import redis.clients.jedis.Transaction;

/**
 * @author Mathieu Carbou
 */
abstract class RedisWritable extends RedisInvoker {
    protected RedisWritable(RedisCommand command, Object key, Object... args) {
        super(command, key, args);
    }

    abstract void execute(Transaction jedis);
}
