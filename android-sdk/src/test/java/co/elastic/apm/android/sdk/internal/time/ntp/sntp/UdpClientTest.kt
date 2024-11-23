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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.Duration


class UdpClientTest {
    private lateinit var client: UdpClient
    private lateinit var server: FlexiServer

    companion object {
        private const val SERVER_HOST = "localhost"
        private const val SERVER_PORT = 4447
    }

    @BeforeEach
    fun setUp() {
        server = FlexiServer(SERVER_PORT)
        server.start()
        client = UdpClient.create(SERVER_HOST, SERVER_PORT, 256)
    }

    @AfterEach
    fun tearDown() {
        client.close()
        server.close()
    }

    @Test
    fun `Happy path`() {
        val message = "Hello World!"
        val packet = message.toByteArray()

        val response = client.send(packet)

        assertThat(String(response)).isEqualTo("Server response")
    }

    @Test
    fun `Server takes too long to respond`() {
        server.responseHandler = {
            Thread.sleep(Duration.ofSeconds(5).toMillis())
        }

        assertThrows<SocketTimeoutException> {
            client.send(
                "Example".toByteArray(),
                Duration.ofSeconds(1)
            )
        }
    }

    @Test
    fun `Server port is not reachable`() {
        client = UdpClient.create(SERVER_HOST, SERVER_PORT + 1, 256)

        assertThrows<SocketTimeoutException> {
            client.send("Example".toByteArray(), Duration.ofSeconds(1))
        }
    }

    @Test
    fun `Server is not active`() {
        server.close()

        assertThrows<SocketTimeoutException> {
            client.send("Example".toByteArray(), Duration.ofSeconds(1))
        }
    }

    @Test
    fun `Server host not found`() {
        assertThrows<UnknownHostException> {
            client = UdpClient.create("nonexistent", SERVER_PORT, 256)
        }
    }

    private class FlexiServer(port: Int) : Thread() {
        private val buf = ByteArray(256)
        val socket = DatagramSocket(port)

        @Volatile
        var responseHandler: (ClientInfo) -> Unit = { client ->
            val response = "Server response".toByteArray()
            val packet = DatagramPacket(response, response.size, client.address, client.port)
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

                    val address = packet.address
                    val port = packet.port
                    responseHandler(ClientInfo(address, port))
                } catch (e: SocketException) {
                    continue
                }
            }
        }

        fun close() {
            socket.close()
        }

        data class ClientInfo(val address: InetAddress, val port: Int)
    }
}