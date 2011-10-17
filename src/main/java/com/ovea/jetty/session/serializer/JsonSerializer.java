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

import com.ovea.jetty.session.SerializerException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.introspect.AnnotatedField;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.map.introspect.VisibilityChecker;

import java.io.IOException;

import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.ANY;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class JsonSerializer extends SerializerSkeleton {

    private ObjectMapper mapper;

    @Override
    public void start() {
        mapper = new ObjectMapper();

        mapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, false);
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, false);
        mapper.configure(SerializationConfig.Feature.AUTO_DETECT_GETTERS, false);
        mapper.configure(SerializationConfig.Feature.AUTO_DETECT_IS_GETTERS, false);
        mapper.configure(SerializationConfig.Feature.AUTO_DETECT_FIELDS, true);
        mapper.configure(SerializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS, true);
        mapper.configure(SerializationConfig.Feature.USE_STATIC_TYPING, false);
        mapper.configure(SerializationConfig.Feature.WRITE_ENUMS_USING_TO_STRING, false);
        mapper.configure(SerializationConfig.Feature.SORT_PROPERTIES_ALPHABETICALLY, true);
        mapper.configure(SerializationConfig.Feature.USE_ANNOTATIONS, true);

        mapper.configure(DeserializationConfig.Feature.USE_ANNOTATIONS, true);
        mapper.configure(DeserializationConfig.Feature.AUTO_DETECT_SETTERS, false);
        mapper.configure(DeserializationConfig.Feature.AUTO_DETECT_CREATORS, true);
        mapper.configure(DeserializationConfig.Feature.AUTO_DETECT_FIELDS, true);
        mapper.configure(DeserializationConfig.Feature.USE_GETTERS_AS_SETTERS, false);
        mapper.configure(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS, true);
        mapper.configure(DeserializationConfig.Feature.READ_ENUMS_USING_TO_STRING, true);

        mapper.setVisibilityChecker(new VisibilityChecker.Std(ANY, ANY, ANY, ANY, ANY));

        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        mapper = null;
    }

    @Override
    public String serialize(Object o) throws SerializerException {
        try {
            return mapper.writeValueAsString(o);
        } catch (IOException e) {
            throw new SerializerException(e);
        }
    }

    @Override
    public <T> T deserialize(String o, Class<T> targetType) throws SerializerException {
        try {
            return mapper.readValue(o, targetType);
        } catch (IOException e) {
            throw new SerializerException(e);
        }
    }
}
