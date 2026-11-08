package it.bwlab.clima.util

import android.content.Context
import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import it.bwlab.clima.R
import java.text.NumberFormat
import java.util.Date

/**
 * Formattazione localizzata: temperature e tempi relativi seguono il Locale
 * corrente ("26,5 °C" in italiano/tedesco, "26.5 °C" in inglese).
 */
object Formatters {

    val currentLocale: java.util.Locale
        get() = java.util.Locale.getDefault()

    /** Temperatura come stringa localizzata, senza simbolo: "26,5". */
    fun temperature(celsius: Int, fahrenheit: Boolean): String {
        val value = if (fahrenheit) celsius * 9.0 / 5.0 + 32.0 else celsius.toDouble()
        val nf = NumberFormat.getNumberInstance(currentLocale)
        nf.minimumFractionDigits = 0
        nf.maximumFractionDigits = 1
        return nf.format(value)
    }

    @Composable
    fun temperatureWithUnit(celsius: Int, fahrenheit: Boolean): String {
        val formatted = temperature(celsius, fahrenheit)
        return if (fahrenheit) {
            stringResource(R.string.temp_fahrenheit, formatted)
        } else {
            stringResource(R.string.temp_celsius, formatted)
        }
    }

    /** "Aggiornato X s fa": tempo relativo localizzato di sistema. */
    @Composable
    fun updatedAgo(timestamp: Long): String {
        if (timestamp == 0L) return stringResource(R.string.updated_now)
        return DateUtils.getRelativeTimeSpanString(
            timestamp,
            Date().time,
            DateUtils.SECOND_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }
}
