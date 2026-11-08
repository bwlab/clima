package it.bwlab.clima

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.*
import it.bwlab.gree.core.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import java.io.File

class ClimateViewModel(application: Application) : AndroidViewModel(application) {

    private val client: GreeClient by lazy {
        val keysFile = File(application.applicationContext.filesDir, "gree_keys.json")
        GreeClientHolder.init(keysFile)
        GreeClientHolder.get()
    }

    /** Dispositivo associato: riusato per status/comandi senza rifare scan+bind. */
    private var lastDevice: DeviceInfo? = null

    var state by mutableStateOf<ClimateState?>(null)

    private val _message = MutableStateFlow<String>("")
    val message = _message.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private fun onError(fallback: String, e: Throwable) {
        // Le CancellationException non vanno scambiate per errori.
        if (e is CancellationException) throw e
        _message.value = "$fallback: ${e.message ?: e.javaClass.simpleName}"
    }

    fun discover() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val found = withContext(Dispatchers.IO) { client.discover() }
                if (found.isEmpty()) {
                    _message.value = "Nessun clima trovato"
                } else {
                    val ips = found.joinToString(", ") { it.ip }
                    _message.value = "Trovati ${found.size}: $ips"
                }
            } catch (e: Throwable) {
                onError("Scan fallito", e)
            }
            _loading.value = false
        }
    }

    fun bind(ip: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val device = withContext(Dispatchers.IO) { client.bind(ip) }
                lastDevice = device
                val st = withContext(Dispatchers.IO) { client.status(device) }
                state = st
                _message.value = "Bind OK: ${device.mac} (${device.firmware})"
            } catch (e: Throwable) {
                onError("Bind fallito", e)
            }
            _loading.value = false
        }
    }

    fun loadStatus(ip: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val st = withContext(Dispatchers.IO) {
                    val dev = lastDevice ?: client.bind(ip).also { lastDevice = it }
                    client.status(dev)
                }
                state = st
                _message.value = "Stato letto"
            } catch (e: Throwable) {
                onError("Errore lettura", e)
            }
            _loading.value = false
        }
    }

    fun power(ip: String, on: Boolean) {
        sendCommand { dev ->
            if (dev != null) command(dev, listOf("Pow"), listOf(if (on) 1 else 0))
            else power(ip, on)
        }
    }

    fun temperature(ip: String, celsius: Int) {
        sendCommand { dev ->
            if (dev != null) command(dev, listOf("TemUn", "SetTem"), listOf(0, celsius))
            else temperature(ip, celsius)
        }
    }

    fun mode(ip: String, mode: Mode) {
        sendCommand { dev ->
            if (dev != null) command(dev, listOf("Mod"), listOf(mode.code))
            else mode(ip, mode)
        }
    }

    fun fan(ip: String, fan: FanSpeed) {
        sendCommand { dev ->
            if (dev != null) command(dev, listOf("WdSpd"), listOf(fan.code))
            else fan(ip, fan)
        }
    }

    /**
     * I comandi usano il dispositivo già associato se disponibile (veloce);
     * solo in assenza di bind precedente rifanno scan+bind verso l'IP indicato.
     */
    private fun sendCommand(action: GreeClient.(DeviceInfo?) -> Boolean) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val ok = withContext(Dispatchers.IO) { client.action(lastDevice) }
                _message.value = if (ok) "Comando inviato" else "Nessuna risposta"
            } catch (e: Throwable) {
                onError("Errore comando", e)
            }
            _loading.value = false
        }
    }
}
