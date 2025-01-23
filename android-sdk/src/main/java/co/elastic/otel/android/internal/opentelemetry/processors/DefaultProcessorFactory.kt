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
package co.elastic.otel.android.internal.opentelemetry.processors

import co.elastic.otel.android.internal.services.backgroundwork.BackgroundWorkService
import co.elastic.otel.android.processors.ProcessorFactory
import io.opentelemetry.sdk.logs.LogRecordProcessor
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.metrics.export.MetricReader
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.sdk.trace.export.SpanExporter
import java.time.Duration

internal class DefaultProcessorFactory(private val backgroundWorkService: BackgroundWorkService) :
    ProcessorFactory {

    companion object {
        private val PROCESSING_INTERVAL = Duration.ofSeconds(2)
        private val READING_INTERVAL = Duration.ofSeconds(4)
    }

    override fun createSpanProcessor(exporter: SpanExporter?): SpanProcessor? {
        return exporter?.let {
            BatchSpanProcessor.builder(exporter)
                .setScheduleDelay(PROCESSING_INTERVAL)
                .build()
        }
    }

    override fun createLogRecordProcessor(exporter: LogRecordExporter?): LogRecordProcessor? {
        return exporter?.let {
            BatchLogRecordProcessor.builder(exporter)
                .setScheduleDelay(PROCESSING_INTERVAL)
                .build()
        }
    }

    override fun createMetricReader(exporter: MetricExporter?): MetricReader? {
        return exporter?.let {
            PeriodicMetricReader.builder(exporter)
                .setInterval(READING_INTERVAL)
                .setExecutor(backgroundWorkService.executorService)
                .build()
        }
    }
}