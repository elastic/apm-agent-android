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
package co.elastic.apm.android.sdk.testdata;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;

public class LogRecordDataUtil {

    public static LogRecordData createLogRecordData(Resource resource,
                                                    InstrumentationScopeInfo scopeInfo,
                                                    String body,
                                                    Attributes attributes) {
        return createLogRecordData(resource, scopeInfo, body, attributes,
                12345, "b535b3b5232b5dabced5b0ab8037eb78", "f3fc364fb6b77cff",
                Severity.INFO, null);
    }

    public static LogRecordData createLogRecordData(Resource resource,
                                                    InstrumentationScopeInfo scopeInfo,
                                                    String body,
                                                    Attributes attributes,
                                                    long epochNanos,
                                                    String traceId,
                                                    String spanId,
                                                    Severity severity,
                                                    String severityText) {
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
                return SpanContext.create(traceId, spanId, TraceFlags.getSampled(), TraceState.getDefault());
            }

            @Override
            public Severity getSeverity() {
                return severity;
            }

            @Override
            public String getSeverityText() {
                return severityText;
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
