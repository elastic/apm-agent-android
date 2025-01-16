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
import java.io.Closeable

/**
 * According to RFC-4330.
 */
interface SntpClient : Closeable {

    fun fetchTimeOffset(currentTimeMillis: Long): Response

    companion object {
        private val noop by lazy {
            object : SntpClient {
                override fun fetchTimeOffset(currentTimeMillis: Long): Response {
                    return Response.Error(ErrorType.TRY_LATER)
                }

                override fun close() {}
            }
        }

        fun create(): SntpClient {
            if (System.getProperty("elastic.test")?.equals("true") == true) {
                return noop
            }
            return SntpClientImpl(UdpClient("time.android.com", 123, 48), SystemTimeProvider.get())
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

