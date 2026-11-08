package it.bwlab.clima.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import it.bwlab.clima.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "clima_prefs")

/**
 * Preferenze dell'app (DataStore): tema, unità, aggiornamento automatico,
 * dispositivi salvati e timer programmati.
 */
class ClimaPreferences(private val context: Context) {

    private object Keys {
        val theme = stringPreferencesKey("theme")
        val unit = stringPreferencesKey("unit")
        val autoRefresh = booleanPreferencesKey("auto_refresh")
        val devices = stringPreferencesKey("devices_json")
        val timers = stringPreferencesKey("timers_json")
    }

    // ---- Impostazioni ----

    val theme: Flow<ThemeMode> = context.dataStore.data.map { p ->
        when (p[Keys.theme]) {
            "light" -> ThemeMode.LIGHT
            "dark" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    val usesFahrenheit: Flow<Boolean> = context.dataStore.data.map { p ->
        p[Keys.unit] == "F"
    }

    val autoRefresh: Flow<Boolean> = context.dataStore.data.map { p ->
        // default: attivo
        p[Keys.autoRefresh] ?: true
    }

    suspend fun setTheme(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.theme] = mode.name.lowercase() }
    }

    suspend fun setUnit(fahrenheit: Boolean) {
        context.dataStore.edit { it[Keys.unit] = if (fahrenheit) "F" else "C" }
    }

    suspend fun setAutoRefresh(enabled: Boolean) {
        context.dataStore.edit { it[Keys.autoRefresh] = enabled }
    }

    /** Lettura una tantum, bloccante: usata solo in edge case (nav iniziale, allarmi). */
    fun readSavedDevicesBlocking(): List<SavedDevice> = kotlinx.coroutines.runBlocking {
        savedDevices.first()
    }

    // ---- Dispositivi salvati ----

    val savedDevices: Flow<List<SavedDevice>> = context.dataStore.data.map { p ->
        parseDevices(p[Keys.devices])
    }

    suspend fun setSavedDevices(devices: List<SavedDevice>) {
        val arr = JSONArray()
        devices.forEach { arr.put(it.toJson()) }
        context.dataStore.edit { it[Keys.devices] = arr.toString() }
    }

    private fun parseDevices(json: String?): List<SavedDevice> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val out = mutableListOf<SavedDevice>()
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) out.add(SavedDevice.fromJson(arr.getJSONObject(i)))
            out
        } catch (e: Throwable) {
            emptyList()
        }
    }

    // ---- Timer ----

    val timers: Flow<List<AppTimer>> = context.dataStore.data.map { p ->
        parseTimers(p[Keys.timers])
    }

    suspend fun setTimers(list: List<AppTimer>) {
        val arr = JSONArray()
        list.forEach { arr.put(it.toJson()) }
        context.dataStore.edit { it[Keys.timers] = arr.toString() }
    }

    /** Tutti i timer, in lettura una tantum: usato dal ricevitore degli allarmi. */
    fun readTimersBlocking(): List<AppTimer> = kotlinx.coroutines.runBlocking {
        timers.first()
    }

    private fun parseTimers(json: String?): List<AppTimer> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val out = mutableListOf<AppTimer>()
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) out.add(AppTimer.fromJson(arr.getJSONObject(i)))
            out
        } catch (e: Throwable) {
            emptyList()
        }
    }
}
