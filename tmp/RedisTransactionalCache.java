package net.playtouch.jaxspot.module.session.serializer.redis;

import net.playtouch.jaxspot.module.caching.CacheException;
import net.playtouch.jaxspot.module.caching.TransactionalCache;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class RedisTransactionalCache<K, V> implements TransactionalCache<K, V, RedisConnection> {

    private static final Logger LOGGER = Logger.getLogger(RedisTransactionalCache.class.getName());
    private final Serializer serializer;

    public RedisTransactionalCache(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public V get(final RedisConnection connection, K key) throws CacheException {
        return connection.invoke(new RedisReadable<V>(RedisCommand.GET, key) {
            @Override
            V execute(Jedis jedis) {
                String s = serializer.keyToString(key);
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("REDIS - GET " + s);
                String v = jedis.get(s);
                return v == null ? null : (V) serializer.valueFromString(v);
            }
        });
    }

    @Override
    public void put(RedisConnection connection, K key, V o) throws CacheException {
        if (o == null) {
            remove(connection, key);
        } else {
            connection.invokeLater(new RedisWritable(RedisCommand.SET, key, o) {
                @Override
                void execute(Transaction jedis) {
                    String s = serializer.keyToString(key);
                    if (LOGGER.isLoggable(Level.FINE))
                        LOGGER.fine("REDIS - SET " + s);
                    jedis.set(s, serializer.valueToString(args[0]));
                }
            });
        }
    }

    @Override
    public void put(RedisConnection connection, K key, V o, int ttlInSeconds) throws CacheException {
        if (o == null) {
            remove(connection, key);
        } else {
            connection.invokeLater(new RedisWritable(RedisCommand.SET_TTL, key, o, ttlInSeconds) {
                @Override
                void execute(Transaction jedis) {
                    String s = serializer.keyToString(key);
                    if (LOGGER.isLoggable(Level.FINE))
                        LOGGER.fine("REDIS - SET " + s + " TTL " + args[1]);
                    jedis.set(s, serializer.valueToString(args[0]));
                    jedis.expire(s, (Integer) args[1]);
                }
            });
        }
    }

    @Override
    public void remove(RedisConnection connection, K key) throws CacheException {
        connection.invokeLater(new RedisWritable(RedisCommand.DEL, key) {
            @Override
            void execute(Transaction jedis) {
                String s = serializer.keyToString(key);
                if (LOGGER.isLoggable(Level.FINE))
                        LOGGER.fine("REDIS - DEL " + s);
                jedis.del(s);
            }
        });
    }

}
