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

package com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel;

import com.ovea.jetty.session.serializer.jboss.serial.classmetamodel.ClassMetaData;
import com.ovea.jetty.session.serializer.jboss.serial.classmetamodel.ClassMetamodelFactory;
import com.ovea.jetty.session.serializer.jboss.serial.classmetamodel.StreamingClass;
import com.ovea.jetty.session.serializer.jboss.serial.exception.SerializationException;
import com.ovea.jetty.session.serializer.jboss.serial.util.ClassMetaConsts;

import java.io.IOException;


/**
 * @author clebert suconic
 */
public class ObjectDescriptorFactory implements ClassMetaConsts {
    static Object objectFromDescription(final ObjectsCache cache,
                                        ObjectsCache.JBossSeralizationInputInterface input) throws IOException {
        if (cache.getSubstitution() != null) {
            return cache.getSubstitution().replaceObject(objectFromDescriptionInternal(cache, input));
        } else {
            return objectFromDescriptionInternal(cache, input);
        }
    }


    private static Object objectFromDescriptionInternal(final ObjectsCache cache,
                                                        ObjectsCache.JBossSeralizationInputInterface input)
            throws IOException {
        Object description = null;

        byte byteIdentify = (byte) cache.getInput().readByte();

        if (byteIdentify == DataContainerConstants.RESET) {
            cache.reset();
            return objectFromDescription(cache, input);
        }
        if (byteIdentify == DataContainerConstants.NULLREF) {
            return null;
        } else if (byteIdentify == DataContainerConstants.NEWDEF) {

            return readObjectDescriptionFromStreaming(cache, input);
        } else if (byteIdentify == DataContainerConstants.SMARTCLONE_DEF) {
            int reference = input.readObjectReference();
            if (cache.getSafeToReuse() == null) {
                throw new IOException("SafeClone repository mismatch");
            }
            description = cache.getSafeToReuse().findReference(reference);
            if (description == null) {
                throw new IOException("SafeClone repository mismatch - didn't find reference " + reference);
            }
            return description;

        } else if (byteIdentify == DataContainerConstants.OBJECTREF) {
            int reference = input.readObjectReference();
            if (description == null) {
                description = cache.findObjectInCacheRead(reference);
            }

            if (description == null) {
                throw new SerializationException("Object reference " + reference + " was not found");
            }

            return description;
        } else {
            return cache.getObjectDescriptorStrategy().readObjectSpecialCase(input, cache, byteIdentify);
        }

    }


    /**
     * First level of a describe object, will look if it's a newDef, or if it's already loaded.
     * If the object was never loaded before, it will call readObjectDescriptionFromStreaming.
     * If it was already loaded, it will just return from objectCache
     */
    static void describeObject(final ObjectsCache cache, Object obj) throws IOException {
        ObjectsCache.JBossSeralizationOutputInterface outputParent = cache.getOutput();
        ObjectDescriptorStrategy objectDescriptorStrategy = cache.getObjectDescriptorStrategy();

        if (objectDescriptorStrategy.writeObjectSpecialCase(outputParent, cache, obj)) {
            return;
        }

        ClassMetaData metaData = getMetaData(obj, cache);

        if (objectDescriptorStrategy.writeDuplicateObject(outputParent, cache, obj, metaData)) {
            return;
        }

        Object originalObject = obj;
        Object newObject = obj;

        do {
            obj = newObject;
            metaData = getMetaData(obj, cache);
            newObject = objectDescriptorStrategy.replaceObjectByClass(cache, obj, metaData);

            if (objectDescriptorStrategy.writeObjectSpecialCase(outputParent, cache, newObject)) {
                return;
            }
        }
        while (!objectDescriptorStrategy.doneReplacing(cache, newObject, obj, metaData));

        obj = newObject;
        metaData = getMetaData(obj, cache);

        if (cache.getSubstitution() != null) {
            Object orig = obj;
            obj = cache.getSubstitution().replaceObject(obj);
            if (obj != orig) {
                if (objectDescriptorStrategy.writeObjectSpecialCase(outputParent, cache, obj)) {
                    return;
                }

                metaData = getMetaData(obj, cache);
            }
        }

        int description = 0;

        if (cache.getSafeToReuse() != null) {
            description = cache.getSafeToReuse().storeSafe(obj);
            if (description != 0) {
                outputParent.writeByte(DataContainerConstants.SMARTCLONE_DEF);
                cache.getOutput().addObjectReference(description);
                return;
            }

        }

        description = cache.findIdInCacheWrite(obj, metaData.isImmutable());

        if (description != 0) // Shouldn't happen.
        {
            outputParent.writeByte(DataContainerConstants.OBJECTREF);
            cache.getOutput().addObjectReference(description);
            return;
        } else {

            ClassMetaData originalMetaData = metaData;
            if (obj != originalObject) {
                originalMetaData = getMetaData(originalObject, cache);
            }
            description = cache.putObjectInCacheWrite(originalObject, originalMetaData.isImmutable());
            outputParent.writeByte(DataContainerConstants.NEWDEF);
            cache.getOutput().addObjectReference(description);
            cache.getClassDescriptorStrategy().writeClassDescription(obj, metaData, cache, description);
            cache.getObjectDescriptorStrategy().writeObject(outputParent, cache, metaData, obj);

            return;
        }
    }

    private static Object readObjectDescriptionFromStreaming(final ObjectsCache cache,
                                                             ObjectsCache.JBossSeralizationInputInterface input) throws IOException {
        int reference = input.readObjectReference();
        StreamingClass streamingClass = cache.getClassDescriptorStrategy().readClassDescription(cache, input, cache.getClassResolver(), null);
        return cache.getObjectDescriptorStrategy().readObject(input, cache, streamingClass, reference);
    }

    private static ClassMetaData getMetaData(Object obj, ObjectsCache cache) throws IOException {
        if (obj instanceof Class) {
            return ClassMetamodelFactory.getClassMetaData((Class) obj, false);
        } else {
            return ClassMetamodelFactory.getClassMetaData(obj.getClass(), cache.isCheckSerializableClass());
        }
    }
}
