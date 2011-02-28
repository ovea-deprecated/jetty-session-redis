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
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.XmlHeaderAwareReader;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.io.xml.AbstractXmlDriver;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;
import com.thoughtworks.xstream.io.xml.XppReader;

import java.io.*;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class XStreamSerializer extends SerializerSkeleton {

    private XStream xStream;

    @Override
    public void start() {
        xStream = new XStream(new AbstractXmlDriver(new XmlFriendlyReplacer()) {
            @Override
            public HierarchicalStreamReader createReader(Reader in) {
                return new XppReader(in, xmlFriendlyReplacer());
            }

            @Override
            public HierarchicalStreamReader createReader(InputStream in) {
                try {
                    return createReader(new XmlHeaderAwareReader(in));
                } catch (UnsupportedEncodingException e) {
                    throw new StreamException(e);
                } catch (IOException e) {
                    throw new StreamException(e);
                }
            }

            @Override
            public HierarchicalStreamWriter createWriter(Writer out) {
                return new CompactWriter(out, xmlFriendlyReplacer());
            }

            @Override
            public HierarchicalStreamWriter createWriter(OutputStream out) {
                return createWriter(new OutputStreamWriter(out));
            }
        });
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        xStream = null;
    }

    @Override
    public String serialize(Object o) throws SerializerException {
        return xStream.toXML(o);
    }

    @Override
    public <T> T deserialize(String o, Class<T> targetType) throws SerializerException {
        return targetType.cast(xStream.fromXML(o));
    }
}
