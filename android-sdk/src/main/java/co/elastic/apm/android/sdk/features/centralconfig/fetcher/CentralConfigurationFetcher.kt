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
package co.elastic.apm.android.sdk.features.centralconfig.fetcher

import co.elastic.apm.android.common.internal.logging.Elog
import co.elastic.apm.android.sdk.connectivity.ConnectivityConfiguration
import co.elastic.apm.android.sdk.internal.services.kotlin.preferences.PreferencesService
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.regex.Pattern
import org.slf4j.Logger

class CentralConfigurationFetcher(
    private val destFile: File,
    private val preferences: PreferencesService
) {
    private val logger: Logger = Elog.getLogger()

    @Throws(IOException::class)
    internal fun fetch(connectivity: ConnectivityConfiguration): FetchResult {
        val connection = getUrl(connectivity).openConnection() as HttpURLConnection
        val eTag = getETag()
        connection.setRequestProperty("Content-Type", "application/json")
        if (eTag != null) {
            connection.setRequestProperty("If-None-Match", eTag)
        }
        connectivity.getHeaders().forEach { (key, value) ->
            connection.setRequestProperty(key, value)
        }
        try {
            storeETag(connection.getHeaderField("ETag"))
            val maxAgeInSeconds = parseMaxAge(connection.getHeaderField("Cache-Control"))
            val responseCode = connection.responseCode
            if (responseCode == REQUEST_OK) {
                saveConfiguration(connection.inputStream)
                return FetchResult(maxAgeInSeconds, true)
            } else {
                handleUnsuccessfulResponse(responseCode)
            }
            return FetchResult(maxAgeInSeconds, false)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseMaxAge(cacheControlHeader: String?): Int? {
        if (cacheControlHeader == null) {
            logger.debug("Central config cache control header not found")
            return null
        }
        val matcher = MAX_AGE.matcher(cacheControlHeader)
        if (!matcher.find()) {
            logger.debug(
                "Central config cache control header has invalid format: {}",
                cacheControlHeader
            )
            return null
        }
        return matcher.group(1)?.toInt()
    }

    private fun handleUnsuccessfulResponse(responseCode: Int) {
        when (responseCode) {
            CONFIGURATION_NOT_MODIFIED -> logger.debug("Central configuration did not change")
            CONFIGURATION_NOT_FOUND -> logger.debug("This APM Server does not support central configuration. Update to APM Server 7.3+")
            REQUEST_FORBIDDEN -> logger.debug("Central configuration is disabled. Set kibana.enabled: true in your APM Server configuration.")
            SERVICE_UNAVAILABLE -> throw IllegalStateException("Remote configuration is not available. Check the connection between APM Server and Kibana.")
            else -> throw IllegalStateException("Unexpected status $responseCode while fetching configuration")
        }
    }

    @Throws(IOException::class)
    private fun saveConfiguration(inputStream: InputStream) {
        destFile.outputStream().use {
            inputStream.copyTo(it)
        }
    }

    private fun storeETag(eTag: String?) {
        logger.debug("Storing central config ETag {}", eTag)
        eTag?.let {
            preferences.store(ETAG_PREFERENCE_NAME, it)
        } ?: preferences.remove(ETAG_PREFERENCE_NAME)
    }

    private fun getETag(): String? {
        val eTag = preferences.retrieveString(ETAG_PREFERENCE_NAME)
        logger.debug("Retrieving central config ETag {}", eTag)
        return eTag
    }

    @Throws(MalformedURLException::class)
    private fun getUrl(connectivity: ConnectivityConfiguration): URL {
        val url = connectivity.getUrl()
        logger.debug("Central config url: {}", url)
        return URL(url)
    }

    companion object {
        private const val REQUEST_OK = 200
        private const val CONFIGURATION_NOT_MODIFIED = 304
        private const val REQUEST_FORBIDDEN = 403
        private const val CONFIGURATION_NOT_FOUND = 404
        private const val SERVICE_UNAVAILABLE = 503
        private const val ETAG_PREFERENCE_NAME = "central_configuration_etag"
        private val MAX_AGE: Pattern = Pattern.compile("max-age\\s*=\\s*(\\d+)")
    }
}