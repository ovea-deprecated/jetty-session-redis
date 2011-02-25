package com.ovea.jetty.session.redis;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
interface JedisExecutor {
    <V> V execute(JedisCallback<V> cb);
}
