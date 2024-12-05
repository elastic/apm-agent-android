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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ApmServerExporterProviderTest {

    @Test
    fun `Verify initialization's default configuration`() {
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
                ExportProtocol.HTTP
            )
        )
    }

    @Test
    fun `Verify initialization's handling of trailing slash`() {
        val url = "http://my.server.url"
        val providedUrl = "http://my.server.url/"

        val instance = ApmServerExporterProvider.builder()
            .setUrl(providedUrl)
            .build()

        assertThat(instance.getApmServerConfiguration()).isEqualTo(
            ApmServerConfiguration(providedUrl, ApmServerConfiguration.Auth.None)
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
                ExportProtocol.HTTP
            )
        )
    }

    @Test
    fun `Verify initialization with custom protocol`() {
        val url = "http://my.server.url"

        val instance = ApmServerExporterProvider.builder()
            .setUrl(url)
            .setExportProtocol(ExportProtocol.GRPC)
            .build()

        assertThat(instance.getApmServerConfiguration()).isEqualTo(
            ApmServerConfiguration(url, ApmServerConfiguration.Auth.None)
        )
        val configurableProvider = instance.exporterProvider
        assertThat(configurableProvider.getSpanExporterConfiguration()).isEqualTo(
            ExporterConfiguration.Span(url, emptyMap(), ExportProtocol.GRPC)
        )
        assertThat(configurableProvider.getLogRecordExporterConfiguration()).isEqualTo(
            ExporterConfiguration.LogRecord(url, emptyMap(), ExportProtocol.GRPC)
        )
        assertThat(configurableProvider.getMetricExporterConfiguration()).isEqualTo(
            ExporterConfiguration.Metric(
                url,
                emptyMap(),
                ExportProtocol.GRPC
            )
        )
    }

    @Test
    fun `Verify initialization with secret token`() {
        val url = "http://my.server.url"
        val token = "the-token"

        val instance = ApmServerExporterProvider.builder()
            .setUrl(url)
            .setAuthentication(ApmServerConfiguration.Auth.SecretToken(token))
            .build()

        assertThat(instance.getApmServerConfiguration()).isEqualTo(
            ApmServerConfiguration(url, ApmServerConfiguration.Auth.SecretToken(token))
        )
        val configurableProvider = instance.exporterProvider
        val headers = mapOf("Authorization" to "Bearer $token")
        assertThat(configurableProvider.getSpanExporterConfiguration()).isEqualTo(
            ExporterConfiguration.Span("$url/v1/traces", headers, ExportProtocol.HTTP)
        )
        assertThat(configurableProvider.getLogRecordExporterConfiguration()).isEqualTo(
            ExporterConfiguration.LogRecord("$url/v1/logs", headers, ExportProtocol.HTTP)
        )
        assertThat(configurableProvider.getMetricExporterConfiguration()).isEqualTo(
            ExporterConfiguration.Metric(
                "$url/v1/metrics",
                headers,
                ExportProtocol.HTTP
            )
        )
    }

    @Test
    fun `Verify initialization with api key`() {
        val url = "http://my.server.url"
        val key = "the-key"

        val instance = ApmServerExporterProvider.builder()
            .setUrl(url)
            .setAuthentication(ApmServerConfiguration.Auth.ApiKey(key))
            .build()

        assertThat(instance.getApmServerConfiguration()).isEqualTo(
            ApmServerConfiguration(url, ApmServerConfiguration.Auth.ApiKey(key))
        )
        val configurableProvider = instance.exporterProvider
        val headers = mapOf("Authorization" to "ApiKey $key")
        assertThat(configurableProvider.getSpanExporterConfiguration()).isEqualTo(
            ExporterConfiguration.Span("$url/v1/traces", headers, ExportProtocol.HTTP)
        )
        assertThat(configurableProvider.getLogRecordExporterConfiguration()).isEqualTo(
            ExporterConfiguration.LogRecord("$url/v1/logs", headers, ExportProtocol.HTTP)
        )
        assertThat(configurableProvider.getMetricExporterConfiguration()).isEqualTo(
            ExporterConfiguration.Metric(
                "$url/v1/metrics",
                headers,
                ExportProtocol.HTTP
            )
        )
    }


    @Test
    fun `Asser url is set during initialization`() {
        assertThrows<IllegalArgumentException> {
            ApmServerExporterProvider.builder().build()
        }
    }

    @Test
    fun `Verify configuration change`() {
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
                ExportProtocol.HTTP
            )
        )

        // Config change
        val newUrl = "http://my.new.url"
        val apiKey = "the-key"
        val newAuth = ApmServerConfiguration.Auth.ApiKey(apiKey)
        instance.setApmServerConfiguration(ApmServerConfiguration(newUrl, newAuth))

        val headers = mapOf("Authorization" to "ApiKey $apiKey")
        assertThat(instance.getApmServerConfiguration()).isEqualTo(
            ApmServerConfiguration(newUrl, newAuth)
        )
        assertThat(configurableProvider.getSpanExporterConfiguration()).isEqualTo(
            ExporterConfiguration.Span("$newUrl/v1/traces", headers, ExportProtocol.HTTP)
        )
        assertThat(configurableProvider.getLogRecordExporterConfiguration()).isEqualTo(
            ExporterConfiguration.LogRecord("$newUrl/v1/logs", headers, ExportProtocol.HTTP)
        )
        assertThat(configurableProvider.getMetricExporterConfiguration()).isEqualTo(
            ExporterConfiguration.Metric(
                "$newUrl/v1/metrics",
                headers,
                ExportProtocol.HTTP
            )
        )
    }

    @Test
    fun `Verify configuration change url trailing slash handling`() {
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
                ExportProtocol.HTTP
            )
        )

        // Config change
        val newUrl = "http://my.new.url"
        val providedNewUrl = "http://my.new.url/"
        val apiKey = "the-key"
        val newAuth = ApmServerConfiguration.Auth.ApiKey(apiKey)
        instance.setApmServerConfiguration(ApmServerConfiguration(providedNewUrl, newAuth))

        val headers = mapOf("Authorization" to "ApiKey $apiKey")
        assertThat(instance.getApmServerConfiguration()).isEqualTo(
            ApmServerConfiguration(providedNewUrl, newAuth)
        )
        assertThat(configurableProvider.getSpanExporterConfiguration()).isEqualTo(
            ExporterConfiguration.Span("$newUrl/v1/traces", headers, ExportProtocol.HTTP)
        )
        assertThat(configurableProvider.getLogRecordExporterConfiguration()).isEqualTo(
            ExporterConfiguration.LogRecord("$newUrl/v1/logs", headers, ExportProtocol.HTTP)
        )
        assertThat(configurableProvider.getMetricExporterConfiguration()).isEqualTo(
            ExporterConfiguration.Metric(
                "$newUrl/v1/metrics",
                headers,
                ExportProtocol.HTTP
            )
        )
    }

    @Test
    fun `Verify configuration change for auth types`() {
        val url = "http://my.server.url"
        val token = "the-token"

        val instance = ApmServerExporterProvider.builder()
            .setUrl(url)
            .setAuthentication(ApmServerConfiguration.Auth.SecretToken(token))
            .build()

        assertThat(instance.getApmServerConfiguration()).isEqualTo(
            ApmServerConfiguration(url, ApmServerConfiguration.Auth.SecretToken(token))
        )
        val configurableProvider = instance.exporterProvider
        val headers = mapOf("Authorization" to "Bearer $token")
        assertThat(configurableProvider.getSpanExporterConfiguration()).isEqualTo(
            ExporterConfiguration.Span("$url/v1/traces", headers, ExportProtocol.HTTP)
        )
        assertThat(configurableProvider.getLogRecordExporterConfiguration()).isEqualTo(
            ExporterConfiguration.LogRecord("$url/v1/logs", headers, ExportProtocol.HTTP)
        )
        assertThat(configurableProvider.getMetricExporterConfiguration()).isEqualTo(
            ExporterConfiguration.Metric(
                "$url/v1/metrics",
                headers,
                ExportProtocol.HTTP
            )
        )

        // Config change
        val newUrl = "http://my.new.url"
        val apiKey = "the-key"
        val newAuth = ApmServerConfiguration.Auth.ApiKey(apiKey)
        instance.setApmServerConfiguration(ApmServerConfiguration(newUrl, newAuth))

        val newHeaders = mapOf("Authorization" to "ApiKey $apiKey")
        assertThat(instance.getApmServerConfiguration()).isEqualTo(
            ApmServerConfiguration(newUrl, newAuth)
        )
        assertThat(configurableProvider.getSpanExporterConfiguration()).isEqualTo(
            ExporterConfiguration.Span("$newUrl/v1/traces", newHeaders, ExportProtocol.HTTP)
        )
        assertThat(configurableProvider.getLogRecordExporterConfiguration()).isEqualTo(
            ExporterConfiguration.LogRecord("$newUrl/v1/logs", newHeaders, ExportProtocol.HTTP)
        )
        assertThat(configurableProvider.getMetricExporterConfiguration()).isEqualTo(
            ExporterConfiguration.Metric(
                "$newUrl/v1/metrics",
                newHeaders,
                ExportProtocol.HTTP
            )
        )
    }

    @Test
    fun `Verify configuration change preserves existing headers`() {
        val url = "http://my.server.url"

        val instance = ApmServerExporterProvider.builder()
            .setUrl(url)
            .build()

        val headers = mapOf("Authorization" to "Custom auth", "Something" to "Something value")
        instance.exporterProvider.setSpanExporterConfiguration(
            instance.exporterProvider.getSpanExporterConfiguration()?.copy(headers = headers)
        )
        instance.exporterProvider.setLogRecordExporterConfiguration(
            instance.exporterProvider.getLogRecordExporterConfiguration()?.copy(headers = headers)
        )
        instance.exporterProvider.setMetricExporterConfiguration(
            instance.exporterProvider.getMetricExporterConfiguration()?.copy(headers = headers)
        )
        assertThat(instance.getApmServerConfiguration()).isEqualTo(
            ApmServerConfiguration(url, ApmServerConfiguration.Auth.None)
        )
        val configurableProvider = instance.exporterProvider
        assertThat(configurableProvider.getSpanExporterConfiguration()).isEqualTo(
            ExporterConfiguration.Span("$url/v1/traces", headers, ExportProtocol.HTTP)
        )
        assertThat(configurableProvider.getLogRecordExporterConfiguration()).isEqualTo(
            ExporterConfiguration.LogRecord("$url/v1/logs", headers, ExportProtocol.HTTP)
        )
        assertThat(configurableProvider.getMetricExporterConfiguration()).isEqualTo(
            ExporterConfiguration.Metric(
                "$url/v1/metrics",
                headers,
                ExportProtocol.HTTP
            )
        )

        // Config change
        val newUrl = "http://my.new.url"
        val apiKey = "the-key"
        val newAuth = ApmServerConfiguration.Auth.ApiKey(apiKey)
        instance.setApmServerConfiguration(ApmServerConfiguration(newUrl, newAuth))

        val newHeaders =
            mapOf("Authorization" to "ApiKey $apiKey", "Something" to "Something value")
        assertThat(instance.getApmServerConfiguration()).isEqualTo(
            ApmServerConfiguration(newUrl, newAuth)
        )
        assertThat(configurableProvider.getSpanExporterConfiguration()).isEqualTo(
            ExporterConfiguration.Span("$newUrl/v1/traces", newHeaders, ExportProtocol.HTTP)
        )
        assertThat(configurableProvider.getLogRecordExporterConfiguration()).isEqualTo(
            ExporterConfiguration.LogRecord("$newUrl/v1/logs", newHeaders, ExportProtocol.HTTP)
        )
        assertThat(configurableProvider.getMetricExporterConfiguration()).isEqualTo(
            ExporterConfiguration.Metric(
                "$newUrl/v1/metrics",
                newHeaders,
                ExportProtocol.HTTP
            )
        )
    }
}