package com.ovea.jetty.session.serializer.jboss.serial.references;

public class EmptyReference extends PersistentReference {

    public EmptyReference() {
        super(null, null, REFERENCE_WEAK);
    }

    public Object rebuildReference() throws Exception {
        return null;
    }

}

