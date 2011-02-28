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

import java.io.Serializable;

/**
 * This class is intended to TAG immutable serializable classes.
 * When you have too identical user immutable classes in a serialization graph tree, they will be considered as a single entity
 * and reading the object tree would cause a single object reference also.
 * <p/>
 * This is useful if for example you are serializing an object read from the database, and you have several identical instances
 * on the tree. They will be all considered a single instance and JBossSerialziation will place then as references.
 * <p/>
 * Classes implementing Immutable are required to provide a proper equals and a proper hashCode implementations.
 *
 * @author clebert suconic
 */
public interface Immutable extends Serializable {
}
