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
import static co.elastic.apm.android.sdk.testdata.LogRecordDataUtil.SPAN_ID;
import static co.elastic.apm.android.sdk.testdata.LogRecordDataUtil.TRACE_ID;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

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

public class LogsDataConverterTest extends BaseConverterTest {

    @Test
    public void verifyConversion() {
        LogRecord log = getLogRecord();
        ScopeLogs scopeLogs = ScopeLogs.newBuilder()
                .addLogRecords(log)
                .setSchemaUrl("scopeSchemaUrl")
                .setScope(InstrumentationScope.newBuilder().setName("scopeName").setVersion("1.2.3").addAttributes(singleItemAttributes("scopeAttr", "scopeAttrValue")))
                .build();
        ResourceLogs resourceLogs = ResourceLogs.newBuilder()
                .addScopeLogs(scopeLogs)
                .setSchemaUrl("resourceSchemaUrl")
                .setResource(Resource.newBuilder().addAttributes(singleItemAttributes("resourceAttr", "resourceAttrValue")))
                .build();

        LogCollection collection = map(LogsData.newBuilder().addResourceLogs(resourceLogs).build());

        assertEquals(1, collection.logs.size());
    }

    private LogRecord getLogRecord() {
        return LogRecord.newBuilder()
                .setTimeUnixNano(23456)
                .setSeverityNumber(SeverityNumber.SEVERITY_NUMBER_DEBUG3)
                .setSeverityText("some severity text")
                .setBody(AnyValue.newBuilder().setStringValue("some body").build())
                .setFlags(0x01)
                .addAttributes(singleItemAttributes("someKey", "someValue"))
                .setTraceId(ByteString.copyFrom(TRACE_ID, StandardCharsets.UTF_8))
                .setSpanId(ByteString.copyFrom(SPAN_ID, StandardCharsets.UTF_8))
                .build();
    }

    @NonNull
    private KeyValue singleItemAttributes(String key, String value) {
        return KeyValue.newBuilder().setKey(key).setValue(AnyValue.newBuilder().setStringValue(value).build()).build();
    }
}
