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
import com.ovea.jetty.session.serializer.jboss.serial.classmetamodel.StreamingClass;
import com.ovea.jetty.session.serializer.jboss.serial.exception.SerializationException;
import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.ObjectSubstitutionInterface;
import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.ObjectsCache;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * $Id: ExternalizePersister.java 231 2006-04-24 23:49:41Z csuconic $
 *
 * @author Clebert Suconic
 */
public class ExternalizePersister implements Persister {
    byte id;

    public byte getId() {
        return id;
    }

    public void setId(byte id) {
        this.id = id;
    }


    /* (non-Javadoc)
     * @see org.jboss.serial.persister.Persister#writeData(org.jboss.serial.objectmetamodel.DataContainer, java.lang.Object)
     */
    public void writeData(ClassMetaData metaData, ObjectOutput out, Object obj, ObjectSubstitutionInterface substitution) throws IOException {
        ((Externalizable) obj).writeExternal(out);
    }

    /* (non-Javadoc)
     * @see org.jboss.serial.persister.Persister
     */
    public Object readData(ClassLoader loader, StreamingClass streaming, ClassMetaData metaData, int referenceId, ObjectsCache cache, ObjectInput input, ObjectSubstitutionInterface substitution) throws IOException {

        Object obj = metaData.newInstance();
        cache.putObjectInCacheRead(referenceId, obj);

        try {
            ((Externalizable) obj).readExternal(input);
        } catch (ClassNotFoundException e) {
            throw new SerializationException(e);
        }

        return obj;
    }

    public boolean canPersist(Object obj) {
        return false;
    }

}
