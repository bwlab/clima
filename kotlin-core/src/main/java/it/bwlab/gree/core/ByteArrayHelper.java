package it.bwlab.gree.core;

/**
 * Helper Java per creare array di byte nativi (byte[]), bypassando
 * anomalie del compilatore Kotlin 1.5.31 con byteArrayOf/ByteArray(size){}.
 */
public class ByteArrayHelper {

    /** IV fisso per AES-GCM (protocollo Gree). */
    public static byte[] ivGcm() {
        return new byte[] {
            0x54, 0x40, 0x78, 0x44, 0x49, 0x67,
            0x5A, 0x51, 0x6C, 0x5E, 0x63, 0x13
        };
    }

    /** Padding PKCS7: array di `pad` byte tutti pari a `pad`. */
    public static byte[] pkcs7Pad(int pad) {
        byte[] b = new byte[pad];
        for (int i = 0; i < pad; i++) b[i] = (byte) pad;
        return b;
    }
}
