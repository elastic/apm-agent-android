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

import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class SntpClientTest {
    private lateinit var client: SntpClient
    private lateinit var udpClient: UdpClient
    private lateinit var systemTimeProvider: SystemTimeProvider

    companion object {
        private const val NTP_EPOCH_DIFF_MILLIS = 2208988800000L // According to RFC-868.
        private const val DEFAULT_SERVER_RECEIVE_TIME = 1_000_000_000_000L
    }

    @BeforeEach
    fun setUp() {
        udpClient = mockk()
        systemTimeProvider = mockk()
        client = SntpClient(udpClient, systemTimeProvider)
        every { systemTimeProvider.elapsedRealTime }.returns(0L)
    }

    @Test
    fun `Fetch time offset, happy path`() {
        val expectedOffset = 100L
        setUpResponse(expectedOffset)

        val response = client.fetchTimeOffset()

        if (response !is SntpClient.Response.Success) {
            fail("Response must be successful.")
        }
        assertThat(response.offsetMillis).isEqualTo(expectedOffset)
    }

    @Test
    fun `Verify returned origin timestamp matches the timestamp sent`() {
        val timestampSent = DEFAULT_SERVER_RECEIVE_TIME - 100
        setUpResponse(100, transmitClientTime = timestampSent, originateTimestamp = 123)

        assertThat(client.fetchTimeOffset()).isEqualTo(SntpClient.Response.Error(SntpClient.ErrorType.ORIGIN_TIME_NOT_MATCHING))
    }

    @Test
    fun `Discard response if LI value is 3`() {
        setUpResponse(100, responseLeapIndicator = 3)

        assertThat(client.fetchTimeOffset()).isEqualTo(SntpClient.Response.Error(SntpClient.ErrorType.TRY_LATER))
    }

    @Test
    fun `Verify VN is the same as the one sent from the client`() {
        setUpResponse(100, requestVersionNumber = 4, responseVersionNumber = 3)

        assertThat(client.fetchTimeOffset()).isEqualTo(SntpClient.Response.Error(SntpClient.ErrorType.INVALID_VERSION))
    }

    @Test
    fun `Discard response if mode is not 4`() {
        setUpResponse(100, responseMode = 3)

        assertThat(client.fetchTimeOffset()).isEqualTo(SntpClient.Response.Error(SntpClient.ErrorType.INVALID_MODE))
    }

    @Test
    fun `Discard response if transmit timestamp is 0`() {
        setUpResponse(100, transmitServerTime = 0)

        assertThat(client.fetchTimeOffset()).isEqualTo(SntpClient.Response.Error(SntpClient.ErrorType.INVALID_TRANSMIT_TIMESTAMP))
    }

    @Test
    fun `Discard response if stratum is 0`() {
        setUpResponse(100, responseStratum = 0)

        assertThat(client.fetchTimeOffset()).isEqualTo(SntpClient.Response.Error(SntpClient.ErrorType.TRY_LATER))
    }

    private fun setUpResponse(
        expectedOffset: Long,
        receiveServerTime: Long = DEFAULT_SERVER_RECEIVE_TIME,
        transmitClientTime: Long = receiveServerTime - expectedOffset,
        transmitServerTime: Long = receiveServerTime + 5,
        receiveClientTime: Long = transmitClientTime + 5,
        originateTimestamp: Long = transmitClientTime,
        responseLeapIndicator: Int = 0,
        requestVersionNumber: Int = 4,
        responseVersionNumber: Int = requestVersionNumber,
        responseMode: Int = 4,
        responseStratum: Int = 1
    ) {
        every { systemTimeProvider.currentTimeMillis }.returns(transmitClientTime)
            .andThen(receiveClientTime)
        every {
            udpClient.send(
                NtpPacket.createForClient(toNtpTime(transmitClientTime), requestVersionNumber)
                    .toByteArray()
            )
        }.returns(
            NtpPacket(
                responseLeapIndicator,
                responseVersionNumber,
                responseMode,
                responseStratum,
                toNtpTime(originateTimestamp),
                toNtpTime(receiveServerTime),
                if (transmitServerTime != 0L) toNtpTime(transmitServerTime) else 0
            ).toByteArray()
        )
    }

    private fun toNtpTime(time: Long): Long {
        return time + NTP_EPOCH_DIFF_MILLIS
    }
}