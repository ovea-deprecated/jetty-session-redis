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

import java.lang.reflect.Field;

/**
 * $Id: FieldsManager.java 217 2006-04-18 18:42:42Z csuconic $
 * <p/>
 * FieldsManager is the class responsible to manage changing the fields.
 * It will be up to implementations of this class to decide wether we should use Unsafe operations or
 * pure reflection
 *
 * @author <a href="mailto:clebert.suconic@jboss.com">Clebert Suconic</a>
 */
public abstract class FieldsManager {

    /**
     * We need to test if Reflection could be used to change final fields or not.
     * In case negative we will use UnsafeFieldsManager and this class will be used to execute this test
     */
    private static class InternalFinalFieldTestClass {
        final int x = 0;
    }

    private static FieldsManager fieldsManager;

    static {
        if (UnsafeFieldsManager.isSupported()) {
            fieldsManager = new UnsafeFieldsManager();
        } else {
            try {
                Field fieldX = InternalFinalFieldTestClass.class.getDeclaredField("x");
                fieldX.setAccessible(true);

                InternalFinalFieldTestClass fieldTest = new InternalFinalFieldTestClass();
                fieldX.setInt(fieldTest, 33);


                fieldsManager = new ReflectionFieldsManager();

            } catch (Throwable e) {
            }
        }
        if (fieldsManager == null) {
            System.err.println("Couldn't set FieldsManager, JBoss Serialization can't work properly on this VM");
        }

    }

    public static FieldsManager getFieldsManager() {
        return fieldsManager;
    }


    public abstract void fillMetadata(ClassMetadataField field);

    public abstract void setInt(Object obj, ClassMetadataField field, int value);

    public abstract int getInt(Object obj, ClassMetadataField field);

    public abstract void setByte(Object obj, ClassMetadataField field, byte value);

    public abstract byte getByte(Object obj, ClassMetadataField field);

    public abstract void setLong(Object obj, ClassMetadataField field, long value);

    public abstract long getLong(Object obj, ClassMetadataField field);

    public abstract void setFloat(Object obj, ClassMetadataField field, float value);

    public abstract float getFloat(Object obj, ClassMetadataField field);

    public abstract void setDouble(Object obj, ClassMetadataField field, double value);

    public abstract double getDouble(Object obj, ClassMetadataField field);

    public abstract void setShort(Object obj, ClassMetadataField field, short value);

    public abstract short getShort(Object obj, ClassMetadataField field);

    public abstract void setCharacter(Object obj, ClassMetadataField field, char value);

    public abstract char getCharacter(Object obj, ClassMetadataField field);

    public abstract void setBoolean(Object obj, ClassMetadataField field, boolean value);

    public abstract boolean getBoolean(Object obj, ClassMetadataField field);

    public abstract void setObject(Object obj, ClassMetadataField field, Object value);

    public abstract Object getObject(Object obj, ClassMetadataField field);

}
