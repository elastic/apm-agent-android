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
package co.elastic.apm.android.sdk.exporters.configurable

import androidx.annotation.GuardedBy
import co.elastic.apm.android.sdk.exporters.ExporterProvider
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.trace.export.SpanExporter

internal class ConfigurableExporterProvider(
    private val spanExporter: MutableSpanExporter,
    private val logRecordExporter: MutableLogRecordExporter,
    private val metricExporter: MutableMetricExporter
) : ExporterProvider {
    @GuardedBy("spanConfigurationLock")
    private var spanExporterConfiguration: ExporterConfiguration.Span? = null

    @GuardedBy("logRecordConfigurationLock")
    private var logRecordExporterConfiguration: ExporterConfiguration.LogRecord? = null

    @GuardedBy("metricConfigurationLock")
    private var metricExporterConfiguration: ExporterConfiguration.Metric? = null
    private val spanConfigurationLock = Any()
    private val logRecordConfigurationLock = Any()
    private val metricConfigurationLock = Any()

    companion object {
        fun create(
            spanExporterConfiguration: ExporterConfiguration.Span?,
            logRecordExporterConfiguration: ExporterConfiguration.LogRecord?,
            metricExporterConfiguration: ExporterConfiguration.Metric?
        ): ConfigurableExporterProvider {
            val provider = ConfigurableExporterProvider(
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

    fun getSpanExporterConfiguration(): ExporterConfiguration.Span? =
        synchronized(spanConfigurationLock) {
            spanExporterConfiguration
        }

    fun getLogRecordExporterConfiguration(): ExporterConfiguration.LogRecord? =
        synchronized(logRecordConfigurationLock) {
            logRecordExporterConfiguration
        }

    fun getMetricExporterConfiguration(): ExporterConfiguration.Metric? =
        synchronized(metricConfigurationLock) {
            metricExporterConfiguration
        }

    fun setSpanExporterConfiguration(configuration: ExporterConfiguration.Span?): Unit =
        synchronized(spanConfigurationLock) {
            if (spanExporterConfiguration != configuration) {
                spanExporterConfiguration = configuration
                spanExporter.setDelegate(spanExporterConfiguration?.let {
                    createSpanExporter(it)
                })
            }
        }

    fun setLogRecordExporterConfiguration(configuration: ExporterConfiguration.LogRecord?): Unit =
        synchronized(logRecordConfigurationLock) {
            if (logRecordExporterConfiguration != configuration) {
                logRecordExporterConfiguration = configuration
                logRecordExporter.setDelegate(logRecordExporterConfiguration?.let {
                    createLogRecordExporter(
                        it
                    )
                })
            }
        }

    fun setMetricExporterConfiguration(configuration: ExporterConfiguration.Metric?): Unit =
        synchronized(metricConfigurationLock) {
            if (metricExporterConfiguration != configuration) {
                metricExporterConfiguration = configuration
                metricExporter.setDelegate(metricExporterConfiguration?.let {
                    createMetricExporter(it)
                })
            }
        }

    private fun createSpanExporter(configuration: ExporterConfiguration.Span): SpanExporter {
        return when (configuration.protocol) {
            ExportProtocol.HTTP -> OtlpHttpSpanExporter.builder()
                .setEndpoint(configuration.url)
                .setHeaders { configuration.headers }
                .build()

            ExportProtocol.GRPC -> OtlpGrpcSpanExporter.builder()
                .setEndpoint(configuration.url)
                .setHeaders { configuration.headers }
                .build()
        }
    }

    private fun createLogRecordExporter(configuration: ExporterConfiguration.LogRecord): LogRecordExporter {
        return when (configuration.protocol) {
            ExportProtocol.HTTP -> OtlpHttpLogRecordExporter.builder()
                .setEndpoint(configuration.url)
                .setHeaders { configuration.headers }
                .build()

            ExportProtocol.GRPC -> OtlpGrpcLogRecordExporter.builder()
                .setEndpoint(configuration.url)
                .setHeaders { configuration.headers }
                .build()
        }
    }

    private fun createMetricExporter(configuration: ExporterConfiguration.Metric): MetricExporter {
        return when (configuration.protocol) {
            ExportProtocol.HTTP -> OtlpHttpMetricExporter.builder()
                .setEndpoint(configuration.url)
                .setHeaders { configuration.headers }
                .build()

            ExportProtocol.GRPC -> OtlpGrpcMetricExporter.builder()
                .setEndpoint(configuration.url)
                .setHeaders { configuration.headers }
                .build()
        }
    }
}