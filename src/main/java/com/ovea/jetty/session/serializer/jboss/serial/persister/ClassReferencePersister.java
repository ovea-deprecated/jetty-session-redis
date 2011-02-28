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
import com.ovea.jetty.session.serializer.jboss.serial.classmetamodel.ClassMetamodelFactory;
import com.ovea.jetty.session.serializer.jboss.serial.classmetamodel.ClassResolver;
import com.ovea.jetty.session.serializer.jboss.serial.classmetamodel.StreamingClass;
import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.ObjectSubstitutionInterface;
import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.ObjectsCache;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Proxy;

/**
 * $Id: ClassReferencePersister.java 314 2006-06-08 16:39:44Z csuconic $
 *
 * @author <a href="mailto:clebert.suconic@jboss.com">Clebert Suconic</a>
 */
public class ClassReferencePersister implements Persister {
    byte id;

    public byte getId() {
        return id;
    }

    public void setId(byte id) {
        this.id = id;
    }

    public void writeData(ClassMetaData clazzMetaData, ObjectOutput output, Object obj, ObjectSubstitutionInterface substitution) throws IOException {
        Class clazz = (Class) obj;

        boolean isProxy = clazzMetaData.isProxy();
        output.writeBoolean(isProxy);

        if (isProxy) {
            Class interfaces[] = clazz.getInterfaces();
            output.writeInt(interfaces.length);
            for (int i = 0; i < interfaces.length; i++) {
                output.writeUTF(interfaces[i].getName());
            }
        } else {
            output.writeUTF(clazz.getName());
        }

    }

    public Object readData(ClassLoader loader, StreamingClass streaming, ClassMetaData metaData, int referenceId, ObjectsCache cache, ObjectInput input, ObjectSubstitutionInterface substitution) throws IOException {
        boolean isProxy = input.readBoolean();

        if (isProxy) {
            int size = input.readInt();
            Class interfaces[] = new Class[size];
            for (int i = 0; i < interfaces.length; i++) {
                interfaces[i] = lookupClass(cache.getClassResolver(), loader, input.readUTF());
            }

            Object proxyReturn = Proxy.getProxyClass(loader, interfaces);
            cache.putObjectInCacheRead(referenceId, proxyReturn);
            return proxyReturn;

        } else {
            String name = input.readUTF();
            Class classReturn = lookupClass(cache.getClassResolver(), loader, name);
            cache.putObjectInCacheRead(referenceId, classReturn);
            return classReturn;
        }
    }

    private Class lookupClass(ClassResolver resolver, ClassLoader loader, String name) throws IOException {
        if (name.equals("int")) {
            return Integer.TYPE;
        } else if (name.equals("long")) {
            return Long.TYPE;
        } else if (name.equals("double")) {
            return Double.TYPE;
        } else if (name.equals("float")) {
            return Float.TYPE;
        } else if (name.equals("char")) {
            return Character.TYPE;
        } else if (name.equals("boolean")) {
            return Boolean.TYPE;
        } else if (name.equals("byte")) {
            return Byte.TYPE;
        } else if (name.equals("short")) {
            return Short.TYPE;
        } else {
            ClassMetaData metaData = ClassMetamodelFactory.getClassMetaData(name, resolver, loader, false);
            if (metaData.isArray()) {
                return metaData.getArrayRepresentation();
            } else {
                return metaData.getClazz();
            }
        }
    }

    public boolean canPersist(Object obj) {
        // not implemented
        return false;
    }
}
