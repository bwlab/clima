package it.bwlab.gree.core

/**
 * Facade per creare un [GreeClient] pronto all'uso con trasporto UDP reale
 * e salvataggio chiavi su file JSON.
 */
object Gree {

    fun clientFor(ip: String, keysFile: java.io.File): GreeClient {
        val transport = UdpTransportImpl()
        val store = JsonFileDeviceKeyStore(keysFile)
        return GreeClient(transport, store)
    }
}
