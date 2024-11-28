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
package co.elastic.apm.android.sdk.internal.features.centralconfig

import android.content.Context
import co.elastic.apm.android.sdk.connectivity.Connectivity
import co.elastic.apm.android.sdk.internal.configuration.Configuration
import co.elastic.apm.android.sdk.internal.configuration.ConfigurationOption
import co.elastic.apm.android.sdk.internal.configuration.Configurations
import co.elastic.apm.android.sdk.internal.configuration.OptionsRegistry
import co.elastic.apm.android.sdk.internal.configuration.impl.ConnectivityConfiguration
import co.elastic.apm.android.sdk.internal.services.preferences.PreferencesService
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider
import co.elastic.apm.android.sdk.testutils.ElasticAgentRule
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import java.io.File
import java.io.IOException
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CentralConfigurationManagerTest {
    private lateinit var configFile: File
    private lateinit var webServer: MockWebServer
    private lateinit var manager: CentralConfigurationManager
    private lateinit var configurationsSpy: Configurations

    @MockK
    lateinit var context: Context

    @MockK
    lateinit var preferences: PreferencesService

    @MockK
    lateinit var systemTimeProvider: SystemTimeProvider

    companion object {
        private const val PREFERENCE_REFRESH_TIMEOUT_NAME = "central_configuration_refresh_timeout"
    }

    @get:Rule
    val agentRule = ElasticAgentRule()

    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.applicationContext }.returns(context)
        every { preferences.retrieveString("central_configuration_etag") }.returns("")
        every { preferences.store("central_configuration_etag", any<String>()) } just Runs
        every { preferences.store(PREFERENCE_REFRESH_TIMEOUT_NAME, any<Long>()) } just Runs
        setRefreshTimeoutTime(0)
        setCurrentMillis(1)
        manager = CentralConfigurationManager(
            context,
            systemTimeProvider,
            preferences
        )
        webServer = MockWebServer()
        val filesDir = temporaryFolder.newFolder("filesDir")
        every { context.filesDir }.returns(filesDir)
        configFile = File(filesDir, "elastic_agent_configuration.json")
        agentRule.setCentralConfigurationManager(manager)
        agentRule.addConfiguration(DummyConfiguration("someKey", false))
        agentRule.initialize()
        setUpConfigurationRegistrySpy()
    }

    private fun setUpConfigurationRegistrySpy() {
        configurationsSpy = spyk(Configurations.get())
        Configurations.INSTANCE = configurationsSpy
    }

    @After
    fun tearDown() {
        webServer.shutdown()
    }

    @Test
    fun `Notify configs from cache`() {
        setConfigFileContents("{\"someKey\":\"true\"}")

        manager.publishCachedConfig()

        assertThat(getDummyConfiguration().optionValue).isTrue()
        verify { configurationsSpy.doReload() }
    }

    @Test
    fun `When there's no cached config available while publishing, do nothing`() {
        manager.publishCachedConfig()

        assertThat(getDummyConfiguration().optionValue).isFalse()

        verify(exactly = 0) { configurationsSpy.doReload() }
    }

    @Test
    fun `When cached config content is not valid, do nothing`() {
        setConfigFileContents("not a json")

        manager.publishCachedConfig()

        verify(exactly = 0) { configurationsSpy.doReload() }
    }

    @Test
    fun `Notify configs from the network`() {
        stubNetworkResponse(200, "{\"someKey\":\"true\"}")

        manager.sync()

        assertThat(getDummyConfiguration().optionValue).isTrue()
    }

    @Test
    fun `When max age is provided from the network, store it`() {
        val currentTimeMillis: Long = 1000000
        setCurrentMillis(currentTimeMillis)
        stubNetworkResponse(200, "{\"aKey\":\"aValue\"}", "max-age=500")

        val maxAge = manager.sync()

        assertThat(maxAge.toLong()).isEqualTo(500)
        verify { preferences.store(PREFERENCE_REFRESH_TIMEOUT_NAME, 1500000L) }
    }

    @Test
    fun `When max age is not provided, do not update the preferences`() {
        stubNetworkResponse(200, "{\"aKey\":\"aValue\"}")

        val maxAge = manager.sync()

        assertNull(maxAge)
        verify(exactly = 0) { preferences.store(PREFERENCE_REFRESH_TIMEOUT_NAME, any<Long>()) }
    }

    @Test
    fun `Do not reload configs when the network request fails`() {
        stubNetworkResponse(304, "{\"aKey\":\"aValue\"}")

        manager.sync()

        verify(exactly = 0) { configurationsSpy.doReload() }
    }

    @Test
    fun `During sync, execute fetching when max age is available and expired`() {
        stubNetworkResponse(200, "{}")
        val currentTimeMillis: Long = 1000000
        setCurrentMillis(currentTimeMillis)
        setRefreshTimeoutTime(currentTimeMillis - 1)

        manager.sync()

        verify { configurationsSpy.doReload() }
    }

    @Test
    fun `During sync, do not execute fetching when max age hasn't expired`() {
        stubNetworkResponse(200, "{}")
        val currentTimeMillis: Long = 1000000
        setCurrentMillis(currentTimeMillis)
        setRefreshTimeoutTime(currentTimeMillis + 1)

        manager.sync()

        verify(exactly = 0) { configurationsSpy.doReload() }
    }

    private fun setCurrentMillis(currentTimeMillis: Long) {
        every { systemTimeProvider.currentTimeMillis }.returns(currentTimeMillis)
    }

    private fun getDummyConfiguration(): DummyConfiguration {
        return Configurations.get(DummyConfiguration::class.java)!!
    }

    private fun setRefreshTimeoutTime(timeInMillis: Long) {
        every { preferences.retrieveLong(PREFERENCE_REFRESH_TIMEOUT_NAME, 0) }.returns(
            timeInMillis
        )
    }

    private fun stubNetworkResponse(code: Int, body: String) {
        stubNetworkResponse(code, body, null)
    }

    private fun stubNetworkResponse(code: Int, body: String, cacheControlHeader: String?) {
        val connectivity = mockk<Connectivity>()
        val field = ConnectivityConfiguration::class.java.getDeclaredField("connectivity")
        field.isAccessible = true
        field[Configurations.get(ConnectivityConfiguration::class.java)] = connectivity
        every { connectivity.endpoint() }.returns("http://" + webServer.hostName + ":" + webServer.port)
        every { connectivity.authConfiguration() }.returns(null)
        var response = MockResponse().setResponseCode(code).setBody(body)
        if (cacheControlHeader != null) {
            response = response.setHeader("Cache-Control", cacheControlHeader)
        }
        webServer.enqueue(response)
    }

    private fun setConfigFileContents(contents: String) {
        try {
            if (!configFile.exists()) {
                configFile.createNewFile()
            }
            configFile.writeText(contents)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private class DummyConfiguration(optionKey: String, optionValue: Boolean) :
        Configuration() {
        val option: ConfigurationOption<Boolean> = createBooleanOption(optionKey, optionValue)

        val optionValue: Boolean
            get() = option.get()

        override fun visitOptions(options: OptionsRegistry) {
            super.visitOptions(options)
            options.register(option)
        }
    }
}