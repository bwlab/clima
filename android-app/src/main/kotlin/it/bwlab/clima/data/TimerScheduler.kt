package it.bwlab.clima.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import it.bwlab.clima.GreeClientHolder
import it.bwlab.gree.core.DeviceInfo
import it.bwlab.gree.core.GreeClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar

/**
 * Timer lato app. Le librerie del protocollo locale non gestiscono i timer
 * dell'unità: qui si usa [AlarmManager.setExactAndAllowWhileIdle] e all'ora
 * prevista si invia il comando Pow. Nota UI obbligatoria: il telefono dev'essere
 * connesso alla rete di casa o alla VPN in quel momento.
 */
object TimerScheduler {

    private const val ACTION_TIMER = "it.bwlab.clima.action.TIMER"
    internal const val EXTRA_TIMER_ID = "timerId"

    fun schedule(context: Context, timer: AppTimer) {
        cancel(context, timer.id)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(context, timer.id)
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, timer.hour)
            set(Calendar.MINUTE, timer.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (time.time <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        val triggerAt = cal.timeInMillis
        if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
            // Senza permesso exact: fallback su inexact (rispetta la finestra di mantenimento)
            am.setWindow(AlarmManager.RTC_WAKEUP, triggerAt, 60_000L, pi)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    fun cancel(context: Context, timerId: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context, timerId))
    }

    /** Riprogramma tutti i timer della categoria (usato all'avvio e dopo un riavvio). */
    fun rescheduleAll(context: Context) {
        val prefs = ClimaPreferences(context)
        val timers = runBlocking { prefs.timers.first() }
        timers.filter { !it.once }.forEach { schedule(context, it) }
        // i timer "una volta" già scaduti oggi vengono riprogrammati (domani) dallo schedule()
    }

    private fun pendingIntent(context: Context, timerId: String): PendingIntent {
        val intent = Intent(context, TimerReceiver::class.java)
            .setAction(ACTION_TIMER)
            .putExtra(EXTRA_TIMER_ID, timerId)
        return PendingIntent.getBroadcast(
            context, timerId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

/** Riceve l'allarme e invia il comando di accensione/spegnimento all'unità. */
class TimerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val timerId = intent.getStringExtra(TimerScheduler.EXTRA_TIMER_ID) ?: return
        val prefs = ClimaPreferences(context)
        val timers = runBlocking { prefs.timers.first() }
        val timer = timers.firstOrNull { it.id == timerId } ?: return
        val repo = DeviceRepository(context)
        val device = repo.saved.value.firstOrNull { it.id == timer.deviceId } ?: return

        val ok = runBlocking { repo.command(device, listOf("Pow"), listOf(if (timer.turnOn) 1 else 0)) }

        if (timer.once) {
            // timer "una volta": consumato
            val remaining = timers.filter { it.id != timer.id }
            runBlocking { prefs.setTimers(remaining) }
        } else {
            // timer quotidiano: la prossima occorrenza va riprogrammata
            runBlocking { prefs.setTimers(timers) }
            TimerScheduler.schedule(context, timer)
        }
    }
}

/** Riprogramma gli allarmi quotidiani dopo il riavvio del telefono. */
class BootScheduler : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            TimerScheduler.rescheduleAll(context)
        }
    }
}
