# Jetty session clustering with REDIS

* [REDIS website](http://redis.io/) (tested on version 2.2.1)
* [JEDIS website](https://github.com/xetorthio/jedis) (tested on version 1.5.2)
* [Jetty website](http://www.eclipse.org/jetty/) (tested on version 8.0.0.M2)

## Build instruction:

    git clone git://github.com/Ovea/jetty-session-redis.git
    jetty-session-redis
    mvn package

## Download

All downloads are [here](https://github.com/Ovea/jetty-session-redis/downloads). They will also be available in Maven central repository and also in Sonatype OSS repositories soon:

* [Snapshots - in Sonatype OSS Repository](https://oss.sonatype.org/content/repositories/snapshots/com/ovea/jetty-session-redis/)
* [Releases - in Sonatype OSS Repository](https://oss.sonatype.org/content/repositories/releases/com/ovea/jetty-session-redis/)
* [Releases - in Maven Central Repository](http://repo2.maven.org/maven2/com/ovea/jetty-session-redis/)

See the section below to know which package you need (the jar file or the -all bundle).

## Installation

You need to put in Jetty's lib/ext folder:

* jedis
* commons-pool

and one of the following JAR:

* jetty-session-redis-X.Y.jar (if you are going to put all serializer dependencies also as independant jar files)
* jetty-session-redis-X.Y-all.jar (contains already packaged-relocated serializers)

I strongly recommand you use the jetty-session-redis-X.Y-all.jar because some serializers (like JBoss Serializer) have been improved for performance.

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

        <Set name="sessionHandler">
            <New class="org.eclipse.jetty.server.session.SessionHandler">
                <Arg>
                    <New class="com.ovea.jetty.session.redis.RedisSessionManager">
                        <Arg>session/redis</Arg>
                        <Arg>
                            <New class="com.ovea.jetty.session.serializer.JsonSerializer"/>
                        </Arg>
                        <!-- set the interval in seconds to force session persistence event if it didn't changed. Default to 60 seconds -->
                        <Set name="saveInterval">20</Set>
                        <!-- set the cookie domain -->
                        <Set name="sessionDomain">127.0.0.1</Set>
                        <!-- set the cookie path -->
                        <Set name="sessionPath">/</Set>
                        <!-- set the cookie max age in seconds. Default is -1 (no max age). 1 day = 86400 seconds -->
                        <Set name="maxCookieAge">86400</Set>
                        <!-- set the interval in seconds to refresh the cookie max age. Default to 0. This number should be lower than the session expirity time. -->
                        <Set name="refreshCookieAge">300</Set>
                    </New>
                </Arg>
            </New>
        </Set>

    </Configure>

Note: Jetty's default for maxCookieAge is -1 and as per my tests, setting it to a too short value may cause issues in session retrieval.

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

## Debugging

If you need to troobleshoot something, you can put Jetty in DEBUG mode and see the traces from RedisSessionManager and RedisSessionIdManager.

Also, with Redis you have the ability to monitor all the calls. Simply issue in a command-line:

    redis-cli monitor

To see all Redis requests going to the Redis server. If you are using a String serializer such as XStream of Json, you'll be able to see all your session attributes into.

## Authors and help

* <strong>Mathieu Carbou</strong> [mail](mailto:mathieu.carbou@gmail.com) | [blog](http://blog.mycila.com/) | [website](http://www.mycila.com/)

## TODO

* Add asynchronous support for save tasks to not slow requests
* Support save queues for each session to only take newer save requests
* byte[] serialization and access for Redis (avoid B64, option to keep strings)
