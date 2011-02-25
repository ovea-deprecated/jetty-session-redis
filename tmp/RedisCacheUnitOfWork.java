package net.playtouch.jaxspot.module.session.serializer.redis;

import net.playtouch.jaxspot.module.caching.CacheException;
import net.playtouch.jaxspot.module.caching.CacheUnitOfWorkSkeleton;
import redis.clients.jedis.JedisException;
import redis.clients.jedis.JedisPool;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class RedisCacheUnitOfWork extends CacheUnitOfWorkSkeleton<RedisConnection> {

    private static final Logger LOGGER = Logger.getLogger(RedisCacheUnitOfWork.class.getName());
    private final JedisPool pool;

    public RedisCacheUnitOfWork(JedisPool pool) {
        this.pool = pool;
    }

    @Override
    protected RedisConnection getConnection() {
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("Creating new RedisConnection");
        try {
            return new RedisConnection(pool);
        } catch (JedisException e) {
            throw new CacheException(e.getMessage(), e);
        }
    }

    @Override
    protected void doCommit(RedisConnection connection) {
        connection.exec();
    }

    @Override
    protected void doRollback(RedisConnection connection, Throwable t) {
        connection.discard(t);
    }

}
