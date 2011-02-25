package net.playtouch.jaxspot.module.session.serializer.redis;

/**
 * @author Mathieu Carbou
 */
public final class SerializerException extends RuntimeException {
    public SerializerException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    public SerializerException(String message) {
        super(message);
    }

    public SerializerException(String message, Throwable cause) {
        super(message, cause);
    }
}
