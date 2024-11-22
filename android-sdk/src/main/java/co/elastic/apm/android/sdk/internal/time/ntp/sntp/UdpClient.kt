package co.elastic.apm.android.sdk.internal.time.ntp.sntp

import java.io.Closeable
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class UdpClient(
    private val address: InetAddress,
    private val port: Int,
    private val bufferSize: Int
) : Closeable {
    private lateinit var socket: DatagramSocket

    fun open() {
        socket = DatagramSocket()
    }

    fun sendPacket(bytes: ByteArray): ByteArray {
        val packet = DatagramPacket(bytes, bytes.size, address, port)
        socket.send(packet)

        val responsePacket = DatagramPacket(ByteArray(bufferSize), bufferSize)
        socket.receive(responsePacket)
        return responsePacket.data.copyOf(responsePacket.length)
    }

    override fun close() {
        socket.close()
    }
}