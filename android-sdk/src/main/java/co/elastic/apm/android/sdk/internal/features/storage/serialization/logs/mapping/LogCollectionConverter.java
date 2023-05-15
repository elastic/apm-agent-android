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

import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.elastic.apm.android.sdk.internal.features.storage.serialization.logs.models.LogCollection;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.mapping.Converter;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.mapping.Mapper;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.InstrumentationScope;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.SeverityNumber;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;

public class LogCollectionConverter extends Converter<LogCollection, LogsData> {

    @Override
    protected LogsData doConvert(Mapper mapper, LogCollection from) {
        return LogsData.newBuilder()
                .addAllResourceLogs(convertToResourceLogItems(mapper, from.logs))
                .build();
    }

    private List<ResourceLogs> convertToResourceLogItems(Mapper mapper, List<LogRecordData> logs) {
        List<ResourceLogs> resourceLogItems = new ArrayList<>();
        Map<Resource, Map<InstrumentationScopeInfo, List<LogRecord>>> logModelsByResource = getLogRecordsByResourceAndScope(mapper, logs);
        logModelsByResource.forEach((resource, logsByScope) -> {
            List<ScopeLogs> scopeLogs = new ArrayList<>();
            logsByScope.forEach((scopeInfo, logRecords) -> {
                ScopeLogs.Builder builder = ScopeLogs.newBuilder()
                        .setScope(mapper.<InstrumentationScope>map(scopeInfo))
                        .addAllLogRecords(logRecords);
                String schemaUrl = scopeInfo.getSchemaUrl();
                if (schemaUrl != null) {
                    builder.setSchemaUrl(schemaUrl);
                }
                scopeLogs.add(builder.build());
            });
            ResourceLogs.Builder builder = ResourceLogs.newBuilder()
                    .setResource(convertToProtoResource(mapper, resource))
                    .addAllScopeLogs(scopeLogs);
            String schemaUrl = resource.getSchemaUrl();
            if (schemaUrl != null) {
                builder.setSchemaUrl(schemaUrl);
            }
            resourceLogItems.add(builder.build());
        });

        return resourceLogItems;
    }

    private Map<Resource, Map<InstrumentationScopeInfo, List<LogRecord>>> getLogRecordsByResourceAndScope(Mapper mapper, List<LogRecordData> logs) {
        Map<Resource, Map<InstrumentationScopeInfo, List<LogRecord>>> logsByResourceAndScope = new HashMap<>();
        logs.forEach(logRecordData -> {
            Map<InstrumentationScopeInfo, List<LogRecord>> logsByScope = logsByResourceAndScope.get(logRecordData.getResource());
            if (logsByScope == null) {
                logsByScope = new HashMap<>();
                logsByResourceAndScope.put(logRecordData.getResource(), logsByScope);
            }
            List<LogRecord> logModels = logsByScope.get(logRecordData.getInstrumentationScopeInfo());
            if (logModels == null) {
                logModels = new ArrayList<>();
                logsByScope.put(logRecordData.getInstrumentationScopeInfo(), logModels);
            }

            logModels.add(createLogRecord(mapper, logRecordData));
        });
        return logsByResourceAndScope;
    }

    private LogRecord createLogRecord(Mapper mapper, LogRecordData logRecordData) {
        SpanContext spanContext = logRecordData.getSpanContext();
        LogRecord.Builder builder = LogRecord.newBuilder()
                .setTimeUnixNano(logRecordData.getEpochNanos())
                .setSeverityNumber(convertToSeverityNumber(logRecordData.getSeverity()))
                .setBody(AnyValue.newBuilder().setStringValue(logRecordData.getBody().asString()))
                .addAllAttributes(mapper.map(logRecordData.getAttributes()))
                .setFlags(spanContext.getTraceFlags().asByte())
                .setTraceId(ByteString.copyFrom(spanContext.getTraceIdBytes()))
                .setSpanId(ByteString.copyFrom(spanContext.getSpanIdBytes()));
        String severityText = logRecordData.getSeverityText();
        if (severityText != null) {
            builder.setSeverityText(severityText);
        }
        return builder.build();
    }

    private SeverityNumber convertToSeverityNumber(Severity severity) {
        return SeverityNumber.forNumber(severity.getSeverityNumber());
    }

    private co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource convertToProtoResource(Mapper mapper, Resource resource) {
        return co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource.newBuilder()
                .addAllAttributes(mapper.map(resource.getAttributes()))
                .build();
    }
}
