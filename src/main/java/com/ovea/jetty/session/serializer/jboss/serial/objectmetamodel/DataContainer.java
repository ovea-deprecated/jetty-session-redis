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

package com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel;

import com.ovea.jetty.session.serializer.jboss.serial.classmetamodel.ClassDescriptorStrategy;
import com.ovea.jetty.session.serializer.jboss.serial.classmetamodel.ClassMetamodelFactory;
import com.ovea.jetty.session.serializer.jboss.serial.classmetamodel.ClassResolver;
import com.ovea.jetty.session.serializer.jboss.serial.classmetamodel.DefaultClassDescriptorStrategy;
import com.ovea.jetty.session.serializer.jboss.serial.exception.SerializationException;
import com.ovea.jetty.session.serializer.jboss.serial.finalcontainers.*;
import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.safecloning.SafeCloningRepository;
import com.ovea.jetty.session.serializer.jboss.serial.util.StringUtil;
import com.ovea.jetty.session.serializer.jboss.serial.util.StringUtilBuffer;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * DataContainer is a Container of Immutables and Object References.
 * It emmulates the repository as it would be a DataOutputStream and DataInputStream
 * <p/>
 * $Id: DataContainer.java 388 2010-08-01 03:58:10Z clebert.suconic@jboss.com $
 *
 * @author clebert suconic
 */
public class DataContainer extends DataExport implements DataContainerConstants, Externalizable {
    /**
     * These are the bytes used during streaming of Datacontainer. They are used to control and they are mixed within content.
     * So, they should be read in the same order they were written
     */
    byte[] controlStreaming;

    DataContainerOutput currentOutput = null;

    ArrayList content = new ArrayList();

    /**
     * I used the transient tag as a documentation feature.
     * this contains the root objectCache being used.
     * It would be possible to use ThreadLocals for this.
     * But I didn't want to take the risk of having problems when the application was running
     * in ThreadPools generating leaks.
     */
    transient ObjectsCache cache;

    public DataContainer cloneContainer() {
        DataContainer newContainer = new DataContainer();
        newContainer.content = this.content;
        newContainer.controlStreaming = this.controlStreaming;
        newContainer.cache = this.cache.cloneCache();
        return newContainer;
    }

    private DataContainer() {
    }

    public DataContainer(boolean checkSerializable) {
        this((ClassLoader) null, checkSerializable, null);
    }

    public DataContainer(boolean checkSerializable, StringUtilBuffer buffer) {
        this((ClassLoader) null, checkSerializable, buffer);
    }

    public DataContainer(ClassLoader loader, boolean checkSerializable, StringUtilBuffer buffer) {
        this(loader, null, checkSerializable, buffer);
    }

    public DataContainer(ClassLoader loader, boolean checkSerializable, StringUtilBuffer buffer, ClassResolver resolver) {
        this(loader, null, checkSerializable, buffer);
    }

    public DataContainer(ClassLoader loader, ObjectSubstitutionInterface substitution, boolean checkSerializable) {
        this(loader, substitution, null, checkSerializable, null);
    }

    public DataContainer(ClassLoader loader,
                         ObjectSubstitutionInterface substitution,
                         boolean checkSerializable,
                         StringUtilBuffer buffer) {
        this(loader, substitution, null, checkSerializable, buffer);
    }

    public DataContainer(ClassLoader loader,
                         ObjectSubstitutionInterface substitution,
                         boolean checkSerializable,
                         StringUtilBuffer buffer,
                         ClassDescriptorStrategy classDescriptorStrategy,
                         ObjectDescriptorStrategy objectDescriptorStrategy) {
        this(loader, substitution, null, checkSerializable, buffer, classDescriptorStrategy, objectDescriptorStrategy);
    }

    public DataContainer(ClassLoader loader,
                         ObjectSubstitutionInterface substitution,
                         SafeCloningRepository safeToReuse,
                         boolean checkSerializable) {
        this();
        this.cache = new ObjectsCache(substitution,
                loader,
                safeToReuse,
                checkSerializable,
                null,
                new DefaultClassDescriptorStrategy(),
                new DefaultObjectDescriptorStrategy());
    }

    public DataContainer(ClassLoader loader,
                         ObjectSubstitutionInterface substitution,
                         SafeCloningRepository safeToReuse,
                         boolean checkSerializable,
                         StringUtilBuffer buffer) {
        this();
        this.cache = new ObjectsCache(substitution,
                loader,
                safeToReuse,
                checkSerializable,
                buffer,
                new DefaultClassDescriptorStrategy(),
                new DefaultObjectDescriptorStrategy());
    }

    public DataContainer(ClassLoader loader,
                         ObjectSubstitutionInterface substitution,
                         SafeCloningRepository safeToReuse,
                         boolean checkSerializable,
                         StringUtilBuffer buffer,
                         ClassDescriptorStrategy classDescriptorStrategy,
                         ObjectDescriptorStrategy objectDescriptorStrategy) {
        this();
        this.cache = new ObjectsCache(substitution,
                loader,
                safeToReuse,
                checkSerializable,
                buffer,
                classDescriptorStrategy,
                objectDescriptorStrategy);
    }

    public DataContainer(ObjectsCache cache) {
        this.cache = cache;
    }

    public int getSize() {
        return content.size();
    }

    public ObjectInput getInput() {
        return new DataContainerInput();
    }

    public ObjectOutput getOutput() {
        if (currentOutput == null) {
            currentOutput = new DataContainerOutput();
        }
        return currentOutput;
    }

    public ObjectOutput getDirectOutput(DataOutputStream dataOut) {
        return new DataContainerDirectOutput(dataOut);
    }

    public ObjectInput getDirectInput(DataInputStream dataInput) {
        return new DataContainerDirectInput(dataInput);
    }

    public void flush() throws IOException {
        if (currentOutput != null) {
            currentOutput.flushByteArray();
        }
    }

    class DataContainerDirectOutput implements ObjectsCache.JBossSeralizationOutputInterface {
        DataOutputStream dataOut;

        public void addObjectReference(int reference) throws IOException {
            this.writeInt(reference);
        }

        public void openObjectDefinition() throws IOException {
        }

        public void closeObjectDefinition() throws IOException {
        }

        public void writeByteDirectly(byte parameter) throws IOException {
            this.write(parameter);
        }

        public boolean isCheckSerializableClass() {
            return DataContainer.this.getCache().isCheckSerializableClass();
        }

        public void writeObject(Object obj) throws IOException {
            DataContainer.this.cache.setOutput(this);
            if (cache.getSubstitution() != null) {
                obj = cache.getSubstitution().replaceObject(obj);
            }
            ObjectDescriptorFactory.describeObject(DataContainer.this.cache, obj);
        }

        public void write(byte b[]) throws IOException {
            dataOut.write(b);
        }

        public void close() throws IOException {
            dataOut.close();
        }

        public DataContainerDirectOutput(DataOutputStream dataOut) {
            this.dataOut = dataOut;
        }

        public void write(int b) throws IOException {
            dataOut.write(b);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            dataOut.write(b, off, len);
        }

        public void flush() throws IOException {
            dataOut.flush();
        }

        public void writeBoolean(boolean v) throws IOException {
            dataOut.writeBoolean(v);
        }

        public void writeByte(int v) throws IOException {
            dataOut.writeByte(v);
        }

        public void writeShort(int v) throws IOException {
            dataOut.writeShort(v);
        }

        public void writeChar(int v) throws IOException {
            dataOut.writeChar(v);
        }

        public void writeInt(int v) throws IOException {
            dataOut.writeInt(v);
        }

        public void writeLong(long v) throws IOException {
            dataOut.writeLong(v);
        }

        public void writeFloat(float v) throws IOException {
            dataOut.writeFloat(v);
        }

        public void writeDouble(double v) throws IOException {
            dataOut.writeDouble(v);
        }

        public void writeBytes(String s) throws IOException {
            dataOut.writeBytes(s);
        }

        public void writeChars(String s) throws IOException {
            dataOut.writeChars(s);
        }

        public void writeUTF(String str) throws IOException {
            StringUtil.saveString(dataOut, str, cache.getStringBuffer());
        }

        public int size() {
            return dataOut.size();
        }

        public void saveImmutable(ObjectsCache cache, Object obj) throws IOException {

            int id = cache.findIdInCacheWrite(obj, true);
            if (id != 0) {
                this.writeByte(DataContainerConstants.IMMUTABLE_OBJREF);
                this.addObjectReference(id);
                return;
            }
            id = cache.putObjectInCacheWrite(obj, true);
            if (obj instanceof String) {
                this.writeByte(DataContainerConstants.STRING);
                this.addObjectReference(id);
                StringUtil.saveString(this, (String) obj, cache.getStringBuffer());
            } else if (obj instanceof Byte) {
                this.writeByte(DataContainerConstants.BYTE);
                this.addObjectReference(id);
                this.writeByte(((Byte) obj).byteValue());
            } else if (obj instanceof Character) {
                this.writeByte(DataContainerConstants.CHARACTER);
                this.addObjectReference(id);
                this.writeChar(((Character) obj).charValue());
            } else if (obj instanceof Short) {
                this.writeByte(DataContainerConstants.SHORT);
                this.addObjectReference(id);
                this.writeShort(((Short) obj).shortValue());
            } else if (obj instanceof Integer) {
                this.writeByte(DataContainerConstants.INTEGER);
                this.addObjectReference(id);
                this.writeInt(((Integer) obj).intValue());
            } else if (obj instanceof Long) {
                this.writeByte(DataContainerConstants.LONG);
                this.addObjectReference(id);
                this.writeLong(((Long) obj).longValue());
            } else if (obj instanceof Double) {
                this.writeByte(DataContainerConstants.DOUBLE);
                this.addObjectReference(id);
                this.writeDouble(((Double) obj).doubleValue());
            } else if (obj instanceof Float) {
                this.writeByte(DataContainerConstants.FLOAT);
                this.addObjectReference(id);
                this.writeFloat(((Float) obj).floatValue());
            } else if (obj instanceof BooleanContainer || obj instanceof Boolean) {
                this.writeByte(DataContainerConstants.BOOLEAN);
                this.addObjectReference(id);
                this.writeBoolean(((Boolean) obj).booleanValue());
            } else {
                throw new SerializationException("I don't know how to write type " + obj.getClass().getName() + " yet");
            }

        }

    }

    class DataContainerOutput implements ObjectsCache.JBossSeralizationOutputInterface {

        /**
         * outByte is used to hold writeByte operations until we start doing a different operation
         */
        ByteArrayOutputStream outByte = new ByteArrayOutputStream();

        // DataOutputStream dataOutput = null;

        private void flushByteArray() {
            if (outByte != null) {
                byte controlStreaming[] = outByte.toByteArray();
                DataContainer.this.setControlStreaming(controlStreaming);
            }
        }

        /* (non-Javadoc)
        * @see java.io.ObjectOutput#writeObject(java.lang.Object)
        */
        public void writeObject(Object obj) throws IOException {

            if (obj == null) {
                this.writeByte(DataContainerConstants.NULLREF);
            } else {
                if (ClassMetamodelFactory.isImmutable(obj.getClass())) {
                    if (cache.getSubstitution() != null) {
                        obj = cache.getSubstitution().replaceObject(obj);
                    }
                }
                DataContainer.this.cache.setOutput(this);
                ObjectDescriptorFactory.describeObject(DataContainer.this.cache, obj);
            }
        }

        public void addObjectReference(int reference) throws IOException {
            this.writeInt(reference);
        }

        public void saveImmutable(ObjectsCache cache, Object obj) throws IOException {
            if (obj instanceof String) {
                this.writeByte(DataContainerConstants.STRING);
                DataContainer.this.content.add(obj);
            } else if (obj instanceof Byte) {
                this.writeByte(DataContainerConstants.BYTE);
                DataContainer.this.content.add(obj);
            } else if (obj instanceof Character) {
                this.writeByte(DataContainerConstants.CHARACTER);
                DataContainer.this.content.add(obj);
            } else if (obj instanceof Short) {
                this.writeByte(DataContainerConstants.SHORT);
                DataContainer.this.content.add(obj);
            } else if (obj instanceof Integer) {
                this.writeByte(DataContainerConstants.INTEGER);
                DataContainer.this.content.add(obj);
            } else if (obj instanceof Long) {
                this.writeByte(DataContainerConstants.LONG);
                DataContainer.this.content.add(obj);
            } else if (obj instanceof Double) {
                this.writeByte(DataContainerConstants.DOUBLE);
                DataContainer.this.content.add(obj);
            } else if (obj instanceof Float) {
                this.writeByte(DataContainerConstants.FLOAT);
                DataContainer.this.content.add(obj);
            } else if (obj instanceof BooleanContainer || obj instanceof Boolean) {
                this.writeByte(DataContainerConstants.BOOLEAN);
                DataContainer.this.content.add(obj);
            } else {
                throw new SerializationException("I don't know how to write type " + obj.getClass().getName() + " yet");
            }
        }

        public void writeByteDirectly(byte parameter) throws IOException {
            writeByte(parameter);
        }

        public void openObjectDefinition() throws IOException {
            // TODO: -TME Implement
        }

        public void closeObjectDefinition() throws IOException {
            // TODO: -TME Implement
        }

        /* (non-Javadoc)
        * @see java.io.ObjectOutput#write(int)
        */
        public void write(int b) throws IOException {
            outByte.write(b);
        }

        /* (non-Javadoc)
        * @see java.io.ObjectOutput#write(byte[])
        */
        public void write(byte[] b) throws IOException {
            outByte.write(b);
        }

        /* (non-Javadoc)
        * @see java.io.ObjectOutput#write(byte[], int, int)
        */
        public void write(byte[] b, int off, int len) throws IOException {
            outByte.write(b, off, len);

        }

        /* (non-Javadoc)
        * @see java.io.ObjectOutput#flush()
        */
        public void flush() throws IOException {
            flushByteArray();
        }

        /* (non-Javadoc)
        * @see java.io.ObjectOutput#close()
        */
        public void close() throws IOException {
            flush();

        }

        /* (non-Javadoc)
        * @see java.io.DataOutput#writeBoolean(boolean)
        */
        public void writeBoolean(boolean v) throws IOException {
            content.add(BooleanContainer.valueOf(v));
        }

        /* (non-Javadoc)
        * @see java.io.DataOutput#writeByte(int)
        */
        public void writeByte(int v) throws IOException {
            // createByteArray();
            outByte.write(v);
        }

        /* (non-Javadoc)
        * @see java.io.DataOutput#writeShort(int)
        */
        public void writeShort(int v) throws IOException {
            // DataContainer.this.content.add(Short.valueOf((short)v));
            DataContainer.this.content.add(new ShortContainer((short) v));
        }

        /* (non-Javadoc)
        * @see java.io.DataOutput#writeChar(int)
        */
        public void writeChar(int v) throws IOException {
            // flush();
            DataContainer.this.content.add(new CharacterContainer((char) v));
        }

        /* (non-Javadoc)
        * @see java.io.DataOutput#writeInt(int)
        */
        public void writeInt(int v) throws IOException {
            // flush();
            // DataContainer.this.content.add(Integer.valueOf(v));
            DataContainer.this.content.add(new IntegerContainer(v));
        }

        /* (non-Javadoc)
        * @see java.io.DataOutput#writeLong(long)
        */
        public void writeLong(long v) throws IOException {
            // flush();
            // DataContainer.this.content.add(Long.valueOf(v));
            DataContainer.this.content.add(new LongContainer(v));
        }

        /* (non-Javadoc)
        * @see java.io.DataOutput#writeFloat(float)
        */
        public void writeFloat(float v) throws IOException {
            // flush();
            // DataContainer.this.content.add(Float.valueOf(v));
            DataContainer.this.content.add(new FloatContainer(v));
        }

        /* (non-Javadoc)
        * @see java.io.DataOutput#writeDouble(double)
        */
        public void writeDouble(double v) throws IOException {
            // flush();
            // DataContainer.this.content.add(Double.valueOf(v));
            DataContainer.this.content.add(new DoubleContainer(v));
        }

        /* (non-Javadoc)
        * @see java.io.DataOutput#writeBytes(java.lang.String)
        */
        public void writeBytes(String s) throws IOException {
            // createByteArray();

            char[] chars = s.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                this.write(chars[i]);
            }
        }

        /* (non-Javadoc)
        * @see java.io.DataOutput#writeChars(java.lang.String)
        */
        public void writeChars(String s) throws IOException {
            // createByteArray();
            char[] chars = s.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                this.writeChar(chars[i]);
            }
        }

        /* (non-Javadoc)
        * @see java.io.DataOutput#writeUTF(java.lang.String)
        */
        public void writeUTF(String str) throws IOException {
            // flush();
            DataContainer.this.content.add(str);
        }

        public boolean isCheckSerializableClass() {
            return DataContainer.this.cache.isCheckSerializableClass();
        }

    }

    class DataContainerDirectInput implements ObjectsCache.JBossSeralizationInputInterface {
        DataInputStream dataInp;

        public DataContainerDirectInput(DataInputStream dataInp) {
            this.dataInp = dataInp;
        }

        public int readObjectReference() throws IOException {
            return this.readInt();
        }

        public byte readByteDirectly() throws IOException {
            return (byte) this.read();
        }

        public Object readObject() throws ClassNotFoundException, IOException {
            DataContainer.this.cache.setInput(this);
            return ObjectDescriptorFactory.objectFromDescription(DataContainer.this.cache, this);
        }

        /**
         * Reads a byte of data. This method will block if no input is
         * available.
         *
         * @return the byte read, or -1 if the end of the
         *         stream is reached.
         * @throws java.io.IOException If an I/O error has occurred.
         */
        public int read() throws IOException {
            return dataInp.read();
        }

        /**
         * Skips n bytes of input.
         *
         * @param n the number of bytes to be skipped
         * @return the actual number of bytes skipped.
         * @throws java.io.IOException If an I/O error has occurred.
         */
        public long skip(long n) throws IOException {
            return dataInp.skip(n);
        }

        /**
         * Returns the number of bytes that can be read
         * without blocking.
         *
         * @return the number of available bytes.
         * @throws java.io.IOException If an I/O error has occurred.
         */
        public int available() throws IOException {
            return dataInp.available();
        }

        /**
         * Closes the input stream. Must be called
         * to release any resources associated with
         * the stream.
         *
         * @throws java.io.IOException If an I/O error has occurred.
         */
        public void close() throws IOException {
            dataInp.close();
        }

        public int read(byte[] b) throws IOException {
            return dataInp.read(b);
        }

        public int read(byte[] b, int off, int len) throws IOException {
            return dataInp.read(b, off, len);
        }

        public void readFully(byte[] b) throws IOException {
            dataInp.readFully(b);
        }

        public void readFully(byte[] b, int off, int len) throws IOException {
            dataInp.readFully(b, off, len);
        }

        public int skipBytes(int n) throws IOException {
            return dataInp.skipBytes(n);
        }

        public boolean readBoolean() throws IOException {
            return dataInp.readBoolean();
        }

        public byte readByte() throws IOException {
            return dataInp.readByte();
        }

        public int readUnsignedByte() throws IOException {
            return dataInp.readUnsignedByte();
        }

        public short readShort() throws IOException {
            return dataInp.readShort();
        }

        public int readUnsignedShort() throws IOException {
            return dataInp.readUnsignedShort();
        }

        public char readChar() throws IOException {
            return dataInp.readChar();
        }

        public int readInt() throws IOException {
            return dataInp.readInt();
        }

        public long readLong() throws IOException {
            return dataInp.readLong();
        }

        public float readFloat() throws IOException {
            return dataInp.readFloat();
        }

        public double readDouble() throws IOException {
            return dataInp.readDouble();
        }

        @SuppressWarnings({"deprecation"})
        public String readLine() throws IOException {
            return dataInp.readLine();
        }

        public String readUTF() throws IOException {
            // return dataInp.readUTF();
            return StringUtil.readString(dataInp, cache.getStringBuffer());
        }

        public String readUTF(DataInput in) throws IOException {
            // return dataInp.readUTF(in);
            return StringUtil.readString(in, cache.getStringBuffer());
        }

        public Object readImmutable(byte byteDescription, ObjectsCache cache) throws IOException {

            Object retObject = null;
            int reference = this.readObjectReference();
            switch (byteDescription) {
                case DataContainerConstants.IMMUTABLE_OBJREF:
                    retObject = cache.findObjectInCacheRead(reference);
                    if (retObject == null) {
                        throw new IOException("reference " + reference + " not found no readImmutable");
                    }
                    break;
                case DataContainerConstants.STRING:
                    retObject = StringUtil.readString(this, cache.getStringBuffer());
                    break;
                case DataContainerConstants.BYTE:
                    retObject = new Byte(this.readByte());
                    break;
                case DataContainerConstants.CHARACTER:
                    retObject = new Character(this.readChar());
                    break;
                case DataContainerConstants.SHORT:
                    retObject = new Short(this.readShort());
                    break;
                case DataContainerConstants.INTEGER:
                    retObject = new Integer(this.readInt());
                    break;
                case DataContainerConstants.LONG:
                    retObject = new Long(this.readLong());
                    break;
                case DataContainerConstants.DOUBLE:
                    retObject = new Double(this.readDouble());
                    break;
                case DataContainerConstants.FLOAT:
                    retObject = new Float(this.readFloat());
                    break;
                case DataContainerConstants.BOOLEAN:
                    retObject = new Boolean(this.readBoolean());
                    break;
            }

            if (byteDescription != DataContainerConstants.IMMUTABLE_OBJREF) {
                cache.putObjectInCacheRead(reference, retObject);
            }

            return retObject;
        }

    }

    class DataContainerInput implements ObjectsCache.JBossSeralizationInputInterface {
        int position = -1;

        Object currentObject = null;

        ByteArrayInputStream byteStreamInput;

        public DataContainerInput() {
            byteStreamInput = new ByteArrayInputStream(DataContainer.this.getControlStreaming());
        }

        public void reset() {
            position = -1;
            byteStreamInput = null;
        }

        boolean moveNext() throws EOFException {
            position++;

            int size = content.size();

            if (position >= size) {
                throw new EOFException("Unexpected end of repository");
            }

            currentObject = content.get(position);

            return position < size;
        }

        /* (non-Javadoc)
        * @see java.io.ObjectInput#readObject()
        */
        public Object readObject() throws ClassNotFoundException, IOException {
            DataContainer.this.cache.setInput(this);
            return ObjectDescriptorFactory.objectFromDescription(DataContainer.this.cache, this);
        }

        public int readObjectReference() throws IOException {
            moveNext();
            return ((IntegerContainer) currentObject).getValue();
        }

        public byte readByteDirectly() throws IOException {
            return readByte();
        }

        /*void testNextBytes() throws IOException
        {
            if(byteStreamInput==null)
            {
                moveNext();
            }

            if (this.byteStreamInput==null)
            {
                throw new SerializationException("Excepted a byteBuffer in the sequence");
            }
        }*/

        /* (non-Javadoc)
        * @see java.io.ObjectInput#read()
        */
        public int read() throws IOException {
            // testNextBytes();
            return byteStreamInput.read();
        }

        /* (non-Javadoc)
        * @see java.io.ObjectInput#read(byte[])
        */
        public int read(byte[] b) throws IOException {
            // testNextBytes();
            return byteStreamInput.read(b);
        }

        /* (non-Javadoc)
        * @see java.io.ObjectInput#read(byte[], int, int)
        */
        public int read(final byte[] b, final int off, final int len) throws IOException {
            // testNextBytes();
            return byteStreamInput.read(b, off, len);
        }

        /* (non-Javadoc)
        * @see java.io.ObjectInput#skip(long)
        */
        public long skip(long n) throws IOException {
            // testNextBytes();
            return byteStreamInput.skip(n);
        }

        /* (non-Javadoc)
        * @see java.io.ObjectInput#available()
        */
        public int available() throws IOException {
            // testNextBytes();
            return byteStreamInput.available();
        }

        /* (non-Javadoc)
        * @see java.io.ObjectInput#close()
        */
        public void close() throws IOException {
        }

        /* (non-Javadoc)
        * @see java.io.DataInput#readFully(byte[])
        */
        public void readFully(byte[] b) throws IOException {
            // testNextBytes();
            byteStreamInput.read(b);
        }

        /* (non-Javadoc)
        * @see java.io.DataInput#readFully(byte[], int, int)
        */
        public void readFully(byte[] b, int off, int len) throws IOException {
            // testNextBytes();
            byteStreamInput.read(b, off, len);

        }

        /* (non-Javadoc)
        * @see java.io.DataInput#skipBytes(int)
        */
        public int skipBytes(int n) throws IOException {
            // testNextBytes();
            return (int) byteStreamInput.skip(n);
        }

        /* (non-Javadoc)
        * @see java.io.DataInput#readBoolean()
        */
        public boolean readBoolean() throws IOException {
            moveNext();

            try {
                return ((BooleanContainer) currentObject).getValue();
            } catch (ClassCastException e) {
                throw new SerializationException("Excepted to be boolean", e);
            }
        }

        /* (non-Javadoc)
        * @see java.io.DataInput#readByte()
        */
        public byte readByte() throws IOException {
            // testNextBytes();

            return (byte) byteStreamInput.read();
        }

        /* (non-Javadoc)
        * @see java.io.DataInput#readUnsignedByte()
        */
        public int readUnsignedByte() throws IOException {
            // testNextBytes();

            return byteStreamInput.read();
        }

        /* (non-Javadoc)
        * @see java.io.DataInput#readShort()
        */
        public short readShort() throws IOException {
            moveNext();

            try {
                return ((ShortContainer) currentObject).getValue();
            } catch (ClassCastException e) {
                throw new SerializationException("Excepted to be short", e);
            }
        }

        /* (non-Javadoc)
        * @see java.io.DataInput#readUnsignedShort()
        */
        public int readUnsignedShort() throws IOException {
            moveNext();

            try {
                return ((ShortContainer) currentObject).getValue();
            } catch (ClassCastException e) {
                throw new SerializationException("Excepted to be short", e);
            }
        }

        /* (non-Javadoc)
        * @see java.io.DataInput#readChar()
        */
        public char readChar() throws IOException {
            moveNext();

            try {
                return ((CharacterContainer) currentObject).getValue();
            } catch (ClassCastException e) {
                throw new SerializationException("Excepted to be char", e);
            }
        }

        /* (non-Javadoc)
        * @see java.io.DataInput#readInt()
        */
        public int readInt() throws IOException {
            moveNext();

            try {
                return ((IntegerContainer) currentObject).getValue();
            } catch (ClassCastException e) {
                throw new SerializationException("Excepted to be int", e);
            }
        }

        /* (non-Javadoc)
        * @see java.io.DataInput#readLong()
        */
        public long readLong() throws IOException {
            moveNext();

            try {
                return ((LongContainer) currentObject).getValue();
            } catch (ClassCastException e) {
                throw new SerializationException("Excepted to be long", e);
            }
        }

        /* (non-Javadoc)
        * @see java.io.DataInput#readFloat()
        */
        public float readFloat() throws IOException {
            moveNext();

            try {
                return ((FloatContainer) currentObject).getValue();
            } catch (ClassCastException e) {
                throw new SerializationException("Excepted to be float", e);
            }
        }

        /* (non-Javadoc)
        * @see java.io.DataInput#readDouble()
        */
        public double readDouble() throws IOException {
            moveNext();

            try {
                return ((DoubleContainer) currentObject).getValue();
            } catch (ClassCastException e) {
                throw new SerializationException("Excepted to be double", e);
            }
        }

        /* (non-Javadoc)
        * @see java.io.DataInput#readLine()
        */
        public String readLine() throws IOException {
            return readLine();
        }

        /* (non-Javadoc)
        * @see java.io.DataInput#readUTF()
        */
        public String readUTF() throws IOException {
            moveNext();

            try {
                return (String) currentObject;
            } catch (ClassCastException e) {
                throw new SerializationException("Excepted to be String", e);
            }
        }

        public Object readImmutable(byte byteDescription, ObjectsCache cache) throws IOException {

            moveNext();
            return currentObject;
        }

    }

    /**
     * @return Returns the cache.
     */
    public ObjectsCache getCache() {
        return cache;
    }

    /**
     * Sends this data over the wire to a streaming.
     */
    public void saveData(DataOutput output) throws IOException {
        // This only flushes internal buffers from sub models, it doesn't flush to the external Streams.
        this.flush();
        output.writeInt(getControlStreaming().length);
        output.write(getControlStreaming());
        writeMyself(output);
        // this.cache.writeMyself(output);
        /*if (output instanceof DataOutputStream)
        {
            ((DataOutputStream)output).flush();
        }*/
        output.write(DataContainerConstants.closeSign);
    }

    private void writeInteger(DataOutput output, Object obj) throws IOException {
        if (obj instanceof IntegerContainer) {
            output.writeByte(INTEGER);
            output.writeInt(((IntegerContainer) obj).getValue());
        } else {
            output.writeByte(INTEGEROBJ);
            output.writeInt(((Integer) obj).intValue());
        }
    }

    /**
     * @param output
     * @param obj
     */
    private void writeDouble(DataOutput output, Object obj) throws IOException {
        if (obj instanceof DoubleContainer) {
            output.writeByte(DOUBLE);
            output.writeDouble(((DoubleContainer) obj).getValue());
        } else {
            output.writeByte(DOUBLEOBJ);
            output.writeDouble(((Double) obj).doubleValue());
        }
    }

    private void saveString(DataOutput output, Object obj) throws IOException {
        output.writeByte(STRING);
        StringUtil.saveString(output, (String) obj, cache.getStringBuffer());
    }

    /**
     * this reads data from a streaming.
     */
    public void loadData(DataInput input) throws IOException {
        int size = input.readInt();
        byte byteControl[] = new byte[size];
        input.readFully(byteControl);
        this.setControlStreaming(byteControl);

        readMyself(input);
        // this.cache.readMyself(input);

    }

    private boolean compareBuffer(byte[] buffer1, byte[] buffer2) {
        for (int i = 0; i < buffer1.length; i++) {
            if (buffer1[i] != buffer2[i]) {
                return false;
            }
        }
        return true;

    }

    /* (non-Javadoc)
    * @see org.jboss.serial.objectmetamodel.DataExport#writeMyself(java.io.DataOutput)
    */
    public void writeMyself(DataOutput output) throws IOException {
        if (currentOutput != null && currentOutput.outByte != null) {
            currentOutput.flushByteArray();
        }

        output.writeInt(content.size());
        Iterator iter = content.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            if (obj == null) {
                output.writeByte(NULLREF); // read is implemented
            } else if (obj instanceof String) {
                saveString(output, obj); // read is implemented
            } else if (obj instanceof ByteContainer || obj instanceof Byte) {
                // Although this was not supposed to happen as we will use array of bytes, we still want to keep it just in
                // case
                writeByte(output, obj); // read is implemented
            } else if (obj instanceof CharacterContainer || obj instanceof Character) {
                writeCharacter(output, obj); // read is implemented
            } else if (obj instanceof ShortContainer || obj instanceof Short) {
                writeShort(output, obj); // read is implemented
            } else if (obj instanceof IntegerContainer || obj instanceof Integer) {
                writeInteger(output, obj); // read is implemented
            } else if (obj instanceof LongContainer || obj instanceof Long) {
                writeLong(output, obj); // read is implemented
            } else if (obj instanceof DoubleContainer || obj instanceof Double) {
                writeDouble(output, obj); // read is implemented
            } else if (obj instanceof FloatContainer || obj instanceof Float) {
                writeFloat(output, obj); // read is implemented
            } else if (obj instanceof BooleanContainer || obj instanceof Boolean) {
                writeBoolean(output, obj);
            } else if (obj instanceof byte[]) {
                writeByteArray(output, obj);
            } else {
                throw new SerializationException("I don't know how to write type " + obj.getClass().getName() + " yet");
            }
        }
    }

    /**
     * @param output
     * @param obj
     */
    private void writeFloat(DataOutput output, Object obj) throws IOException {
        if (obj instanceof FloatContainer) {
            output.writeByte(FLOAT);
            output.writeFloat(((FloatContainer) obj).getValue());
        } else {
            output.writeByte(FLOATOBJ);
            output.writeFloat(((Float) obj).floatValue());
        }
    }

    private void writeByteArray(DataOutput output, Object obj) throws IOException {
        output.writeByte(BYTEARRAY);
        output.writeInt(((byte[]) obj).length);
        output.write((byte[]) obj);
    }

    /**
     * @param output
     * @param obj
     */
    private void writeBoolean(DataOutput output, Object obj) throws IOException {
        if (obj instanceof BooleanContainer) {
            output.writeByte(BOOLEAN);
            output.writeBoolean(((BooleanContainer) obj).getValue());
        } else {
            output.writeByte(BOOLEANOBJ);
            output.writeBoolean(((Boolean) obj).booleanValue());
        }
    }

    /**
     * @param output
     * @param obj
     */
    private void writeLong(DataOutput output, Object obj) throws IOException {
        if (obj instanceof LongContainer) {
            output.writeByte(LONG);
            output.writeLong(((LongContainer) obj).getValue());
        } else {
            output.writeByte(LONGOBJ);
            output.writeLong(((Long) obj).longValue());
        }
    }

    /**
     * @param output
     * @param obj
     */
    private void writeShort(DataOutput output, Object obj) throws IOException {
        if (obj instanceof ShortContainer) {
            output.writeByte(SHORT);
            output.writeShort(((ShortContainer) obj).getValue());
        } else {
            output.writeByte(SHORTOBJ);
            output.writeShort(((Short) obj).shortValue());
        }
    }

    /**
     * @param output
     * @param obj
     */
    private void writeCharacter(DataOutput output, Object obj) throws IOException {
        if (obj instanceof Character) {
            output.writeByte(CHARACTEROBJ);
            output.writeChar(((Character) obj).charValue());
        } else {
            output.writeByte(CHARACTER);
            output.writeChar(((CharacterContainer) obj).getValue());
        }
    }

    /**
     * This is probably never used as we always use byteArrays for bytes, due to our implementation in the DataOutput.
     *
     * @param output
     * @param obj
     */
    private void writeByte(DataOutput output, Object obj) throws IOException {
        if (obj instanceof ByteContainer) {
            output.writeByte(BYTE);
            output.writeByte(((ByteContainer) obj).getValue());
        } else {
            output.writeByte(BYTEOBJ);
            output.writeByte(((Byte) obj).byteValue());
        }
    }

    /* (non-Javadoc)
    * @see org.jboss.serial.objectmetamodel.DataExport#readMyself(java.io.DataInput)
    */
    public void readMyself(DataInput input) throws IOException {
        int size = input.readInt();

        content.clear();

        for (int i = 0; i < size; i++) {
            byte type = input.readByte();

            switch (type) {
                case NULLREF:
                    content.add(null);
                    break;
                case STRING:
                    content.add(StringUtil.readString(input, cache.getStringBuffer()));
                    break;
                case BYTE:
                    content.add(new ByteContainer(input.readByte()));
                    break;
                case BYTEOBJ:
                    content.add(new Byte(input.readByte()));
                    break;
                case SHORT:
                    content.add(new ShortContainer(input.readShort()));
                    break;
                case SHORTOBJ:
                    content.add(new Short(input.readShort()));
                    break;
                case INTEGER:
                    content.add(new IntegerContainer(input.readInt()));
                    break;
                case INTEGEROBJ:
                    content.add(new Integer(input.readInt()));
                    break;
                case LONG:
                    content.add(new LongContainer(input.readLong()));
                    break;
                case LONGOBJ:
                    content.add(new Long(input.readLong()));
                    break;
                case FLOAT:
                    content.add(new FloatContainer(input.readFloat()));
                    break;
                case FLOATOBJ:
                    content.add(new Float(input.readFloat()));
                    break;
                case DOUBLE:
                    content.add(new DoubleContainer(input.readDouble()));
                    break;
                case DOUBLEOBJ:
                    content.add(new Double(input.readDouble()));
                    break;
                case CHARACTER:
                    content.add(new CharacterContainer(input.readChar()));
                    break;
                case CHARACTEROBJ:
                    content.add(new Character(input.readChar()));
                    break;
                case BOOLEAN:
                    content.add(BooleanContainer.valueOf(input.readBoolean()));
                    break;
                case BOOLEANOBJ:
                    content.add(Boolean.valueOf(input.readBoolean()));
                    break;
                case BYTEARRAY:
                    int sizebArray = input.readInt();
                    byte[] barray = new byte[sizebArray];
                    input.readFully(barray);
                    content.add(barray);
                    break;
                default:
                    throw new SerializationException("I don't know how to read type " + type + " yet");
            }
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        this.saveData(out);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.loadData(in);
    }

    public byte[] getControlStreaming() {
        if (controlStreaming == null) {
            controlStreaming = new byte[0];
        }
        return controlStreaming;
    }

    public void setControlStreaming(byte[] controlStreaming) {
        this.controlStreaming = controlStreaming;
    }

    public ClassResolver getClassResolver() {
        return cache.getClassResolver();
    }

    public void setClassResolver(ClassResolver resolver) {
        cache.setClassResolver(resolver);
    }

    public StringUtilBuffer getStringBuffer() {
        return cache.getStringBuffer();
    }

    public void setStringBuffer(StringUtilBuffer stringBuffer) {
        cache.setStringBuffer(stringBuffer);
    }

}