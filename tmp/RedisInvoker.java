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
