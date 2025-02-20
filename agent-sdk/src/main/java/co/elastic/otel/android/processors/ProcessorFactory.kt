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
package co.elastic.otel.android.processors

import io.opentelemetry.sdk.logs.LogRecordProcessor
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.metrics.export.MetricReader
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.sdk.trace.export.SpanExporter

/**
 * Provides instances of each OpenTelemetry's signal processor.
 */
interface ProcessorFactory {
    /**
     * Provides a processor for spans.
     *
     * @param exporter A preconfigured exporter where the processor should send data to.
     * @return A span processor or null to disable this signal.
     */
    fun createSpanProcessor(exporter: SpanExporter?): SpanProcessor?

    /**
     * Provides a processor for log records.
     *
     * @param exporter A preconfigured exporter where the processor should send data to.
     * @return A log record processor or null to disable this signal.
     */
    fun createLogRecordProcessor(exporter: LogRecordExporter?): LogRecordProcessor?

    /**
     * Provides a reader (analogous to processors for other signals) for metrics.
     *
     * @param exporter A preconfigured exporter where the reader should send data to.
     * @return A metric reader or null to disable this signal.
     */
    fun createMetricReader(exporter: MetricExporter?): MetricReader?
}