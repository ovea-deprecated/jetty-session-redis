package com.ovea.jetty.session.serializer.jboss.serial.io;

import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.DataContainer;
import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.safecloning.SafeCloningRepository;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


/**
 * This is the equivalent for MarshalledObject on RMI, but this is optimized for local calls.
 * Instead of converting every single field into Bytes, this will use a DataContainer.
 *
 * @author Clebert Suconic
 */
public class MarshalledObjectForLocalCalls implements Externalizable {
    private static final long serialVersionUID = 785809358605094514L;

    DataContainer container;

    public MarshalledObjectForLocalCalls() {
    }

    public MarshalledObjectForLocalCalls(Object obj) throws IOException {
        container = new DataContainer(false);
        ObjectOutput output = container.getOutput();
        output.writeObject(obj);
        output.flush();
        container.flush();
    }

    public MarshalledObjectForLocalCalls(Object obj, SafeCloningRepository safeToReuse) throws IOException {
        container = new DataContainer(null, null, safeToReuse, false, null);
        ObjectOutput output = container.getOutput();
        output.writeObject(obj);
        output.flush();
        container.flush();
    }

    /**
     * The object has to be unserialized only when the first get is executed.
     */
    public Object get() throws IOException, ClassNotFoundException {
        try {
            container.getCache().setLoader(Thread.currentThread().getContextClassLoader());
            return container.getInput().readObject();
        } catch (RuntimeException e) {
            throw e;
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        container.saveData(out);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        container = new DataContainer(false);
        container.loadData(in);
    }
}
