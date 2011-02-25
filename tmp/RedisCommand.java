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
