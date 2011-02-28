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

import sun.reflect.ReflectionFactory;

import java.io.Externalizable;
import java.io.Serializable;
import java.lang.reflect.Constructor;


/**
 * This constructor manager requires sun package present.
 * If the class is not present, we will not be able to use this constructor manager
 * $Id: SunConstructorManager.java 315 2006-06-15 16:29:07Z csuconic $
 *
 * @author Clebert Suconic
 */
public class SunConstructorManager extends ConstructorManager {
    static boolean supported = true;

    static {
        try {
            reflectionFactory = ReflectionFactory.getReflectionFactory();
        } catch (Throwable e) {
            e.printStackTrace();
            supported = false;
        }
    }

    static ReflectionFactory reflectionFactory;


    /* (non-Javadoc)
    * @see org.jboss.serial.classmetamodel.ConstructorManager#getConstructor(java.lang.Class)
    */
    public Constructor getConstructor(Class clazz) throws SecurityException, NoSuchMethodException {
        if (clazz.isInterface()) {
            throw new NoSuchMethodException("Can't create a constructor for a pure interface");
        } else if (!Serializable.class.isAssignableFrom(clazz)) {
            Constructor constr = clazz.getDeclaredConstructor(EMPTY_CLASS_ARRY);
            constr.setAccessible(true);
            return constr;
        } else if (Externalizable.class.isAssignableFrom(clazz)) {
            Constructor constr = clazz.getConstructor(EMPTY_CLASS_ARRY);
            constr.setAccessible(true);
            return constr;
        } else {
            Class currentClass = clazz;
            while (Serializable.class.isAssignableFrom(currentClass)) {
                currentClass = currentClass.getSuperclass();
            }
            Constructor constr = currentClass.getDeclaredConstructor(EMPTY_CLASS_ARRY);
            constr.setAccessible(true);

            // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6220682
            Constructor newConstructor = reflectionFactory.newConstructorForSerialization(clazz, constr);
            newConstructor.setAccessible(true);

            return newConstructor;
        }
    }

    /* (non-Javadoc)
     * @see org.jboss.serial.classmetamodel.ConstructorManager#isSupported()
     */
    public boolean isSupported() {
        return supported;
    }

}
