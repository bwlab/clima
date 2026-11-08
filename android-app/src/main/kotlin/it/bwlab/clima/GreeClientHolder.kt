package it.bwlab.clima

import it.bwlab.gree.core.*
import java.io.File

/**
 * Holder del [GreeClient] con chiavi salvate su file.
 * Viene inizializzato una volta all'avvio dell'app.
 */
object GreeClientHolder {

    private var client: GreeClient? = null

    fun init(keysFile: File): GreeClient {
        if (client == null) {
            val transport = UdpTransportImpl()
            val store = JsonFileDeviceKeyStore(keysFile)
            client = GreeClient(transport, store)
        }
        return client!!
    }

    fun get(): GreeClient = client ?: error("GreeClientHolder non inizializzato. Chiama init() prima.")
}
