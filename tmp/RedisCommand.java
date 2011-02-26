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

/**
 * @author Mathieu Carbou
 */
enum RedisCommand {
    SET {
        @Override
        void validate(Object key, Object... args) {
            if (key == null)
                throw new IllegalArgumentException("Key required");
            if (args == null || args.length != 1)
                throw new IllegalArgumentException("One argument required");
        }},
    SET_TTL {
        @Override
        void validate(Object key, Object... args) {
            if (key == null)
                throw new IllegalArgumentException("Key required");
            if (args == null || args.length != 2)
                throw new IllegalArgumentException("One argument required");
        }},
    DEL {
        @Override
        void validate(Object key, Object... args) {
            if (key == null)
                throw new IllegalArgumentException("Key required");
        }},
    GET {
        @Override
        void validate(Object key, Object... args) {
            if (key == null)
                throw new IllegalArgumentException("Key required");
        }};

    abstract void validate(Object key, Object... args);

}
