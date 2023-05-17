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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static co.elastic.apm.android.sdk.testutils.ListUtils.listOf;

import androidx.annotation.NonNull;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import co.elastic.apm.android.sdk.internal.features.storage.serialization.common.models.ProtoAttributes;
import co.elastic.apm.android.sdk.testutils.BaseConverterTest;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.KeyValue;

public class ProtoAttributesConverterTest extends BaseConverterTest {

    @Test
    public void verifyStringConversion() {
        KeyValue keyValue = keyValue("someKey", stringValue("someValue"));
        ProtoAttributes attributes = new ProtoAttributes(listOf(keyValue));

        Attributes result = map(attributes);

        assertEquals(1, result.size());
        assertEquals("someValue", result.get(AttributeKey.stringKey("someKey")));
    }

    @Test
    public void verifyMultipleItemsConversion() {
        KeyValue keyValue = keyValue("someKey", stringValue("someValue"));
        KeyValue keyValue2 = keyValue("someOtherKey", booleanValue(true));
        ProtoAttributes attributes = new ProtoAttributes(listOf(keyValue, keyValue2));

        Attributes result = map(attributes);

        assertEquals(2, result.size());
        assertEquals("someValue", result.get(AttributeKey.stringKey("someKey")));
        assertEquals(true, result.get(AttributeKey.booleanKey("someOtherKey")));
    }

    @Test
    public void verifyBooleanConversion() {
        KeyValue keyValue = keyValue("someKey", booleanValue(true));
        ProtoAttributes attributes = new ProtoAttributes(listOf(keyValue));

        Attributes result = map(attributes);

        assertEquals(1, result.size());
        assertEquals(true, result.get(AttributeKey.booleanKey("someKey")));
    }

    @Test
    public void verifyLongConversion() {
        KeyValue keyValue = keyValue("someKey", longValue(12));
        ProtoAttributes attributes = new ProtoAttributes(listOf(keyValue));

        Attributes result = map(attributes);

        assertEquals(1, result.size());
        assertEquals(12L, (long) result.get(AttributeKey.longKey("someKey")));
    }

    @Test
    public void verifyDoubleConversion() {
        KeyValue keyValue = keyValue("someKey", doubleValue(15));
        ProtoAttributes attributes = new ProtoAttributes(listOf(keyValue));

        Attributes result = map(attributes);

        assertEquals(1, result.size());
        assertEquals(15.0, result.get(AttributeKey.doubleKey("someKey")), 0);
    }

    @Test
    public void verifyStringArrayConversion() {
        KeyValue keyValue = keyValue("someKey", stringArrayValue(listOf("oneString", "secondString")));
        ProtoAttributes attributes = new ProtoAttributes(listOf(keyValue));

        Attributes result = map(attributes);

        assertEquals(1, result.size());
        List<String> list = result.get(AttributeKey.stringArrayKey("someKey"));
        assertEquals(2, list.size());
        assertEquals("oneString", list.get(0));
        assertEquals("secondString", list.get(1));
    }

    @Test
    public void verifyBooleanArrayConversion() {
        KeyValue keyValue = keyValue("someKey", booleanArrayValue(listOf(true, false)));
        ProtoAttributes attributes = new ProtoAttributes(listOf(keyValue));

        Attributes result = map(attributes);

        assertEquals(1, result.size());
        List<Boolean> list = result.get(AttributeKey.booleanArrayKey("someKey"));
        assertEquals(2, list.size());
        assertTrue(list.get(0));
        assertFalse(list.get(1));
    }

    @Test
    public void verifyLongArrayConversion() {
        KeyValue keyValue = keyValue("someKey", longArrayValue(listOf(5L, 10L)));
        ProtoAttributes attributes = new ProtoAttributes(listOf(keyValue));

        Attributes result = map(attributes);

        assertEquals(1, result.size());
        List<Long> list = result.get(AttributeKey.longArrayKey("someKey"));
        assertEquals(2, list.size());
        assertEquals(5L, (long) list.get(0));
        assertEquals(10L, (long) list.get(1));
    }

    @Test
    public void verifyDoubleArrayConversion() {
        KeyValue keyValue = keyValue("someKey", doubleArrayValue(listOf(5.0, 15.0)));
        ProtoAttributes attributes = new ProtoAttributes(listOf(keyValue));

        Attributes result = map(attributes);

        assertEquals(1, result.size());
        List<Double> list = result.get(AttributeKey.doubleArrayKey("someKey"));
        assertEquals(2, list.size());
        assertEquals(5.0, list.get(0), 0);
        assertEquals(15.0, list.get(1), 0);
    }

    @NonNull
    private KeyValue keyValue(String key, AnyValue value) {
        return KeyValue.newBuilder().setKey(key).setValue(value).build();
    }

    private AnyValue stringValue(String value) {
        return AnyValue.newBuilder().setStringValue(value).build();
    }

    private AnyValue longValue(long value) {
        return AnyValue.newBuilder().setIntValue(value).build();
    }

    private AnyValue booleanValue(boolean value) {
        return AnyValue.newBuilder().setBoolValue(value).build();
    }

    private AnyValue doubleValue(double value) {
        return AnyValue.newBuilder().setDoubleValue(value).build();
    }

    private AnyValue stringArrayValue(List<String> values) {
        List<AnyValue> anyValues = new ArrayList<>();
        values.forEach(value -> anyValues.add(stringValue(value)));
        return arrayValue(anyValues);
    }

    private AnyValue booleanArrayValue(List<Boolean> values) {
        List<AnyValue> anyValues = new ArrayList<>();
        values.forEach(value -> anyValues.add(booleanValue(value)));
        return arrayValue(anyValues);
    }

    private AnyValue longArrayValue(List<Long> values) {
        List<AnyValue> anyValues = new ArrayList<>();
        values.forEach(value -> anyValues.add(longValue(value)));
        return arrayValue(anyValues);
    }

    private AnyValue doubleArrayValue(List<Double> values) {
        List<AnyValue> anyValues = new ArrayList<>();
        values.forEach(value -> anyValues.add(doubleValue(value)));
        return arrayValue(anyValues);
    }

    private AnyValue arrayValue(List<AnyValue> values) {
        ArrayValue arrayValue = ArrayValue.newBuilder().addAllValues(values).build();
        return AnyValue.newBuilder().setArrayValue(arrayValue).build();
    }
}
