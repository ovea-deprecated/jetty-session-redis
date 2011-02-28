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

package com.ovea.jetty.session.serializer.jboss.serial.util;

import com.ovea.jetty.session.serializer.jboss.serial.references.EmptyReference;
import com.ovea.jetty.session.serializer.jboss.serial.references.PersistentReference;
import gnu.trove.TObjectHashingStrategy;


/**
 * Contribution made by Bob Morris.
 * To improve performance we should use EMPTY_CLASS_ARRAY and EMPTY_OBJECT_ARRAY instead of creating an instance every time we need.
 *
 * @author Bob Morris
 */
public interface ClassMetaConsts {
    static final int REFERENCE_TYPE_IN_USE = PersistentReference.REFERENCE_SOFT;
    static public final Class[] EMPTY_CLASS_ARRY = new Class[0];
    static public final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    static final PersistentReference emptyReference = new EmptyReference();
    static public final TObjectHashingStrategy identityHashStrategy =
            new TObjectHashingStrategy() {
                public int computeHashCode(Object o) {
                    return System.identityHashCode(o);
                }

                public boolean equals(Object o1, Object o2) {
                    return o1 == o2;
                }
            };

    static public final TObjectHashingStrategy regularHashStrategy =
            new TObjectHashingStrategy() {
                public int computeHashCode(Object o) {
                    return o.hashCode();
                }

                public boolean equals(Object o1, Object o2) {
                    return o1.getClass() == o2.getClass() && o1.equals(o2);
                }
            };
}
