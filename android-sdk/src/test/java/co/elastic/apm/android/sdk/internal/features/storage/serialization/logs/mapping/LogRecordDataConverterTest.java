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
package co.elastic.apm.android.sdk.internal.features.storage.serialization.logs.mapping;

import static org.junit.Assert.assertEquals;
import static co.elastic.apm.android.sdk.testdata.LogRecordDataUtil.createLogRecordData;

import org.junit.Test;

import java.util.List;

import co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord;
import co.elastic.apm.android.sdk.testutils.BaseConverterTest;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;

public class LogRecordDataConverterTest extends BaseConverterTest {

    @Test
    public void verifyConversion() {
        InstrumentationScopeInfo scope = InstrumentationScopeInfo.create("scopeName");
        Resource resource = Resource.create(Attributes.of(AttributeKey.stringKey("resourceAttr"), "resourceValue"));
        String traceId = "b535b3b5232b5dabced5b0ab8037eb78";
        String spanId = "f3fc364fb6b77cff";
        LogRecordData logRecordData = createLogRecordData(resource, scope, "some body", Attributes.of(AttributeKey.stringKey("someAttr"), "someValue"),
                1234, traceId, spanId, Severity.DEBUG3, "some severity text");

        LogRecord result = map(logRecordData);

        assertEquals("some body", result.getBody().getStringValue());
        List<KeyValue> attributesList = result.getAttributesList();
        assertEquals(1, attributesList.size());
        KeyValue keyValue = attributesList.get(0);
        assertEquals("someAttr", keyValue.getKey());
        assertEquals("someValue", keyValue.getValue().getStringValue());
        assertEquals(1234, result.getTimeUnixNano());
        assertEquals(Severity.DEBUG3.getSeverityNumber(), result.getSeverityNumber().getNumber());
        assertEquals("some severity text", result.getSeverityText());
        assertEquals(traceId, result.getTraceId().toStringUtf8());
        assertEquals(spanId, result.getSpanId().toStringUtf8());
    }

    @Test
    public void verifyConversionWithSeverityTextNull() {
        InstrumentationScopeInfo scope = InstrumentationScopeInfo.create("scopeName");
        Resource resource = Resource.create(Attributes.of(AttributeKey.stringKey("resourceAttr"), "resourceValue"));
        String traceId = "b535b3b5232b5dabced5b0ab8037eb78";
        String spanId = "f3fc364fb6b77cff";
        LogRecordData logRecordData = createLogRecordData(resource, scope, "some body", Attributes.of(AttributeKey.stringKey("someAttr"), "someValue"),
                1234, traceId, spanId, Severity.DEBUG3, null);

        LogRecord result = map(logRecordData);

        assertEquals("some body", result.getBody().getStringValue());
        List<KeyValue> attributesList = result.getAttributesList();
        assertEquals(1, attributesList.size());
        KeyValue keyValue = attributesList.get(0);
        assertEquals("someAttr", keyValue.getKey());
        assertEquals("someValue", keyValue.getValue().getStringValue());
        assertEquals(1234, result.getTimeUnixNano());
        assertEquals(Severity.DEBUG3.getSeverityNumber(), result.getSeverityNumber().getNumber());
        assertEquals("", result.getSeverityText());
        assertEquals(traceId, result.getTraceId().toStringUtf8());
        assertEquals(spanId, result.getSpanId().toStringUtf8());
    }
}
