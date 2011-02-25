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
