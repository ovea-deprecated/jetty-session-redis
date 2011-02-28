/**
 * Copyright (C) 2011 Ovea <dev@ovea.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ovea.jetty.session.serializer;

import com.ovea.jetty.session.SerializerException;
import org.eclipse.jetty.util.B64Code;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public abstract class BinarySerializer extends SerializerSkeleton {

    private boolean gzip = true;

    public void setGzip(boolean gzip) {
        this.gzip = gzip;
    }

    public boolean isGzip() {
        return gzip;
    }

    @Override
    public final String serialize(Object o) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (gzip) {
                GZIPOutputStream gout = new GZIPOutputStream(baos);
                write(gout, o);
                gout.finish();
                gout.close();
            } else {
                write(baos, o);
            }
            return String.valueOf(B64Code.encode(baos.toByteArray(), false));
        } catch (Exception e) {
            throw new SerializerException("Error serializing " + o + " : " + e.getMessage(), e);
        }
    }

    @Override
    public final <T> T deserialize(String o, Class<T> targetType) throws SerializerException {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(B64Code.decode(o));
            return targetType.cast(read(gzip ? new GZIPInputStream(bais) : bais));
        } catch (Exception e) {
            throw new SerializerException("Error deserializing " + o + " : " + e.getMessage(), e);
        }
    }

    protected abstract void write(OutputStream out, Object o) throws Exception;

    protected abstract Object read(InputStream is) throws Exception;
}
