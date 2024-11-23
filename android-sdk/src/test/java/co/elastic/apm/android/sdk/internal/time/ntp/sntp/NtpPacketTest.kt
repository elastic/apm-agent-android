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
        val originateTimestamp = 10_000_000_000
        val receiveTimestamp = 20_000_000_000
        val transmitTimestamp = 30_000_000_000
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
            00000000000000000000000000000010
            01010100000010111110010000000000
            
            Receive Timestamp
            00000000000000000000000000000100
            10101000000101111100100000000000
            
            Transmit Timestamp
            00000000000000000000000000000110
            11111100001000111010110000000000
        """.trimIndent()

        assertThat(packet.toByteArray()).isEqualTo(binaryStringToByteArray(expected))
    }

    @Test
    fun `Parse byte array`() {
        val expectedLeapIndicator = 1
        val expectedVersionNumber = 3
        val expectedMode = 4
        val expectedStratum = 10
        val expectedOriginateTimestamp = 30_000_000_000
        val expectedReceiveTimestamp = 10_000_000_000
        val expectedTransmitTimestamp = 20_000_000_000
        val input = """
            LI-VN-Mode-[padding]
            01-011-100-00001010 ${"0".repeat(16)}
           
            Root delay
            ${"0".repeat(32)}
            
            Root dispersion
            ${"0".repeat(32)}
            
            Reference identifier
            ${"0".repeat(32)}
            
            Reference timestamp
            ${"0".repeat(64)}
            
            Originate Timestamp
            00000000000000000000000000000110
            11111100001000111010110000000000
            
            Receive Timestamp
            00000000000000000000000000000010
            01010100000010111110010000000000
            
            Transmit Timestamp
            00000000000000000000000000000100
            10101000000101111100100000000000
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