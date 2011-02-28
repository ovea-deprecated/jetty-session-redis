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

/**
 * $Id: StringUtilBuffer.java 297 2006-05-20 03:02:32Z csuconic $
 *
 * @author <a href="mailto:clebert.suconic@jboss.com">Clebert Suconic</a>
 */
public class StringUtilBuffer {

    /* A way to pass an integer as a parameter to a method */
    public static class Position {
        int pos;
        long size;

        public Position reset() {
            pos = 0;
            size = 0;
            return this;
        }
    }

    Position position = new Position();


    public char charBuffer[];
    public byte byteBuffer[];

    public void resizeCharBuffer(int newSize) {
        if (newSize <= charBuffer.length) {
            throw new RuntimeException("New buffer can't be smaller");
        }
        char[] newCharBuffer = new char[newSize];
        for (int i = 0; i < charBuffer.length; i++) {
            newCharBuffer[i] = charBuffer[i];
        }
        charBuffer = newCharBuffer;
    }

    public void resizeByteBuffer(int newSize) {
        if (newSize <= byteBuffer.length) {
            throw new RuntimeException("New buffer can't be smaller");
        }
        byte[] newByteBuffer = new byte[newSize];
        for (int i = 0; i < byteBuffer.length; i++) {
            newByteBuffer[i] = byteBuffer[i];
        }
        byteBuffer = newByteBuffer;
    }

    public StringUtilBuffer() {
        this(1024, 1024);
    }

    public StringUtilBuffer(int sizeChar, int sizeByte) {
        charBuffer = new char[sizeChar];
        byteBuffer = new byte[sizeByte];
    }


}
