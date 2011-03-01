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

import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
@RunWith(JUnit4.class)
public final class XStreamSerializerTest {

    private transient XStreamSerializer serializer = new XStreamSerializer();

    private int a = 1;
    private transient int b = 1;

    @Test
    public void test() throws Exception {
        serializer.start();
        
        a = 2;
        b = 2;
        XStreamSerializerTest c = round(this);
        assertEquals(2, c.a);
        assertEquals(0, c.b);
        //round(new JFrame());
    }

    private <T> T round(T obj) {
        String data = serializer.serialize(obj);
        System.out.println(data);
        return (T) serializer.deserialize(data, obj.getClass());
    }
}