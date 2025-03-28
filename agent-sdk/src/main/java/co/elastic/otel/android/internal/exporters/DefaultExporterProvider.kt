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
package co.elastic.otel.android.internal.exporters

import co.elastic.otel.android.exporters.ExporterProvider
import co.elastic.otel.android.internal.connectivity.ExportConnectivityManager
import co.elastic.otel.android.internal.connectivity.SignalConnectivityChangeListener
import co.elastic.otel.android.internal.exporters.configurable.MutableExporterProvider
import co.elastic.otel.android.internal.opentelemetry.SignalType
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.metrics.export.MetricExporter
import io.opentelemetry.sdk.trace.export.SpanExporter

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal class DefaultExporterProvider internal constructor(
    private val connectivityManager: ExportConnectivityManager,
    private val exporterProvider: MutableExporterProvider
) : ExporterProvider, SignalConnectivityChangeListener {

    companion object {
        internal fun create(connectivityManager: ExportConnectivityManager): DefaultExporterProvider {
            val exporterProvider = MutableExporterProvider.create(
                connectivityManager.getSpansConnectivityConfiguration(),
                connectivityManager.getLogsConnectivityConfiguration(),
                connectivityManager.getMetricsConnectivityConfiguration()
            )
            return DefaultExporterProvider(connectivityManager, exporterProvider)
        }
    }

    init {
        connectivityManager.addChangeListener(this)
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

    override fun onConnectivityConfigurationChange(signalType: SignalType) {
        when (signalType) {
            SignalType.TRACE -> exporterProvider.setSpanExporterConfiguration(connectivityManager.getSpansConnectivityConfiguration())
            SignalType.LOG -> exporterProvider.setLogRecordExporterConfiguration(connectivityManager.getLogsConnectivityConfiguration())
            SignalType.METRIC -> exporterProvider.setMetricExporterConfiguration(connectivityManager.getMetricsConnectivityConfiguration())
        }
    }
}