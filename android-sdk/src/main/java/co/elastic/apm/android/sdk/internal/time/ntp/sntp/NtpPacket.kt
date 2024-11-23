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

data class NtpPacket(
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
        buffer.putLong(24, originateTimestamp)
        buffer.putLong(32, receiveTimestamp)
        buffer.putLong(40, transmitTimestamp)

        return buffer.array()
    }

    companion object {
        private const val PACKET_SIZE_IN_BYTES = 48

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
            val buffer = ByteBuffer.wrap(bytes)
            val firstByte = buffer.get().toInt()
            val leapIndicator = firstByte shr 6
            val versionNumber = (firstByte shr 3) and 3
            val mode = firstByte and 7
            val stratum = buffer.get().toInt()
            val originateTimestamp = buffer.getLong(24)
            val receiveTimestamp = buffer.getLong(32)
            val transmitTimestamp = buffer.getLong(40)

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
    }
}