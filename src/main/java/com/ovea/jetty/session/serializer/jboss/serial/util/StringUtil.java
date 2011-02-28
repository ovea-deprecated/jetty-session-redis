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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


/**
 * This class respects definitions on http://java.sun.com/j2se/1.5.0/docs/api/java/io/DataInput.html#modified-utf-8
 *
 * @author <a href="mailto:clebert.suconic@jboss.com">Clebert Suconic</a>
 */
public class StringUtil {
    static boolean optimizeStrings = true;

    private static void flushByteBuffer(DataOutput out, byte[] byteBuffer, StringUtilBuffer.Position pos) throws IOException {
        out.write(byteBuffer, 0, pos.pos);
        pos.pos = 0;
    }

    public static void saveString(DataOutput out, String str, StringUtilBuffer buffer) throws IOException {
        if (!optimizeStrings) {
            saveString(out, str);
            return;
        }

        if (buffer == null) {
            buffer = StringUtil.getThreadLocalBuffer();
        }

        long len = StringUtil.calculateUTFSize(str, buffer);
        if (len > 0xffff) {
            out.writeBoolean(true); // the size is bigger than a short value
            out.writeLong(len);
        } else {
            out.writeBoolean(false);
            out.writeShort((short) len);
        }

        if (len == (long) str.length()) {
            if (len > buffer.byteBuffer.length) {
                buffer.resizeByteBuffer((int) len);
            }

            for (int byteLocation = 0; byteLocation < len; byteLocation++) {
                buffer.byteBuffer[byteLocation] = (byte) buffer.charBuffer[byteLocation];
            }
            out.write(buffer.byteBuffer, 0, (int) len);
        } else {
            StringUtilBuffer.Position pos = buffer.position.reset();

            int stringLength = str.length();
            for (int bufferPosition = 0; bufferPosition < stringLength;) {
                int countArray = Math.min(stringLength - bufferPosition, buffer.charBuffer.length);
                str.getChars(bufferPosition, bufferPosition + countArray, buffer.charBuffer, 0);

                for (int i = 0; i < countArray; i++) {
                    char charAtPos = buffer.charBuffer[i];
                    if (charAtPos >= 1 && charAtPos < 0x7f) {
                        if (pos.pos >= buffer.byteBuffer.length) {
                            flushByteBuffer(out, buffer.byteBuffer, pos);
                        }
                        buffer.byteBuffer[pos.pos++] = (byte) charAtPos;
                    } else if (charAtPos >= 0x800) {
                        if (pos.pos + 3 >= buffer.byteBuffer.length) {
                            flushByteBuffer(out, buffer.byteBuffer, pos);
                        }

                        buffer.byteBuffer[pos.pos++] = (byte) (0xE0 | ((charAtPos >> 12) & 0x0F));
                        buffer.byteBuffer[pos.pos++] = (byte) (0x80 | ((charAtPos >> 6) & 0x3F));
                        buffer.byteBuffer[pos.pos++] = (byte) (0x80 | ((charAtPos >> 0) & 0x3F));
                    } else {
                        if (pos.pos + 2 >= buffer.byteBuffer.length) {
                            flushByteBuffer(out, buffer.byteBuffer, pos);
                        }

                        buffer.byteBuffer[pos.pos++] = (byte) (0xC0 | ((charAtPos >> 6) & 0x1F));
                        buffer.byteBuffer[pos.pos++] = (byte) (0x80 | ((charAtPos >> 0) & 0x3F));

                    }
                }

                bufferPosition += countArray;
            }
            flushByteBuffer(out, buffer.byteBuffer, pos);
        }
    }


    private static ThreadLocal currenBuffer = new ThreadLocal();

    private static StringUtilBuffer getThreadLocalBuffer() {
        StringUtilBuffer retValue = (StringUtilBuffer) currenBuffer.get();
        if (retValue == null) {
            retValue = new StringUtilBuffer();
            currenBuffer.set(retValue);
        }

        return retValue;
    }

    public static void saveString(DataOutput out, String str) throws IOException {
        if (optimizeStrings) {
            StringUtilBuffer buffer = getThreadLocalBuffer();
            saveString(out, str, buffer);
        } else {
            out.writeUTF(str);
        }
    }

    /*public static String readString(DataInput input) throws IOException
    {
        if (optimizeStrings)
        {
            StringUtilBuffer buffer = getBuffer();
            return readString(input,buffer);
        }
        else
        {
            return input.readUTF();
        }
    } */

    private static void pullDataToBuffer(DataInput input, StringUtilBuffer.Position pos, byte[] byteBuffer, long currentPosition, long size) throws IOException {
        pos.pos = 0;

        pos.size = (int) Math.min(size - currentPosition, (long) byteBuffer.length);

        input.readFully(byteBuffer, 0, (int) pos.size);
    }

    public static String readString(DataInput input, StringUtilBuffer buffer) throws IOException {
        if (!optimizeStrings) {
            return input.readUTF();
        }

        if (buffer == null) {
            buffer = StringUtil.getThreadLocalBuffer();
        }

        long size = 0;

        boolean isLong = input.readBoolean();

        if (isLong) {
            size = input.readLong();
        } else {
            size = input.readUnsignedShort();
        }
        long count = 0;
        int byte1, byte2, byte3;
        int charCount = 0;
        StringUtilBuffer.Position pos = buffer.position.reset();
        ;
        StringBuffer strbuffer = null;

        while (count < size) {
            if (pos.pos >= pos.size) {
                pullDataToBuffer(input, pos, buffer.byteBuffer, count, size);
            }
            byte1 = buffer.byteBuffer[pos.pos++];
            count++;

            if (byte1 > 0 && byte1 <= 0x7F) {
                buffer.charBuffer[charCount++] = (char) byte1;
            } else {
                int c = (byte1 & 0xff);
                switch (c >> 4) {
                    case 0xc:
                    case 0xd:
                        if (pos.pos >= pos.size) {
                            pullDataToBuffer(input, pos, buffer.byteBuffer, count, size);
                        }
                        byte2 = buffer.byteBuffer[pos.pos++];
                        buffer.charBuffer[charCount++] = (char) (((c & 0x1F) << 6) |
                                (byte2 & 0x3F));
                        count++;
                        break;
                    case 0xe:
                        if (pos.pos >= pos.size) {
                            pullDataToBuffer(input, pos, buffer.byteBuffer, count, size);
                        }
                        byte2 = buffer.byteBuffer[pos.pos++];
                        count++;
                        if (pos.pos >= pos.size) {
                            pullDataToBuffer(input, pos, buffer.byteBuffer, count, size);
                        }
                        byte3 = buffer.byteBuffer[pos.pos++];
                        buffer.charBuffer[charCount++] = (char) (((c & 0x0F) << 12) |
                                ((byte2 & 0x3F) << 6) |
                                ((byte3 & 0x3F) << 0));
                        count++;

                        break;
                }
            }

            if (charCount == buffer.charBuffer.length) {
                if (strbuffer == null) {
                    strbuffer = new StringBuffer((int) size);
                }
                strbuffer.append(buffer.charBuffer);
                charCount = 0;
            }
        }

        if (strbuffer != null) {
            strbuffer.append(buffer.charBuffer, 0, charCount);
            return strbuffer.toString();
        } else {
            return new String(buffer.charBuffer, 0, charCount);
        }


    }


    public static long calculateUTFSize(String str, StringUtilBuffer stringBuffer) {
        long calculatedLen = 0;
        int stringLength = str.length();
        if (stringLength > stringBuffer.charBuffer.length) {
            stringBuffer.resizeCharBuffer(stringLength);
        }
        str.getChars(0, stringLength, stringBuffer.charBuffer, 0);

        for (int i = 0; i < stringLength; i++) {
            char c = stringBuffer.charBuffer[i];

            if (c >= 1 && c < 0x7f) {
                calculatedLen++;
            } else if (c >= 0x800) {
                calculatedLen += 3;
            } else {
                calculatedLen += 2;
            }

        }

        return calculatedLen;
    }
}
