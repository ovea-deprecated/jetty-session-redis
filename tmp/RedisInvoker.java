package net.playtouch.jaxspot.module.session.serializer.redis;

import java.util.Arrays;

/**
 * @author Mathieu Carbou
 */
abstract class RedisInvoker {
    final RedisCommand command;
    final Object key;
    final Object[] args;

    protected RedisInvoker(RedisCommand command, Object key, Object... args) {
        command.validate(key, args);
        this.command = command;
        this.key = key;
        this.args = args;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RedisInvoker that = (RedisInvoker) o;
        return Arrays.equals(args, that.args) && key.equals(that.key);
    }

    @Override
    public final int hashCode() {
        int result = key.hashCode();
        result = 31 * result + (args != null ? Arrays.hashCode(args) : 0);
        return result;
    }

    @Override
    public final String toString() {
        return command + " " + key;
    }
}
