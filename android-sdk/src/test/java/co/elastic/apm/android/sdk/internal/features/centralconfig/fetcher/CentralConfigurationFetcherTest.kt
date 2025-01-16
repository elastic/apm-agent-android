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
package co.elastic.apm.android.sdk.internal.features.centralconfig.fetcher

import co.elastic.apm.android.sdk.connectivity.ConnectivityConfiguration
import co.elastic.apm.android.sdk.features.centralconfig.fetcher.CentralConfigurationFetcher
import co.elastic.apm.android.sdk.internal.services.preferences.PreferencesService
import co.elastic.apm.android.sdk.testutils.ElasticAgentRule
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.io.File
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CentralConfigurationFetcherTest {
    private lateinit var preferences: PreferencesService
    private lateinit var connectivity: ConnectivityConfiguration
    private lateinit var configurationFile: File
    private lateinit var fetcher: CentralConfigurationFetcher
    private lateinit var webServer: MockWebServer

    @get:Rule
    val agentRule = ElasticAgentRule()

    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Before
    fun setUp() {
        setUpConnectivity()
        preferences = mockk<PreferencesService>()
        every { preferences.retrieveString("central_configuration_etag") }.returns(null)
        every { preferences.store("central_configuration_etag", any<String>()) } just Runs
        every { preferences.remove("central_configuration_etag") } just Runs
        configurationFile = temporaryFolder.newFile("configFile.json")
        fetcher = CentralConfigurationFetcher(configurationFile, preferences)
        agentRule.initialize()
    }

    @After
    fun tearDown() {
        webServer.shutdown()
    }

    private fun setUpConnectivity() {
        webServer = MockWebServer()
        connectivity = mockk<ConnectivityConfiguration>()
        setConnectivityEndpoint("")
    }

    @Test
    fun `On successful response, notify config changed`() {
        enqueueSimpleResponse()

        val fetch = fetcher.fetch(connectivity)

        assertThat(fetch.configurationHasChanged).isTrue()
    }

    @Test
    fun `When config not changed response, notify the caller`() {
        enqueueResponse(getResponse(304, ""))

        val fetch = fetcher.fetch(connectivity)

        assertThat(fetch.configurationHasChanged).isFalse()
    }

    @Test
    fun `Verify request content type`() {
        enqueueSimpleResponse()

        fetcher.fetch(connectivity)

        assertThat(webServer.takeRequest().getHeader("Content-Type")).isEqualTo("application/json")
    }

    @Test
    fun `Send headers when available`() {
        val authHeaderValue = "Bearer something"
        every { connectivity.getHeaders() }.returns(mapOf("Authorization" to authHeaderValue))
        enqueueSimpleResponse()

        fetcher.fetch(connectivity)

        assertThat(webServer.takeRequest().getHeader("Authorization")).isEqualTo(authHeaderValue)
    }

    @Test
    fun `Store eTag when received`() {
        val theEtag = "someEtag"
        enqueueResponse(getResponse(200, "{}").setHeader("ETag", theEtag))

        fetcher.fetch(connectivity)

        verify { preferences.store("central_configuration_etag", theEtag) }
    }

    @Test
    fun `Send eTag when available`() {
        val theEtag = "someEtag"
        every { preferences.retrieveString("central_configuration_etag") }.returns(theEtag)
        enqueueSimpleResponse()

        fetcher.fetch(connectivity)

        val sentEtag = webServer.takeRequest().getHeader("If-None-Match")
        assertThat(sentEtag).isEqualTo(theEtag)
    }

    @Test
    fun `When max age is provided, return it`() {
        val headerMaxAge = 12345
        val headerValue = "max-age=$headerMaxAge"
        enqueueResponse(getResponse(200, "{}").setHeader("Cache-Control", headerValue))

        val result = fetcher.fetch(connectivity)

        assertThat(result.maxAgeInSeconds?.toLong()).isEqualTo(headerMaxAge.toLong())
    }

    @Test
    fun `When max age is not provided, return null`() {
        val headerValue = "no-cache"
        enqueueResponse(getResponse(200, "{}").setHeader("Cache-Control", headerValue))

        val result = fetcher.fetch(connectivity)

        assertNull(result.maxAgeInSeconds)
    }

    @Test
    fun `When no cache control is received, return null max age`() {
        enqueueSimpleResponse()

        val result = fetcher.fetch(connectivity)

        assertNull(result.maxAgeInSeconds)
    }

    @Test
    fun `Store received config in provided file`() {
        val body = "{\"some\":\"configValue\"}"
        enqueueResponse(getResponse(200, body))

        fetcher.fetch(connectivity)

        assertThat(configurationFile.readText()).isEqualTo(body)
    }

    private fun enqueueSimpleResponse() {
        enqueueResponse(getResponse(200, "{}"))
    }

    private fun enqueueResponse(response: MockResponse) {
        webServer.enqueue(response)
    }

    private fun getResponse(code: Int, body: String): MockResponse {
        return MockResponse().setResponseCode(code).setBody(body)
    }

    private fun setConnectivityEndpoint(path: String) {
        every { connectivity.getUrl() }.returns("http://" + webServer.hostName + ":" + webServer.port + path)
        every { connectivity.getHeaders() }.returns(emptyMap())
    }
}