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
package co.elastic.apm.android.sdk.internal.features.storage.serialization.logs.mapping.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static co.elastic.apm.android.sdk.testdata.LogRecordDataGenerator.SPAN_ID;
import static co.elastic.apm.android.sdk.testdata.LogRecordDataGenerator.TRACE_ID;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import co.elastic.apm.android.sdk.internal.features.storage.serialization.logs.models.LogCollection;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.KeyValue;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.SeverityNumber;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource;
import co.elastic.apm.android.sdk.testutils.BaseConverterTest;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;

public class LogsDataConverterTest extends BaseConverterTest {

    @Test
    public void verifyConversionData() {
        LogRecord log = getLogRecord("some body");
        ScopeLogs scopeLogs = getScopeLogs("scopeName", log);
        ResourceLogs resourceLogs = getResourceLogs("resourceSchemaUrl", scopeLogs);

        LogCollection collection = map(LogsData.newBuilder().addResourceLogs(resourceLogs).build());

        assertEquals(1, collection.logs.size());
        LogRecordData logRecordData = collection.logs.get(0);
        assertEquals(23456, logRecordData.getEpochNanos());
        assertEquals(Severity.DEBUG3, logRecordData.getSeverity());
        assertEquals("some severity text", logRecordData.getSeverityText());
        assertEquals("some body", logRecordData.getBody().asString());
        SpanContext spanContext = logRecordData.getSpanContext();
        assertEquals(TraceFlags.getSampled(), spanContext.getTraceFlags());
        assertEquals(TRACE_ID, spanContext.getTraceId());
        assertEquals(SPAN_ID, spanContext.getSpanId());
        Attributes attributes = logRecordData.getAttributes();
        assertEquals(1, attributes.size());
        assertEquals("someValue", attributes.get(AttributeKey.stringKey("someKey")));
        assertEquals("resourceSchemaUrl", logRecordData.getResource().getSchemaUrl());
        Attributes resourceAttrs = logRecordData.getResource().getAttributes();
        assertEquals(1, resourceAttrs.size());
        assertEquals("resourceAttrValue", resourceAttrs.get(AttributeKey.stringKey("resourceAttr")));
        InstrumentationScopeInfo scopeInfo = logRecordData.getInstrumentationScopeInfo();
        assertEquals("scopeSchemaUrl", scopeInfo.getSchemaUrl());
        assertEquals("1.2.3", scopeInfo.getVersion());
        assertEquals("scopeName", scopeInfo.getName());
        Attributes scopeAttrs = scopeInfo.getAttributes();
        assertEquals(1, scopeAttrs.size());
        assertEquals("scopeAttrValue", scopeAttrs.get(AttributeKey.stringKey("scopeAttr")));
    }

    @Test
    public void verifyConversionStructureWithMultipleScopes() {
        LogRecord firstLog = getLogRecord("first body");
        LogRecord otherLog = getLogRecord("other body");
        LogRecord secondLog = getLogRecord("second body");
        ScopeLogs firstScope = getScopeLogs("firstScope", firstLog, otherLog);
        ScopeLogs secondScope = getScopeLogs("secondScope", secondLog);
        ResourceLogs resourceLogs = getResourceLogs("resourceSchemaUrl", firstScope, secondScope);

        LogCollection result = map(LogsData.newBuilder().addResourceLogs(resourceLogs).build());

        List<LogRecordData> logs = result.logs;
        assertEquals(3, logs.size());
        LogRecordData firstLogRecord = logs.get(0);
        LogRecordData secondLogRecord = logs.get(1);
        LogRecordData thirdLogRecord = logs.get(2);
        assertEquals("first body", firstLogRecord.getBody().asString());
        assertEquals("other body", secondLogRecord.getBody().asString());
        assertEquals("second body", thirdLogRecord.getBody().asString());
        assertEquals(firstLogRecord.getInstrumentationScopeInfo(), secondLogRecord.getInstrumentationScopeInfo());
        assertNotEquals(thirdLogRecord, firstLogRecord);
        assertNotEquals(thirdLogRecord, secondLogRecord);
        assertEquals("firstScope", firstLogRecord.getInstrumentationScopeInfo().getName());
        assertEquals("secondScope", thirdLogRecord.getInstrumentationScopeInfo().getName());
    }

    @Test
    public void verifyConversionStructureWithMultipleResources() {
        LogRecord firstLog = getLogRecord("first body");
        LogRecord otherLog = getLogRecord("other body");
        LogRecord secondLog = getLogRecord("second body");
        ScopeLogs firstScope = getScopeLogs("firstScope", firstLog, otherLog);
        ScopeLogs secondScope = getScopeLogs("secondScope", secondLog);
        ResourceLogs firstResourceLogs = getResourceLogs("firstResourceSchema", firstScope);
        ResourceLogs secondResourceLogs = getResourceLogs("secondResourceSchema", secondScope);

        LogCollection result = map(LogsData.newBuilder().addResourceLogs(firstResourceLogs).addResourceLogs(secondResourceLogs).build());

        List<LogRecordData> logs = result.logs;
        assertEquals(3, logs.size());
        LogRecordData firstLogRecord = logs.get(0);
        LogRecordData secondLogRecord = logs.get(1);
        LogRecordData thirdLogRecord = logs.get(2);
        assertEquals("first body", firstLogRecord.getBody().asString());
        assertEquals("other body", secondLogRecord.getBody().asString());
        assertEquals("second body", thirdLogRecord.getBody().asString());
        assertEquals(firstLogRecord.getResource(), secondLogRecord.getResource());
        assertEquals(firstLogRecord.getInstrumentationScopeInfo(), secondLogRecord.getInstrumentationScopeInfo());
        assertNotEquals(firstLogRecord.getResource(), thirdLogRecord.getResource());
    }

    private LogRecord getLogRecord(String body) {
        return LogRecord.newBuilder()
                .setTimeUnixNano(23456)
                .setSeverityNumber(SeverityNumber.SEVERITY_NUMBER_DEBUG3)
                .setSeverityText("some severity text")
                .setBody(AnyValue.newBuilder().setStringValue(body).build())
                .setFlags(0x01)
                .setTraceId(ByteString.copyFrom(TRACE_ID, StandardCharsets.UTF_8))
                .setSpanId(ByteString.copyFrom(SPAN_ID, StandardCharsets.UTF_8))
                .addAttributes(singleItemAttributes("someKey", "someValue"))
                .build();
    }

    private ResourceLogs getResourceLogs(String resourceSchemaUrl, ScopeLogs... scopeLogs) {
        return ResourceLogs.newBuilder()
                .addAllScopeLogs(Arrays.asList(scopeLogs))
                .setSchemaUrl(resourceSchemaUrl)
                .setResource(Resource.newBuilder().addAttributes(singleItemAttributes("resourceAttr", "resourceAttrValue")))
                .build();
    }

    private ScopeLogs getScopeLogs(String scopeName, LogRecord... logs) {
        return ScopeLogs.newBuilder()
                .addAllLogRecords(Arrays.asList(logs))
                .setSchemaUrl("scopeSchemaUrl")
                .setScope(InstrumentationScope.newBuilder().setName(scopeName).setVersion("1.2.3").addAttributes(singleItemAttributes("scopeAttr", "scopeAttrValue")))
                .build();
    }

    @NonNull
    private KeyValue singleItemAttributes(String key, String value) {
        return KeyValue.newBuilder().setKey(key).setValue(AnyValue.newBuilder().setStringValue(value).build()).build();
    }
}
