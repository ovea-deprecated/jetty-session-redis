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

package com.ovea.jetty.session.serializer.jboss.serial.classmetamodel;

import sun.misc.Unsafe;

import java.io.ObjectStreamClass;
import java.lang.reflect.Field;

/**
 * $Id: UnsafeFieldsManager.java 217 2006-04-18 18:42:42Z csuconic $
 * <p/>
 * This FieldsManager uses the only hook available to change final fields into JVM 1.4 (operations with sun.misc.Unsafe).
 * As this is a reflection functionality, we've gotten the Unsafe instance used by java.io.ObjectStreamClass
 *
 * @author <a href="mailto:clebert.suconic@jboss.com">Clebert Suconic</a>
 */
public class UnsafeFieldsManager extends FieldsManager {
    UnsafeFieldsManager() {
    }

    static Unsafe unsafe;

    public static boolean isSupported() {
        return unsafe != null;
    }

    static {
        try {
            Class[] classes = ObjectStreamClass.class.getDeclaredClasses();
            for (int i = 0; i < classes.length; i++) {
                if (classes[i].getName().equals("java.io.ObjectStreamClass$FieldReflector")) {
                    Field unsafeField = classes[i].getDeclaredField("unsafe");
                    unsafeField.setAccessible(true);

                    unsafe = (Unsafe) unsafeField.get(null);
                    break;
                }
            }
        } catch (Throwable e) {
            //e.printStackTrace()
        }
    }

    public void fillMetadata(ClassMetadataField field) {
        if (field.getField() != null) {
            field.setUnsafeKey(unsafe.objectFieldOffset(field.getField()));
        }
    }

    public void setInt(Object obj, ClassMetadataField field, int value) {
        unsafe.putInt(obj, field.getUnsafeKey(), value);
    }

    public int getInt(Object obj, ClassMetadataField field) {
        return unsafe.getInt(obj, field.getUnsafeKey());
    }

    public void setByte(Object obj, ClassMetadataField field, byte value) {
        unsafe.putByte(obj, field.getUnsafeKey(), value);
    }

    public byte getByte(Object obj, ClassMetadataField field) {
        return unsafe.getByte(obj, field.getUnsafeKey());
    }

    public void setLong(Object obj, ClassMetadataField field, long value) {
        unsafe.putLong(obj, field.getUnsafeKey(), value);
    }

    public long getLong(Object obj, ClassMetadataField field) {
        return unsafe.getLong(obj, field.getUnsafeKey());
    }

    public void setFloat(Object obj, ClassMetadataField field, float value) {
        unsafe.putFloat(obj, field.getUnsafeKey(), value);
    }

    public float getFloat(Object obj, ClassMetadataField field) {
        return unsafe.getFloat(obj, field.getUnsafeKey());
    }

    public void setDouble(Object obj, ClassMetadataField field, double value) {
        unsafe.putDouble(obj, field.getUnsafeKey(), value);
    }

    public double getDouble(Object obj, ClassMetadataField field) {
        return unsafe.getDouble(obj, field.getUnsafeKey());
    }

    public void setShort(Object obj, ClassMetadataField field, short value) {
        unsafe.putShort(obj, field.getUnsafeKey(), value);
    }

    public short getShort(Object obj, ClassMetadataField field) {
        return unsafe.getShort(obj, field.getUnsafeKey());
    }

    public void setCharacter(Object obj, ClassMetadataField field, char value) {
        unsafe.putChar(obj, field.getUnsafeKey(), value);
    }

    public char getCharacter(Object obj, ClassMetadataField field) {
        return unsafe.getChar(obj, field.getUnsafeKey());
    }

    public void setBoolean(Object obj, ClassMetadataField field, boolean value) {
        unsafe.putBoolean(obj, field.getUnsafeKey(), value);
    }

    public boolean getBoolean(Object obj, ClassMetadataField field) {
        return unsafe.getBoolean(obj, field.getUnsafeKey());
    }

    public void setObject(Object obj, ClassMetadataField field, Object value) {
        unsafe.putObject(obj, field.getUnsafeKey(), value);
    }

    public Object getObject(Object obj, ClassMetadataField field) {
        return unsafe.getObject(obj, field.getUnsafeKey());
    }

}
