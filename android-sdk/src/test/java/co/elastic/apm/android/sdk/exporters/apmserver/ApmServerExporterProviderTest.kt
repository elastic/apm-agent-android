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

import co.elastic.apm.android.sdk.exporters.configurable.ExportProtocol
import co.elastic.apm.android.sdk.exporters.configurable.ExporterConfiguration
import io.opentelemetry.sdk.metrics.data.AggregationTemporality
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ApmServerExporterProviderTest {

    @Test
    fun `Verify default configuration`() {
        val url = "http://my.server.url"

        val instance = ApmServerExporterProvider.builder()
            .setUrl(url)
            .build()

        assertThat(instance.getApmServerConfiguration()).isEqualTo(
            ApmServerConfiguration(url, ApmServerConfiguration.Auth.None)
        )
        val configurableProvider = instance.exporterProvider
        assertThat(configurableProvider.getSpanExporterConfiguration()).isEqualTo(
            ExporterConfiguration.Span("$url/v1/traces", emptyMap(), ExportProtocol.HTTP)
        )
        assertThat(configurableProvider.getLogRecordExporterConfiguration()).isEqualTo(
            ExporterConfiguration.LogRecord("$url/v1/logs", emptyMap(), ExportProtocol.HTTP)
        )
        assertThat(configurableProvider.getMetricExporterConfiguration()).isEqualTo(
            ExporterConfiguration.Metric(
                "$url/v1/metrics",
                emptyMap(),
                ExportProtocol.HTTP,
                AggregationTemporality.CUMULATIVE
            )
        )
    }

    @Test
    fun `Verify handling of trailing slash`() {
        val url = "http://my.server.url"
        val providedUrl = "http://my.server.url/"

        val instance = ApmServerExporterProvider.builder()
            .setUrl(providedUrl)
            .build()

        assertThat(instance.getApmServerConfiguration()).isEqualTo(
            ApmServerConfiguration(url, ApmServerConfiguration.Auth.None)
        )
        val configurableProvider = instance.exporterProvider
        assertThat(configurableProvider.getSpanExporterConfiguration()).isEqualTo(
            ExporterConfiguration.Span("$url/v1/traces", emptyMap(), ExportProtocol.HTTP)
        )
        assertThat(configurableProvider.getLogRecordExporterConfiguration()).isEqualTo(
            ExporterConfiguration.LogRecord("$url/v1/logs", emptyMap(), ExportProtocol.HTTP)
        )
        assertThat(configurableProvider.getMetricExporterConfiguration()).isEqualTo(
            ExporterConfiguration.Metric(
                "$url/v1/metrics",
                emptyMap(),
                ExportProtocol.HTTP,
                AggregationTemporality.CUMULATIVE
            )
        )
    }
}