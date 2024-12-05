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

        private fun authAsHeaders(auth: ApmServerConfiguration.Auth): Map<String, String> {
            val authHeaderValue: String? = when (auth) {
                is ApmServerConfiguration.Auth.ApiKey -> "ApiKey ${auth.key}"
                is ApmServerConfiguration.Auth.SecretToken -> "Bearer ${auth.token}"
                else -> null
            }
            val headers: Map<String, String> =
                authHeaderValue?.let { mapOf("Authorization" to it) } ?: emptyMap()
            return headers
        }

        private fun getTracesUrl(baseUrl: String, exportProtocol: ExportProtocol): String {
            return getSignalUrl(baseUrl, "traces", exportProtocol)
        }

        private fun getLogsUrl(baseUrl: String, exportProtocol: ExportProtocol): String {
            return getSignalUrl(baseUrl, "logs", exportProtocol)
        }

        private fun getMetricsUrl(baseUrl: String, exportProtocol: ExportProtocol): String {
            return getSignalUrl(baseUrl, "metrics", exportProtocol)
        }

        private fun getSignalUrl(
            baseUrl: String,
            signalId: String,
            exportProtocol: ExportProtocol
        ): String {
            return when (exportProtocol) {
                ExportProtocol.GRPC -> baseUrl
                ExportProtocol.HTTP -> getHttpUrl(baseUrl, signalId)
            }
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
            this.configuration = configuration
            val spansOldConfig = exporterProvider.getSpanExporterConfiguration()!!
            val logsOldConfig = exporterProvider.getLogRecordExporterConfiguration()!!
            val metricsOldConfig = exporterProvider.getMetricExporterConfiguration()!!

            val baseUrl = configuration.url.trimEnd('/')
            val headers = authAsHeaders(configuration.auth)
            val spansNewConfig =
                spansOldConfig.copy(
                    url = getTracesUrl(baseUrl, spansOldConfig.protocol),
                    headers = spansOldConfig.headers + headers
                )
            val logsNewConfig =
                logsOldConfig.copy(
                    url = getLogsUrl(baseUrl, logsOldConfig.protocol),
                    headers = logsOldConfig.headers + headers
                )
            val metricsNewConfig =
                metricsOldConfig.copy(
                    url = getMetricsUrl(
                        baseUrl,
                        metricsOldConfig.protocol
                    ),
                    headers = metricsOldConfig.headers + headers
                )

            // Setting new configs
            exporterProvider.setSpanExporterConfiguration(spansNewConfig)
            exporterProvider.setLogRecordExporterConfiguration(logsNewConfig)
            exporterProvider.setMetricExporterConfiguration(metricsNewConfig)
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
                val configuration = ApmServerConfiguration(finalUrl, authentication)
                val baseUrl = configuration.url.trimEnd('/')
                val spansUrl = getTracesUrl(baseUrl, exportProtocol)
                val logRecordsUrl = getLogsUrl(baseUrl, exportProtocol)
                val metricsUrl = getMetricsUrl(baseUrl, exportProtocol)
                val headers: Map<String, String> = authAsHeaders(configuration.auth)
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