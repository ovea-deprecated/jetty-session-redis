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

import com.ovea.jetty.session.serializer.jboss.serial.references.MethodPersistentReference;
import com.ovea.jetty.session.serializer.jboss.serial.references.PersistentReference;
import com.ovea.jetty.session.serializer.jboss.serial.util.ClassMetaConsts;
import com.ovea.jetty.session.serializer.jboss.serial.util.HashStringUtil;
import gnu.trove.TLongObjectHashMap;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;

public class ClassMetaDataSlot implements ClassMetaConsts {

    public ClassMetaDataSlot(Class slotClass) {
        this.slotClass = new WeakReference(slotClass);
        this.name = slotClass.getName();
        this.shaHash = HashStringUtil.hashName(this.name);

        if (!Serializable.class.isAssignableFrom(slotClass)) {
            explorefieldsNonSerializable(slotClass);
        } else {
            exploreFields(slotClass);
        }
        explorePrivateMethod(slotClass);
    }

    private void explorePrivateMethod(Class slotClass) {
        Method method = null;
        try {
            method = slotClass.getDeclaredMethod("readObject", new Class[]{ObjectInputStream.class});
            method.setAccessible(true);
            this.setPrivateMethodRead(method);
        } catch (Exception ignored) {
        }

        try {
            method = slotClass.getDeclaredMethod("writeObject", new Class[]{ObjectOutputStream.class});
            method.setAccessible(true);
            this.setPrivateMethodWrite(method);
        } catch (Exception ignored) {
        }
    }

    private void exploreFields(Class slotClass) {
        Field[] fields = slotClass.getDeclaredFields();
        ArrayList fieldsList = new ArrayList();
        for (short i = 0; i < fields.length; i++) {
            if ((fields[i].getModifiers() & (Modifier.TRANSIENT | Modifier.STATIC)) == 0) {
                fields[i].setAccessible(true);
                ClassMetadataField classfield = new ClassMetadataField(fields[i]);
                FieldsManager.getFieldsManager().fillMetadata(classfield);
                this.addField(classfield.getShaHash(), classfield.getFieldName(), classfield);
                classfield.setOrder((short) fieldsList.size());
                fieldsList.add(classfield);
            }
        }
        this.fieldsCollection = (ClassMetadataField[]) fieldsList.toArray(new ClassMetadataField[fieldsList.size()]);
    }

    private void explorefieldsNonSerializable(Class slotClass) {
        ArrayList fieldsList = new ArrayList();
        while (slotClass != null && slotClass != Object.class) {
            Field[] fields = slotClass.getDeclaredFields();
            for (short i = 0; i < fields.length; i++) {
                if ((fields[i].getModifiers() & (Modifier.TRANSIENT | Modifier.STATIC)) == 0) {
                    fields[i].setAccessible(true);
                    ClassMetadataField classfield = new ClassMetadataField(fields[i]);
                    FieldsManager.getFieldsManager().fillMetadata(classfield);
                    this.addField(classfield.getShaHash(), classfield.getFieldName(), classfield);
                    classfield.setOrder((short) fieldsList.size());
                    fieldsList.add(classfield);
                }
            }
            slotClass = slotClass.getSuperclass();
        }
        this.fieldsCollection = (ClassMetadataField[]) fieldsList.toArray(new ClassMetadataField[fieldsList.size()]);
    }

    WeakReference slotClass;
    String name;
    HashMap fields = new HashMap();
    TLongObjectHashMap hashFields = new TLongObjectHashMap();
    long shaHash;

    PersistentReference privateMethodWrite = emptyReference;
    PersistentReference privateMethodRead = emptyReference;

    /**
     * This collection exists just to ensure order.
     *
     * @todo - If it's possible to use a fastHashMap that keeps the order, this fieldsCollections should go away
     */
    ClassMetadataField[] fieldsCollection;

    public Class getSlotClass() {
        return (Class) slotClass.get();
    }

    public void setSlotClass(Class newSlotClass) {
        slotClass = new WeakReference(newSlotClass);
    }

    /**
     * @param fieldName
     * @param classfield
     */
    private void addField(long shaHashKey, String fieldName, ClassMetadataField classfield) {
        this.fields.put(fieldName, classfield);
        this.hashFields.put(shaHashKey, classfield);
    }

    /**
     * @return
     */
    public ClassMetadataField getField(String name) {
        return (ClassMetadataField) this.fields.get(name);
    }

    public ClassMetadataField getField(long shaKey) {
        return (ClassMetadataField) this.hashFields.get(shaKey);
    }

    public ClassMetadataField[] getFields() {
        return fieldsCollection;
    }

    public Method getPrivateMethodRead() {
        return (Method) privateMethodRead.get();
    }

    public void setPrivateMethodRead(Method privateMethodRead) {
        this.privateMethodRead = new MethodPersistentReference(privateMethodRead, REFERENCE_TYPE_IN_USE);
    }

    public Method getPrivateMethodWrite() {
        return (Method) privateMethodWrite.get();
    }

    public void setPrivateMethodWrite(Method privateMethodWrite) {
        this.privateMethodWrite = new MethodPersistentReference(privateMethodWrite, REFERENCE_TYPE_IN_USE);
    }

    public long getShaHash() {
        return shaHash;
    }

    public void setShaHash(long shaHash) {
        this.shaHash = shaHash;
    }


}
