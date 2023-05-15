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
import static co.elastic.apm.android.sdk.testutils.ListUtils.listOf;

import org.junit.Test;

import java.util.List;

import co.elastic.apm.android.sdk.internal.features.storage.serialization.logs.models.LogCollection;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs;
import co.elastic.apm.android.sdk.testutils.BaseConverterTest;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;

public class LogCollectionConverterTest extends BaseConverterTest {

    @Test
    public void verifyConversionDataStructure() {
        Attributes attributes = Attributes.builder()
                .put(AttributeKey.stringKey("someLogAttr"), "someLogAttrValue").build();
        LogRecordData logRecordData = createLogRecordData(singleAttributeResource("someAttr", "someValue", "resourceSchema"),
                createScope("someName", "1.2.3", "scopeSchema"), 12345, Severity.DEBUG3, "Some body", attributes);

        LogsData result = map(new LogCollection(listOf(logRecordData)));

        List<ResourceLogs> resourceLogsList = result.getResourceLogsList();
        assertEquals(1, resourceLogsList.size());
        ResourceLogs oneLog = resourceLogsList.get(0);
        assertEquals("resourceSchema", oneLog.getSchemaUrl());
        List<ScopeLogs> scopeLogsList = oneLog.getScopeLogsList();
        assertEquals(1, scopeLogsList.size());
        ScopeLogs oneScopeLog = scopeLogsList.get(0);
        assertEquals("scopeSchema", oneScopeLog.getSchemaUrl());
        List<LogRecord> logRecordsList = oneScopeLog.getLogRecordsList();
        assertEquals(1, logRecordsList.size());
        LogRecord logRecord = logRecordsList.get(0);
        assertEquals("Some body", logRecord.getBody().getStringValue());
    }

    private InstrumentationScopeInfo createScope(String name, String version, String schemaUrl) {
        return InstrumentationScopeInfo.builder(name)
                .setVersion(version)
                .setSchemaUrl(schemaUrl)
                .build();
    }

    private Resource singleAttributeResource(String someAttr, String someValue, String schemaUrl) {
        return Resource.builder()
                .put(AttributeKey.stringKey(someAttr), someValue)
                .setSchemaUrl(schemaUrl)
                .build();
    }

    private LogRecordData createLogRecordData(Resource resource,
                                              InstrumentationScopeInfo scopeInfo,
                                              long epochNanos,
                                              Severity severity,
                                              String body,
                                              Attributes attributes) {
        return new LogRecordData() {
            @Override
            public Resource getResource() {
                return resource;
            }

            @Override
            public InstrumentationScopeInfo getInstrumentationScopeInfo() {
                return scopeInfo;
            }

            @Override
            public long getEpochNanos() {
                return epochNanos;
            }

            @Override
            public SpanContext getSpanContext() {
                return SpanContext.create("b535b3b5232b5dabced5b0ab8037eb78", "f3fc364fb6b77cff", TraceFlags.getSampled(), TraceState.getDefault());
            }

            @Override
            public Severity getSeverity() {
                return severity;
            }

            @Override
            public String getSeverityText() {
                return null;
            }

            @Override
            public Body getBody() {
                return Body.string(body);
            }

            @Override
            public Attributes getAttributes() {
                return attributes;
            }

            @Override
            public int getTotalAttributeCount() {
                return 0;
            }
        };
    }
}
