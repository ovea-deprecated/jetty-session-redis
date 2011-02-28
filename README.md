# Jetty session clustering with REDIS

* [REDIS website](http://redis.io/) (tested on version 2.2.1)
* [JEDIS website](https://github.com/xetorthio/jedis) (tested on version 1.5.2)
* [Jetty website](http://www.eclipse.org/jetty/) (tested on version 8.0.0.M2)

## Build instruction:

    git clone git://github.com/Ovea/jetty-session-redis.git
    jetty-session-redis
    mvn package

## Installation

You can simply put the BUNDLE "*-all.jar" (i.e.jetty-session-redis-1.0-SNAPSHOT-all.jar) in the Jetty lib/ext foder. This bundle contains all the required dependencies, repackadged internally to not interfer with potential other versions on the classpath.

If you want to upgrade a dependency (i.e. Jedis), you may want to use instead the JAR version (jetty-session-redis-1.0-SNAPSHOT.jar), and you'll have to put also in Jetty lib/ext foder all the required dependencies:

* jedis
* commons-pool
* xstream (optional)
* jackson mapper (optional)

## Configuration

In Jetty server configuration file (i.e. jetty.xml):

    <?xml version="1.0"?>
    <!DOCTYPE Configure PUBLIC "-//Jetty//Configure//EN" "http://www.eclipse.org/jetty/configure.dtd">
    <Configure id="Server" class="org.eclipse.jetty.server.Server">

        <!--
            Configure session id management
        -->
        <Set name="sessionIdManager">
            <New class="com.ovea.jetty.session.redis.RedisSessionIdManager">
                <Arg>
                    <Ref id="Server"/>
                </Arg>
                <Arg>session/redis</Arg>
                <!-- time interval to check for expired sessions in redis cache, in milliseconds. Defaults to 1 min -->
                <Set name="scavengerInterval">30000</Set>
                <!-- cluster node name -->
                <Set name="workerName">
                    <SystemProperty name="jetty.node" default="node1"/>
                </Set>
            </New>
        </Set>

        <!--
            Provides a Redis Pool for session management on server and each webapp
        -->
        <New class="org.eclipse.jetty.plus.jndi.Resource">
            <Arg>session/redis</Arg>
            <Arg>
                <New class="redis.clients.jedis.JedisPool">
                    <Arg>
                        <New class="org.apache.commons.pool.impl.GenericObjectPool$Config">
                            <Set type="int" name="minIdle">5</Set>
                            <Set type="int" name="maxActive">15</Set>
                            <Set type="boolean" name="testOnBorrow">true</Set>
                        </New>
                    </Arg>
                    <Arg>127.0.0.1</Arg>
                    <Arg type="int">6379</Arg>
                </New>
            </Arg>
        </New>

    </Configure>

In each web application context file using session clustering (i.e. in WEB-INF/jetty-env.xml):

    <Configure id="webappContext" class="org.eclipse.jetty.webapp.WebAppContext">

        <Set name="contextPath">/webapp1</Set>

        <Get name="server">
            <Get id="RedisSessionIdManager" name="sessionIdManager"/>
        </Get>
        <Set name="sessionHandler">
            <New class="org.eclipse.jetty.server.session.SessionHandler">
                <Arg>
                    <New class="com.ovea.jetty.session.redis.RedisSessionManager">
                        <Arg>session/redis</Arg>
                        <Arg>
                            <New class="com.ovea.jetty.session.serializer.JsonSerializer"/>
                        </Arg>
                        <Set name="idManager">
                            <Ref id="RedisSessionIdManager"/>
                        </Set>
                        <!-- set the interval in seconds to force session persistence event if it didn't changed. Default to 60 seconds -->
                        <Set name="saveInterval">20</Set>
                        <!-- set the cookie domain -->
                        <Set name="sessionDomain">127.0.0.1</Set>
                        <!-- set the cookie path -->
                        <Set name="sessionPath">/</Set>
                        <!-- set the cookie max age in seconds. Default is -1 (no max age). 1 day = 86400 seconds -->
                        <Set name="cookieMaxAge">86400</Set>
                        <!-- set the interval in seconds to refresh the cookie max age. Default to 0. This number should be lower than the session expirity time. -->
                        <Set name="refreshCookieAge">300</Set>
                    </New>
                </Arg>
            </New>
        </Set>

    </Configure>

Note: Jetty's default for cookieMaxAge is -1 and as per my tests, setting it to a too short value may cause issues in session retrieval.

## Controlling session serialization

By default, session attributes are serialized using XStream, but this is clearly the worst serializer and you must make sure that you configure the serializer according to your needs.
If you have small sessions with simple types, consider the <strong>JsonSerializer</strong>. If you have complexe objects but all serializable, you can consider the <strong>JbossSerializer</strong>.
You can also create your own ones byt implementing the <strong>Serializer</strong> class of a provided skeleton (<a href="https://github.com/Ovea/jetty-session-redis/tree/master/src/main/java/com/ovea/jetty/session/serializer">see examples here</a>).

Here is the list of provided Serializer:

* com.ovea.jetty.session.serializer.JsonSerializer
* com.ovea.jetty.session.serializer.JdkSerializer
* com.ovea.jetty.session.serializer.XStreamSerializer
* com.ovea.jetty.session.serializer.JBossSerializer

     <Set name="sessionHandler">
        <New class="org.eclipse.jetty.server.session.SessionHandler">
            <Arg>
                <New class="com.ovea.jetty.session.redis.RedisSessionManager">
                    <Arg>session/redis</Arg>
                    <Arg>
                        <New class="com.ovea.jetty.session.serializer.XStreamSerializer"/>
                    </Arg>
                    <Set name="idManager">
                        <Ref id="RedisSessionIdManager"/>
                    </Set>
                </New>
            </Arg>
        </New>
    </Set>

## Authors and help

* <strong>Mathieu Carbou</strong> [mail](mailto:mathieu.carbou@gmail.com) | [blog](http://blog.mycila.com/) | [website](http://www.mycila.com/)
