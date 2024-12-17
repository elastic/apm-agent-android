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
package co.elastic.apm.android.sdk.testutils

import co.elastic.apm.android.sdk.internal.time.ntp.NtpPacket

object NtpUtils {
    private const val NTP_EPOCH_DIFF_MILLIS = 2208988800000L // According to RFC-868.

    internal fun createNtpPacket(
        clientCurrentTime: Long,
        expectedOffset: Long = 100L,
        receiveServerTime: Long = clientCurrentTime + expectedOffset,
        transmitServerTime: Long = receiveServerTime + 5,
        originateTimestamp: Long = clientCurrentTime,
        responseLeapIndicator: Int = 0,
        requestVersionNumber: Int = 4,
        responseVersionNumber: Int = requestVersionNumber,
        responseMode: Int = 4,
        responseStratum: Int = 1
    ): NtpPacket {
        return NtpPacket(
            responseLeapIndicator,
            responseVersionNumber,
            responseMode,
            responseStratum,
            toNtpTime(originateTimestamp),
            toNtpTime(receiveServerTime),
            if (transmitServerTime != 0L) toNtpTime(transmitServerTime) else 0
        )
    }

    fun toNtpTime(time: Long): Long {
        return time + NTP_EPOCH_DIFF_MILLIS
    }
}