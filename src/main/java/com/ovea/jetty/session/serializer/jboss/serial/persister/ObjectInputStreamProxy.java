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

package com.ovea.jetty.session.serializer.jboss.serial.persister;

import com.ovea.jetty.session.serializer.jboss.serial.classmetamodel.ClassMetaDataSlot;
import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.FieldsContainer;
import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.ObjectSubstitutionInterface;

import java.io.*;

/**
 * $Id: ObjectInputStreamProxy.java 231 2006-04-24 23:49:41Z csuconic $
 *
 * @author Clebert Suconic
 */
public class ObjectInputStreamProxy extends ObjectInputStream {

    Object currentObj;
    ClassMetaDataSlot currentMetaClass;
    ObjectSubstitutionInterface currentSubstitution;

    short[] fieldsKey;

    ObjectInput input;

    public ObjectInputStreamProxy(ObjectInput input, short[] fieldsKey, Object currentObj, ClassMetaDataSlot currentMetaClass, ObjectSubstitutionInterface currentSubstitution) throws IOException {
        super();
        this.input = input;
        this.fieldsKey = fieldsKey;
        this.currentObj = currentObj;
        this.currentMetaClass = currentMetaClass;
        this.currentSubstitution = currentSubstitution;
    }

    protected Object readObjectOverride() throws IOException,
            ClassNotFoundException {
        return input.readObject();
    }

    public Object readUnshared() throws IOException, ClassNotFoundException {
        return readObjectOverride();
    }

    public void defaultReadObject() throws IOException, ClassNotFoundException {
        RegularObjectPersister.readSlotWithFields(fieldsKey, currentMetaClass, input, currentObj); // @todo - finish this
    }

    public void registerValidation(ObjectInputValidation obj, int prio)
            throws NotActiveException, InvalidObjectException {
    }

    protected void readStreamHeader() throws IOException,
            StreamCorruptedException {
    }

    protected ObjectStreamClass readClassDescriptor() throws IOException,
            ClassNotFoundException {
        return null;
    }

    public int read() throws IOException {
        return input.read();
    }

    public int read(byte[] buf, int off, int len) throws IOException {
        return input.read(buf, off, len);
    }

    /**
     * Returns the number of bytes that can be read without blocking.
     *
     * @return the number of available bytes.
     * @throws java.io.IOException if there are I/O errors while reading from the underlying
     *                             <code>InputStream</code>
     */
    public int available() throws IOException {
        return 1;
    }

    public void close() throws IOException {
    }

    public boolean readBoolean() throws IOException {
        return input.readBoolean();
    }

    public byte readByte() throws IOException {
        return input.readByte();
    }

    public int readUnsignedByte() throws IOException {
        return input.readUnsignedByte();
    }

    public char readChar() throws IOException {
        return input.readChar();
    }

    public short readShort() throws IOException {
        return input.readShort();
    }

    public int readUnsignedShort() throws IOException {
        return input.readUnsignedShort();
    }

    public int readInt() throws IOException {
        return input.readInt();
    }

    public long readLong() throws IOException {
        return input.readLong();
    }

    public float readFloat() throws IOException {
        return input.readFloat();
    }

    public double readDouble() throws IOException {
        return input.readDouble();
    }

    public void readFully(byte[] buf) throws IOException {
        input.readFully(buf);
    }

    public void readFully(byte[] buf, int off, int len) throws IOException {
        input.readFully(buf, off, len);
    }

    public int skipBytes(int len) throws IOException {
        return input.skipBytes(len);
    }

    @SuppressWarnings({"deprecation"})
    public String readLine() throws IOException {
        return input.readLine();
    }

    public String readUTF() throws IOException {
        return input.readUTF();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.ObjectInput#read(byte[])
     */
    public int read(byte[] b) throws IOException {
        return input.read(b);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.ObjectInput#skip(long)
     */
    public long skip(long n) throws IOException {
        return input.skip(n);
    }


    public GetField readFields()
            throws IOException, ClassNotFoundException {
        FieldsContainer container = new FieldsContainer(currentMetaClass);
        container.readMyself(this);
        return container.createGet();
    }


}
