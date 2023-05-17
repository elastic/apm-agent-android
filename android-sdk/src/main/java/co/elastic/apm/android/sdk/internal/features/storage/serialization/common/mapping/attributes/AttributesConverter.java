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
package co.elastic.apm.android.sdk.internal.features.storage.serialization.common.mapping.attributes;

import java.util.ArrayList;
import java.util.List;

import co.elastic.apm.android.sdk.internal.features.storage.serialization.mapping.Converter;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.mapping.Mapper;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.KeyValue;

public class AttributesConverter extends Converter<Attributes, List<KeyValue>> {

    @Override
    public List<KeyValue> doConvert(Mapper mapper, Attributes from) {
        List<KeyValue> list = new ArrayList<>();
        from.forEach((attributeKey, o) -> list.add(createKeyValue(attributeKey, o)));
        return list;
    }

    private static KeyValue createKeyValue(AttributeKey<?> attributeKey, Object o) {
        switch (attributeKey.getType()) {
            case STRING:
                return createStringKeyValue(attributeKey.getKey(), o);
            case BOOLEAN:
                return createBooleanKeyValue(attributeKey.getKey(), o);
            case LONG:
                return createLongKeyValue(attributeKey.getKey(), o);
            case DOUBLE:
                return createDoubleKeyValue(attributeKey.getKey(), o);
            case STRING_ARRAY:
                return createStringArrayKeyValue(attributeKey.getKey(), (List<Object>) o);
            case BOOLEAN_ARRAY:
                return createBooleanArrayKeyValue(attributeKey.getKey(), (List<Object>) o);
            case LONG_ARRAY:
                return createLongArrayKeyValue(attributeKey.getKey(), (List<Object>) o);
            case DOUBLE_ARRAY:
                return createDoubleArrayKeyValue(attributeKey.getKey(), (List<Object>) o);
            default:
                throw new UnsupportedOperationException();
        }
    }

    private static KeyValue createStringArrayKeyValue(String key, List<Object> items) {
        List<AnyValue> values = new ArrayList<>();
        items.forEach(o -> values.add(createStringValue(o)));
        return keyValueBuilder(key)
                .setValue(createArrayValue(values))
                .build();
    }

    private static KeyValue createBooleanArrayKeyValue(String key, List<Object> items) {
        List<AnyValue> values = new ArrayList<>();
        items.forEach(o -> values.add(createBooleanValue(o)));
        return keyValueBuilder(key)
                .setValue(createArrayValue(values))
                .build();
    }

    private static KeyValue createLongArrayKeyValue(String key, List<Object> items) {
        List<AnyValue> values = new ArrayList<>();
        items.forEach(o -> values.add(createLongValue(o)));
        return keyValueBuilder(key)
                .setValue(createArrayValue(values))
                .build();
    }

    private static KeyValue createDoubleArrayKeyValue(String key, List<Object> items) {
        List<AnyValue> values = new ArrayList<>();
        items.forEach(o -> values.add(createDoubleValue(o)));
        return keyValueBuilder(key)
                .setValue(createArrayValue(values))
                .build();
    }

    private static KeyValue createStringKeyValue(String key, Object value) {
        return keyValueBuilder(key).setValue(createStringValue(value)).build();
    }

    private static KeyValue createBooleanKeyValue(String key, Object value) {
        return keyValueBuilder(key).setValue(createBooleanValue(value)).build();
    }

    private static KeyValue createLongKeyValue(String key, Object value) {
        return keyValueBuilder(key).setValue(createLongValue(value)).build();
    }

    private static KeyValue createDoubleKeyValue(String key, Object value) {
        return keyValueBuilder(key).setValue(createDoubleValue(value)).build();
    }

    private static KeyValue.Builder keyValueBuilder(String key) {
        return KeyValue.newBuilder().setKey(key);
    }

    private static AnyValue createStringValue(Object value) {
        return AnyValue.newBuilder()
                .setStringValue((String) value)
                .build();
    }

    private static AnyValue createBooleanValue(Object value) {
        return AnyValue.newBuilder()
                .setBoolValue((Boolean) value)
                .build();
    }

    private static AnyValue createLongValue(Object value) {
        return AnyValue.newBuilder()
                .setIntValue((Long) value)
                .build();
    }

    private static AnyValue createDoubleValue(Object value) {
        return AnyValue.newBuilder()
                .setDoubleValue((Double) value)
                .build();
    }

    private static AnyValue createArrayValue(List<AnyValue> values) {
        return AnyValue.newBuilder()
                .setArrayValue(ArrayValue.newBuilder().addAllValues(values).build())
                .build();
    }
}
