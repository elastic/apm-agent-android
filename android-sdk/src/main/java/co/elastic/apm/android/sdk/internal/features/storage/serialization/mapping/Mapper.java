/*
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package co.elastic.apm.android.sdk.internal.features.storage.serialization.mapping;

import java.util.HashMap;
import java.util.Map;

import co.elastic.apm.android.sdk.internal.features.storage.serialization.common.mapping.AttributesConverter;
import io.opentelemetry.api.common.Attributes;

@SuppressWarnings("unchecked")
public class Mapper {
    private final Map<Class<?>, Converter<?, ?>> converters;

    public Mapper(Map<Class<?>, Converter<?, ?>> converters) {
        this.converters = converters;
    }

    public <RESULT> RESULT map(Object item) {
        return (RESULT) findConverter(item.getClass()).convert(this, item);
    }

    private <RESULT> Converter<?, RESULT> findConverter(Class<?> original) {
        Converter<?, ?> converter = converters.get(original);
        if (converter == null) {
            for (Class<?> aClass : converters.keySet()) {
                if (aClass.isAssignableFrom(original)) {
                    converter = converters.get(aClass);
                    break;
                }
            }
        }
        return (Converter<?, RESULT>) converter;
    }

    public static Mapper createDefault() {
        Map<Class<?>, Converter<?, ?>> map = new HashMap<>();
        map.put(Attributes.class, new AttributesConverter());
        return new Mapper(map);
    }
}