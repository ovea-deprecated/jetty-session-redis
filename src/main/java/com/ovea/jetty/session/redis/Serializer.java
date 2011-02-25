package com.ovea.jetty.session.redis;

public interface Serializer {
    String serialize(Object o);

    Object deserialize(String o);
}
