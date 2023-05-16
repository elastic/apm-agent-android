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
import static co.elastic.apm.android.sdk.testdata.LogRecordDataUtil.createLogRecordData;
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
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;

public class LogCollectionConverterTest extends BaseConverterTest {

    @Test
    public void verifyConversionDataStructure() {
        LogRecordData logRecordData = createLogRecordData(singleAttributeResource("someAttr", "someValue", "resourceSchema"),
                createScope("someName", "1.2.3", "scopeSchema"),
                "Some body", singleItemAttributes("someLogAttr", "someLogAttrValue"));

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

    @Test
    public void verifyMultipleLogsWithSameResourceAndScope() {
        Resource resource = singleAttributeResource("oneResourceAttr", "oneResourceValue");
        InstrumentationScopeInfo scope = createScope("oneScope");
        LogRecordData firstLog = createLogRecordData(resource, scope,
                "firstBody", singleItemAttributes("oneAttr", "oneValue"));
        LogRecordData secondLog = createLogRecordData(resource, scope,
                "secondBody", singleItemAttributes("otherAttr", "otherValue"));

        LogsData result = map(new LogCollection(listOf(firstLog, secondLog)));

        List<ResourceLogs> resourceLogsList = result.getResourceLogsList();
        assertEquals(1, resourceLogsList.size());
        List<ScopeLogs> scopeLogsList = resourceLogsList.get(0).getScopeLogsList();
        assertEquals(1, scopeLogsList.size());
        List<LogRecord> logRecordsList = scopeLogsList.get(0).getLogRecordsList();
        assertEquals(2, logRecordsList.size());
        assertEquals("firstBody", logRecordsList.get(0).getBody().getStringValue());
        assertEquals("secondBody", logRecordsList.get(1).getBody().getStringValue());
    }

    @Test
    public void verifyMultipleLogsWithSameResourceDifferentScope() {
        Resource resource = singleAttributeResource("oneResourceAttr", "oneResourceValue");
        LogRecordData firstLog = createLogRecordData(resource, createScope("firstScope"),
                "firstBody", singleItemAttributes("oneAttr", "oneValue"));
        LogRecordData secondLog = createLogRecordData(resource, createScope("secondScope"),
                "secondBody", singleItemAttributes("otherAttr", "otherValue"));

        LogsData result = map(new LogCollection(listOf(firstLog, secondLog)));

        List<ResourceLogs> resourceLogsList = result.getResourceLogsList();
        assertEquals(1, resourceLogsList.size());
        List<ScopeLogs> scopeLogsList = resourceLogsList.get(0).getScopeLogsList();
        assertEquals(2, scopeLogsList.size());
        ScopeLogs firstScope = scopeLogsList.get(0);
        ScopeLogs secondScope = scopeLogsList.get(1);
        List<LogRecord> firstScopeLogs = firstScope.getLogRecordsList();
        List<LogRecord> secondScopeLogs = secondScope.getLogRecordsList();
        assertEquals(1, firstScopeLogs.size());
        assertEquals(1, secondScopeLogs.size());
    }

    @Test
    public void verifyMultipleLogsWithDifferentResource() {
        Resource firstResource = singleAttributeResource("oneResourceAttr", "oneResourceValue");
        Resource secondResource = singleAttributeResource("otherResourceAttr", "otherResourceValue");
        LogRecordData firstLog = createLogRecordData(firstResource, createScope("firstScope"),
                "firstBody", singleItemAttributes("oneAttr", "oneValue"));
        LogRecordData secondLog = createLogRecordData(secondResource, createScope("secondScope"),
                "secondBody", singleItemAttributes("otherAttr", "otherValue"));

        LogsData result = map(new LogCollection(listOf(firstLog, secondLog)));

        List<ResourceLogs> resourceLogsList = result.getResourceLogsList();
        assertEquals(2, resourceLogsList.size());
        ResourceLogs firstResourceLogs = resourceLogsList.get(0);
        ResourceLogs secondResourceLogs = resourceLogsList.get(1);
        List<ScopeLogs> firstScopeLogsList = firstResourceLogs.getScopeLogsList();
        List<ScopeLogs> secondScopeLogsList = secondResourceLogs.getScopeLogsList();
        assertEquals(1, firstScopeLogsList.size());
        assertEquals(1, secondScopeLogsList.size());
        ScopeLogs firstScope = firstScopeLogsList.get(0);
        ScopeLogs secondScope = secondScopeLogsList.get(0);
        List<LogRecord> firstScopeLogs = firstScope.getLogRecordsList();
        List<LogRecord> secondScopeLogs = secondScope.getLogRecordsList();
        assertEquals(1, firstScopeLogs.size());
        assertEquals(1, secondScopeLogs.size());
    }

    private Attributes singleItemAttributes(String key, String value) {
        return Attributes.builder()
                .put(AttributeKey.stringKey(key), value).build();
    }

    private InstrumentationScopeInfo createScope(String name) {
        return createScope(name, null, null);
    }

    private InstrumentationScopeInfo createScope(String name, String version, String schemaUrl) {
        return InstrumentationScopeInfo.builder(name)
                .setVersion(version)
                .setSchemaUrl(schemaUrl)
                .build();
    }

    private Resource singleAttributeResource(String someAttr, String someValue) {
        return singleAttributeResource(someAttr, someValue, null);
    }

    private Resource singleAttributeResource(String someAttr, String someValue, String schemaUrl) {
        return Resource.builder()
                .put(AttributeKey.stringKey(someAttr), someValue)
                .setSchemaUrl(schemaUrl)
                .build();
    }
}
