/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

import com.ovea.jetty.session.serializer.jboss.serial.classmetamodel.ClassMetaData;
import com.ovea.jetty.session.serializer.jboss.serial.classmetamodel.ClassMetamodelFactory;
import com.ovea.jetty.session.serializer.jboss.serial.classmetamodel.StreamingClass;
import com.ovea.jetty.session.serializer.jboss.serial.exception.SerializationException;
import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.ObjectsCache.JBossSeralizationInputInterface;
import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.ObjectsCache.JBossSeralizationOutputInterface;
import com.ovea.jetty.session.serializer.jboss.serial.persister.ClassReferencePersister;
import com.ovea.jetty.session.serializer.jboss.serial.persister.PersistResolver;
import com.ovea.jetty.session.serializer.jboss.serial.persister.Persister;
import com.ovea.jetty.session.serializer.jboss.serial.util.ClassMetaConsts;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * @author <a href="clebert.suconic@jboss.com">Clebert Suconic</a>
 * @author <a href="ron.sigal@jboss.com">Ron Sigal</a>
 * @version <p>
 *          Copyright Feb 1, 2009
 *          </p>
 */
public class DefaultObjectDescriptorStrategy implements ObjectDescriptorStrategy, ClassMetaConsts {

    public boolean writeObjectSpecialCase(JBossSeralizationOutputInterface output, ObjectsCache cache, Object obj) throws IOException {
        if (obj == null) {
            output.writeByte(DataContainerConstants.NULLREF);
            return true;
        }

        if (obj != null && ClassMetamodelFactory.isImmutable(obj.getClass())) {
            output.saveImmutable(cache, obj);
            return true;
        }

        return false;
    }

    public boolean writeDuplicateObject(JBossSeralizationOutputInterface output, ObjectsCache cache, Object obj, ClassMetaData metaData) throws IOException {
        int description = cache.findIdInCacheWrite(obj, metaData.isImmutable());
        if (description != 0) {
            output.writeByte(DataContainerConstants.OBJECTREF);
            cache.getOutput().addObjectReference(description);
            return true;
        }
        return false;
    }

    public Object replaceObjectByClass(ObjectsCache cache, Object obj, ClassMetaData metaData) throws IOException {
        if (obj instanceof Class) {
            return obj;
        }

        if (metaData.getWriteReplaceMethod() != null) {
            try {
                return metaData.getWriteReplaceMethod().invoke(obj, EMPTY_OBJECT_ARRAY);
            } catch (Exception e) {
                IOException io = new IOException("Metadata Serialization Error");
                io.initCause(e);
                throw io;
            }
        }

        return obj;
    }

    public Object replaceObjectByStream(ObjectsCache cache, Object obj, ClassMetaData metaData) throws IOException {
        if (cache.getSubstitution() != null) {
            return cache.getSubstitution().replaceObject(obj);
        }
        return obj;
    }

    public boolean doneReplacing(ObjectsCache cache, Object newObject, Object oldObject, ClassMetaData oldMetaData) throws IOException {
        return (newObject == null || newObject == oldObject || newObject.getClass() == oldMetaData.getClazz());
    }

    public void writeObject(JBossSeralizationOutputInterface output, ObjectsCache cache, ClassMetaData metadata, Object obj) throws IOException {
        Persister persister = PersistResolver.resolvePersister(obj, metadata);
        output.writeByte(persister.getId());
        persister.writeData(metadata, cache.getOutput(), obj, cache.getSubstitution());
    }

    public Object readObjectSpecialCase(JBossSeralizationInputInterface input, ObjectsCache cache, byte byteIdentify) throws IOException {
        return input.readImmutable(byteIdentify, cache);
    }

    public Object readObject(JBossSeralizationInputInterface input, ObjectsCache cache, StreamingClass streamingClass, int reference) throws IOException {
        ClassMetaData metaData = streamingClass.getMetadata();
        byte persisterId = input.readByte();
        Persister persister = PersistResolver.resolvePersister(persisterId);

        //Persister persister = PersistResolver.resolvePersister(description.getMetaData().getClazz(),
        //        description.getMetaData(),description.getMetaData().isArray());

        /*ObjectDescription description = new ObjectDescription();
       description.setMetaData(ClassMetamodelFactory.getClassMetaData(reference.getClassName(),cache.getLoader(),false));
       cache.putObjectInCache(reference,description); */

        Object value = persister.readData(cache.getLoader(), streamingClass, metaData, reference, cache, cache.getInput(), cache.getSubstitution());

        if (!(persister instanceof ClassReferencePersister)) //JBSER-83
        {
            if (cache.getSubstitution() != null) {
                value = cache.getSubstitution().replaceObject(value);
            }

            try {
                if (metaData.getReadResolveMethod() != null) {
                    value = metaData.getReadResolveMethod().invoke(value, new Object[]{});
                    cache.reassignObjectInCacheRead(reference, value);
                }
            } catch (IllegalAccessException e) {
                throw new SerializationException(e);
            } catch (InvocationTargetException e) {
                throw new SerializationException(e);
            }
        }

        return value;
    }
}
