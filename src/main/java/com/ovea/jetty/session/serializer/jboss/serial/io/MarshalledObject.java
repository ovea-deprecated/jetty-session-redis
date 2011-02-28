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
package com.ovea.jetty.session.serializer.jboss.serial.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Turns content into a byte array that is delayed in unmarshalling.  JBoss Serialization
 * equivalent to java.rmi.MarshalledObject
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 123 $
 */
public class MarshalledObject implements Serializable {
    private byte[] bytes;
    private int hash;

    static final long serialVersionUID = -1433248532959364465L;


    public MarshalledObject() {
    }

    public MarshalledObject(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JBossObjectOutputStream mvos = new JBossObjectOutputStream(baos);
        mvos.writeObject(obj);
        mvos.flush();
        bytes = baos.toByteArray();
        mvos.close();
        hash = 0;
        for (int i = 0; i < bytes.length; i++) {
            hash += bytes[i];
        }
    }

    public Object get() throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        JBossObjectInputStream ois = new JBossObjectInputStream(bais);
        try {
            return ois.readObject();
        } finally {
            ois.close();
            bais.close();
        }
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final MarshalledObject that = (MarshalledObject) o;

        if (!Arrays.equals(bytes, that.bytes)) return false;

        return true;
    }

    public int hashCode() {
        return hash;
    }

}
