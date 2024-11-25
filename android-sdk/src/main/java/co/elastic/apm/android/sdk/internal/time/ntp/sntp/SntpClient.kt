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
package co.elastic.apm.android.sdk.internal.time.ntp.sntp

import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider
import java.io.Closeable

/**
 * According to RFC-4330.
 */
internal class SntpClient(
    private val udpClient: UdpClient,
    private val systemTime: SystemTimeProvider
) : Closeable {

    fun fetchTimeOffset(): Response {
        val t1 = getCurrentNtpTimeMillis()
        val request = NtpPacket.createForClient(t1)
        val responseBytes = udpClient.send(request.toByteArray())
        val t4 = getCurrentNtpTimeMillis()
        val response = NtpPacket.parse(responseBytes)
        val t2 = response.receiveTimestamp
        val t3 = response.transmitTimestamp
        val clockOffsetMillis = ((t2 - t1) + (t3 - t4)) / 2

        return Response.Success(clockOffsetMillis)
    }

    private fun getCurrentNtpTimeMillis(): Long {
        return systemTime.currentTimeMillis + NTP_EPOCH_DIFF_MILLIS
    }

    override fun close() {
        udpClient.close()
    }

    companion object {
        private const val NTP_EPOCH_DIFF_MILLIS = 2208988800000L // According to RFC-868.

        fun create(): SntpClient {
            return SntpClient(UdpClient("time.android.com", 123, 48), SystemTimeProvider.get())
        }
    }

    sealed class Response {
        data class Success(val offsetMillis: Long) : Response()
    }
}