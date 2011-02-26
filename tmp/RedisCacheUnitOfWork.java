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
