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
package co.elastic.apm.android.sdk.internal.features.storage.serialization.logs;

import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class LogsSerializer {
    private static final Mapper mapper = Mapper.createDefault();

    public static String serialize(List<LogRecordData> logs) {
        LogsData logsData = LogsData.newBuilder()
                .addAllResourceLogs(convertToResourceLogItems(logs))
                .build();

        return logsData.
    }

    private static List<ResourceLogs> convertToResourceLogItems(List<LogRecordData> logs) {
        List<ResourceLogs> resourceLogItems = new ArrayList<>();
        Map<Resource, Map<InstrumentationScopeInfo, List<LogRecord>>> logModelsByResource = getLogRecordsByResourceAndScope(logs);
        logModelsByResource.forEach((resource, logsByScope) -> {
            List<ScopeLogs> scopeLogs = new ArrayList<>();
            logsByScope.forEach((scopeInfo, logRecords) -> scopeLogs.add(ScopeLogs.newBuilder()
                    .setScope(convertToScopeProto(scopeInfo))
                    .setSchemaUrl(scopeInfo.getSchemaUrl())
                    .addAllLogRecords(logRecords)
                    .build()));
            resourceLogItems.add(
                    ResourceLogs.newBuilder()
                            .setResource(convertToProtoResource(resource))
                            .setSchemaUrl(resource.getSchemaUrl())
                            .addAllScopeLogs(scopeLogs)
                            .build()
            );
        });

        return resourceLogItems;
    }

    private static InstrumentationScope convertToScopeProto(InstrumentationScopeInfo scopeInfo) {
        return InstrumentationScope.newBuilder()
                .setName(scopeInfo.getName())
                .setVersion(scopeInfo.getVersion())
                .addAllAttributes(mapper.map(scopeInfo.getAttributes()))
                .build();
    }

    private static Map<Resource, Map<InstrumentationScopeInfo, List<LogRecord>>> getLogRecordsByResourceAndScope(List<LogRecordData> logs) {
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

            logModels.add(createLogRecord(logRecordData));
        });
        return logsByResourceAndScope;
    }

    private static LogRecord createLogRecord(LogRecordData logRecordData) {
        SpanContext spanContext = logRecordData.getSpanContext();
        return LogRecord.newBuilder()
                .setTimeUnixNano(logRecordData.getEpochNanos())
                .setSeverityNumber(convertToSeverityNumber(logRecordData.getSeverity()))
                .setSeverityText(logRecordData.getSeverityText())
                .setBody(AnyValue.newBuilder().setStringValue(logRecordData.getBody().asString()))
                .addAllAttributes(mapper.map(logRecordData.getAttributes()))
                .setFlags(spanContext.getTraceFlags().asByte())
                .setTraceId(ByteString.copyFrom(spanContext.getTraceIdBytes()))
                .setSpanId(ByteString.copyFrom(spanContext.getSpanIdBytes()))
                .build();
    }

    private static SeverityNumber convertToSeverityNumber(Severity severity) {
        return SeverityNumber.forNumber(severity.getSeverityNumber());
    }

    private static co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource convertToProtoResource(Resource resource) {
        return co.elastic.apm.android.sdk.internal.opentelemetry.proto.resource.v1.Resource.newBuilder()
                .addAllAttributes(mapper.map(resource.getAttributes()))
                .build();
    }
}
