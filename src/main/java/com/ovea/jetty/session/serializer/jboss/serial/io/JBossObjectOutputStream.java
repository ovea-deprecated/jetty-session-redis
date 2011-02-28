/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

package com.ovea.jetty.session.serializer.jboss.serial.io;

import com.ovea.jetty.session.serializer.jboss.serial.classmetamodel.ClassDescriptorStrategy;
import com.ovea.jetty.session.serializer.jboss.serial.classmetamodel.DefaultClassDescriptorStrategy;
import com.ovea.jetty.session.serializer.jboss.serial.exception.SerializationException;
import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.*;
import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.safecloning.SafeCloningRepository;
import com.ovea.jetty.session.serializer.jboss.serial.util.StringUtil;
import com.ovea.jetty.session.serializer.jboss.serial.util.StringUtilBuffer;

import java.io.*;
import java.lang.reflect.Field;

/**
 * @author <a href="mailto:clebert.suconic@jboss.com">Clebert Suconic</a>
 */
public class JBossObjectOutputStream extends ObjectOutputStream implements DataContainerConstants {
    OutputStream output;
    DataOutputStream dataOutput;

    boolean checkSerializableClass = false;
    boolean standardReplacement = false;

    ClassDescriptorStrategy classDescriptorStrategy = new DefaultClassDescriptorStrategy();
    ObjectDescriptorStrategy objectDescriptorStrategy = new DefaultObjectDescriptorStrategy();

    static Field fieldEnableReplace;

    /**
     * one of the optimizations we do is to reuse byte arrays on writeUTF operations, look at {@link org.jboss.serial.util.StringUtil}.
     * StringUtil has also the capability of creating Buffers on ThreadLocal over demand, but having these buffers pre-created
     * is more efficient
     */
    StringUtilBuffer buffer;

    static {
        try {
            fieldEnableReplace = ObjectOutputStream.class.getDeclaredField("enableReplace");
            fieldEnableReplace.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }


    protected ObjectSubstitutionInterface getSubstitutionInterface() throws IOException {
        try {
            if (fieldEnableReplace.getBoolean(this)) {
                return new ObjectSubstitutionInterface() {
                    public Object replaceObject(Object obj) throws IOException {
                        return JBossObjectOutputStream.this.replaceObject(obj);
                    }
                };
            } else {
                return null;
            }
        } catch (IllegalAccessException ex) {
            throw new SerializationException(ex.getMessage(), ex);
        }
    }


    /**
     * Creates an OutputStream, that by default doesn't require
     */
    public JBossObjectOutputStream(OutputStream output) throws IOException {
        this(output, false);
    }

    /**
     * Creates an OutputStream, that by default doesn't require
     */
    public JBossObjectOutputStream(OutputStream output, StringUtilBuffer buffer) throws IOException {
        this(output, false, buffer);
    }

    public JBossObjectOutputStream(OutputStream output, boolean checkSerializableClass) throws IOException {
        this(output, checkSerializableClass, null);
    }

    public JBossObjectOutputStream(OutputStream output, boolean checkSerializableClass, StringUtilBuffer buffer) throws IOException {
        super();

        this.buffer = buffer;
        this.output = output;
        this.checkSerializableClass = checkSerializableClass;
        writeStreamHeader();

        if (output instanceof DataOutputStream) {
            dataOutput = (DataOutputStream) output;
        } else {
            dataOutput = new DataOutputStream(output);
        }
    }

    public void writeObjectUsingDataContainer(Object obj) throws IOException {
        DataContainer dataContainer = new DataContainer(null, this.getSubstitutionInterface(), checkSerializableClass, buffer, classDescriptorStrategy, objectDescriptorStrategy);
        if (output instanceof DataOutputStream) {
            dataOutput = (DataOutputStream) output;
        } else {
            dataOutput = new DataOutputStream(output);
        }

        ObjectOutput objectOutput = dataContainer.getOutput();
        objectOutput.writeObject(obj);

        //objectOutput.flush();
        dataContainer.saveData(dataOutput);

        //this.flush();
    }

    protected void writeObjectOverride(Object obj) throws IOException {
        DataContainer dataContainer = new DataContainer(null, this.getSubstitutionInterface(), checkSerializableClass, buffer, classDescriptorStrategy, objectDescriptorStrategy);
        if (output instanceof DataOutputStream) {
            dataOutput = (DataOutputStream) output;
        } else {
            dataOutput = new DataOutputStream(output);
        }

        dataContainer.setStringBuffer(buffer);

        ObjectOutput objectOutput = dataContainer.getDirectOutput(this.dataOutput);
        objectOutput.writeObject(obj);

        //objectOutput.flush();
        //dataContainer.saveData(dataOutput);

        //this.flush();
    }

    public void writeUnshared(Object obj) throws IOException {
        writeObjectOverride(obj);
    }

    public void defaultWriteObject() throws IOException {
    }

    public void writeFields() throws IOException {
    }

    public void reset() throws IOException {
    }

    protected void writeStreamHeader() throws IOException {
        if (output != null) {
            output.write(openSign);
        }
    }

    protected void writeClassDescriptor(ObjectStreamClass desc)
            throws IOException {
    }

    protected ClassDescriptorStrategy getClassDescriptorStrategy() {
        return classDescriptorStrategy;
    }

    protected void setClassDescriptorStrategy(ClassDescriptorStrategy classDescriptorStrategy) {
        this.classDescriptorStrategy = classDescriptorStrategy;
    }

    protected ObjectDescriptorStrategy getObjectDescriptorStrategy() {
        return objectDescriptorStrategy;
    }

    protected void setObjectDescriptorStrategy(ObjectDescriptorStrategy objectDescriptorStrategy) {
        this.objectDescriptorStrategy = objectDescriptorStrategy;
    }

    protected boolean isStandardReplacement() {
        return standardReplacement;
    }

    protected void setStandardReplacement(boolean standardReplacement) {
        this.standardReplacement = standardReplacement;
    }

    /**
     * Writes a byte. This method will block until the byte is actually
     * written.
     *
     * @param val the byte to be written to the stream
     * @throws java.io.IOException If an I/O error has occurred.
     */
    public void write(int val) throws IOException {
        dataOutput.write(val);
    }

    /**
     * Writes an array of bytes. This method will block until the bytes are
     * actually written.
     *
     * @param buf the data to be written
     * @throws java.io.IOException If an I/O error has occurred.
     */
    public void write(byte[] buf) throws IOException {
        dataOutput.write(buf);
    }

    public void write(byte[] buf, int off, int len) throws IOException {
        if (buf == null) {
            throw new SerializationException("buf parameter can't be null");
        }
        dataOutput.write(buf, off, len);
    }

    /**
     * Flushes the stream. This will write any buffered output bytes and flush
     * through to the underlying stream.
     *
     * @throws java.io.IOException If an I/O error has occurred.
     */
    public void flush() throws IOException {
        if (dataOutput != null) {
            dataOutput.flush();
        } else {
            output.flush();
        }
    }

    protected void drain() throws IOException {
        //bout.drain();
    }

    public void close() throws IOException {
        flush();
        dataOutput.close();
    }

    public void writeBoolean(boolean val) throws IOException {
        dataOutput.writeBoolean(val);
    }

    public void writeByte(int val) throws IOException {
        dataOutput.writeByte(val);
    }

    public void writeShort(int val) throws IOException {
        dataOutput.writeShort(val);
    }

    public void writeChar(int val) throws IOException {
        dataOutput.writeChar(val);
    }

    public void writeInt(int val) throws IOException {
        dataOutput.writeInt(val);
    }

    public void writeLong(long val) throws IOException {
        dataOutput.writeLong(val);
    }

    public void writeFloat(float val) throws IOException {
        dataOutput.writeFloat(val);
    }

    public void writeDouble(double val) throws IOException {
        dataOutput.writeDouble(val);
    }

    public void writeBytes(String str) throws IOException {
        dataOutput.writeBytes(str);
    }

    public void writeChars(String str) throws IOException {
        dataOutput.writeChars(str);
    }

    public void writeUTF(String str) throws IOException {
        StringUtil.saveString(dataOutput, str, buffer);
    }

    /**
     * Reuses every primitive value to recreate another object.
     */
    public Object smartClone(Object obj) throws IOException {
        return smartClone(obj, null, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Reuses every primitive value to recreate another object.
     */
    public Object smartClone(Object obj, SafeCloningRepository safeToReuse) throws IOException {
        return smartClone(obj, safeToReuse, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Reuses every primitive value to recreate another object.
     * and if safeToReuse!=null, it can reuse the entire object
     */
    public Object smartClone(Object obj, SafeCloningRepository safeToReuse, ClassLoader loader) throws IOException {

        DataContainer container = new DataContainer(loader, this.getSubstitutionInterface(), safeToReuse, checkSerializableClass, buffer);
        ObjectOutput output = container.getOutput();
        output.writeObject(obj);
        output.flush();

        ObjectInput input = container.getInput();
        try {
            return input.readObject();
        } catch (ClassNotFoundException e) {
            throw new SerializationException(e.getMessage(), e);
        }
    }


}
