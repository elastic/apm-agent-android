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
package co.elastic.otel.android.testutils

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketException

class TestUdpServer : Thread() {
    private val buf = ByteArray(256)
    val socket = DatagramSocket()

    @Volatile
    var responseHandler: (DatagramPacket) -> Unit = { clientPacket ->
        val response = "Server response".toByteArray()
        val packet =
            DatagramPacket(response, response.size, clientPacket.address, clientPacket.port)
        socket.send(packet)
    }

    override fun run() {
        while (true) {
            if (socket.isClosed) {
                continue
            }
            try {
                val packet = DatagramPacket(buf, buf.size)
                socket.receive(packet)

                responseHandler(packet)
            } catch (e: SocketException) {
                continue
            }
        }
    }

    fun close() {
        socket.close()
    }

    fun getPort(): Int {
        return socket.localPort
    }
}
