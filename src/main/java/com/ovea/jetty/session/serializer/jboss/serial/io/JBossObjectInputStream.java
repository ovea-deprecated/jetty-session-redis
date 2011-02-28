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
import com.ovea.jetty.session.serializer.jboss.serial.classmetamodel.ClassResolver;
import com.ovea.jetty.session.serializer.jboss.serial.classmetamodel.DefaultClassDescriptorStrategy;
import com.ovea.jetty.session.serializer.jboss.serial.exception.SerializationException;
import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.*;
import com.ovea.jetty.session.serializer.jboss.serial.util.ClassMetaConsts;
import com.ovea.jetty.session.serializer.jboss.serial.util.StringUtil;
import com.ovea.jetty.session.serializer.jboss.serial.util.StringUtilBuffer;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * @author <a href="mailto:clebert.suconic@jboss.com">Clebert Suconic</a>
 */
public class JBossObjectInputStream extends ObjectInputStream implements DataContainerConstants {

    InputStream is;
    DataInputStream dis;
    DataContainer container;
    ClassLoader classLoader;
    StringUtilBuffer buffer;

    private static Constructor constructorObjectStreamClass;
    private static Field setNameField;

    ClassDescriptorStrategy classDescriptorStrategy = new DefaultClassDescriptorStrategy();
    ObjectDescriptorStrategy objectDescriptorStrategy = new DefaultObjectDescriptorStrategy();

    static {
        try {
            constructorObjectStreamClass = ObjectStreamClass.class.getDeclaredConstructor(ClassMetaConsts.EMPTY_CLASS_ARRY);
            constructorObjectStreamClass.setAccessible(true);
            setNameField = ObjectStreamClass.class.getDeclaredField("name");
            setNameField.setAccessible(true);
        } catch (Exception e) {
            constructorObjectStreamClass = null;
            setNameField = null;
            e.printStackTrace();
        }
    }


    private static Field fieldEnableResolve;

    static {
        try {
            fieldEnableResolve = ObjectInputStream.class.getDeclaredField("enableResolve");
            fieldEnableResolve.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }


    protected ObjectSubstitutionInterface getSubstitutionInterface() throws IOException {
        try {
            if (fieldEnableResolve.getBoolean(this)) {
                return new ObjectSubstitutionInterface() {
                    public Object replaceObject(Object obj) throws IOException {
                        return JBossObjectInputStream.this.resolveObject(obj);
                    }
                };
            } else {
                return null;
            }
        } catch (IllegalAccessException ex) {
            throw new SerializationException(ex.getMessage(), ex);
        }
    }


    public JBossObjectInputStream(InputStream is) throws IOException {
        this(is, Thread.currentThread().getContextClassLoader());
    }

    public JBossObjectInputStream(InputStream is, StringUtilBuffer buffer) throws IOException {
        this(is, Thread.currentThread().getContextClassLoader(), buffer);
    }

    public JBossObjectInputStream(InputStream is, ClassLoader loader) throws IOException {
        this(is, loader, null);
    }

    /**
     * In case of InputStream is null, the only method that can be used on this class is smartClone
     */
    public JBossObjectInputStream(InputStream is, ClassLoader loader, StringUtilBuffer buffer) throws IOException {
        super();
        this.buffer = buffer;

        if (is != null) {
            this.is = is;
            readStreamHeader();
            if (is instanceof DataInputStream) {
                dis = (DataInputStream) is;
            } else {
                dis = new DataInputStream(is);
            }
        }

        this.classLoader = loader;
    }

    private void checkSignature(InputStream is) throws IOException {
        byte signature[] = new byte[openSign.length];
        is.read(signature);
        if (!Arrays.equals(signature, openSign)) {
            throw new IOException("Mismatch version of JBossSerialization signature");
        }
    }

    ClassResolver resolver = new ClassResolver() {

        public Class resolveClass(String name) throws ClassNotFoundException {
            if (constructorObjectStreamClass != null) {
                try {
                    ObjectStreamClass streamClass = (ObjectStreamClass) constructorObjectStreamClass.newInstance(ClassMetaConsts.EMPTY_OBJECT_ARRAY);
                    setNameField.set(streamClass, name);
                    return JBossObjectInputStream.this.resolveClass(streamClass);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        }

    };

    protected Class resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        return Class.forName(desc.getName(), false, getClassLoader());
    }

    /**
     * This is the old method of reading objects, always loading everything to a datacontainer
     */
    public Object readObjectUsingDataContainer() throws IOException,
            ClassNotFoundException {
        DataContainer container = new DataContainer(classLoader, false, buffer);
        container.setClassResolver(resolver);
        container.loadData(dis);
        ObjectInput input = container.getInput();
        return input.readObject();
    }

    public Object readObjectOverride() throws IOException,
            ClassNotFoundException {
        DataContainer container = new DataContainer(classLoader, this.getSubstitutionInterface(), false, buffer, classDescriptorStrategy, objectDescriptorStrategy);
        container.setClassResolver(resolver);
        //container.loadData(dis);
        //ObjectInput input = container.getInput();
        ObjectInput input = container.getDirectInput(dis);
        return input.readObject();
    }

    public Object readUnshared() throws IOException, ClassNotFoundException {
        return readObjectOverride();
    }

    public void defaultReadObject() throws IOException, ClassNotFoundException {
    }

    public void registerValidation(ObjectInputValidation obj, int prio)
            throws NotActiveException, InvalidObjectException {
    }

    protected void readStreamHeader() throws IOException,
            StreamCorruptedException {
        checkSignature(is);
    }

    protected ObjectStreamClass readClassDescriptor() throws IOException,
            ClassNotFoundException {
        return null;
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

    public int read() throws IOException {
        return dis.read();
    }

    public int read(byte[] buf, int off, int len) throws IOException {
        return dis.read(buf, off, len);
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
        return dis.readBoolean();
    }

    public byte readByte() throws IOException {
        return dis.readByte();
    }

    public int readUnsignedByte() throws IOException {
        return dis.readUnsignedByte();
    }

    public char readChar() throws IOException {
        return dis.readChar();
    }

    public short readShort() throws IOException {
        return dis.readShort();
    }

    public int readUnsignedShort() throws IOException {
        return dis.readUnsignedShort();
    }

    public int readInt() throws IOException {
        return dis.readInt();
    }

    public long readLong() throws IOException {
        return dis.readLong();
    }

    public float readFloat() throws IOException {
        return dis.readFloat();
    }

    public double readDouble() throws IOException {
        return dis.readDouble();
    }

    public void readFully(byte[] buf) throws IOException {
        dis.readFully(buf);
    }

    public void readFully(byte[] buf, int off, int len) throws IOException {
        dis.readFully(buf, off, len);
    }

    public int skipBytes(int len) throws IOException {
        return dis.skipBytes(len);
    }

    @SuppressWarnings({"deprecation"})
    public String readLine() throws IOException {
        return dis.readLine();
    }

    public String readUTF() throws IOException {
        return StringUtil.readString(dis, buffer);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.ObjectInput#read(byte[])
     */
    public int read(byte[] b) throws IOException {
        return dis.read(b);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.ObjectInput#skip(long)
     */
    public long skip(long n) throws IOException {
        return dis.skip(n);
    }

    public ClassLoader getClassLoader() {
        if (classLoader == null) {
            return Thread.currentThread().getContextClassLoader();
        } else {
            return classLoader;
        }
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

}
