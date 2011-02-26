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
