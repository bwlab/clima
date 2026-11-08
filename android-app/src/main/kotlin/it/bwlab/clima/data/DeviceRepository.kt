package it.bwlab.clima.data

import android.content.Context
import it.bwlab.gree.core.DeviceInfo
import it.bwlab.gree.core.Gree
import it.bwlab.gree.core.GreeClient
import it.bwlab.clima.GreeClientHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Repository: unico punto di contatto tra UI e protocollo.
 * La logica di protocollo in kotlin-core NON è toccata: qui si orchestrano
 * scan, bind (automatico, invisibile all'utente), status e comandi.
 */
class DeviceRepository(context: Context) {

    private val appContext = context.applicationContext
    private val preferences = ClimaPreferences(appContext)

    private val client: GreeClient by lazy {
        GreeClientHolder.init(File(appContext.filesDir, "gree_keys.json"))
        GreeClientHolder.get()
    }

    private val _saved = MutableStateFlow<List<SavedDevice>>(emptyList())
    val saved: StateFlow<List<SavedDevice>> = _saved.asStateFlow()

    private val _found = MutableStateFlow<List<DeviceInfo>>(emptyList())
    val found: StateFlow<List<DeviceInfo>> = _found.asStateFlow()

    /** MAC di cui l'unità non ha risposto all'ultimo ping/status. */
    private val _unreachable = MutableStateFlow<Set<String>>(emptySet())
    val unreachable: StateFlow<Set<String>> = _unreachable.asStateFlow()

    val prefs get() = preferences

    init {
        refreshSaved()
    }

    /** Ricarica i dispositivi salvati dal DataStore. */
    fun refreshSaved() {
        _saved.value = preferences.readSavedDevicesBlocking()
    }

    // ---- Ricerca in rete ----

    /**
     * Scan broadcast; non passa via VPN (i router la bloccano): per le unità
     * oltre VPN si usa l'IP fisso salvato. Ritorna le unità non ancora salvate.
     */
    suspend fun scan(): List<DeviceInfo> = withContext(Dispatchers.IO) {
        val all = client.discover()
        val savedMacs = _saved.value.map { it.mac }.toSet()
        _found.value = all.filter { it.mac !in savedMacs }
        // aggiorna lo stato raggiungibilità delle unità salvate
        val reachableNow = all.map { it.mac }.toSet()
        _unreachable.value = _saved.value.map { it.mac }.filter { it !in reachableNow }.toSet()
        _found.value
    }

    // ---- Salvataggio / rimozione / rinomina ----

    /** Aggiunge un dispositivo trovato (bind automatico + chiave salvata). */
    suspend fun add(found: DeviceInfo, name: String?): SavedDevice {
        val bound = withContext(Dispatchers.IO) { client.bind(found.ip) }
        val device = SavedDevice(
            id = UUID.randomUUID().toString(),
            name = name?.takeIf { it.isNotBlank() } ?: found.name.takeIf { it.isNotBlank() }
                ?: "Gree " + bound.mac.takeLast(8).lowercase(),
            ip = bound.ip,
            mac = bound.mac,
            firmware = bound.firmware,
            usesGcm = bound.usesGcm
        )
        _found.value = _found.value.filter { it.mac != device.mac }
        persistSaved(_saved.value + device)
        return device
    }

    /** Aggiunge manualmente da IP: fa il bind (che fa già lo scan diretto verso l'IP). */
    suspend fun addByIp(ip: String, name: String?): SavedDevice {
        val bound = withContext(Dispatchers.IO) { client.bind(ip) }
        val device = SavedDevice(
            id = UUID.randomUUID().toString(),
            name = name?.takeIf { it.isNotBlank() }
                ?: bound.name.takeIf { it.isNotBlank() } ?: "Gree " + bound.mac.takeLast(8).lowercase(),
            ip = bound.ip,
            mac = bound.mac,
            firmware = bound.firmware,
            usesGcm = bound.usesGcm
        )
        persistSaved(_saved.value + device)
        return device
    }

    fun rename(device: SavedDevice, newName: String) {
        persistSaved(
            _saved.value.map {
                if (it.id == device.id) it.copy(name = newName.trim()) else it
            }
        )
    }

    fun delete(device: SavedDevice) {
        persistSaved(_saved.value.filter { it.id != device.id })
    }

    private fun persistSaved(list: List<SavedDevice>) {
        _saved.value = list
        // Rimuove eventuali timer riferiti a dispositivi non più presenti
        val timers = preferences.readTimersBlocking().filter { t ->
            list.any { it.id == t.deviceId }
        }
        try {
            kotlinx.coroutines.runBlocking {
                preferences.setTimers(timers)
                preferences.setSavedDevices(list)
            }
        } catch (e: Throwable) {
            // il salvataggio non deve far crashare la UI
        }
    }

    // ---- Status e comandi ----

    /** Legge lo stato: il bind avviene in automatico se la chiave non è nota. */
    suspend fun status(device: SavedDevice): it.bwlab.gree.core.ClimateState =
        withContext(Dispatchers.IO) {
            client.status(it.bwlab.gree.core.DeviceInfo(device.ip, device.mac, device.name, device.firmware, device.usesGcm))
        }

    /** Ping rapido: usa lo status completo (bind in automatico). Ritorna null se irraggiungibile. */
    suspend fun ping(device: SavedDevice): it.bwlab.gree.core.ClimateState? = try {
        status(device).also {
            _unreachable.value = _unreachable.value - device.mac
        }
    } catch (e: Throwable) {
        _unreachable.value = _unreachable.value + device.mac
        null
    }

    /** Invia un comando con lista opzioni/valori grezzi del protocollo. */
    suspend fun command(device: SavedDevice, opts: List<String>, values: List<Any>): Boolean =
        withContext(Dispatchers.IO) {
            try {
                client.command(
                    DeviceInfo(device.ip, device.mac, device.name, device.firmware, device.usesGcm),
                    opts,
                    values
                )
            } catch (e: Throwable) {
                false
            }
        }

    companion object {

        /** Rileva se è attiva una VPN (chip di rete nella schermata Controllo). */
        fun isVpnActive(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
                ?: return false
            val networks = cm.allNetworks
            for (n in networks) {
                val caps = cm.getNetworkCapabilities(n) ?: continue
                if (caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_VPN)) return true
            }
            return false
        }

        /** Rete disponibile (qualsiasi trasporto)? */
        fun isNetworkAvailable(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
                ?: return false
            val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
            return caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    }
}
