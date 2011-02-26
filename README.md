# Jetty session clustering with REDIS

* [REDIS website](http://redis.io/) (tested on version 2.2.1)
* [JEDIS website](https://github.com/xetorthio/jedis) (tested on version 1.5.2)
* [Jetty website](http://www.eclipse.org/jetty/) (tested on version 8.0.0.M2)

## Build instruction:

    git clone git://github.com/Ovea/jetty-session-redis.git
    jetty-session-redis
    mvn package

## Installation

Put the jar files on the server lib/ext folder:

* jedis
* commons-pool
* jetty-session-redis

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
                <Set name="scavengerInterval">60000</Set>
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

