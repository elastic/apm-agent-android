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

import co.elastic.apm.android.sdk.internal.features.storage.serialization.common.mapping.attributes.AttributesConverter;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.common.mapping.attributes.ProtoAttributesConverter;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.common.mapping.instrumentationscopeinfo.InstrumentationScopeInfoConverter;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.common.mapping.instrumentationscopeinfo.ProtoInstrumentationScopeConverter;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.common.mapping.resource.ProtoResourceConverter;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.common.mapping.resource.ResourceConverter;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.common.models.ProtoAttributes;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.common.models.ProtoInstrumentationScope;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.common.models.ProtoResource;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.logs.mapping.LogRecordDataConverter;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.logs.mapping.collection.LogCollectionConverter;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.logs.mapping.collection.LogsDataConverter;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.logs.models.LogCollection;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;

@SuppressWarnings("unchecked")
public class Mapper {
    private static Mapper INSTANCE;
    private final Map<Class<?>, Converter<?, ?>> converters;

    public static Mapper get() {
        if (INSTANCE == null) {
            INSTANCE = create();
        }
        return INSTANCE;
    }

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
        if (converter == null) {
            throw new UnsupportedOperationException("Could not find converter for " + original);
        }
        return (Converter<?, RESULT>) converter;
    }

    private static Mapper create() {
        Map<Class<?>, Converter<?, ?>> map = new HashMap<>();
        map.put(Attributes.class, new AttributesConverter());
        map.put(LogCollection.class, new LogCollectionConverter());
        map.put(InstrumentationScopeInfo.class, new InstrumentationScopeInfoConverter());
        map.put(Resource.class, new ResourceConverter());
        map.put(LogRecordData.class, new LogRecordDataConverter());
        map.put(LogsData.class, new LogsDataConverter());
        map.put(ProtoAttributes.class, new ProtoAttributesConverter());
        map.put(ProtoResource.class, new ProtoResourceConverter());
        map.put(ProtoInstrumentationScope.class, new ProtoInstrumentationScopeConverter());
        return new Mapper(map);
    }
}
