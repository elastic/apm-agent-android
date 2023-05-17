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

import java.nio.charset.StandardCharsets;

import co.elastic.apm.android.sdk.internal.features.storage.serialization.mapping.Converter;
import co.elastic.apm.android.sdk.internal.features.storage.serialization.mapping.Mapper;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.logs.v1.LogRecord;
import io.opentelemetry.proto.logs.v1.SeverityNumber;
import io.opentelemetry.sdk.logs.data.LogRecordData;

public class LogRecordDataConverter extends Converter<LogRecordData, LogRecord> {

    @Override
    protected LogRecord doConvert(Mapper mapper, LogRecordData from) {
        SpanContext spanContext = from.getSpanContext();
        LogRecord.Builder builder = LogRecord.newBuilder()
                .setTimeUnixNano(from.getEpochNanos())
                .setSeverityNumber(convertToSeverityNumber(from.getSeverity()))
                .setBody(AnyValue.newBuilder().setStringValue(from.getBody().asString()))
                .addAllAttributes(mapper.map(from.getAttributes()))
                .setFlags(spanContext.getTraceFlags().asByte())
                .setTraceId(ByteString.copyFrom(spanContext.getTraceId(), StandardCharsets.UTF_8))
                .setSpanId(ByteString.copyFrom(spanContext.getSpanId(), StandardCharsets.UTF_8));
        String severityText = from.getSeverityText();
        if (severityText != null) {
            builder.setSeverityText(severityText);
        }
        return builder.build();
    }

    private SeverityNumber convertToSeverityNumber(Severity severity) {
        return SeverityNumber.forNumber(severity.getSeverityNumber());
    }
}
