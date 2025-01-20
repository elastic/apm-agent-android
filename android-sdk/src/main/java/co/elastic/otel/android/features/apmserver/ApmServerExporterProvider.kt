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
package co.elastic.otel.android.features.apmserver

import co.elastic.otel.android.connectivity.ConnectivityConfigurationHolder
import co.elastic.otel.android.exporters.ExporterProvider
import co.elastic.otel.android.exporters.configurable.ConfigurableExporterProvider
import co.elastic.otel.android.exporters.configuration.ExporterConfiguration
import co.elastic.otel.android.tools.provider.Provider
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.trace.export.SpanExporter

class ApmServerExporterProvider internal constructor(
    private val connectivityConfigurationProvider: Provider<ApmServerConnectivity>,
    private val exporterProvider: ConfigurableExporterProvider
) : ExporterProvider, ConnectivityConfigurationHolder.Listener {

    companion object {
        internal fun create(connectivityConfigurationManager: ApmServerConnectivityManager.ConnectivityHolder): ApmServerExporterProvider {
            val configuration =
                connectivityConfigurationManager.getConnectivityConfiguration()
            val exporterProvider = ConfigurableExporterProvider.create(
                ExporterConfiguration.Span(
                    configuration.getTracesUrl(),
                    configuration.getHeaders(),
                    configuration.exportProtocol
                ),
                ExporterConfiguration.LogRecord(
                    configuration.getLogsUrl(),
                    configuration.getHeaders(),
                    configuration.exportProtocol
                ),
                ExporterConfiguration.Metric(
                    configuration.getMetricsUrl(),
                    configuration.getHeaders(),
                    configuration.exportProtocol
                )
            )
            val apmServerExporterProvider = ApmServerExporterProvider(
                connectivityConfigurationManager::getConnectivityConfiguration,
                exporterProvider
            )
            connectivityConfigurationManager.addListener(apmServerExporterProvider)
            return apmServerExporterProvider
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

    private fun setApmServerConfiguration(configuration: ApmServerConnectivity) {
        val spanConfiguration = ExporterConfiguration.Span(
            configuration.getTracesUrl(),
            configuration.getHeaders(),
            configuration.exportProtocol
        )
        val logConfiguration = ExporterConfiguration.LogRecord(
            configuration.getLogsUrl(),
            configuration.getHeaders(),
            configuration.exportProtocol
        )
        val metricConfiguration = ExporterConfiguration.Metric(
            configuration.getMetricsUrl(),
            configuration.getHeaders(),
            configuration.exportProtocol
        )

        // Setting new configs
        exporterProvider.setSpanExporterConfiguration(spanConfiguration)
        exporterProvider.setLogRecordExporterConfiguration(logConfiguration)
        exporterProvider.setMetricExporterConfiguration(metricConfiguration)
    }

    override fun onConnectivityConfigurationChange() {
        setApmServerConfiguration(connectivityConfigurationProvider.get())
    }
}