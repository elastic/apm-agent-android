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

import java.io.Closeable
import java.time.Duration

interface UdpClient : Closeable {
    fun send(bytes: ByteArray, timeout: Duration = Duration.ofSeconds(5)): ByteArray

    data class Configuration(val host: String, val port: Int, val responseBufferSize: Int)

    private object Noop : UdpClient {
        override fun send(bytes: ByteArray, timeout: Duration): ByteArray {
            return ByteArray(0)
        }

        override fun close() {

        }
    }

    companion object {
        fun create(configuration: Configuration): UdpClient {
            return when {
                configuration.host == "localhost" -> UdpClientImpl(configuration)
                System.getProperty("elastic.test") != null -> Noop
                else -> UdpClientImpl(configuration)
            }
        }
    }
}