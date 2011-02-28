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
                <!-- time interval to check for expired sessions in redis cache, in milliseconds. Defaults to 1min -->
                <Set name="scavengerInterval">60000</Set>
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

    <Configure class="org.eclipse.jetty.webapp.WebAppContext">
    
        <Get name="server">
            <Get id="RedisSessionIdManager" name="sessionIdManager"/>
        </Get>
        <Set name="sessionHandler">
            <New class="org.eclipse.jetty.server.session.SessionHandler">
                <Arg>
                    <New class="com.ovea.jetty.session.redis.RedisSessionManager">
                        <Arg>session/redis</Arg>
                        <!-- set the interval in seconds to force session persistence event if it didn't changed. Default to 60 seconds -->
                        <Set name="saveInterval">60</Set>
                        <Set name="idManager">
                            <Ref id="RedisSessionIdManager"/>
                        </Set>
                    </New>
                </Arg>
            </New>
        </Set>

    </Configure>


## Controlling session serialization

By default, session attributes are serialized in JSON using Jackson. This serialization type is fast and reliable as long as you have really simple types in your session. You can change this behavior and use one of the provided Serializer:

* com.ovea.jetty.session.serializer.JsonSerializer
* com.ovea.jetty.session.serializer.JdkSerializer
* com.ovea.jetty.session.serializer.XStreamSerializer
* com.ovea.jetty.session.serializer.JbossSerializer

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

You can also create your own ones (<a href="https://github.com/Ovea/jetty-session-redis/tree/master/src/main/java/com/ovea/jetty/session/serializer">see examples here</a>).
