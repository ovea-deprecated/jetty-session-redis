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
package com.ovea.jetty.session.serializer.jboss.serial.classmetamodel;

import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.DataContainerConstants;
import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.ObjectsCache;
import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.ObjectsCache.JBossSeralizationInputInterface;

import java.io.IOException;

/**
 * @author <a href="clebert.suconic@jboss.com">Clebert Suconic</a>
 * @author <a href="ron.sigal@jboss.com">Ron Sigal</a>
 * @version <p>
 *          Copyright Jan 28, 2009
 *          </p>
 */
public class DefaultClassDescriptorStrategy implements ClassDescriptorStrategy {
    public void writeClassDescription(Object obj, ClassMetaData metaData, ObjectsCache cache, int description) throws IOException {
        writeClassDescription(obj, metaData, cache, description, true);
    }

    public void writeClassDescription(Object obj, ClassMetaData metaData, ObjectsCache cache, int description, boolean writeClassDescription) throws IOException {
        ObjectsCache.JBossSeralizationOutputInterface outputParent = cache.getOutput();
        int cacheId = cache.findIdInCacheWrite(metaData, false);
        if (cacheId == 0) {
            cacheId = cache.putObjectInCacheWrite(metaData, false);
            outputParent.writeByte(DataContainerConstants.NEWDEF);
            outputParent.addObjectReference(cacheId);
            if (writeClassDescription) {
                outputParent.writeUTF(metaData.getClassName());
            }
            StreamingClass.saveStream(metaData, outputParent);
        } else {
            outputParent.writeByte(DataContainerConstants.OBJECTREF);
            outputParent.addObjectReference(cacheId);
        }
    }

    public StreamingClass readClassDescription(ObjectsCache cache, JBossSeralizationInputInterface input, ClassResolver classResolver, String className) throws IOException {
        return readClassDescription(cache, input, classResolver, className, null);
    }

    public StreamingClass readClassDescription(ObjectsCache cache, JBossSeralizationInputInterface input, ClassResolver classResolver, String className, Class clazz) throws IOException {
        byte defClass = input.readByte();
        StreamingClass streamingClass = null;
        if (defClass == DataContainerConstants.NEWDEF) {
            int referenceId = input.readObjectReference();
            if (className == null) {
                className = input.readUTF();
            }

            streamingClass = StreamingClass.readStream(input, classResolver, cache.getLoader(), className);
            cache.putObjectInCacheRead(referenceId, streamingClass);
        } else {
            int referenceId = input.readObjectReference();
            streamingClass = (StreamingClass) cache.findObjectInCacheRead(referenceId);
            if (streamingClass == null) {
                throw new IOException("Didn't find StreamingClass circular refernce id=" + referenceId);
            }
        }

        return streamingClass;
    }
}

