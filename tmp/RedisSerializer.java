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

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class RedisSerializer implements Serializer {

    private final ObjectMapper mapper;

    public RedisSerializer(ObjectMapper objectMapper) {
        this.mapper = objectMapper;
    }

    @Override
    public String valueToString(Object o) throws SerializerException {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(o.getClass().getName(), o);
            return mapper.writeValueAsString(map);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Object valueFromString(String o) throws SerializerException {
        JsonNode node;
        try {
            node = mapper.readTree(o);
        } catch (IOException e) {
            throw new SerializerException(e);
        }
        if (!(node instanceof ObjectNode))
            throw new SerializerException("Illegal JSON: " + o);
        Map.Entry<String, JsonNode> entry = ((ObjectNode) node).getFields().next();
        try {
            Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(entry.getKey());
            return mapper.treeToValue(entry.getValue(), c);
        } catch (ClassNotFoundException e) {
            throw new SerializerException("Unable to find class in current context classloader: " + Thread.currentThread().getContextClassLoader() + " : " + e.getMessage(), e);
        } catch (IOException e) {
            throw new SerializerException(e);
        }
    }

    @Override
    public String keyToString(Object o) throws SerializerException {
        return String.valueOf(o);
    }
}
