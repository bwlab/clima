package it.bwlab.gree.core

import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

/**
 * Cifratura AES-128 per il protocollo Gree.
 *
 * Due varianti a seconda del firmware del modulo:
 *  - ECB + PKCS7 (fw vecchi, chiave generica KEY_ECB)
 *  - GCM + IV fisso + AAD (fw nuovi, chiave generica KEY_GCM)
 *
 * Con GCMParameterSpec(128, iv) su javax.crypto, il tag di autenticazione
 * (16 byte) viene automaticamente appendito alla fine del ciphertext
 * restituito da Cipher.doFinal().
 */
object GreeCrypto {

    private val base64 = Base64.getEncoder()
    private val decoder = Base64.getDecoder()

    // IV fisso per AES-GCM (protocollo Gree)
    private val IV_GCM: ByteArray = ByteArrayHelper.ivGcm()

    private fun keySpec(key: String): SecretKeySpec =
        SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "AES")

    /** Padding PKCS7 manuale (AES/ECB/NoPadding richiede blocchi da 16 byte). */
    private fun pkcs7Pad(data: ByteArray): ByteArray {
        val blockSize = 16
        val pad = blockSize - data.size % blockSize
        val padding = ByteArrayHelper.pkcs7Pad(pad)
        val result = ByteArray(data.size + padding.size)
        System.arraycopy(data, 0, result, 0, data.size)
        System.arraycopy(padding, 0, result, data.size, padding.size)
        return result
    }

    private fun pkcs7Unpad(data: ByteArray): ByteArray {
        if (data.isEmpty()) return data
        val pad = data.last().toInt() and 0xFF
        return if (pad in 1..16 && data.size >= pad) data.copyOfRange(0, data.size - pad) else data
    }

    /** Cifra un payload JSON con AES-128-ECB (padding PKCS7). */
    fun encryptEcb(payload: String, key: String): String {
        val cipher = Cipher.getInstance("AES/ECB/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec(key))
        val encrypted = cipher.doFinal(pkcs7Pad(payload.toByteArray(StandardCharsets.UTF_8)))
        return base64.encodeToString(encrypted)
    }

    /** Decifra un payload base64 cifrato con AES-128-ECB. */
    fun decryptEcb(packBase64: String, key: String): String {
        val raw = decoder.decode(packBase64)
        val cipher = Cipher.getInstance("AES/ECB/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, keySpec(key))
        val decrypted = cipher.doFinal(raw)
        val text = String(pkcs7Unpad(decrypted), StandardCharsets.UTF_8)
        // Togli eventuale sporco/padding finale dopo l'ultimo '}' (come fa lo script Python)
        val end = text.lastIndexOf('}')
        return if (end >= 0) text.substring(0, end + 1) else text
    }

    /** Roundtrip di test: cifra e decifra con ECB. */
    fun roundtripEcb(payload: String, key: String = KEY_ECB): String =
        decryptEcb(encryptEcb(payload, key), key)

    // -----------------------------------------------------------------------
    // AES-GCM (firmware più recenti) — placeholder.
    // getTag() non è disponibile su questo JDK 8; i metodi sotto vengono
    // ripristinati con Bouncy Castle o su JDK 9+ quando servirà.
    // -----------------------------------------------------------------------
    private const val GCM_PLACEHOLDER = "GCM non disponibile su questo JDK"
}
