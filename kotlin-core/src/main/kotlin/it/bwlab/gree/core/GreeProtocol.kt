package it.bwlab.gree.core

/**
 * Costanti e modelli del protocollo Gree / EWPE Smart (UDP porta 7000).
 */

// ---- Chiavi generiche di fabbrica (firmware vecchi / nuovi) ----
const val PORT = 7000
const val TIMEOUT_MS = 5000

const val KEY_ECB = "a3K8Bx%2r8Y7#xDh"
const val KEY_GCM = "{yxAHAY_Lm6pbC/<"
val IV_GCM: ByteArray = byteArrayOf(
    0x54, 0x40, 0x78, 0x44, 0x49, 0x67,
    0x5A, 0x51, 0x6C, 0x5E, 0x63, 0x13
)
const val AAD_GCM = "qualcomm-test"

// ---- Campi restituiti dallo status ----
val STATUS_COLS = listOf(
    "Pow", "Mod", "SetTem", "TemUn", "WdSpd", "TemSen",
    "Lig", "SwUpDn", "Quiet", "Tur", "SvSt"
)

// ---- Enum ----
enum class Mode(val code: Int) {
    AUTO(0), COOL(1), DRY(2), FAN(3), HEAT(4)
}

enum class FanSpeed(val code: Int) {
    AUTO(0), LOW(1), MED_LOW(2), MED(3), MED_HIGH(4), HIGH(5)
}

fun modeFromCode(code: Int): Mode = Mode.values().firstOrNull { it.code == code } ?: Mode.AUTO
fun fanSpeedFromCode(code: Int): FanSpeed = FanSpeed.values().firstOrNull { it.code == code } ?: FanSpeed.AUTO

// ---- Data class ----
data class DeviceInfo(
    val ip: String,
    val mac: String,
    val name: String,
    val firmware: String,
    val usesGcm: Boolean
)

data class ClimateState(
    val pow: Boolean,
    val mode: Mode,
    val setTem: Int,
    val temUn: Int,
    val wdSpd: FanSpeed,
    val temSen: Int,
    val lig: Int,
    val swUpDn: Int,
    val quiet: Int,
    val tur: Int,
    val svSt: Int
) {
    fun temSenCorrected(): Int = if (temSen >= 40) temSen - 40 else temSen
}

data class BindResponse(val t: String, val key: String? = null)
data class StatusResponse(val t: String, val cols: List<String>, val dat: List<Int>)
data class CmdResponse(val r: Int, val p: List<Int> = emptyList())
