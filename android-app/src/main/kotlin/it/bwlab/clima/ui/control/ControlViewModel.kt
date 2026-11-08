package it.bwlab.clima.ui.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.bwlab.clima.data.DeviceRepository
import it.bwlab.clima.data.SavedDevice
import it.bwlab.gree.core.ClimateState
import it.bwlab.gree.core.FanSpeed
import it.bwlab.gree.core.Mode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

/**
 * Stato e comandi della schermata Controllo.
 *
 * Aggiornamento ottimistico: la UI mostra subito il valore scelto, il comando
 * parte con debounce 400 ms e in caso di fallimento si ri-legge lo stato reale.
 */
class ControlViewModel(
    private val repository: DeviceRepository,
    private val deviceId: String
) : ViewModel() {

    // Chiamare ControlScreen solo se il device è nei salvati (guard in ClimaNavHost).
    private val device: SavedDevice =
        repository.saved.value.first { it.id == deviceId }

    data class UiState(
        val device: SavedDevice,
        val state: ClimateState? = null,
        val connecting: Boolean = true,
        val reachable: Boolean = true,
        /** temperatura locale ottimistica in °C (durante il debounce) */
        val optimistTemp: Int? = null,
        val vpn: Boolean = false,
        val lastUpdated: Long = 0,
        /** chiave stringa dell'ultimo errore, per lo Snackbar */
        val errorKey: String? = null
    ) {
        // temperatura mostrata: ottimistica durante il debounce
        val displayedTemp: Int?
            get() = optimistTemp ?: state?.setTemCelsius()
    }

    private val _state = MutableStateFlow(UiState(device))
    val ui = _state.asStateFlow()

    private var pollingJob: Job? = null
    private var tempJob: Job? = null

    init {
        viewModelScope.launch {
            repository.saved.collect { list ->
                val still = list.firstOrNull { it.id == deviceId }
                if (still != null) _state.value = _state.value.copy(device = still)
            }
        }
        refresh()
    }

    // ---- Lettura / polling ----

    fun refresh() {
        viewModelScope.launch {
            try {
                val st = repository.status(device)
                _state.value = _state.value.copy(
                    state = st,
                    connecting = false,
                    reachable = true,
                    lastUpdated = System.currentTimeMillis(),
                    optimistTemp = null
                )
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                _state.value = _state.value.copy(
                    connecting = false,
                    reachable = false,
                    lastUpdated = System.currentTimeMillis()
                )
            }
        }
    }

    fun startPolling(autoRefresh: Boolean) {
        stopPolling()
        if (!autoRefresh) return
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(5_000)
                if (!_state.value.connecting) refresh()
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun setVpn(active: Boolean) {
        if (_state.value.vpn != active) {
            _state.value = _state.value.copy(vpn = active)
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorKey = null)
    }

    // ---- Comandi ----

    fun power(on: Boolean) {
        // aggiornamento ottimistico immediato del toggle
        _state.value.state?.let { st ->
            _state.value = _state.value.copy(state = st.copy(pow = on))
        }
        send(listOf("Pow"), listOf(if (on) 1 else 0))
    }

    /** Quadrante e pulsanti −/+: passo 1 °C, debounce 400 ms. */
    fun changeTemperature(delta: Int) {
        val current = _state.value.displayedTemp ?: return
        setTemperature((current + delta).coerceIn(16, 30))
    }

    fun dialTemperature(celsius: Int) {
        setTemperature(celsius.coerceIn(16, 30))
    }

    private fun setTemperature(celsius: Int) {
        // UI ottimistica subito
        _state.value = _state.value.copy(optimistTemp = celsius)
        tempJob?.cancel()
        tempJob = viewModelScope.launch {
            delay(400)
            val ok = sendWithResult(listOf("TemUn", "SetTem"), listOf(0, celsius))
            if (!ok) {
                // rollback: ri-leggi lo stato reale
                _state.value = _state.value.copy(optimistTemp = null)
                refresh()
            }
        }
    }

    fun setMode(mode: Mode) {
        _state.value.state?.let { st ->
            _state.value = _state.value.copy(state = st.copy(mode = mode))
        }
        send(listOf("Mod"), listOf(mode.code))
    }

    fun setFan(fan: FanSpeed) {
        _state.value.state?.let { st ->
            _state.value = _state.value.copy(state = st.copy(wdSpd = fan))
        }
        send(listOf("WdSpd"), listOf(fan.code))
    }

    /** Chips funzione: Turbo, Silenzioso, Notte, Luce, Salute, Ricambio aria, X-Fan. */
    fun toggleFeature(key: String, turnOn: Boolean) {
        _state.value.state?.let { st ->
            val caller: ClimateState = st
            _state.value = _state.value.copy(state = when (key) {
                "Tur" -> caller.copy(tur = if (turnOn) 1 else 0)
                "Quiet" -> caller.copy(quiet = if (turnOn) 1 else 0)
                "SwhSlp" -> caller.copy(swhSlp = if (turnOn) 1 else 0)
                "Lig" -> caller.copy(lig = if (turnOn) 1 else 0)
                "Health" -> caller.copy(health = if (turnOn) 1 else 0)
                "Air" -> caller.copy(air = if (turnOn) 1 else 0)
                "Blo" -> caller.copy(blo = if (turnOn) 1 else 0)
                else -> caller
            })
        }
        send(listOf(key), listOf(if (turnOn) 1 else 0))
    }

    /** Alette verticali (SwUpDn 0–11) e orizzontali (SwingLfRig 0–6). */
    fun setLouvre(horizontal: Boolean, code: Int) {
        _state.value.state?.let { st ->
            _state.value = _state.value.copy(
                state = if (horizontal) st.copy(swingLfRig = code) else st.copy(swUpDn = code)
            )
        }
        send(if (horizontal) listOf("SwingLfRig") else listOf("SwUpDn"), listOf(code))
    }

    private fun send(opts: List<String>, values: List<Any>) {
        viewModelScope.launch { sendWithResult(opts, values) }
    }

    private suspend fun sendWithResult(opts: List<String>, values: List<Any>): Boolean {
        val ok = repository.command(device, opts, values)
        if (!ok) {
            _state.value = _state.value.copy(errorKey = "command_failed")
        }
        return ok
    }
}
