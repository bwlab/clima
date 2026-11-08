package it.bwlab.gree.core

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

/**
 * Trasporto UDP reale verso il climatizzatore (porta 7000).
 */
class UdpTransportImpl(private val timeoutMs: Int = TIMEOUT_MS) :

    UdpTransport {

    private var socket: DatagramSocket? = null

    private fun getSocket(): DatagramSocket {
        socket ?: run {
            socket = DatagramSocket()
            socket!!.soTimeout = timeoutMs
            socket!!
        }
        return socket!!
    }

    override fun sendReceive(
        ip: String,
        port: Int,
        payload: ByteArray,
        timeoutMs: Int
    ): ByteArray? {
        val sock = getSocket()
        sock.soTimeout = timeoutMs
        val addr = InetAddress.getByName(ip)
        val send = DatagramPacket(payload, payload.size, addr, port)
        sock.send(send)

        val buf = ByteArray(65535)
        val receive = DatagramPacket(buf, buf.size)
        return try {
            sock.receive(receive)
            buf.copyOfRange(0, receive.length)
        } catch (e: SocketTimeoutException) {
            null
        }
    }

    override fun close() {
        socket?.close()
        socket = null
    }
}

/**
 * Interfaccia del trasporto UDP, sostituibile con mock nei test.
 */
interface UdpTransport {
    fun sendReceive(
        ip: String,
        port: Int,
        payload: ByteArray,
        timeoutMs: Int
    ): ByteArray?
    fun close()
}
