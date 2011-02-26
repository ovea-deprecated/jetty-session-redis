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
package net.playtouch.jaxspot.module.session.serializer.redis;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import net.playtouch.jaxspot.module.caching.*;
import net.playtouch.jaxspot.module.caching.redis.*;
import net.playtouch.jaxspot.util.properties.PropertySettings;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.introspect.VisibilityChecker;
import redis.clients.jedis.JedisPool;

import javax.inject.Provider;

import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.ANY;
import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.NONE;

public final class RedisCacheModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(JedisPool.class);
        requireBinding(PropertySettings.class);
    }

    @Provides
    @Singleton
    CacheService cacheService(PropertySettings settings, Provider<TransactionalCache<Object, Object, RedisConnection>> cache) {
        if ("memory".equals(settings.getString("caching.system", "redis"))) {
            return new RetainingCacheService(new MemoryConstrainedCacheService());
        } else {
            return new TransactionalCacheService(cache.get());
        }
    }

    @Provides
    TransactionalCache<Object, Object, RedisConnection> transactionalCache(Serializer serializer) {
        return new RedisTransactionalCache<Object, Object>(serializer);
    }

    @Provides
    CacheUnitOfWork cacheUnitOfWork(JedisPool pool) {
        return new RedisCacheUnitOfWork(pool);
    }

    @Provides
    Serializer serializer(ObjectMapper mapper) {
        return new RedisSerializer(mapper);
    }

    @Provides
    @Singleton
    ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.configure(DeserializationConfig.Feature.USE_ANNOTATIONS, false);
        mapper.configure(DeserializationConfig.Feature.AUTO_DETECT_SETTERS, false);
        mapper.configure(DeserializationConfig.Feature.AUTO_DETECT_CREATORS, true);
        mapper.configure(DeserializationConfig.Feature.AUTO_DETECT_FIELDS, true);
        mapper.configure(DeserializationConfig.Feature.USE_GETTERS_AS_SETTERS, false);
        mapper.configure(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS, true);
        mapper.configure(DeserializationConfig.Feature.WRAP_ROOT_VALUE, false);

        mapper.configure(SerializationConfig.Feature.USE_ANNOTATIONS, false);
        mapper.configure(SerializationConfig.Feature.AUTO_DETECT_GETTERS, false);
        mapper.configure(SerializationConfig.Feature.AUTO_DETECT_IS_GETTERS, false);
        mapper.configure(SerializationConfig.Feature.AUTO_DETECT_FIELDS, true);
        mapper.configure(SerializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS, true);
        mapper.configure(SerializationConfig.Feature.USE_STATIC_TYPING, false);
        mapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, false);

        mapper.setVisibilityChecker(new VisibilityChecker.Std(NONE, NONE, NONE, ANY, ANY));

        return mapper;
    }

}
