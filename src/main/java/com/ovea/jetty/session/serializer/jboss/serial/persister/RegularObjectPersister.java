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

import com.ovea.jetty.session.serializer.jboss.serial.classmetamodel.*;
import com.ovea.jetty.session.serializer.jboss.serial.exception.SerializationException;
import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.FieldsContainer;
import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.ObjectSubstitutionInterface;
import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.ObjectsCache;

import java.io.*;
import java.lang.reflect.Field;

/**
 * This is the persister of a regular object.
 *
 * @author clebert suconic
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public class RegularObjectPersister implements Persister {
    byte id;

    public byte getId() {
        return id;
    }

    public void setId(byte id) {
        this.id = id;
    }

    public void writeData(ClassMetaData metaData, ObjectOutput out, Object obj, ObjectSubstitutionInterface substitution) throws IOException {
        defaultWrite(out, obj, metaData, substitution);
    }

    public static void defaultWrite(ObjectOutput output, Object obj, ClassMetaData metaClass, ObjectSubstitutionInterface substitution) throws IOException {
        ClassMetaDataSlot slots[] = metaClass.getSlots();

        for (int slotNr = 0; slotNr < slots.length; slotNr++) {
            if (slots[slotNr].getPrivateMethodWrite() != null) {
                writeSlotWithMethod(slots[slotNr], output, obj, substitution);
            } else {
                writeSlotWithFields(slots[slotNr], output, obj, substitution);
            }
        }

    }

    private static void readSlotWithMethod(ClassMetaDataSlot slot, short[] fieldsKey, ObjectInput input, Object obj, ObjectSubstitutionInterface substitution) throws IOException {
        try {
            slot.getPrivateMethodRead().invoke(obj, new Object[]{new ObjectInputStreamProxy(input, fieldsKey, obj, slot, substitution)});
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            IOException io = new IOException(e.getMessage());
            io.initCause(e);
            throw io;
        }
    }

    private static void writeSlotWithMethod(ClassMetaDataSlot slot, ObjectOutput out, Object obj, ObjectSubstitutionInterface substitution) throws IOException {
        try {
            slot.getPrivateMethodWrite().invoke(obj, new Object[]{new ObjectOutputStreamProxy(out, obj, slot, substitution)});
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            IOException io = new IOException(e.getMessage());
            io.initCause(e);
            throw io;
        }
    }

    static void writeSlotWithFields(ClassMetaDataSlot slot, ObjectOutput output, Object obj, ObjectSubstitutionInterface substitution) throws IOException {
        ClassMetadataField[] fields = slot.getFields();
        for (int fieldNR = 0; fieldNR < fields.length; fieldNR++) {
            ClassMetadataField field = fields[fieldNR];
            if (field.getField().getType().isPrimitive() && !field.getField().getType().isArray()) {
                writeOnPrimitive(output, obj, field);
            } else {
                Object value = null;
                value = FieldsManager.getFieldsManager().getObject(obj, field);
                output.writeObject(value);
            }
        }
    }

    private static void writeOnPrimitive(final ObjectOutput out, final Object obj, final ClassMetadataField metaField) throws IOException {

        try {
            final Field field = metaField.getField();
            final Class clazz = field.getType();
            if (clazz == Integer.TYPE) {
                out.writeInt(FieldsManager.getFieldsManager().getInt(obj, metaField));
            } else if (clazz == Byte.TYPE) {
                out.writeByte(FieldsManager.getFieldsManager().getByte(obj, metaField));
            } else if (clazz == Long.TYPE) {
                out.writeLong(FieldsManager.getFieldsManager().getLong(obj, metaField));
            } else if (clazz == Float.TYPE) {
                out.writeFloat(FieldsManager.getFieldsManager().getFloat(obj, metaField));
            } else if (clazz == Double.TYPE) {
                out.writeDouble(FieldsManager.getFieldsManager().getDouble(obj, metaField));
            } else if (clazz == Short.TYPE) {
                out.writeShort(FieldsManager.getFieldsManager().getShort(obj, metaField));
            } else if (clazz == Character.TYPE) {
                out.writeChar(field.getChar(obj));
            } else if (clazz == Boolean.TYPE) {
                out.writeBoolean(field.getBoolean(obj));
            } else {
                throw new RuntimeException("Unexpected datatype " + clazz.getName());
            }
        } catch (IllegalAccessException access) {
            IOException io = new IOException(access.getMessage());
            io.initCause(access);
            throw io;
        }
    }

    public Object readData(ClassLoader loader, StreamingClass streaming, ClassMetaData metaData, int referenceId, ObjectsCache cache, ObjectInput input, ObjectSubstitutionInterface substitution) throws IOException {
        Object obj = metaData.newInstance();
        cache.putObjectInCacheRead(referenceId, obj);
        return defaultRead(input, obj, streaming, metaData, substitution);

    }

    public static Object defaultRead(ObjectInput input, Object obj, StreamingClass streaming, ClassMetaData metaData, ObjectSubstitutionInterface substitution) throws IOException {

        try {

            ClassMetaDataSlot[] slots = metaData.getSlots();
            for (int slotNR = 0; slotNR < slots.length; slotNR++) {
                ClassMetaDataSlot slot = metaData.getSlots()[slotNR];
                if (slot.getPrivateMethodRead() != null) {
                    readSlotWithMethod(slot, streaming.getKeyFields()[slotNR], input, obj, substitution);
                }
//        		else if (slot.getPrivateMethodWrite() != null)
//        		{
//        		   readSlotWithDefaultMethod(slot, streaming.getKeyFields()[slotNR], input, obj);
//        		}
                else {
                    readSlotWithFields(streaming.getKeyFields()[slotNR], slot, input, obj);
                }
            }


            return obj;
        } catch (ClassNotFoundException e) {
            throw new SerializationException("Error reading " + obj.getClass().getName(), e);
        }
    }

    static void readSlotWithFields(short fieldsKey[], ClassMetaDataSlot slot, ObjectInput input, Object obj) throws IOException, ClassNotFoundException {
        short numberOfFields = (short) fieldsKey.length;
        for (short i = 0; i < numberOfFields; i++) {
            ClassMetadataField field = slot.getFields()[fieldsKey[i]];
            if (field.getField().getType() == Integer.TYPE) {
                FieldsManager.getFieldsManager().setInt(obj, field, input.readInt());
            } else if (field.getField().getType() == Byte.TYPE) {
                FieldsManager.getFieldsManager().setByte(obj, field, input.readByte());
            } else if (field.getField().getType() == Long.TYPE) {
                FieldsManager.getFieldsManager().setLong(obj, field, input.readLong());
            } else if (field.getField().getType() == Float.TYPE) {
                FieldsManager.getFieldsManager().setFloat(obj, field, input.readFloat());
            } else if (field.getField().getType() == Double.TYPE) {
                FieldsManager.getFieldsManager().setDouble(obj, field, input.readDouble());
            } else if (field.getField().getType() == Short.TYPE) {
                FieldsManager.getFieldsManager().setShort(obj, field, input.readShort());
            } else if (field.getField().getType() == Character.TYPE) {
                FieldsManager.getFieldsManager().setCharacter(obj, field, input.readChar());
            } else if (field.getField().getType() == Boolean.TYPE) {
                FieldsManager.getFieldsManager().setBoolean(obj, field, input.readBoolean());
            } else {
                Object objTmp = input.readObject();
                FieldsManager.getFieldsManager().setObject(obj, field, objTmp);
            }
        }
    }

    static void writeSlotWithDefaultMethod(ClassMetaDataSlot slot, ObjectOutput out, Object obj, ObjectSubstitutionInterface substitution) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStreamProxy(out, obj, slot, substitution);
        ObjectOutputStream.PutField putField = oos.putFields();
        ClassMetadataField[] fields = slot.getFields();
        FieldsManager fieldsManager = FieldsManager.getFieldsManager();

        for (int fieldNR = 0; fieldNR < fields.length; fieldNR++) {
            ClassMetadataField classMetadataField = fields[fieldNR];
            Class type = classMetadataField.getField().getType();
            String fieldName = classMetadataField.getFieldName();

            if (type == Boolean.TYPE) {
                putField.put(fieldName, fieldsManager.getBoolean(obj, classMetadataField));
            } else if (type == Byte.TYPE) {
                putField.put(fieldName, fieldsManager.getByte(obj, classMetadataField));
            } else if (type == Character.TYPE) {
                putField.put(fieldName, fieldsManager.getCharacter(obj, classMetadataField));
            } else if (type == Short.TYPE) {
                putField.put(fieldName, fieldsManager.getShort(obj, classMetadataField));
            } else if (type == Integer.TYPE) {
                putField.put(fieldName, fieldsManager.getInt(obj, classMetadataField));
            } else if (type == Long.TYPE) {
                putField.put(fieldName, fieldsManager.getLong(obj, classMetadataField));
            } else if (type == Float.TYPE) {
                putField.put(fieldName, fieldsManager.getFloat(obj, classMetadataField));
            } else if (type == Double.TYPE) {
                putField.put(fieldName, fieldsManager.getDouble(obj, classMetadataField));
            } else {
                Object value = fieldsManager.getObject(obj, classMetadataField);
                putField.put(fieldName, value);
            }
        }

        oos.writeFields();
    }

    static void readSlotWithDefaultMethod(ClassMetaDataSlot slot, short[] fieldsKey, ObjectInput input, Object obj) throws IOException, ClassNotFoundException {
        FieldsContainer container = new FieldsContainer(slot);
        container.readMyself(input);
        ClassMetadataField[] fields = slot.getFields();
        ObjectInputStream.GetField getField = container.createGet();
        short numberOfFields = (short) fieldsKey.length;

        for (short i = 0; i < numberOfFields; i++) {
            ClassMetadataField classMetadata = slot.getFields()[fieldsKey[i]];
            Field field = classMetadata.getField();
            Class type = field.getType();
            String name = classMetadata.getFieldName();
            FieldsManager fieldsManager = FieldsManager.getFieldsManager();

            if (type == Boolean.TYPE) {
                fieldsManager.setBoolean(obj, classMetadata, getField.get(name, false));
            } else if (type == Byte.TYPE) {
                fieldsManager.setByte(obj, classMetadata, getField.get(name, (byte) 0));
            } else if (type == Character.TYPE) {
                fieldsManager.setCharacter(obj, classMetadata, getField.get(name, (char) 0));
            } else if (type == Short.TYPE) {
                fieldsManager.setShort(obj, classMetadata, getField.get(name, (short) 0));
            } else if (type == Integer.TYPE) {
                fieldsManager.setInt(obj, classMetadata, getField.get(name, (int) 0));
            } else if (type == Long.TYPE) {
                fieldsManager.setLong(obj, classMetadata, getField.get(name, (long) 0));
            } else if (type == Float.TYPE) {
                fieldsManager.setFloat(obj, classMetadata, getField.get(name, (float) 0));
            } else if (type == Double.TYPE) {
                fieldsManager.setDouble(obj, classMetadata, getField.get(name, (double) 0));
            } else {
                fieldsManager.setObject(obj, classMetadata, getField.get(name, null));
            }
        }
    }

    public boolean canPersist(Object obj) {
        // not implemented
        return false;
    }

}
