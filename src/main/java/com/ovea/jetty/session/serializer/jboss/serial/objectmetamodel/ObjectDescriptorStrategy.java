/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
import com.ovea.jetty.session.serializer.jboss.serial.classmetamodel.StreamingClass;
import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.ObjectsCache.JBossSeralizationInputInterface;
import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.ObjectsCache.JBossSeralizationOutputInterface;

import java.io.IOException;

/**
 * @author <a href="clebert.suconic@jboss.com">Clebert Suconic</a>
 * @author <a href="ron.sigal@jboss.com">Ron Sigal</a>
 * @version <p>
 *          Copyright Feb 1, 2009
 *          </p>
 */
public interface ObjectDescriptorStrategy {
    public boolean writeObjectSpecialCase(JBossSeralizationOutputInterface output, ObjectsCache cache, Object obj) throws IOException;

    public boolean writeDuplicateObject(JBossSeralizationOutputInterface output, ObjectsCache cache, Object obj, ClassMetaData metaData) throws IOException;

    public Object replaceObjectByClass(ObjectsCache cache, Object obj, ClassMetaData metaData) throws IOException;

    public Object replaceObjectByStream(ObjectsCache cache, Object obj, ClassMetaData metaData) throws IOException;

    public boolean doneReplacing(ObjectsCache cache, Object newObject, Object oldObject, ClassMetaData oldMetaData) throws IOException;

    public void writeObject(JBossSeralizationOutputInterface output, ObjectsCache cache, ClassMetaData metadata, Object obj) throws IOException;

    public Object readObjectSpecialCase(JBossSeralizationInputInterface input, ObjectsCache cache, byte byteIdentify) throws IOException;

    public Object readObject(JBossSeralizationInputInterface input, ObjectsCache cache, StreamingClass streamingClass, int reference) throws IOException;
}

