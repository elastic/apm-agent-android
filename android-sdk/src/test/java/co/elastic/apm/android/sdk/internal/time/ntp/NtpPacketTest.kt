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

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class NtpPacketTest {

    @Test
    fun `To byte array`() {
        val leapIndicator = 3
        val versionNumber = 2
        val mode = 4
        val stratum = 14
        val originateTimestamp = 4_294_967_295_123L
        val receiveTimestamp = 3_000_000_000_200L
        val transmitTimestamp = 2_000_000_000_300L
        val packet = NtpPacket(
            leapIndicator,
            versionNumber,
            mode,
            stratum,
            originateTimestamp,
            receiveTimestamp,
            transmitTimestamp
        )
        val expected = """
            LI-VN-Mode-Stratum [padding]
            11-010-100-00001110 ${"0".repeat(16)}
            
            Root delay
            ${"0".repeat(32)}
            
            Root dispersion
            ${"0".repeat(32)}
            
            Reference identifier
            ${"0".repeat(32)}
            
            Reference timestamp
            ${"0".repeat(64)}
            
            Originate Timestamp
            11111111111111111111111111111111
            00011111011111001110110110010001
            
            Receive Timestamp
            10110010110100000101111000000000
            00110011001100110011001100110011
            
            Transmit Timestamp
            01110111001101011001010000000000
            01001100110011001100110011001101
        """.trimIndent()

        assertThat(packet.toByteArray()).isEqualTo(binaryStringToByteArray(expected))
    }

    @Test
    fun `Parse byte array`() {
        val expectedLeapIndicator = 2
        val expectedVersionNumber = 4
        val expectedMode = 4
        val expectedStratum = 10
        val expectedOriginateTimestamp = 2_000_000_000_300L
        val expectedReceiveTimestamp = 4_294_967_295_123L
        val expectedTransmitTimestamp = 3_000_000_000_200L
        val input = """
            LI-VN-Mode-[padding]
            10-100-100-00001010 ${"0".repeat(16)}
           
            Root delay
            ${"0".repeat(32)}
            
            Root dispersion
            ${"0".repeat(32)}
            
            Reference identifier
            ${"0".repeat(32)}
            
            Reference timestamp
            ${"0".repeat(64)}
            
            Originate Timestamp
            01110111001101011001010000000000
            01001100110011001100110011001101
            
            Receive Timestamp
            11111111111111111111111111111111
            00011111011111001110110110010001
            
            Transmit Timestamp
            10110010110100000101111000000000
            00110011001100110011001100110011
        """.trimIndent()

        assertThat(NtpPacket.parse(binaryStringToByteArray(input))).isEqualTo(
            NtpPacket(
                expectedLeapIndicator,
                expectedVersionNumber,
                expectedMode,
                expectedStratum,
                expectedOriginateTimestamp,
                expectedReceiveTimestamp,
                expectedTransmitTimestamp
            )
        )
    }

    @Test
    fun `Parse byte array error if size is less than the min expected (48)`() {
        assertThrows<IllegalArgumentException> {
            NtpPacket.parse("00".toByteArray())
        }
    }

    @Test
    fun `Validate params`() {
        val invalidLeapIndicator = 4
        val invalidVersionNumber = 8
        val invalidMode = 8
        val invalidStratum = 256
        assertThrows<IllegalArgumentException> {
            NtpPacket(invalidLeapIndicator, 1, 1, 1, 1, 1, 1)
        }
        assertThrows<IllegalArgumentException> {
            NtpPacket(1, invalidVersionNumber, 1, 1, 1, 1, 1)
        }
        assertThrows<IllegalArgumentException> {
            NtpPacket(1, 1, invalidMode, 1, 1, 1, 1)
        }
        assertThrows<IllegalArgumentException> {
            NtpPacket(1, 1, 1, invalidStratum, 1, 1, 1)
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun binaryStringToByteArray(binary: String): ByteArray {
        return binary.replace(Regex("[^01]+"), "")
            .chunked(8)
            .map { it.toUByte(2) }
            .toUByteArray()
            .toByteArray()
    }
}