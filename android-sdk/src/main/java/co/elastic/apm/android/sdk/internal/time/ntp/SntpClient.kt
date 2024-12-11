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
package co.elastic.apm.android.sdk.internal.time.ntp

import co.elastic.apm.android.common.internal.logging.Elog
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider
import java.io.Closeable
import java.util.concurrent.atomic.AtomicReference

/**
 * According to RFC-4330.
 */
class SntpClient(
    private val udpClient: UdpClient,
    private val systemTimeProvider: SystemTimeProvider
) : Closeable {

    fun fetchTimeOffset(currentTimeMillis: Long): Response = synchronized(this) {
        val t1 = getCurrentNtpTimeMillis(currentTimeMillis)
        val request = NtpPacket.createForClient(t1, VERSION)
        val timeBeforeRequest = systemTimeProvider.getElapsedRealTime()
        val responseBytes = udpClient.send(request.toByteArray())
        val requestTimeDelta = systemTimeProvider.getElapsedRealTime() - timeBeforeRequest
        val t4 = getCurrentNtpTimeMillis(currentTimeMillis + requestTimeDelta)
        val response = NtpPacket.parse(responseBytes)
        val t2 = response.receiveTimestamp
        val t3 = response.transmitTimestamp

        if (t1 / 1000 != response.originateTimestamp / 1000) {
            return Response.Error(ErrorType.ORIGIN_TIME_NOT_MATCHING)
        }
        if (response.leapIndicator == 3 || response.stratum == 0) {
            return Response.Error(ErrorType.TRY_LATER)
        }
        if (response.versionNumber != VERSION) {
            return Response.Error(ErrorType.INVALID_VERSION)
        }
        if (response.mode != 4) {
            return Response.Error(ErrorType.INVALID_MODE)
        }
        if (response.transmitTimestamp == 0L) {
            return Response.Error(ErrorType.INVALID_TRANSMIT_TIMESTAMP)
        }

        return Response.Success(((t2 - t1) + (t3 - t4)) / 2)
    }

    private fun getCurrentNtpTimeMillis(currentTimeMillis: Long): Long {
        return currentTimeMillis + NTP_EPOCH_DIFF_MILLIS
    }

    override fun close() {
        udpClient.close()
    }

    companion object {
        private const val NTP_EPOCH_DIFF_MILLIS = 2208988800000L // According to RFC-868.
        private const val VERSION = 4
        private val UDP_CONFIG = UdpClient.Configuration("time.android.com", 123, 48)
        private val udpConfigProvider = AtomicReference(UDP_CONFIG)

        internal fun setUdpConfigForTest(config: UdpClient.Configuration) {
            Elog.getLogger().warn("Changing udp config to: {}", config)
            udpConfigProvider.set(config)
        }

        internal fun resetUdpConfigForTest() {
            udpConfigProvider.set(UDP_CONFIG)
        }

        fun create(): SntpClient {
            return SntpClient(UdpClient.create(udpConfigProvider.get()), SystemTimeProvider.get())
        }
    }

    sealed class Response {
        data class Success(val offsetMillis: Long) : Response()
        data class Error(val type: ErrorType) : Response()
    }

    enum class ErrorType {
        TRY_LATER,
        ORIGIN_TIME_NOT_MATCHING,
        INVALID_VERSION,
        INVALID_MODE,
        INVALID_TRANSMIT_TIMESTAMP
    }
}

