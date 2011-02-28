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

import com.ovea.jetty.session.serializer.jboss.serial.exception.SerializationException;
import com.ovea.jetty.session.serializer.jboss.serial.io.Immutable;
import com.ovea.jetty.session.serializer.jboss.serial.references.MethodPersistentReference;
import com.ovea.jetty.session.serializer.jboss.serial.references.PersistentReference;
import com.ovea.jetty.session.serializer.jboss.serial.util.ClassMetaConsts;
import com.ovea.jetty.session.serializer.jboss.serial.util.HashStringUtil;

import java.io.Externalizable;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author clebert suconic
 */
public class ClassMetaData implements ClassMetaConsts {
    static ConstructorManager[] constructorManagers = {new SunConstructorManager(), new DefaultConstructorManager()};


    /**
     * Used to reconstruct the Ghost Constructor used by Serialization's specification
     */
    static class GhostConstructorPersistentReference extends PersistentReference {
        GhostConstructorPersistentReference(Class clazz, Constructor constructor) {
            super(clazz, constructor, REFERENCE_TYPE_IN_USE);
        }

        public synchronized Object rebuildReference() throws Exception {
            Object returnValue;
            if ((returnValue = internalGet()) != null) return returnValue;
            Constructor constructorUsed = findConstructor(getMappedClass());
            buildReference(constructorUsed);
            return constructorUsed;
        }

        public Constructor getConstructor() {
            return (Constructor) get();
        }
    }

    private Method lookupMethodOnHierarchy(Class clazz, String methodName, Class reflectionArguments[]) {
        Class currentClass = clazz;
        while (currentClass != Object.class && currentClass != null) {
            try {
                Method method = currentClass.getDeclaredMethod(methodName, reflectionArguments);
                if (method.getReturnType() == Object.class) {
                    return method;
                }
            } catch (Exception ignored) {
            }
            currentClass = currentClass.getSuperclass();
        }

        return null;
    }

    public ClassMetaData(Class clazz) {
        setClassName(clazz.getName());
        setClazz(clazz);
        setShaHash(HashStringUtil.hashName(clazz.getName()));
        setProxy(Proxy.isProxyClass(clazz));
        lookupInternalMethods(clazz);


        try {
            setConstructor(findConstructor(clazz));
        } catch (NoSuchMethodException e) {
            setConstructor(null);
        }

        setExternalizable(Externalizable.class.isAssignableFrom(clazz));
        setSerializable(Serializable.class.isAssignableFrom(clazz));
        setImmutable(Immutable.class.isAssignableFrom(clazz));

        exploreSlots(clazz);
    }

    PersistentReference constructor = emptyReference;

    WeakReference clazz;
    WeakReference arrayRep;

    String className;

    boolean isArray;

    int arrayDepth;

    boolean isProxy;

    boolean isExternalizable;

    boolean isSerializable;

    boolean isImmutable;

    long shaHash;

    PersistentReference readResolveMethod = emptyReference;
    PersistentReference writeReplaceMethod = emptyReference;

    ClassMetaDataSlot[] slots;

    public ClassMetaDataSlot[] getSlots() {
        return slots;
    }

    /**
     * @return Returns the className.
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className The className to set.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    private void calculateDepthAndName(Class clazz) {
        arrayDepth = 0;
        while (clazz.isArray()) {
            arrayDepth++;
            clazz = clazz.getComponentType();
        }
        this.clazz = new WeakReference(clazz);
    }

    /**
     * @return Returns the clazz.
     */
    public Class getClazz() {
        if (clazz == null) return null;
        else
            return (Class) clazz.get();
    }

    public Class getArrayRepresentation() {
        if (arrayRep == null) return null;
        else
            return (Class) arrayRep.get();
    }

    private void constructArrayRepresentationClass(Class clazz) {
        arrayRep = new WeakReference(clazz);
    }

    /**
     * @param clazz The clazz to set.
     */
    public void setClazz(Class clazz) {
        if (clazz == null) {
            this.clazz = null;
        } else {
            this.clazz = new WeakReference(clazz);
            if (clazz.isArray()) {
                this.setArray(true);
                calculateDepthAndName(clazz);
                constructArrayRepresentationClass(clazz);
            }
        }

    }

    /**
     * @return Returns the constructor.
     */
    public Constructor getConstructor() {
        return (Constructor) constructor.get();
    }

    /**
     * @param constructor The constructor to set.
     */
    public void setConstructor(Constructor constructor) {
        if (constructor != null) {
            constructor.setAccessible(true);
        }
        this.constructor = new GhostConstructorPersistentReference(this.getClazz(), constructor);
    }

    /**
     * @return Returns the isExternalizable.
     */
    public boolean isExternalizable() {
        return isExternalizable;
    }

    /**
     * @param isExternalizable The isExternalizable to set.
     */
    public void setExternalizable(boolean isExternalizable) {
        this.isExternalizable = isExternalizable;
    }

    public boolean isSerializable() {
        return isSerializable;
    }

    public void setSerializable(boolean isSerializable) {
        this.isSerializable = isSerializable;
    }

    public boolean isImmutable() {
        return isImmutable;
    }

    public void setImmutable(boolean isImmutable) {
        this.isImmutable = isImmutable;
    }

    public int hashCode() {
        return className.hashCode();
    }

    public boolean equals(Object obj) {
        return className.equals(((ClassMetaData) obj).className);
    }


    /**
     * @return Returns the isArray.
     */
    public boolean isArray() {
        return isArray;
    }

    /**
     * @param isArray The isArray to set.
     */
    public void setArray(boolean isArray) {
        this.isArray = isArray;
    }


    /**
     * @return Returns the arrayDepth.
     */
    public int getArrayDepth() {
        return arrayDepth;
    }

    /**
     * @return
     */
    public Object newInstance() throws IOException {
        Constructor localConstructor = getConstructor();
        try {
            if (localConstructor == null) {
                return this.getClazz().newInstance();
            } else {
                return localConstructor.newInstance(EMPTY_OBJECT_ARRAY);
            }
        } catch (InstantiationException e) {
            throw new SerializationException("Could not create instance of " + this.className + " - " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new SerializationException("Could not create instance of " + this.className + " - " + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new SerializationException("Could not create instance of " + this.className + " - " + e.getMessage(), e);
        }
    }

    public Method getReadResolveMethod() {
        return (Method) readResolveMethod.get();
    }

    public void setReadResolveMethod(Method readResolveMethod) {
        this.readResolveMethod = new MethodPersistentReference(readResolveMethod, REFERENCE_TYPE_IN_USE);
    }

    public boolean isProxy() {
        return isProxy;
    }

    public void setProxy(boolean proxy) {
        isProxy = proxy;
    }

    public Method getWriteReplaceMethod() {
        return (Method) writeReplaceMethod.get();
    }

    public void setWriteReplaceMethod(Method writeReplaceMethod) {
        this.writeReplaceMethod = new MethodPersistentReference(writeReplaceMethod, REFERENCE_TYPE_IN_USE);
    }

    public long getShaHash() {
        return shaHash;
    }

    public void setShaHash(long shaHash) {
        this.shaHash = shaHash;
    }

    private static Constructor findConstructor(Class clazz) throws NoSuchMethodException {
        if (clazz.isInterface()) {
            return null;
        }
        for (int i = 0; i < constructorManagers.length; i++) {
            if (constructorManagers[i].isSupported()) {
                return constructorManagers[i].getConstructor(clazz);
            }
        }
        // I don't expect this exceptiong being thrown unless the JVM doesn't support reflection at al
        throw new NoSuchMethodException("Constructor not found as having difficulties in reflection");
    }

    private void exploreSlots(Class clazz) {
        ArrayList slots = new ArrayList();
        // if it's externalizable we won't be using any fields
        if (!this.isExternalizable() && !this.isArray && this.isSerializable()) {
            for (Class classIteration = clazz; classIteration != null && Serializable.class.isAssignableFrom(classIteration); classIteration = classIteration.getSuperclass()) {
                ClassMetaDataSlot slot = new ClassMetaDataSlot(classIteration);
                slots.add(slot);
            }
        } else {
            ClassMetaDataSlot slot = new ClassMetaDataSlot(clazz);
            slots.add(slot);
        }

        Collections.reverse(slots);
        this.slots = (ClassMetaDataSlot[]) slots.toArray(new ClassMetaDataSlot[slots.size()]);
    }

    private void lookupInternalMethods(Class clazz) {
        if (clazz.isInterface()) {
            return;
        }

        Method method = lookupMethodOnHierarchy(clazz, "readResolve", EMPTY_CLASS_ARRY);
        if (method != null) {
            method.setAccessible(true);
            this.setReadResolveMethod(method);
        }

        method = lookupMethodOnHierarchy(clazz, "writeReplace", EMPTY_CLASS_ARRY);
        if (method != null) {
            method.setAccessible(true);
            setWriteReplaceMethod(method);
        }
    }


}
