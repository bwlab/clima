package it.bwlab.clima.data

import it.bwlab.clima.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Ponte tra il flusso DataStore (osservato dai ViewModel) e il tema applicato
 * in [it.bwlab.clima.MainActivity] prima che i flussi abbiano emesso.
 */
object ThemePreferences {
    val currentTheme = MutableStateFlow(ThemeMode.SYSTEM)
}
