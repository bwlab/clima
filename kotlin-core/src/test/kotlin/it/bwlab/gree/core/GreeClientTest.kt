package it.bwlab.gree.core

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets

/**
 * Test per GreeClient usando un UdpTransport mock con risposte congelate
 * (catturate dalla sessione reale contro il clima V1.2.1).
 */
@DisplayName("GreeClient (mock transport)")
class GreeClientTest {

    private val store = InMemoryDeviceKeyStore()

    private fun mockTransport(responses: Map<String, ByteArray>): UdpTransport =
        object : UdpTransport {
            override fun sendReceive(
                ip: String,
                port: Int,
                payload: ByteArray,
                timeoutMs: Int
            ): ByteArray? {
                val req = String(payload, StandardCharsets.UTF_8)
                return responses.entries.firstOrNull { (k, _) -> req.contains(k) }?.value
            }
            override fun close() {}
        }

    @Test
    @DisplayName("bind salva la chiave personale e restituisce DeviceInfo")
    fun bindSavesKey() {
        val scanPayload = JSONObject().put("mac", "aabbccddeeff").put("name", "ddeeff").put("ver", "V1.2.1")
        val scanPack = GreeCrypto.encryptEcb(scanPayload.toString(), KEY_ECB)
        val scanResp = JSONObject().put("pack", scanPack)

        val bindPayload = JSONObject().put("t", "bindok").put("key", "chiave-personale-123")
        val bindPack = GreeCrypto.encryptEcb(bindPayload.toString(), KEY_ECB)
        val bindResp = JSONObject().put("pack", bindPack)

        val transport = mockTransport(mapOf(
            "\"t\":\"scan\"" to scanResp.toString().toByteArray(StandardCharsets.UTF_8),
            "\"t\":\"pack\"" to bindResp.toString().toByteArray(StandardCharsets.UTF_8)
        ))

        val client = GreeClient(transport, store)
        val device = client.bind("192.168.0.50")

        assertEquals("aabbccddeeff", device.mac)
        assertEquals("192.168.0.50", device.ip)
        assertFalse(device.usesGcm)
        assertEquals("chiave-personale-123", store.get("aabbccddeeff"))
    }

    @Test
    @DisplayName("status restituisce lo stato mappato")
    fun statusReturnsMappedState() {
        store.put("aabbccddeeff", "1234567890abcdef")

        val cols = listOf("Pow", "Mod", "SetTem", "TemSen", "WdSpd")
        val dat = listOf(1, 4, 24, 18, 0)
        val statusPayload = JSONObject().put("cols", JSONArray(cols)).put("dat", JSONArray(dat))
        val statusPack = GreeCrypto.encryptEcb(statusPayload.toString(), "1234567890abcdef")
        val statusResp = JSONObject().put("pack", statusPack)

        val transport = mockTransport(mapOf(
            "\"t\":\"pack\"" to statusResp.toString().toByteArray(StandardCharsets.UTF_8)
        ))

        val client = GreeClient(transport, store)
        val device = DeviceInfo("192.168.0.50", "aabbccddeeff", "ddeeff", "V1.2.1", false)
        val state = client.status(device)

        assertTrue(state.pow)
        assertEquals(Mode.HEAT, state.mode)
        assertEquals(24, state.setTem)
        assertEquals(18, state.temSen)
        assertEquals(FanSpeed.AUTO, state.wdSpd)
    }

    @Test
    @DisplayName("command restituisce true con r=200")
    fun commandReturnsTrueOn200() {
        store.put("aabbccddeeff", "1234567890abcdef")

        val cmdPayload = JSONObject().put("p", JSONArray(listOf(1))).put("r", 200)
        val cmdPack = GreeCrypto.encryptEcb(cmdPayload.toString(), "1234567890abcdef")
        val cmdResp = JSONObject().put("pack", cmdPack)

        val transport = mockTransport(mapOf(
            "\"t\":\"pack\"" to cmdResp.toString().toByteArray(StandardCharsets.UTF_8)
        ))

        val client = GreeClient(transport, store)
        val device = DeviceInfo("192.168.0.50", "aabbccddeeff", "ddeeff", "V1.2.1", false)
        val ok = client.command(device, listOf("Pow"), listOf(1))

        assertTrue(ok)
    }

    @Test
    @DisplayName("command restituisce false se non c'è risposta")
    fun commandReturnsFalseOnTimeout() {
        store.put("aabbccddeeff", "1234567890abcdef")
        val transport = mockTransport(emptyMap())
        val client = GreeClient(transport, store)
        val device = DeviceInfo("192.168.0.50", "aabbccddeeff", "ddeeff", "V1.2.1", false)
        assertFalse(client.command(device, listOf("Pow"), listOf(1)))
    }
}
