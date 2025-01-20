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
package co.elastic.otel.android.internal.time.ntp

import co.elastic.otel.android.internal.time.SystemTimeProvider
import co.elastic.otel.android.testutils.NtpUtils.toNtpTime
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class SntpClientImplTest {
    private lateinit var client: SntpClient
    private lateinit var udpClient: UdpClient
    private lateinit var systemTimeProvider: SystemTimeProvider

    @BeforeEach
    fun setUp() {
        udpClient = mockk()
        systemTimeProvider = mockk()
        client = SntpClientImpl(udpClient, systemTimeProvider)
        every { systemTimeProvider.getElapsedRealTime() }.returns(0L)
    }

    @Test
    fun `Fetch time offset, happy path`() {
        val clientCurrentTime = 1000L
        val expectedOffset = 100L
        setUpResponse(clientCurrentTime = clientCurrentTime, expectedOffset)

        val response = client.fetchTimeOffset(clientCurrentTime)

        if (response !is SntpClient.Response.Success) {
            fail("Response must be successful.")
        }
        assertThat(response.offsetMillis).isEqualTo(expectedOffset)
    }

    @Test
    fun `Verify returned origin timestamp matches the timestamp sent`() {
        val timestampSent = 1000L
        setUpResponse(
            clientCurrentTime = timestampSent,
            originateTimestamp = 123
        )

        assertThat(client.fetchTimeOffset(timestampSent)).isEqualTo(
            SntpClient.Response.Error(
                SntpClient.ErrorType.ORIGIN_TIME_NOT_MATCHING
            )
        )
    }

    @Test
    fun `Discard response if LI value is 3`() {
        val clientCurrentTime = 1000L
        setUpResponse(
            clientCurrentTime = clientCurrentTime,
            responseLeapIndicator = 3
        )

        assertThat(client.fetchTimeOffset(clientCurrentTime)).isEqualTo(
            SntpClient.Response.Error(
                SntpClient.ErrorType.TRY_LATER
            )
        )
    }

    @Test
    fun `Verify VN is the same as the one sent from the client`() {
        val clientCurrentTime = 1000L
        setUpResponse(
            clientCurrentTime = clientCurrentTime,
            requestVersionNumber = 4,
            responseVersionNumber = 3
        )

        assertThat(client.fetchTimeOffset(clientCurrentTime)).isEqualTo(
            SntpClient.Response.Error(
                SntpClient.ErrorType.INVALID_VERSION
            )
        )
    }

    @Test
    fun `Discard response if mode is not 4`() {
        val clientCurrentTime = 1000L
        setUpResponse(clientCurrentTime = clientCurrentTime, responseMode = 3)

        assertThat(client.fetchTimeOffset(clientCurrentTime)).isEqualTo(
            SntpClient.Response.Error(
                SntpClient.ErrorType.INVALID_MODE
            )
        )
    }

    @Test
    fun `Discard response if transmit timestamp is 0`() {
        val clientCurrentTime = 1000L
        setUpResponse(clientCurrentTime = clientCurrentTime, transmitServerTime = 0)

        assertThat(client.fetchTimeOffset(clientCurrentTime)).isEqualTo(
            SntpClient.Response.Error(
                SntpClient.ErrorType.INVALID_TRANSMIT_TIMESTAMP
            )
        )
    }

    @Test
    fun `Discard response if stratum is 0`() {
        val clientCurrentTime = 1000L
        setUpResponse(clientCurrentTime = clientCurrentTime, responseStratum = 0)

        assertThat(client.fetchTimeOffset(clientCurrentTime)).isEqualTo(
            SntpClient.Response.Error(
                SntpClient.ErrorType.TRY_LATER
            )
        )
    }

    private fun setUpResponse(
        clientCurrentTime: Long,
        expectedOffset: Long = 100L,
        receiveServerTime: Long = clientCurrentTime + expectedOffset,
        receiveClientTimeOffset: Long = 5,
        transmitServerTime: Long = receiveServerTime + 5,
        originateTimestamp: Long = clientCurrentTime,
        responseLeapIndicator: Int = 0,
        requestVersionNumber: Int = 4,
        responseVersionNumber: Int = requestVersionNumber,
        responseMode: Int = 4,
        responseStratum: Int = 1
    ) {
        every { systemTimeProvider.getElapsedRealTime() }.returns(0)
            .andThen(receiveClientTimeOffset)
        every {
            udpClient.send(
                NtpPacket.createForClient(toNtpTime(clientCurrentTime), requestVersionNumber)
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
}