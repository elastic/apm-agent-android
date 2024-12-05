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
package co.elastic.apm.android.sdk.exporters.apmserver

import androidx.annotation.GuardedBy
import co.elastic.apm.android.sdk.exporters.ExporterProvider
import co.elastic.apm.android.sdk.exporters.configurable.ConfigurableExporterProvider
import co.elastic.apm.android.sdk.exporters.configurable.ExportProtocol
import co.elastic.apm.android.sdk.exporters.configurable.ExporterConfiguration
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.trace.export.SpanExporter

class ApmServerExporterProvider internal constructor(
    initialConfiguration: ApmServerConfiguration,
    internal val exporterProvider: ConfigurableExporterProvider
) : ExporterProvider {
    @GuardedBy("configurationLock")
    private var configuration = initialConfiguration
    private val configurationLock = Any()

    companion object {
        fun builder(): Builder {
            return Builder()
        }

        private fun getHttpUrl(url: String, signalId: String): String {
            return String.format("%s/v1/%s", url, signalId)
        }
    }

    override fun getSpanExporter(): SpanExporter {
        return exporterProvider.getSpanExporter()
    }

    override fun getLogRecordExporter(): LogRecordExporter {
        return exporterProvider.getLogRecordExporter()
    }

    override fun getMetricExporter(): MetricExporter {
        return exporterProvider.getMetricExporter()
    }

    fun getApmServerConfiguration(): ApmServerConfiguration = synchronized(configurationLock) {
        configuration
    }

    fun setApmServerConfiguration(configuration: ApmServerConfiguration): Unit =
        synchronized(configurationLock) {
            TODO()
        }

    class Builder internal constructor() {
        private var url: String? = null
        private var authentication: ApmServerConfiguration.Auth = ApmServerConfiguration.Auth.None
        private var exportProtocol: ExportProtocol = ExportProtocol.HTTP

        fun setUrl(value: String) = apply {
            url = value
        }

        fun setAuthentication(value: ApmServerConfiguration.Auth) = apply {
            authentication = value
        }

        fun setExportProtocol(value: ExportProtocol) = apply {
            exportProtocol = value
        }

        fun build(): ApmServerExporterProvider {
            return url?.let { finalUrl ->
                val configuration = ApmServerConfiguration(finalUrl.trimEnd('/'), authentication)
                val spansUrl = when (exportProtocol) {
                    ExportProtocol.GRPC -> configuration.url
                    ExportProtocol.HTTP -> getHttpUrl(configuration.url, "traces")
                }
                val logRecordsUrl = when (exportProtocol) {
                    ExportProtocol.GRPC -> configuration.url
                    ExportProtocol.HTTP -> getHttpUrl(configuration.url, "logs")
                }
                val metricsUrl = when (exportProtocol) {
                    ExportProtocol.GRPC -> configuration.url
                    ExportProtocol.HTTP -> getHttpUrl(configuration.url, "metrics")
                }
                val authHeaderValue: String? = when (configuration.auth) {
                    is ApmServerConfiguration.Auth.ApiKey -> "ApiKey ${configuration.auth.key}"
                    is ApmServerConfiguration.Auth.SecretToken -> "Bearer ${configuration.auth.token}"
                    else -> null
                }
                val headers: Map<String, String> =
                    authHeaderValue?.let { mutableMapOf("Authorization" to it) } ?: emptyMap()
                val provider =
                    ConfigurableExporterProvider.create(
                        ExporterConfiguration.Span(spansUrl, headers, exportProtocol),
                        ExporterConfiguration.LogRecord(logRecordsUrl, headers, exportProtocol),
                        ExporterConfiguration.Metric(metricsUrl, headers, exportProtocol)
                    )
                ApmServerExporterProvider(configuration, provider)
            } ?: throw IllegalArgumentException("The url must be set.")
        }
    }
}