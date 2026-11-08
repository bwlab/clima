package it.bwlab.gree.core

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.json.JSONObject

@DisplayName("GreeCrypto")
class GreeCryptoTest {

    @Test
    @DisplayName("ECB roundtrip con chiave generica")
    fun ecbRoundtrip() {
        val payload = """{"mac":"aabbccddeeff","t":"bind","uid":0}"""
        val decrypted = GreeCrypto.roundtripEcb(payload, KEY_ECB)
        assertEquals(payload, decrypted)
    }

    @Test
    @DisplayName("ECB decrypt tronca sporco dopo l'ultimo }")
    fun ecbTruncatesTrailingGarbage() {
        val payload = """{"t":"bindok","key":"abc"}"""
        val encrypted = GreeCrypto.encryptEcb(payload, KEY_ECB)
        val decrypted = GreeCrypto.decryptEcb(encrypted, KEY_ECB)
        assertEquals(payload, decrypted)
    }

    @Test
    @DisplayName("JSONObject roundtrip costruisce JSON valido")
    fun jsonObjectRoundtrip() {
        val obj = JSONObject().put("t", "scan")
        assertEquals("""{"t":"scan"}""", obj.toString())
    }
}
