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
package co.elastic.otel.android.internal.connectivity

import co.elastic.otel.android.connectivity.ExportEndpointConfiguration
import co.elastic.otel.android.exporters.configuration.ExportProtocol
import co.elastic.otel.android.interceptor.Interceptor
import co.elastic.otel.android.internal.opentelemetry.SignalType

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
internal class ExportConnectivityManager private constructor(
    private val spansConnectivityHolder: ConnectivityHolder,
    private val logsConnectivityHolder: ConnectivityHolder,
    private val metricsConnectivityHolder: ConnectivityHolder,
    private val defaultHeadersInterceptor: Interceptor<Map<String, String>>
) {

    fun setEndpointConfiguration(configuration: ExportEndpointConfiguration) {
        spansConnectivityHolder.setConnectivityConfiguration(
            createSpansExportConnectivityConfiguration(configuration, defaultHeadersInterceptor)
        )
        logsConnectivityHolder.setConnectivityConfiguration(
            createLogsExportConnectivityConfiguration(configuration, defaultHeadersInterceptor)
        )
        metricsConnectivityHolder.setConnectivityConfiguration(
            createMetricsExportConnectivityConfiguration(configuration, defaultHeadersInterceptor)
        )
    }

    fun getSpansConnectivityConfiguration(): ExportConnectivityConfiguration {
        return spansConnectivityHolder.getConnectivityConfiguration()
    }

    fun getLogsConnectivityConfiguration(): ExportConnectivityConfiguration {
        return logsConnectivityHolder.getConnectivityConfiguration()
    }

    fun getMetricsConnectivityConfiguration(): ExportConnectivityConfiguration {
        return metricsConnectivityHolder.getConnectivityConfiguration()
    }

    fun addChangeListener(listener: SignalConnectivityChangeListener) {
        spansConnectivityHolder.addListener(
            SignalConnectivityListenerAdapter(
                SignalType.TRACE,
                listener
            )
        )
        logsConnectivityHolder.addListener(
            SignalConnectivityListenerAdapter(
                SignalType.LOG,
                listener
            )
        )
        metricsConnectivityHolder.addListener(
            SignalConnectivityListenerAdapter(
                SignalType.METRIC,
                listener
            )
        )
    }

    private class ConnectivityHolder(initialValue: ExportConnectivityConfiguration) :
        ConnectivityConfigurationHolder(initialValue) {

        fun setConnectivityConfiguration(value: ExportConnectivityConfiguration) {
            set(value)
        }

        fun getConnectivityConfiguration(): ExportConnectivityConfiguration {
            return get() as ExportConnectivityConfiguration
        }
    }

    private class SignalConnectivityListenerAdapter(
        private val signalType: SignalType,
        private val listener: SignalConnectivityChangeListener
    ) : ConnectivityConfigurationHolder.Listener {

        override fun onConnectivityConfigurationChange() {
            listener.onConnectivityConfigurationChange(signalType)
        }
    }

    companion object {
        fun create(
            configuration: ExportEndpointConfiguration,
            headersInterceptor: Interceptor<Map<String, String>>
        ): ExportConnectivityManager {
            return ExportConnectivityManager(
                ConnectivityHolder(
                    createSpansExportConnectivityConfiguration(
                        configuration,
                        headersInterceptor
                    )
                ),
                ConnectivityHolder(
                    createLogsExportConnectivityConfiguration(
                        configuration,
                        headersInterceptor
                    )
                ),
                ConnectivityHolder(
                    createMetricsExportConnectivityConfiguration(
                        configuration,
                        headersInterceptor
                    )
                ),
                headersInterceptor
            )
        }

        private fun createMetricsExportConnectivityConfiguration(
            configuration: ExportEndpointConfiguration,
            headersInterceptor: Interceptor<Map<String, String>>
        ): ExportConnectivityConfiguration {
            return ExportConnectivityConfiguration(
                getMetricsUrl(configuration.url, configuration.protocol),
                configuration.authentication,
                configuration.protocol,
                headersInterceptor
            )
        }

        private fun createLogsExportConnectivityConfiguration(
            configuration: ExportEndpointConfiguration,
            headersInterceptor: Interceptor<Map<String, String>>
        ): ExportConnectivityConfiguration {
            return ExportConnectivityConfiguration(
                getLogsUrl(configuration.url, configuration.protocol),
                configuration.authentication,
                configuration.protocol,
                headersInterceptor
            )
        }

        private fun createSpansExportConnectivityConfiguration(
            configuration: ExportEndpointConfiguration,
            headersInterceptor: Interceptor<Map<String, String>>
        ): ExportConnectivityConfiguration {
            return ExportConnectivityConfiguration(
                getTracesUrl(configuration.url, configuration.protocol),
                configuration.authentication,
                configuration.protocol,
                headersInterceptor
            )
        }

        private fun getTracesUrl(baseUrl: String, protocol: ExportProtocol): String {
            return getSignalUrl(baseUrl, "traces", protocol)
        }

        private fun getLogsUrl(baseUrl: String, protocol: ExportProtocol): String {
            return getSignalUrl(baseUrl, "logs", protocol)
        }

        private fun getMetricsUrl(baseUrl: String, protocol: ExportProtocol): String {
            return getSignalUrl(baseUrl, "metrics", protocol)
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
}