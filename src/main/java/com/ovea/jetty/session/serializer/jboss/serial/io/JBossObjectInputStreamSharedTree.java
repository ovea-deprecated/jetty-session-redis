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

import com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.DataContainer;
import com.ovea.jetty.session.serializer.jboss.serial.util.StringUtilBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;

/**
 * This implementation will respect reset commands.
 */
public class JBossObjectInputStreamSharedTree extends JBossObjectInputStream {

    ObjectInput input = null;
    DataContainer container = null;

    public JBossObjectInputStreamSharedTree(InputStream is, ClassLoader loader, StringUtilBuffer buffer) throws IOException {
        super(is, loader, buffer);
    }

    public JBossObjectInputStreamSharedTree(InputStream is, ClassLoader loader) throws IOException {
        super(is, loader);
    }

    public JBossObjectInputStreamSharedTree(InputStream is, StringUtilBuffer buffer) throws IOException {
        super(is, buffer);
    }

    public JBossObjectInputStreamSharedTree(InputStream is) throws IOException {
        super(is);
    }

    public Object readObjectOverride() throws IOException, ClassNotFoundException {
        if (input == null) {
            container = new DataContainer(classLoader, getSubstitutionInterface(), false, buffer, classDescriptorStrategy, objectDescriptorStrategy);
            container.setClassResolver(resolver);
            input = container.getDirectInput(dis);
        }
        return input.readObject();
    }

    public ClassLoader getClassLoader() {
        return super.getClassLoader();
    }

    public void setClassLoader(ClassLoader classLoader) {
        if (container != null) {
            container.getCache().setLoader(classLoader);
        }
        super.setClassLoader(classLoader);
    }


}
