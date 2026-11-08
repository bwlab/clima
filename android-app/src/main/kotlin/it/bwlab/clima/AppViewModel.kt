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
import kotlinx.coroutines.launch

/**
 * ViewModel condiviso: gestisce dispositivi salvati, ricerca in rete,
 * preferenze (tema/unità/aggiornamento) e sincronizza il tema per la UI.
 */
class AppViewModel(application: Application) : AndroidViewModel(application) {

    val repository = DeviceRepository(application)

    val savedDevices get() = repository.saved
    val foundDevices get() = repository.found
    val unreachable get() = repository.unreachable
    val prefs get() = repository.prefs

    /** Message di cortesia per la schermata Climatizzatori (chiave di risorsa). */
    val scanMessage = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

    init {
        // Ponte DataStore -> tema applicato
        viewModelScope.launch {
            prefs.theme.collect { ThemePreferences.currentTheme.value = it }
        }
    }

    fun scan() {
        scanMessage.value = null
        viewModelScope.launch {
            try {
                val found = repository.scan()
                if (found.isEmpty()) {
                    scanMessage.value = "scan_empty"
                } else {
                    scanMessage.value = null
                }
            } catch (e: Throwable) {
                scanMessage.value = "scan_failed"
            }
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
