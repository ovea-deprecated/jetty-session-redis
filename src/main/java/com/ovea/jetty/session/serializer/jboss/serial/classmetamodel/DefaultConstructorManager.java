/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package com.ovea.jetty.session.serializer.jboss.serial.classmetamodel;

import java.lang.reflect.Constructor;

/**
 * If SunConstructorManager is not available in this current JVM, we will use the default one which only looks for the default constructor
 * at the current class
 * $Id: DefaultConstructorManager.java 175 2006-03-16 16:25:02Z csuconic $
 *
 * @author Clebert Suconic
 */
public class DefaultConstructorManager extends ConstructorManager {

    /* (non-Javadoc)
     * @see org.jboss.serial.classmetamodel.ConstructorManager#getConstructor(java.lang.Class)
     */
    public Constructor getConstructor(Class clazz) throws SecurityException, NoSuchMethodException {
        Constructor constr = clazz.getDeclaredConstructor(EMPTY_CLASS_ARRY);
        constr.setAccessible(true);
        return constr;
    }

    /* (non-Javadoc)
     * @see org.jboss.serial.classmetamodel.ConstructorManager#isSupported()
     */
    public boolean isSupported() {
        return true;
    }

}
