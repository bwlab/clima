package it.bwlab.clima.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * Dispositivo salvato dall'utente. Le chiavi AES restano nel key store del
 * protocollo (kotlin-core), indicizzate per MAC: qui basta il riferimento.
 */
data class SavedDevice(
    val id: String,
    val name: String,
    val ip: String,
    val mac: String,
    val firmware: String,
    val usesGcm: Boolean
) {
    companion object {
        fun fromJson(o: JSONObject): SavedDevice = SavedDevice(
            id = o.optString("id"),
            name = o.optString("name"),
            ip = o.optString("ip"),
            mac = o.optString("mac"),
            firmware = o.optString("firmware"),
            usesGcm = o.optBoolean("usesGcm", false)
        )
    }

    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("name", name)
        .put("ip", ip)
        .put("mac", mac)
        .put("firmware", firmware)
        .put("usesGcm", usesGcm)
}

/** Timer lato app (AlarmManager): accensione o spegnimento all'ora prevista. */
data class AppTimer(
    val id: String,
    val deviceId: String,
    /** true = accensione, false = spegnimento */
    val turnOn: Boolean,
    val hour: Int,
    val minute: Int,
    /** true = una volta sola, false = ogni giorno */
    val once: Boolean
) {
    companion object {
        fun fromJson(o: JSONObject): AppTimer = AppTimer(
            id = o.optString("id"),
            deviceId = o.optString("deviceId"),
            turnOn = o.optString("type") == "on",
            hour = o.optInt("hour"),
            minute = o.optInt("minute"),
            once = !o.optBoolean("daily", false)
        )
    }

    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("deviceId", deviceId)
        .put("type", if (turnOn) "on" else "off")
        .put("hour", hour)
        .put("minute", minute)
        .put("daily", !once)
}
