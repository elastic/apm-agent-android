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

import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.List;

import co.elastic.apm.android.sdk.internal.features.storage.serialization.common.models.ProtoAttributes;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.common.models.ProtoInstrumentationScope;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.common.models.ProtoResource;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.logs.models.LogCollection;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.logs.models.SimpleLogRecordData;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.mapping.Converter;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.mapping.Mapper;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.common.v1.AnyValue;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogRecord;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.LogsData;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ResourceLogs;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.ScopeLogs;
import co.elastic.apm.android.sdk.internal.opentelemetry.proto.logs.v1.SeverityNumber;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;

public class LogsDataConverter extends Converter<LogsData, LogCollection> {

    @Override
    protected LogCollection doConvert(Mapper mapper, LogsData from) {
        List<LogRecordData> logs = new ArrayList<>();

        for (ResourceLogs resourceLogs : from.getResourceLogsList()) {
            ProtoResource protoResource = new ProtoResource(resourceLogs.getResource(), resourceLogs.getSchemaUrl());
            for (ScopeLogs scopeLogs : resourceLogs.getScopeLogsList()) {
                ProtoInstrumentationScope protoScope = new ProtoInstrumentationScope(scopeLogs.getScope(), scopeLogs.getSchemaUrl());

                Resource resource = mapper.map(protoResource);
                InstrumentationScopeInfo scopeInfo = mapper.map(protoScope);
                logs.addAll(convertLogs(mapper, resource, scopeInfo, scopeLogs.getLogRecordsList()));
            }
        }

        return new LogCollection(logs);
    }

    private List<LogRecordData> convertLogs(Mapper mapper, Resource resource, InstrumentationScopeInfo scopeInfo, List<LogRecord> protoLogs) {
        List<LogRecordData> logs = new ArrayList<>();
        for (LogRecord protoLog : protoLogs) {
            SpanContext spanContext = createSpanContext(protoLog.getTraceId(), protoLog.getSpanId(), protoLog.getFlags());
            logs.add(new SimpleLogRecordData(resource, scopeInfo, protoLog.getTimeUnixNano(),
                    spanContext, findSeverity(protoLog.getSeverityNumber()), protoLog.getSeverityText(), getBody(protoLog.getBody()),
                    mapper.map(new ProtoAttributes(protoLog.getAttributesList())))
            );
        }
        return logs;
    }

    private Body getBody(AnyValue body) {
        return Body.string(body.getStringValue());
    }

    private Severity findSeverity(SeverityNumber severityNumber) {
        for (Severity severity : Severity.values()) {
            if (severity.getSeverityNumber() == severityNumber.getNumber()) {
                return severity;
            }
        }

        throw new IllegalArgumentException();
    }

    private SpanContext createSpanContext(ByteString traceId, ByteString spanId, int flags) {
        return SpanContext.create(traceId.toStringUtf8(), spanId.toStringUtf8(), TraceFlags.fromByte((byte) flags), TraceState.getDefault());
    }
}
