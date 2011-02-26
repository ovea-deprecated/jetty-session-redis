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
package com.ovea.jetty.session;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
@RunWith(JUnit4.class)
public final class ClusteringTest {

    @Test
    public void test() throws Exception {
        Thread.sleep(10000);
    }

    @BeforeClass
    public static void startRedis() throws Exception {
        redis.start();
    }

    @AfterClass
    public static void stopRedis() throws Exception {
        redis.stop();
    }

    @Before
    public void startJetty() throws Exception {
        container1.start();
        container2.start();
    }

    @After
    public void stopJetty() throws Exception {
        container1.stop();
        container2.stop();
    }

    static final RedisProcess redis = new RedisProcess();
    final JettyServer container1 = new JettyServer("src/test/webapp1");
    final JettyServer container2 = new JettyServer("src/test/webapp2");

}
