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

import com.ovea.jetty.session.Serializer;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public abstract class SerializerSkeleton implements Serializer {
    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public String serialize(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}
