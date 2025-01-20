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
package co.elastic.otel.android.exporters.apmserver

import co.elastic.otel.android.exporters.configuration.ExportProtocol
import co.elastic.otel.android.features.apmserver.ApmServerAuthentication
import co.elastic.otel.android.features.apmserver.ApmServerConnectivity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ApmServerConnectivityTest {

    @Test
    fun `Verify default values`() {
        val url = "http://my.server.url"

        val instance = ApmServerConnectivity(url)

        assertThat(instance).isEqualTo(
            ApmServerConnectivity(
                url,
                ApmServerAuthentication.None,
                emptyMap(), ExportProtocol.HTTP
            )
        )
        assertThat(instance.getUrl()).isEqualTo(url)
        assertThat(instance.getTracesUrl()).isEqualTo("$url/v1/traces")
        assertThat(instance.getLogsUrl()).isEqualTo("$url/v1/logs")
        assertThat(instance.getMetricsUrl()).isEqualTo("$url/v1/metrics")
        assertThat(instance.getHeaders()).isEqualTo(emptyMap<String, String>())
    }

    @Test
    fun `Verify handling of trailing slash`() {
        val url = "http://my.server.url"
        val providedUrl = "http://my.server.url/"

        val instance = ApmServerConnectivity(providedUrl)

        assertThat(instance.getUrl()).isEqualTo(providedUrl)
        assertThat(instance.getTracesUrl()).isEqualTo("$url/v1/traces")
        assertThat(instance.getLogsUrl()).isEqualTo("$url/v1/logs")
        assertThat(instance.getMetricsUrl()).isEqualTo("$url/v1/metrics")
        assertThat(instance.getHeaders()).isEqualTo(emptyMap<String, String>())
    }

    @Test
    fun `Verify initialization with grpc protocol`() {
        val url = "http://my.server.url"

        val instance = ApmServerConnectivity(url, exportProtocol = ExportProtocol.GRPC)

        assertThat(instance.getUrl()).isEqualTo(url)
        assertThat(instance.getTracesUrl()).isEqualTo(url)
        assertThat(instance.getLogsUrl()).isEqualTo(url)
        assertThat(instance.getMetricsUrl()).isEqualTo(url)
        assertThat(instance.getHeaders()).isEqualTo(emptyMap<String, String>())
    }

    @Test
    fun `Verify initialization with secret token`() {
        val url = "http://my.server.url"
        val token = "the-token"

        val instance = ApmServerConnectivity(
            url,
            ApmServerAuthentication.SecretToken(token)
        )

        assertThat(instance.getUrl()).isEqualTo(url)
        assertThat(instance.getTracesUrl()).isEqualTo("$url/v1/traces")
        assertThat(instance.getLogsUrl()).isEqualTo("$url/v1/logs")
        assertThat(instance.getMetricsUrl()).isEqualTo("$url/v1/metrics")
        assertThat(instance.getHeaders()).isEqualTo(mapOf("Authorization" to "Bearer $token"))
    }

    @Test
    fun `Verify initialization with api key`() {
        val url = "http://my.server.url"
        val key = "the-key"

        val instance = ApmServerConnectivity(
            url,
            ApmServerAuthentication.ApiKey(key)
        )

        assertThat(instance.getUrl()).isEqualTo(url)
        assertThat(instance.getTracesUrl()).isEqualTo("$url/v1/traces")
        assertThat(instance.getLogsUrl()).isEqualTo("$url/v1/logs")
        assertThat(instance.getMetricsUrl()).isEqualTo("$url/v1/metrics")
        assertThat(instance.getHeaders()).isEqualTo(mapOf("Authorization" to "ApiKey $key"))
    }
}