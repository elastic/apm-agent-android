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
package co.elastic.otel.android.internal.exporters.configurable

import androidx.annotation.GuardedBy
import co.elastic.otel.android.exporters.ExporterProvider
import co.elastic.otel.android.exporters.configuration.ExportProtocol
import co.elastic.otel.android.internal.connectivity.ExportConnectivityConfiguration
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.trace.export.SpanExporter

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal class MutableExporterProvider(
    private val spanExporter: MutableSpanExporter,
    private val logRecordExporter: MutableLogRecordExporter,
    private val metricExporter: MutableMetricExporter
) : ExporterProvider {
    @GuardedBy("spanConfigurationLock")
    private var spanExporterConfiguration: ExportConnectivityConfiguration? = null

    @GuardedBy("logRecordConfigurationLock")
    private var logRecordExporterConfiguration: ExportConnectivityConfiguration? = null

    @GuardedBy("metricConfigurationLock")
    private var metricExporterConfiguration: ExportConnectivityConfiguration? = null
    private val spanConfigurationLock = Any()
    private val logRecordConfigurationLock = Any()
    private val metricConfigurationLock = Any()

    companion object {
        fun create(
            spanExporterConfiguration: ExportConnectivityConfiguration?,
            logRecordExporterConfiguration: ExportConnectivityConfiguration?,
            metricExporterConfiguration: ExportConnectivityConfiguration?
        ): MutableExporterProvider {
            val provider = MutableExporterProvider(
                MutableSpanExporter(),
                MutableLogRecordExporter(),
                MutableMetricExporter()
            )
            provider.setSpanExporterConfiguration(spanExporterConfiguration)
            provider.setLogRecordExporterConfiguration(logRecordExporterConfiguration)
            provider.setMetricExporterConfiguration(metricExporterConfiguration)
            return provider
        }
    }

    override fun getSpanExporter(): SpanExporter {
        return spanExporter
    }

    override fun getLogRecordExporter(): LogRecordExporter {
        return logRecordExporter
    }

    override fun getMetricExporter(): MetricExporter {
        return metricExporter
    }

    fun getSpanExporterConfiguration(): ExportConnectivityConfiguration? =
        synchronized(spanConfigurationLock) {
            spanExporterConfiguration
        }

    fun getLogRecordExporterConfiguration(): ExportConnectivityConfiguration? =
        synchronized(logRecordConfigurationLock) {
            logRecordExporterConfiguration
        }

    fun getMetricExporterConfiguration(): ExportConnectivityConfiguration? =
        synchronized(metricConfigurationLock) {
            metricExporterConfiguration
        }

    fun setSpanExporterConfiguration(configuration: ExportConnectivityConfiguration?): Unit =
        synchronized(spanConfigurationLock) {
            if (spanExporterConfiguration != configuration) {
                spanExporterConfiguration = configuration
                val old = spanExporter.getDelegate()
                spanExporter.setDelegate(spanExporterConfiguration?.let {
                    createSpanExporter(it)
                })
                old?.shutdown()
            }
        }

    fun setLogRecordExporterConfiguration(configuration: ExportConnectivityConfiguration?): Unit =
        synchronized(logRecordConfigurationLock) {
            if (logRecordExporterConfiguration != configuration) {
                logRecordExporterConfiguration = configuration
                val old = logRecordExporter.getDelegate()
                logRecordExporter.setDelegate(logRecordExporterConfiguration?.let {
                    createLogRecordExporter(it)
                })
                old?.shutdown()
            }
        }

    fun setMetricExporterConfiguration(configuration: ExportConnectivityConfiguration?): Unit =
        synchronized(metricConfigurationLock) {
            if (metricExporterConfiguration != configuration) {
                metricExporterConfiguration = configuration
                val old = metricExporter.getDelegate()
                metricExporter.setDelegate(metricExporterConfiguration?.let {
                    createMetricExporter(it)
                })
                old?.shutdown()
            }
        }

    private fun createSpanExporter(configuration: ExportConnectivityConfiguration): SpanExporter {
        return when (configuration.getProtocol()) {
            ExportProtocol.HTTP -> OtlpHttpSpanExporter.builder()
                .setEndpoint(configuration.getUrl())
                .setHeaders { configuration.getHeaders() }
                .build()

            ExportProtocol.GRPC -> OtlpGrpcSpanExporter.builder()
                .setEndpoint(configuration.getUrl())
                .setHeaders { configuration.getHeaders() }
                .build()
        }
    }

    private fun createLogRecordExporter(configuration: ExportConnectivityConfiguration): LogRecordExporter {
        return when (configuration.getProtocol()) {
            ExportProtocol.HTTP -> OtlpHttpLogRecordExporter.builder()
                .setEndpoint(configuration.getUrl())
                .setHeaders { configuration.getHeaders() }
                .build()

            ExportProtocol.GRPC -> OtlpGrpcLogRecordExporter.builder()
                .setEndpoint(configuration.getUrl())
                .setHeaders { configuration.getHeaders() }
                .build()
        }
    }

    private fun createMetricExporter(configuration: ExportConnectivityConfiguration): MetricExporter {
        return when (configuration.getProtocol()) {
            ExportProtocol.HTTP -> OtlpHttpMetricExporter.builder()
                .setEndpoint(configuration.getUrl())
                .setHeaders { configuration.getHeaders() }
                .setAggregationTemporalitySelector(AggregationTemporalitySelector.deltaPreferred())
                .build()

            ExportProtocol.GRPC -> OtlpGrpcMetricExporter.builder()
                .setEndpoint(configuration.getUrl())
                .setHeaders { configuration.getHeaders() }
                .setAggregationTemporalitySelector(AggregationTemporalitySelector.deltaPreferred())
                .build()
        }
    }
}