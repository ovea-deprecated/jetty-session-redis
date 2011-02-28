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

package com.ovea.jetty.session.serializer.jboss.serial.finalcontainers;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * $Id: DoubleContainer.java 217 2006-04-18 18:42:42Z csuconic $
 *
 * @author <a href="mailto:clebert.suconic@jboss.com">Clebert Suconic</a>
 */
public class DoubleContainer extends FinalContainer {
    double value;

    public DoubleContainer(double value) {
        this.value = value;
    }

    public DoubleContainer() {
    }

    public double getValue() {
        return value;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final DoubleContainer that = (DoubleContainer) o;

        if (Double.compare(that.value, value) != 0) return false;

        return true;
    }

    public int hashCode() {
        final long temp = value != +0.0d ? Double.doubleToLongBits(value) : 0L;
        return (int) (temp ^ (temp >>> 32));
    }

    public void writeMyself(DataOutput output) throws IOException {
        output.writeDouble(value);
    }

    public void readMyself(DataInput input) throws IOException {
        value = input.readDouble();
    }

    public void setPrimitive(Object obj, Field field) throws IllegalAccessException {
        field.setDouble(obj, value);
    }

}
