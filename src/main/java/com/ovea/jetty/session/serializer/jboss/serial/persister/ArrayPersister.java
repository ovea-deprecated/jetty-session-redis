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

import com.ovea.jetty.session.serializer.jboss.serial.classmetamodel.ClassMetaData;
import com.ovea.jetty.session.serializer.jboss.serial.classmetamodel.StreamingClass;
import com.ovea.jetty.session.serializer.jboss.serial.exception.SerializationException;
import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.ObjectSubstitutionInterface;
import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.ObjectsCache;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Array;

/**
 * $Id: ArrayPersister.java 231 2006-04-24 23:49:41Z csuconic $
 *
 * @author Clebert Suconic
 */
public class ArrayPersister implements Persister {
    byte id;

    public byte getId() {
        return id;
    }

    public void setId(byte id) {
        this.id = id;
    }

    /* (non-Javadoc)
    * @see org.jboss.serial.persister.Persister
    */
    public void writeData(ClassMetaData metaData, ObjectOutput out, Object obj, ObjectSubstitutionInterface substitution) throws IOException {
        //ClassMetaData metaData = ClassMetamodelFactory.getClassMetaData(obj.getClass(),data.getCache().isCheckSerializableClass());
        //final int depth = metaData.getArrayDepth();

        if (metaData.getArrayDepth() == 1
                && metaData.getClazz().isPrimitive()) {
            Class clazz = metaData.getClazz();
            if (clazz == Integer.TYPE) {
                final int[] finalArray = (int[]) obj;
                out.writeInt(finalArray.length);
                for (int i = 0; i < finalArray.length; i++) out.writeInt(finalArray[i]);
            } else if (clazz == Byte.TYPE) {
                final byte[] finalArray = (byte[]) obj;
                out.writeInt(finalArray.length);
                out.write(finalArray);
            } else if (clazz == Long.TYPE) {
                final long[] finalArray = (long[]) obj;
                out.writeInt(finalArray.length);
                for (int i = 0; i < finalArray.length; i++) out.writeLong(finalArray[i]);
            } else if (clazz == Float.TYPE) {
                float[] finalArray = (float[]) obj;
                out.writeInt(finalArray.length);
                for (int i = 0; i < finalArray.length; i++) out.writeFloat(finalArray[i]);
            } else if (clazz == Double.TYPE) {
                double[] finalArray = (double[]) obj;
                out.writeInt(finalArray.length);
                for (int i = 0; i < finalArray.length; i++) out.writeDouble(finalArray[i]);
            } else if (clazz == Short.TYPE) {
                short[] finalArray = (short[]) obj;
                out.writeInt(finalArray.length);
                for (int i = 0; i < finalArray.length; i++) out.writeShort(finalArray[i]);
            } else if (clazz == Character.TYPE) {
                char[] finalArray = (char[]) obj;
                out.writeInt(finalArray.length);
                for (int i = 0; i < finalArray.length; i++) out.writeChar(finalArray[i]);
            } else if (clazz == Boolean.TYPE) {
                boolean[] finalArray = (boolean[]) obj;
                out.writeInt(finalArray.length);
                for (int i = 0; i < finalArray.length; i++) out.writeBoolean(finalArray[i]);
            } else {
                throw new RuntimeException("Unexpected datatype " + clazz.getName());
            }
        } else {
            saveObjectArray(obj, out);
        }
    }

    private void saveObjectArray(Object obj, ObjectOutput out) throws IOException {
        Object objs[] = (Object[]) obj;
        out.writeInt(objs.length);
        for (int i = 0; i < objs.length; i++) {
            out.writeObject(objs[i]);
        }
    }

    /* (non-Javadoc)
     * @see org.jboss.serial.persister.Persister
     */
    public Object readData(ClassLoader loader, StreamingClass streaming, ClassMetaData metaData, int referenceId, ObjectsCache cache, ObjectInput input, ObjectSubstitutionInterface substitution) throws IOException {
        try {
            final int length = input.readInt();

            if (metaData.getArrayDepth() == 1
                    && metaData.getClazz().isPrimitive()) {
                Class clazz = metaData.getClazz();
                if (clazz == Integer.TYPE) {
                    int[] finalArray = new int[length];
                    cache.putObjectInCacheRead(referenceId, finalArray);
                    for (int i = 0; i < finalArray.length; i++) finalArray[i] = input.readInt();
                    return finalArray;
                } else if (clazz == Byte.TYPE) {
                    byte[] finalArray = new byte[length];
                    cache.putObjectInCacheRead(referenceId, finalArray);
                    input.readFully(finalArray);
                    return finalArray;
                } else if (clazz == Long.TYPE) {
                    long[] finalArray = new long[length];
                    cache.putObjectInCacheRead(referenceId, finalArray);
                    for (int i = 0; i < finalArray.length; i++) finalArray[i] = input.readLong();
                    return finalArray;
                } else if (clazz == Float.TYPE) {
                    float[] finalArray = new float[length];
                    cache.putObjectInCacheRead(referenceId, finalArray);
                    for (int i = 0; i < finalArray.length; i++) finalArray[i] = input.readFloat();
                    return finalArray;
                } else if (clazz == Double.TYPE) {
                    double[] finalArray = new double[length];
                    cache.putObjectInCacheRead(referenceId, finalArray);
                    for (int i = 0; i < finalArray.length; i++) finalArray[i] = input.readDouble();
                    return finalArray;
                } else if (clazz == Short.TYPE) {
                    short[] finalArray = new short[length];
                    cache.putObjectInCacheRead(referenceId, finalArray);
                    for (int i = 0; i < finalArray.length; i++) finalArray[i] = input.readShort();
                    return finalArray;
                } else if (clazz == Character.TYPE) {
                    char[] finalArray = new char[length];
                    cache.putObjectInCacheRead(referenceId, finalArray);
                    for (int i = 0; i < finalArray.length; i++) finalArray[i] = input.readChar();
                    return finalArray;
                } else if (clazz == Boolean.TYPE) {
                    boolean[] finalArray = new boolean[length];
                    cache.putObjectInCacheRead(referenceId, finalArray);
                    for (int i = 0; i < finalArray.length; i++) finalArray[i] = input.readBoolean();
                    return finalArray;
                } else {
                    throw new RuntimeException("Unexpected datatype " + clazz.getName());
                }
            } else {
                return readObjectArray(metaData, referenceId, cache, length, input);
            }
        } catch (ClassNotFoundException ex) {
            throw new SerializationException(ex);
        }
    }

    private Object readObjectArray(ClassMetaData metaData, int referenceId, ObjectsCache cache, int length, ObjectInput input) throws ClassNotFoundException, IOException {
        final int depth = metaData.getArrayDepth();

        int depthParam[] = new int[metaData.getArrayDepth()];
        depthParam[0] = length;
        for (int i = 1; i < depth; i++) {
            depthParam[i] = 0;
        }
        Object producedArray[] = (Object[]) Array.newInstance(metaData.getClazz(), depthParam);

        cache.putObjectInCacheRead(referenceId, producedArray);

        for (int i = 0; i < length; i++) {
            producedArray[i] = input.readObject();
        }
        return producedArray;
    }

    public boolean canPersist(Object obj) {
        // not implemented
        return false;
    }

}
