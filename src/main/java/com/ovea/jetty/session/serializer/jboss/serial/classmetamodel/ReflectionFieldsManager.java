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

/**
 * $Id: ReflectionFieldsManager.java 217 2006-04-18 18:42:42Z csuconic $
 *
 * @author <a href="mailto:clebert.suconic@jboss.com">Clebert Suconic</a>
 */
public class ReflectionFieldsManager extends FieldsManager {
    public void fillMetadata(ClassMetadataField field) {
        if (field.getField() != null) {
            field.getField().setAccessible(true);
        }
    }

    public void setInt(Object obj, ClassMetadataField field, int value) {
        try {
            field.getField().setInt(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getInt(Object obj, ClassMetadataField field) {
        try {
            return field.getField().getInt(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setByte(Object obj, ClassMetadataField field, byte value) {
        try {
            field.getField().setByte(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte getByte(Object obj, ClassMetadataField field) {
        try {
            return field.getField().getByte(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setLong(Object obj, ClassMetadataField field, long value) {
        try {
            field.getField().setLong(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public long getLong(Object obj, ClassMetadataField field) {
        try {
            return field.getField().getLong(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setFloat(Object obj, ClassMetadataField field, float value) {
        try {
            field.getField().setFloat(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public float getFloat(Object obj, ClassMetadataField field) {
        try {
            return field.getField().getFloat(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setDouble(Object obj, ClassMetadataField field, double value) {
        try {
            field.getField().setDouble(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public double getDouble(Object obj, ClassMetadataField field) {
        try {
            return field.getField().getDouble(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setShort(Object obj, ClassMetadataField field, short value) {
        try {
            field.getField().setShort(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public short getShort(Object obj, ClassMetadataField field) {
        try {
            return field.getField().getShort(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setCharacter(Object obj, ClassMetadataField field, char value) {
        try {
            field.getField().setChar(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public char getCharacter(Object obj, ClassMetadataField field) {
        try {
            return field.getField().getChar(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setBoolean(Object obj, ClassMetadataField field, boolean value) {
        try {
            field.getField().setBoolean(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean getBoolean(Object obj, ClassMetadataField field) {
        try {
            return field.getField().getBoolean(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setObject(Object obj, ClassMetadataField field, Object value) {
        try {
            field.getField().set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object getObject(Object obj, ClassMetadataField field) {
        try {
            return field.getField().get(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
