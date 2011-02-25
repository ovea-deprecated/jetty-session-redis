package net.playtouch.jaxspot.module.session.serializer.redis;

public interface Serializer {
    String keyToString(Object o) throws SerializerException;

    String valueToString(Object o) throws SerializerException;

    Object valueFromString(String o) throws SerializerException;
}
