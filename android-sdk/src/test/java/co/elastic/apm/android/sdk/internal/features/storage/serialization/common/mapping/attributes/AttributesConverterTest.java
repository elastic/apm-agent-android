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

import org.junit.Test;

import java.util.List;

import co.elastic.apm.android.sdk.testutils.BaseConverterTest;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.KeyValue;

public class AttributesConverterTest extends BaseConverterTest {

    @Test
    public void verifyConversionForStringAttr() {
        Attributes attributes = Attributes.builder()
                .put(AttributeKey.stringKey("someStringKey"), "someStringValue")
                .build();

        List<KeyValue> result = map(attributes);

        assertEquals(1, result.size());
        KeyValue keyValue = result.get(0);
        assertEquals("someStringKey", keyValue.getKey());
        assertEquals("someStringValue", keyValue.getValue().getStringValue());
    }

    @Test
    public void verifyConversionForBooleanAttr() {
        Attributes attributes = Attributes.builder()
                .put(AttributeKey.booleanKey("someKey"), true)
                .build();

        List<KeyValue> result = map(attributes);

        assertEquals(1, result.size());
        KeyValue keyValue = result.get(0);
        assertEquals("someKey", keyValue.getKey());
        assertTrue(keyValue.getValue().getBoolValue());
    }

    @Test
    public void verifyConversionForLongAttr() {
        Attributes attributes = Attributes.builder()
                .put(AttributeKey.longKey("someKey"), 10L)
                .build();

        List<KeyValue> result = map(attributes);

        assertEquals(1, result.size());
        KeyValue keyValue = result.get(0);
        assertEquals("someKey", keyValue.getKey());
        assertEquals(10L, keyValue.getValue().getIntValue());
    }

    @Test
    public void verifyConversionForDoubleAttr() {
        Attributes attributes = Attributes.builder()
                .put(AttributeKey.doubleKey("someKey"), 15.0)
                .build();

        List<KeyValue> result = map(attributes);

        assertEquals(1, result.size());
        KeyValue keyValue = result.get(0);
        assertEquals("someKey", keyValue.getKey());
        assertEquals(15.0, keyValue.getValue().getDoubleValue(), 0);
    }

    @Test
    public void verifyConversionForStringArray() {
        Attributes attributes = Attributes.builder()
                .put(AttributeKey.stringArrayKey("someKey"), listOf("first", "second"))
                .build();

        List<KeyValue> result = map(attributes);

        assertEquals(1, result.size());
        KeyValue keyValue = result.get(0);
        assertEquals("someKey", keyValue.getKey());
        ArrayValue arrayValue = keyValue.getValue().getArrayValue();
        assertEquals(2, arrayValue.getValuesCount());
        assertEquals("first", arrayValue.getValues(0).getStringValue());
        assertEquals("second", arrayValue.getValues(1).getStringValue());
    }

    @Test
    public void verifyConversionForBooleanArray() {
        Attributes attributes = Attributes.builder()
                .put(AttributeKey.booleanArrayKey("someKey"), listOf(true, false))
                .build();

        List<KeyValue> result = map(attributes);

        assertEquals(1, result.size());
        KeyValue keyValue = result.get(0);
        assertEquals("someKey", keyValue.getKey());
        ArrayValue arrayValue = keyValue.getValue().getArrayValue();
        assertEquals(2, arrayValue.getValuesCount());
        assertTrue(arrayValue.getValues(0).getBoolValue());
        assertFalse(arrayValue.getValues(1).getBoolValue());
    }

    @Test
    public void verifyConversionForLongArray() {
        Attributes attributes = Attributes.builder()
                .put(AttributeKey.longArrayKey("someKey"), listOf(2L, 5L))
                .build();

        List<KeyValue> result = map(attributes);

        assertEquals(1, result.size());
        KeyValue keyValue = result.get(0);
        assertEquals("someKey", keyValue.getKey());
        ArrayValue arrayValue = keyValue.getValue().getArrayValue();
        assertEquals(2, arrayValue.getValuesCount());
        assertEquals(2L, arrayValue.getValues(0).getIntValue());
        assertEquals(5L, arrayValue.getValues(1).getIntValue());
    }

    @Test
    public void verifyConversionForDoubleArray() {
        Attributes attributes = Attributes.builder()
                .put(AttributeKey.doubleArrayKey("someKey"), listOf(10.0, 1.0))
                .build();

        List<KeyValue> result = map(attributes);

        assertEquals(1, result.size());
        KeyValue keyValue = result.get(0);
        assertEquals("someKey", keyValue.getKey());
        ArrayValue arrayValue = keyValue.getValue().getArrayValue();
        assertEquals(2, arrayValue.getValuesCount());
        assertEquals(10.0, arrayValue.getValues(0).getDoubleValue(), 0);
        assertEquals(1.0, arrayValue.getValues(1).getDoubleValue(), 0);
    }
}