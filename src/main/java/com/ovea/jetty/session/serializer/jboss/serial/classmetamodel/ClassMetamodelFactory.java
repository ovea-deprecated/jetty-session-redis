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

import com.ovea.jetty.session.serializer.jboss.serial.util.ClassMetaConsts;
import com.ovea.jetty.session.serializer.jboss.serial.util.FastHashMap;
import com.ovea.jetty.session.serializer.jboss.serial.util.PartitionedWeakHashMap;

import java.io.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * $Id: ClassMetamodelFactory.java 326 2006-07-28 04:57:13Z csuconic $
 *
 * @author clebert suconic
 */
public class ClassMetamodelFactory implements ClassMetaConsts {
    /**
     * table mapping primitive type names to corresponding class objects.
     * This code was created by Carlos de Wolf on a fix of EJBTHREE-440.
     * By coincidence I needed to parse primitive values, and instead of reinventing the wheel I got the same code.
     */
    private static final HashMap primClasses = new HashMap(8, 1.0F);

    static {
        primClasses.put("boolean", boolean.class);
        primClasses.put("byte", byte.class);
        primClasses.put("char", char.class);
        primClasses.put("short", short.class);
        primClasses.put("int", int.class);
        primClasses.put("long", long.class);
        primClasses.put("float", float.class);
        primClasses.put("double", double.class);
        primClasses.put("void", void.class);
        primClasses.put("[Z", boolean[].class);
        primClasses.put("[B", byte[].class);
        primClasses.put("[C", char[].class);
        primClasses.put("[S", short[].class);
        primClasses.put("[I", int[].class);
        primClasses.put("[J", long[].class);
        primClasses.put("[F", float[].class);
        primClasses.put("[D", double[].class);
    }

    /**
     * We are caching the getClassLoader operation, to avoid locks on the WeakHashMap
     */
    private static class CacheLoaderReference {
        WeakReference currentClassLoader;
        Map currentHashMap;

        public ClassLoader getCurrentClassLoader() {
            if (currentClassLoader == null)
                return null;
            else
                return (ClassLoader) currentClassLoader.get();
        }

        public void setCurrentClassLoader(ClassLoader loader) {
            currentClassLoader = new WeakReference(loader);
        }

        public Map getCurrentMap() {
            return currentHashMap;
        }

        public void setCurrentMap(Map currentMap) {
            this.currentHashMap = currentMap;
        }
    }


    /**
     * Method for ObjectStreamClass.lookup
     */
    static Method methodLookup;

    /**
     * Method for ObjectStreamClass.getField
     */
    static Method methodGetField;


    /**
     * The HashMap for SystemclassLoader
     */
    private static Map systemClassLoaderMap = new FastHashMap();

    private static ObjectStreamClass lookup(Class clazz) throws IllegalAccessException, InvocationTargetException {
        return (ObjectStreamClass) methodLookup.invoke(null, new Object[]{clazz, Boolean.TRUE});
    }

    private static Field getField(Object source) throws IllegalAccessException, InvocationTargetException {
        return (Field) methodGetField.invoke(source, EMPTY_OBJECT_ARRAY);
    }

    static ThreadLocal cacheLoader = new ThreadLocal();

    static {
        try {
            methodLookup = ObjectStreamClass.class.getDeclaredMethod("lookup", new Class[]{Class.class, Boolean.TYPE});
            methodLookup.setAccessible(true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            methodGetField = ObjectStreamField.class.getDeclaredMethod("getField", new Class[]{});
            methodGetField.setAccessible(true);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public static void clear(boolean elimiteClassLoadersCached) {
        Iterator iter = cache.values().iterator();
        while (iter.hasNext()) {
            Map map = (Map) iter.next();
            map.clear();
        }
        if (elimiteClassLoadersCached) cache.clear();
        systemClassLoaderMap.clear();
    }

    public static void clear() {
        clear(true);
    }

    public static Map getCache() {
        return cache;
    }

    /**
     * PartitionedWeakHashMap<ClassLoader,ConcurrentHashMap<String,ClassMetaData>>
     * (The current implementation is using FastHashMap instead of ConcurrentHashMap)
     * *
     */
    static PartitionedWeakHashMap cache = new PartitionedWeakHashMap();

    static ClassMetaData proxyMetaData = null;

    static {
        try {
            proxyMetaData = getClassMetaData("java.lang.reflect.Proxy", Thread.currentThread().getContextClassLoader(), true);
            proxyMetaData.setProxy(true);
        } catch (Exception e) {
            e.printStackTrace();
            ;
        }
    }

    private static Map getLoaderMap(ClassLoader loader) {
        if (loader == null) {
            return systemClassLoaderMap;
        }

        CacheLoaderReference loaderReference = (CacheLoaderReference) cacheLoader.get();
        if (loaderReference == null) {
            loaderReference = new CacheLoaderReference();
            cacheLoader.set(loaderReference);
        }

        if (loaderReference.getCurrentClassLoader() == loader) {
            return loaderReference.getCurrentMap();
        }


        Map hashMap = (Map) cache.get(loader);

        ClassLoader returnLoader = null;
        if (hashMap == null) {
            hashMap = new FastHashMap();
            cache.put(loader, hashMap);
            hashMap = (Map) cache.get(loader);
        }

        loaderReference.setCurrentClassLoader(loader);
        loaderReference.setCurrentMap(hashMap);
        return hashMap;
    }

    private static ClassMetaData getClassMetaData(String clazzName, ClassLoader loader, boolean checkSerializable) throws IOException {
        return getClassMetaData(clazzName, null, loader, checkSerializable);
    }


    private static Class resolveClassByName(String clazzName, ClassResolver resolver, ClassLoader loader) throws ClassNotFoundException {
        Class clazz = (Class) primClasses.get(clazzName);

        if (clazz == null) {
            if (resolver != null) {
                clazz = resolver.resolveClass(clazzName);
                if (clazz == null) {
                    clazz = Class.forName(clazzName, false, loader);
                }
            } else {
                clazz = Class.forName(clazzName, false, loader);
            }
        }
        return clazz;
    }

    public static ClassMetaData getClassMetaData(String clazzName, ClassResolver resolver, ClassLoader loader, boolean checkSerializable) throws IOException {
        try {
            Map loaderMap = getLoaderMap(loader);
            ClassMetaData classMetadata = (ClassMetaData) loaderMap.get(clazzName);
            if (classMetadata == null) {

                Class clazz = resolveClassByName(clazzName, resolver, loader);

                if (checkSerializable && !Serializable.class.isAssignableFrom(clazz)) {
                    throw new NotSerializableException(clazz.getName());
                }
                classMetadata = new ClassMetaData(clazz);

                loaderMap = getLoaderMap(loader);
                loaderMap.put(clazzName, classMetadata);
                classMetadata = (ClassMetaData) loaderMap.get(clazzName);
            }

            if (classMetadata.getClazz() == null) {
                // this never happens, so, this is just in case
                // The only possibility for that would be crossed classLoader operations.
                // I tried to replicate this scenario on a testcase and it was not possible.
                // So, I decided to have this verification here to be safe
                Class clazz = resolveClassByName(clazzName, resolver, loader);
                loaderMap.remove(clazzName);
                classMetadata = new ClassMetaData(clazz);
                loaderMap.put(clazz.getName(), classMetadata);
                classMetadata = (ClassMetaData) loaderMap.get(clazzName);
            }


            // It looks dummy but doing Class.isProxy is a very expensive operation, so we just do it on a cached basis
            if (classMetadata.isProxy()) {
                return proxyMetaData;
            } else {
                return classMetadata;
            }
        } catch (ClassNotFoundException e) {

            IOException ex = new IOException(e.getMessage());
            ex.initCause(e);
            throw ex;
        }

    }


    public static ClassMetaData getClassMetaData(Class clazz, boolean checkSerializable) throws IOException {
        if (checkSerializable && !Serializable.class.isAssignableFrom(clazz) && !clazz.isPrimitive()) {
            throw new NotSerializableException(clazz.getName());
        }
        Map loaderMap = getLoaderMap(clazz.getClassLoader());
        ClassMetaData classMetadata = (ClassMetaData) loaderMap.get(clazz.getName());
        if (classMetadata == null) {
            classMetadata = new ClassMetaData(clazz);
            loaderMap.put(clazz.getName(), classMetadata);
            classMetadata = (ClassMetaData) loaderMap.get(clazz.getName());
        }
        if (classMetadata.getClazz() == null) {
            // this never happens, so, this is just in case
            // The only possibility for that would be crossed classLoader operations.
            // I tried to replicate this scenario on a testcase and it was not possible.
            // So, I decided to have this verification here to be safe
            loaderMap.remove(clazz.getName());
            classMetadata = new ClassMetaData(clazz);
            loaderMap.put(clazz.getName(), classMetadata);
            classMetadata = (ClassMetaData) loaderMap.get(clazz.getName());
        }

        // It looks dummy but doing Class.isProxy is a very expensive operation, so we just do it on a cached basis
        if (classMetadata.isProxy()) {
            return proxyMetaData;
        } else {
            return classMetadata;
        }
    }


    public static boolean isImmutable(Class clazz) {
        return (clazz == Character.class || clazz == String.class ||
                clazz == Long.class ||
                clazz == Byte.class ||
                clazz == Double.class ||
                clazz == Float.class ||
                clazz == Integer.class ||
                clazz == Short.class ||
                clazz == Boolean.class ||
                clazz.isPrimitive());
    }

}
