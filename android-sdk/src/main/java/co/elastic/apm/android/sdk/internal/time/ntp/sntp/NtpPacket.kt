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

import java.nio.ByteBuffer
import kotlin.math.roundToLong

internal data class NtpPacket(
    val leapIndicator: Int,
    val versionNumber: Int,
    val mode: Int,
    val stratum: Int,
    val originateTimestamp: Long,
    val receiveTimestamp: Long,
    val transmitTimestamp: Long
) {

    init {
        if (leapIndicator > 3) {
            throw IllegalArgumentException()
        }
        if (versionNumber > 7) {
            throw IllegalArgumentException()
        }
        if (mode > 7) {
            throw IllegalArgumentException()
        }
        if (stratum > 255) {
            throw IllegalArgumentException()
        }
    }

    fun toByteArray(): ByteArray {
        val buffer = ByteBuffer.allocate(PACKET_SIZE_IN_BYTES)
        val li = leapIndicator shl 6
        val version = versionNumber shl 3
        val firstByte = li or version or mode
        buffer.put(firstByte.toByte())
        buffer.put(stratum.toByte())

        buffer.putInt(24, (originateTimestamp / MILLIS_FACTOR).toInt())
        buffer.putInt(28, millisToFraction(originateTimestamp % MILLIS_FACTOR).toInt())
        buffer.putInt(32, (receiveTimestamp / MILLIS_FACTOR).toInt())
        buffer.putInt(36, millisToFraction(receiveTimestamp % MILLIS_FACTOR).toInt())
        buffer.putInt(40, (transmitTimestamp / MILLIS_FACTOR).toInt())
        buffer.putInt(44, millisToFraction(transmitTimestamp % MILLIS_FACTOR).toInt())

        return buffer.array()
    }

    companion object {
        private const val PACKET_SIZE_IN_BYTES = 48
        private const val MILLIS_FACTOR = 1000L
        private const val FRACTIONAL = (1L shl 32).toDouble()

        fun createForClient(
            transmitTimestamp: Long,
            versionNumber: Int = 4
        ): NtpPacket {
            return NtpPacket(0, versionNumber, 3, 0, 0, 0, transmitTimestamp)
        }

        fun parse(bytes: ByteArray): NtpPacket {
            if (bytes.size < PACKET_SIZE_IN_BYTES) {
                throw IllegalArgumentException("The min byte array size allowed is $PACKET_SIZE_IN_BYTES, the provided array size is ${bytes.size}.")
            }
            val longBuffer = ByteBuffer.allocate(Long.SIZE_BYTES)
            val firstByte = bytes.first().toInt()
            val leapIndicator = firstByte shr 6
            val versionNumber = (firstByte shr 3) and 7
            val mode = firstByte and 7
            val stratum = bytes[1].toInt()

            val originateTimestamp = getTimestamp(
                longBuffer,
                bytes.sliceArray(24 until 28),
                bytes.sliceArray(28 until 32)
            )
            val receiveTimestamp = getTimestamp(
                longBuffer,
                bytes.sliceArray(32 until 36),
                bytes.sliceArray(36 until 40)
            )
            val transmitTimestamp = getTimestamp(
                longBuffer,
                bytes.sliceArray(40 until 44),
                bytes.sliceArray(44 until 48)
            )

            return NtpPacket(
                leapIndicator,
                versionNumber,
                mode,
                stratum,
                originateTimestamp,
                receiveTimestamp,
                transmitTimestamp
            )
        }

        private fun millisToFraction(millis: Long): Long {
            return ((FRACTIONAL * millis) / MILLIS_FACTOR).roundToLong()
        }

        private fun fractionToMillis(fraction: Long): Long {
            return ((fraction * MILLIS_FACTOR) / FRACTIONAL).roundToLong()
        }

        private fun getTimestamp(
            longBuffer: ByteBuffer,
            secondsBytes: ByteArray,
            fractionBytes: ByteArray
        ): Long {
            val seconds = bytesToLong(longBuffer, secondsBytes)
            val fraction = bytesToLong(longBuffer, fractionBytes)
            return (seconds * MILLIS_FACTOR) + fractionToMillis(fraction)
        }

        private fun bytesToLong(longBuffer: ByteBuffer, bytes: ByteArray): Long {
            longBuffer.position(4)
            longBuffer.put(bytes)
            longBuffer.clear()
            return longBuffer.getLong()
        }
    }
}