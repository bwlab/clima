package it.bwlab.clima

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import it.bwlab.clima.data.AppTimer
import it.bwlab.clima.data.DeviceRepository
import it.bwlab.clima.data.SavedDevice
import it.bwlab.clima.data.ThemePreferences
import it.bwlab.clima.data.TimerScheduler
import it.bwlab.clima.ui.theme.ThemeMode
import it.bwlab.gree.core.DeviceInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel condiviso: gestisce dispositivi salvati, ricerca in rete,
 * preferenze (tema/unità/aggiornamento) e sincronizza il tema per la UI.
 */
class AppViewModel(application: Application) : AndroidViewModel(application) {

    val repository = DeviceRepository(application)
    val initialDeviceId: String? = repository.saved.value.firstOrNull()?.id

    val savedDevices get() = repository.saved
    val foundDevices get() = repository.found
    val unreachable get() = repository.unreachable
    val prefs get() = repository.prefs

    /** Message di cortesia per la schermata Climatizzatori (chiave di risorsa). */
    val scanMessage = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    private val _scanning = kotlinx.coroutines.flow.MutableStateFlow(false)
    val scanning = _scanning.asStateFlow()
    private val _deviceStates = kotlinx.coroutines.flow.MutableStateFlow<Map<String, it.bwlab.gree.core.ClimateState>>(emptyMap())
    val deviceStates = _deviceStates.asStateFlow()

    init {
        // Ponte DataStore -> tema applicato
        viewModelScope.launch {
            prefs.theme.collect { ThemePreferences.currentTheme.value = it }
        }
        refreshStatuses()
    }

    fun scan() {
        if (_scanning.value) return
        scanMessage.value = null
        _scanning.value = true
        viewModelScope.launch {
            try {
                val found = repository.scan()
                scanMessage.value = if (found.isEmpty()) "scan_empty" else null
                refreshStatuses()
            } catch (e: Throwable) {
                scanMessage.value = "scan_failed"
            } finally {
                _scanning.value = false
            }
        }
    }

    /** Aggiorna automaticamente stato e raggiungibilità delle unità salvate. */
    fun refreshStatuses() {
        viewModelScope.launch {
            val updated = mutableMapOf<String, it.bwlab.gree.core.ClimateState>()
            savedDevices.value.forEach { device ->
                repository.ping(device)?.let { updated[device.mac] = it }
            }
            _deviceStates.value = updated
        }
    }

    fun addDevice(info: DeviceInfo, name: String? = null, onDone: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val saved = repository.add(info, name)
                onDone?.invoke(saved.id)
            } catch (e: Throwable) {
                scanMessage.value = "bind_failed"
            }
        }
    }

    fun addDeviceByIp(ip: String, name: String? = null, onDone: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val saved = repository.addByIp(ip, name)
                scanMessage.value = null
                onDone?.invoke(saved.id)
            } catch (e: Throwable) {
                scanMessage.value = "bind_failed"
            }
        }
    }

    fun rename(device: SavedDevice, newName: String) = repository.rename(device, newName)

    fun delete(device: SavedDevice) = repository.delete(device)

    val timers get() = prefs.timers

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { prefs.setTheme(mode) }
    }

    fun setUnit(fahrenheit: Boolean) {
        viewModelScope.launch { prefs.setUnit(fahrenheit) }
    }

    fun setAutoRefresh(enabled: Boolean) {
        viewModelScope.launch { prefs.setAutoRefresh(enabled) }
    }

    fun saveTimer(timer: AppTimer) {
        viewModelScope.launch {
            val list = prefs.timers.first()
            prefs.setTimers(list + timer)
            TimerScheduler.schedule(getApplication(), timer)
        }
    }

    fun deleteTimer(timer: AppTimer) {
        viewModelScope.launch {
            val list = prefs.timers.first()
            prefs.setTimers(list.filter { it.id != timer.id })
            TimerScheduler.cancel(getApplication(), timer.id)
        }
    }
}
