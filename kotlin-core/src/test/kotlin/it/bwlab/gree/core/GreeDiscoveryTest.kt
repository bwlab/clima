package it.bwlab.gree.core

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.json.JSONObject
import java.nio.charset.StandardCharsets

@DisplayName("GreeDiscovery")
class GreeDiscoveryTest {

    @Test
    @DisplayName("discover restituisce lista vuota se nessun risponde")
    fun discoverEmpty() {
        val transport = object : UdpTransport {
            override fun sendReceive(ip: String, port: Int, payload: ByteArray, timeoutMs: Int): ByteArray? = null
            override fun close() {}
        }
        val store = InMemoryDeviceKeyStore()
        val client = GreeClient(transport, store)
        val found = client.discover()
        assertTrue(found.isEmpty())
    }

    @Test
    @DisplayName("discover decodifica risposte ECB valide")
    fun discoverParsesEcb() {
        val scanPayload = JSONObject().put("mac", "aaaa1111").put("name", "test").put("ver", "V1.0")
        val scanPack = GreeCrypto.encryptEcb(scanPayload.toString(), KEY_ECB)
        val resp = JSONObject().put("pack", scanPack)

        val transport = object : UdpTransport {
            override fun sendReceive(ip: String, port: Int, payload: ByteArray, timeoutMs: Int): ByteArray? {
                val req = String(payload, StandardCharsets.UTF_8)
                return if (req.contains("\"t\":\"scan\"")) {
                    resp.toString().toByteArray(StandardCharsets.UTF_8)
                } else null
            }
            override fun close() {}
        }

        val store = InMemoryDeviceKeyStore()
        val client = GreeClient(transport, store)
        val found = client.discover()

        assertEquals(1, found.size)
        assertEquals("aaaa1111", found[0].mac)
        assertFalse(found[0].usesGcm)
    }
}
