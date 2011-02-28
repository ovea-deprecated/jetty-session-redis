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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.swing.*;
import java.io.Serializable;

import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
@RunWith(JUnit4.class)
public final class JBossSerializerTest implements Serializable {

    private static final long serialVersionUID = -4758353525859226249L;

    private transient JBossSerializer serializer = new JBossSerializer();

    private int a = 1;
    private transient int b = 1;

    @Test
    public void test() throws Exception {
        a = 2;
        b = 2;
        serializer.setGzip(true);
        JBossSerializerTest c = round(this);
        assertEquals(2, c.a);
        assertEquals(0, c.b);
        round(new JFrame());
    }

    @Test
    public void test_non_gzip() throws Exception {
        a = 2;
        b = 2;
        serializer.setGzip(false);
        JBossSerializerTest c = round(this);
        assertEquals(2, c.a);
        assertEquals(0, c.b);
        round(new JFrame());
    }

    private <T> T round(T obj) {
        String data = serializer.serialize(obj);
        System.out.println("gzip=" + serializer.isGzip() + " : " + data);
        return (T) serializer.deserialize(data, obj.getClass());
    }
}