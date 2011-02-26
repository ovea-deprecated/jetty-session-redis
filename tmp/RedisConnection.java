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

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class RedisConnection {

    private static final Logger LOGGER = Logger.getLogger(RedisConnection.class.getName());

    private final JedisPool pool;

    private final Deque<RedisWritable> commandQueue = new LinkedList<RedisWritable>();

    private Jedis readOnlyJedis;
    private Jedis writeOnlyJedis;

    RedisConnection(JedisPool pool) {
        this.pool = pool;
    }

    <V> V invoke(RedisReadable<V> redisReadable) {
        // check if a pending action exists to recover the uncommited value first
        for (Iterator<RedisWritable> it = commandQueue.descendingIterator(); it.hasNext();) {
            RedisWritable rw = it.next();
            // a pending action is found for this key. Check its code
            if (redisReadable.key.equals(rw.key)) {
                switch (rw.command) {
                    case DEL: {
                        if (LOGGER.isLoggable(Level.FINE))
                            LOGGER.fine("{return uncommited DEL} null");
                        return null;
                    }
                    case SET:
                    case SET_TTL: {
                        if (LOGGER.isLoggable(Level.FINE))
                            LOGGER.fine("{return uncommited SEL} " + rw.args[0]);
                        return (V) rw.args[0];
                    }
                    default:
                        throw new AssertionError("Unsupported: " + rw);
                }
            }
        }
        // if we didn't found any pending action, we fetch a redis resource to execute it not in the current
        // transactional redis connexion
        Jedis rJedis = fetchReadableJedis();
        try {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("Executing readOnly actions");
            return redisReadable.execute(rJedis);
        } catch (RuntimeException e) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("readOnly action failure: " + e.getMessage());
            readOnlyJedis = null;
            pool.returnBrokenResource(rJedis);
            throw e;
        }
    }

    void invokeLater(RedisWritable newWritable) {
        // check if a pending action exists for the same key to check if we can override it
        for (Iterator<RedisWritable> it = commandQueue.descendingIterator(); it.hasNext();) {
            RedisWritable rw = it.next();
            // a pending action is found for this key. Check its code
            if (newWritable.key.equals(rw.key)) {
                switch (rw.command) {
                    case DEL:
                    case SET:
                    case SET_TTL: {
                        // remove the old one
                        if (LOGGER.isLoggable(Level.FINE))
                            LOGGER.fine("{removing uncommited action} " + rw);
                        it.remove();
                        break;
                    }
                    default:
                        throw new AssertionError("Unsupported: " + rw);
                }
            }
        }
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("Queueing new redis action");
        // add the new command
        commandQueue.offer(newWritable);
    }

    void discard(Throwable cause) {
        if (writeOnlyJedis != null) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("DISCARD: Returning maybe broken WO redis");
            pool.returnBrokenResource(writeOnlyJedis);
        }
        if (readOnlyJedis != null) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("DISCARD: Returning maybe broken RO redis");
            pool.returnBrokenResource(readOnlyJedis);
        }
    }

    void exec() {
        if (!commandQueue.isEmpty()) {
            Transaction transaction = null;
            try {
                transaction = openTX();
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("Executing command queue...");
                while (!commandQueue.isEmpty()) {
                    commandQueue.poll().execute(transaction);
                }
                transaction.exec();
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("COMMIT: Returning WO redis");
                pool.returnResource(writeOnlyJedis);
            } catch (Exception e) {
                if (transaction != null) {
                    try {
                        transaction.discard();
                    } catch (Exception ee) {
                        LOGGER.log(Level.WARNING, "Discarding of Redis transaction failed due to: " + ee.getMessage() + " - Transaction discarded because of an encountered exception: " + e.getMessage(), e);
                    }
                    if (LOGGER.isLoggable(Level.FINE))
                        LOGGER.fine("ERR: Returning broken WO redis");
                    pool.returnBrokenResource(writeOnlyJedis);
                }
            } finally {
                writeOnlyJedis = null;
            }
        }
        if (readOnlyJedis != null) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("COMMIT: Returning RO redis");
            pool.returnResource(readOnlyJedis);
        }
    }

    private Transaction openTX() {
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("Creating redis transaction (MULTI)");
        writeOnlyJedis = pool.getResource();
        return writeOnlyJedis.multi();
    }

    private Jedis fetchReadableJedis() {
        if (readOnlyJedis == null) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("Getting readOnly redis connection from pool for this thread");
            readOnlyJedis = pool.getResource();
        } else if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Reusing readOnly redis connection bound to this thread");
        }
        return readOnlyJedis;
    }

}
