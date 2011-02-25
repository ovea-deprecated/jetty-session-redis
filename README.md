# Jetty session clustering with REDIS

REDIS: http://redis.io/

## Build instruction:

 git clone git://github.com/Ovea/jetty-session-redis.git
 jetty-session-redis
 mvn package

## Installation

Put the JAR file in Jetty extension folder

## Configuration

In Jetty server configuration file (i.e. jetty.xml):

    <New class="org.eclipse.jetty.plus.jndi.Resource">
        <Arg>redis/jaxspot</Arg>
        <Arg>
            <New class="redis.clients.jedis.JedisPool">
                <Arg>
                    <New class="org.apache.commons.pool.impl.GenericObjectPool$Config">
                        <Set type="int" name="minIdle">4</Set>
                        <Set type="int" name="maxActive">10</Set>
                        <Set type="boolean" name="testOnBorrow">true</Set>
                    </New>
                </Arg>
                <Arg>127.0.0.1</Arg>
                <Arg type="int">6379</Arg>
            </New>
        </Arg>
    </New>

In each web application context file using session clustering (i.e. in WEB-INF/jetty-env.xml):

