package com.ovea.jetty.session.redis;

import redis.clients.jedis.Jedis;

/**
* @author Mathieu Carbou (mathieu.carbou@gmail.com)
*/
interface JedisCallback<V> {
    V execute(Jedis jedis);
}
