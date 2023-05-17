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

import co.elastic.apm.android.sdk.internal.features.storage.serialization.common.models.ProtoAttributes;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.mapping.Converter;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.mapping.Mapper;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributeType;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.KeyValue;

public class ProtoAttributesConverter extends Converter<ProtoAttributes, Attributes> {

    @Override
    protected Attributes doConvert(Mapper mapper, ProtoAttributes from) {
        AttributesBuilder builder = Attributes.builder();

        for (KeyValue item : from.values) {
            AnyValue value = item.getValue();
            if (value.hasArrayValue()) {
                addArray(builder, item.getKey(), value.getArrayValue());
            } else {
                addSimpleValue(builder, item.getKey(), value);
            }
        }

        return builder.build();
    }

    private void addSimpleValue(AttributesBuilder builder, String key, AnyValue value) {
        if (value.hasStringValue()) {
            builder.put(AttributeKey.stringKey(key), value.getStringValue());
        } else if (value.hasBoolValue()) {
            builder.put(AttributeKey.booleanKey(key), value.getBoolValue());
        } else if (value.hasIntValue()) {
            builder.put(AttributeKey.longKey(key), value.getIntValue());
        } else if (value.hasDoubleValue()) {
            builder.put(AttributeKey.doubleKey(key), value.getDoubleValue());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private void addArray(AttributesBuilder builder, String key, ArrayValue arrayValue) {
        List<AnyValue> valuesList = arrayValue.getValuesList();
        AttributeType type = getSimpleType(valuesList.get(0));
        switch (type) {
            case STRING:
                addStringArray(builder, key, valuesList);
                break;
            case BOOLEAN:
                addBooleanArray(builder, key, valuesList);
                break;
            case LONG:
                addLongArray(builder, key, valuesList);
                break;
            case DOUBLE:
                addDoubleArray(builder, key, valuesList);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private void addDoubleArray(AttributesBuilder builder, String key, List<AnyValue> list) {
        List<Double> items = new ArrayList<>();
        for (AnyValue anyValue : list) {
            items.add(anyValue.getDoubleValue());
        }
        builder.put(AttributeKey.doubleArrayKey(key), items);
    }

    private void addLongArray(AttributesBuilder builder, String key, List<AnyValue> list) {
        List<Long> items = new ArrayList<>();
        for (AnyValue anyValue : list) {
            items.add(anyValue.getIntValue());
        }
        builder.put(AttributeKey.longArrayKey(key), items);
    }

    private void addBooleanArray(AttributesBuilder builder, String key, List<AnyValue> list) {
        List<Boolean> items = new ArrayList<>();
        for (AnyValue anyValue : list) {
            items.add(anyValue.getBoolValue());
        }
        builder.put(AttributeKey.booleanArrayKey(key), items);
    }

    private void addStringArray(AttributesBuilder builder, String key, List<AnyValue> list) {
        List<String> items = new ArrayList<>();
        for (AnyValue anyValue : list) {
            items.add(anyValue.getStringValue());
        }
        builder.put(AttributeKey.stringArrayKey(key), items);
    }

    private AttributeType getSimpleType(AnyValue value) {
        if (value.hasStringValue()) {
            return AttributeType.STRING;
        } else if (value.hasBoolValue()) {
            return AttributeType.BOOLEAN;
        } else if (value.hasIntValue()) {
            return AttributeType.LONG;
        } else if (value.hasDoubleValue()) {
            return AttributeType.DOUBLE;
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
