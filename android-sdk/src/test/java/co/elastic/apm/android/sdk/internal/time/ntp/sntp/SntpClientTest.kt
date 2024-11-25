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
    }

    @BeforeEach
    fun setUp() {
        udpClient = mockk()
        systemTimeProvider = mockk()
        client = SntpClient(udpClient, systemTimeProvider)
    }

    @Test
    fun `Fetch time offset, happy path`() {
        val expectedOffset = 100L
        val receiveServerTime = 1_000_000_000_000L
        val transmitClientTime = receiveServerTime - expectedOffset
        val transmitServerTime = receiveServerTime + 5
        val receiveClientTime = transmitClientTime + 5
        every { systemTimeProvider.currentTimeMillis }.returns(transmitClientTime)
            .andThen(receiveClientTime)
        every {
            udpClient.send(
                NtpPacket.createForClient(toNtpTime(transmitClientTime)).toByteArray()
            )
        }.returns(
            NtpPacket(
                0,
                4,
                4,
                1,
                toNtpTime(transmitClientTime),
                toNtpTime(receiveServerTime),
                toNtpTime(transmitServerTime)
            ).toByteArray()
        )

        val response = client.fetchTimeOffset()

        if (response !is SntpClient.Response.Success) {
            fail("Response must be successful.")
        }
        assertThat(response.offsetMillis).isEqualTo(expectedOffset)
    }

    @Test
    fun `Ensure min polling interval is one minute`() {

    }

    @Test
    fun `Check returned origin timestamp matches the timestamp sent`() {

    }

    @Test
    fun `Discard response if LI value is not between 1 and 3`() {

    }

    @Test
    fun `Verify VN is the same as the one sent from the client`() {

    }

    @Test
    fun `Discard response if mode is not 4`() {

    }

    @Test
    fun `Discard response if transmit timestamp is 0`() {

    }

    @Test
    fun `Discard response if stratum is 0`() {

    }

    private fun toNtpTime(time: Long): Long {
        return time + NTP_EPOCH_DIFF_MILLIS
    }
}