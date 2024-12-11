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

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.Duration

class UdpClientImpl internal constructor(
    internal val configuration: UdpClient.Configuration
) : UdpClient {
    private val socket = DatagramSocket()
    private var address: InetAddress? = null

    @Throws(UnknownHostException::class, SocketTimeoutException::class, SocketException::class)
    override fun send(bytes: ByteArray, timeout: Duration): ByteArray =
        synchronized(this) {
            if (address == null) {
                address = InetAddress.getByName(configuration.host)
            }

            socket.soTimeout = timeout.toMillis().toInt()

            val packet = DatagramPacket(bytes, bytes.size, address, configuration.port)
            socket.send(packet)

            val responsePacket = DatagramPacket(
                ByteArray(configuration.responseBufferSize),
                configuration.responseBufferSize
            )
            socket.receive(responsePacket)
            return responsePacket.data.copyOf(responsePacket.length)
        }

    override fun close() {
        socket.close()
    }
}