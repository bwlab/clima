package it.bwlab.gree.core

import java.nio.charset.StandardCharsets
import org.json.JSONArray
import org.json.JSONObject

/**
 * Client del protocollo Gree/EWPE.
 *
 * Tutto il trasporto UDP passa attraverso [UdpTransport] (iniettabile),
 * così i test possono usare un mock senza aprire socket reali.
 */
class GreeClient(
    private val transport: UdpTransport,
    private val keyStore: DeviceKeyStore
) {

    /** Chiave generica da usare per scan e bind (dipende dalla variante del firmware). */
    private fun genericKey(usesGcm: Boolean): String =
        if (usesGcm) KEY_GCM else KEY_ECB

    /**
     * Scansiona la rete alla ricerca di climatizzatori.
     * Ritorna la lista dei dispositivi trovati.
     */
    fun discover(broadcast: String = "255.255.255.255"): List<DeviceInfo> {
        val msg = JSONObject().put("t", "scan")
        val payload = msg.toString().toByteArray(StandardCharsets.UTF_8)
        val found = mutableListOf<DeviceInfo>()

        val responses = mutableListOf<Pair<ByteArray, String>>()
        val subnet = broadcast.substringBeforeLast(".")

        // 1) Broadcast diretto di subnet (x.x.x.255): il più affidabile e rapido.
        val subnetBcast = "$subnet.255"
        for (attempt in 1..2) {
            val reply = transport.sendReceive(subnetBcast, PORT, payload, 1500)
            if (reply != null) {
                responses.add(Pair(reply, subnetBcast))
                break
            }
        }

        // 2) Broadcast globale (255.255.255.255), se ancora niente.
        if (responses.isEmpty() && broadcast != subnetBcast) {
            val reply = transport.sendReceive(broadcast, PORT, payload, 1500)
            if (reply != null) responses.add(Pair(reply, broadcast))
        }

        // 3) Ultima spiaggia: sweep seriale della /24 con timeout corto (max ~25s).
        if (responses.isEmpty()) {
            for (i in 1..254) {
                val ip = "$subnet.$i"
                val r = transport.sendReceive(ip, PORT, payload, 100)
                if (r != null) responses.add(Pair(r, ip))
            }
        }

        for ((raw, ip) in responses) {
            val text = String(raw, StandardCharsets.UTF_8)
            val usesGcm = text.contains("\"tag\"")
            val pack = try {
                JSONObject(text).optString("pack", null)
            } catch (e: Exception) {
                null
            }
            if (pack == null) continue

            val key = genericKey(usesGcm)
            val decrypted = try {
                if (usesGcm) {
                    val tag = JSONObject(text).optString("tag", null)
                        ?: continue
                    error("GCM non supportato su questo JDK")
                } else {
                    GreeCrypto.decryptEcb(pack, key)
                }
            } catch (e: Exception) {
                continue
            }

            val info = JSONObject(decrypted)
            val mac = info.optString("mac", ip.replace(".", ""))
            val name = info.optString("name", "")
            val firmware = info.optString("ver", "")

            found.add(DeviceInfo(ip, mac, name, firmware, usesGcm))
        }
        return found.distinctBy { it.mac }
    }

    /**
     * Effettua il bind con il climatizzatore per ottenere la sua chiave personale.
     * La chiave viene salvata in [keyStore] indicizzata per MAC.
     */
    fun bind(ip: String): DeviceInfo {
        // Scan diretto per capire variante cifratura e recuperare MAC
        val scanMsg = JSONObject().put("t", "scan")
        val reply = transport.sendReceive(ip, PORT, scanMsg.toString().toByteArray(StandardCharsets.UTF_8), 3000)
            ?: error("Nessuna risposta da $ip durante il bind (scan)")

        val text = String(reply, StandardCharsets.UTF_8)
        val usesGcm = text.contains("\"tag\"")
        val scanObj = JSONObject(text)
        val pack = scanObj.optString("pack", null)
            ?: error("Risposta scan senza pack")
        val tag = if (usesGcm) scanObj.optString("tag", null) else null

        val key = genericKey(usesGcm)
        val decrypted = if (usesGcm) {
            error("GCM non supportato su questo JDK")
        } else {
            GreeCrypto.decryptEcb(pack, key)
        }
        val info = JSONObject(decrypted)
        val mac = info.optString("mac", "")
            .takeIf { it.isNotEmpty() }
            ?: error("Risposta scan senza mac")
        val name = info.optString("name", "")
        val firmware = info.optString("ver", "")
        val device = DeviceInfo(ip, mac, name, firmware, usesGcm)

        // Bind vero e proprio
        val bindPayload = JSONObject()
            .put("mac", mac)
            .put("t", "bind")
            .put("uid", 0)
        val (encPack, encTag) = if (usesGcm) {
            error("GCM non supportato su questo JDK (serve Bouncy Castle o JDK 9+)")
        } else {
            Pair(GreeCrypto.encryptEcb(bindPayload.toString(), key), null)
        }

        val bindMsg = JSONObject()
            .put("cid", "app")
            .put("i", 1)
            .put("t", "pack")
            .put("uid", 0)
            .put("tcid", mac)
            .put("pack", encPack)
        if (encTag != null) bindMsg.put("tag", encTag as Any)

        val bindReply = transport.sendReceive(ip, PORT, bindMsg.toString().toByteArray(StandardCharsets.UTF_8), 5000)
            ?: error("Nessuna risposta al bind da $ip")

        val bindText = String(bindReply, StandardCharsets.UTF_8)
        val bindUsesGcm = bindText.contains("\"tag\"")
        val bindObj = JSONObject(bindText)
        val bindPack = bindObj.optString("pack", null)
            ?: error("Risposta bind senza pack")
        val bindTag = if (bindUsesGcm) bindObj.optString("tag", null) else null

        val bindDecrypted = if (bindUsesGcm) {
            error("GCM non supportato su questo JDK")
        } else {
            GreeCrypto.decryptEcb(bindPack, key)
        }
        val bindResp = JSONObject(bindDecrypted)
        if (bindResp.optString("t", "") != "bindok") {
            error("Bind fallito: $bindDecrypted")
        }
        val deviceKey = bindResp.optString("key", null)
            ?: error("Bind ok ma nessuna chiave ricevuta")

        keyStore.put(mac, deviceKey)
        return device
    }

    /**
     * Legge lo stato del climatizzatore. Esegue il bind automatico se non
     * è già nota la chiave per questo dispositivo.
     */
    fun status(ip: String): ClimateState {
        val device = bind(ip)
        return status(device)
    }

    fun status(device: DeviceInfo): ClimateState {
        val key = keyStore.get(device.mac)
            ?: error("Chiave non nota per ${device.mac}, eseguire bind prima")
        val usesGcm = device.usesGcm

        val payload = JSONObject()
            .put("cols", JSONArray(STATUS_COLS) as Any)
            .put("mac", device.mac)
            .put("t", "status")
        val (encPack, encTag) = if (usesGcm) {
            error("GCM non supportato su questo JDK (serve Bouncy Castle o JDK 9+)")
        } else {
            Pair(GreeCrypto.encryptEcb(payload.toString(), key), null)
        }

        val msg = JSONObject()
            .put("cid", "app")
            .put("i", 0)
            .put("t", "pack")
            .put("uid", 0)
            .put("tcid", device.mac)
            .put("pack", encPack)
        if (encTag != null) msg.put("tag", encTag as Any)

        val reply = transport.sendReceive(device.ip, PORT, msg.toString().toByteArray(StandardCharsets.UTF_8), 5000)
            ?: error("Nessuna risposta allo status da ${device.ip}")

        val text = String(reply, StandardCharsets.UTF_8)
        val usesGcmResp = text.contains("\"tag\"")
        val respObj = JSONObject(text)
        val pack = respObj.optString("pack", null)
            ?: error("Risposta status senza pack")
        val tag = if (usesGcmResp) respObj.optString("tag", null) else null

        val decrypted = if (usesGcmResp) {
            error("GCM non supportato su questo JDK")
        } else {
            GreeCrypto.decryptEcb(pack, key)
        }
        val resp = JSONObject(decrypted)

        return mapStatus(resp)
    }

    /**
     * Invia un comando al climatizzatore (es. accendi, temperatura, modalità).
     * Ritorna true se la risposta ha codice 200.
     */
    fun command(ip: String, opt: List<String>, values: List<Any>): Boolean {
        val device = bind(ip)
        return command(device, opt, values)
    }

    fun command(device: DeviceInfo, opt: List<String>, values: List<Any>): Boolean {
        val key = keyStore.get(device.mac)
            ?: error("Chiave non nota per ${device.mac}, eseguire bind prima")
        val usesGcm = device.usesGcm

        val payload = JSONObject()
            .put("opt", JSONArray(opt) as Any)
            .put("p", JSONArray(values) as Any)
            .put("t", "cmd")
        val (encPack, encTag) = if (usesGcm) {
            error("GCM non supportato su questo JDK (serve Bouncy Castle o JDK 9+)")
        } else {
            Pair(GreeCrypto.encryptEcb(payload.toString(), key), null)
        }

        val msg = JSONObject()
            .put("cid", "app")
            .put("i", 0)
            .put("t", "pack")
            .put("uid", 0)
            .put("tcid", device.mac)
            .put("pack", encPack)
        if (encTag != null) msg.put("tag", encTag as Any)

        val reply = transport.sendReceive(device.ip, PORT, msg.toString().toByteArray(StandardCharsets.UTF_8), 5000)
            ?: return false

        val text = String(reply, StandardCharsets.UTF_8)
        val usesGcmResp = text.contains("\"tag\"")
        val respObj = JSONObject(text)
        val pack = respObj.optString("pack", null)
            ?: return false
        val tag = if (usesGcmResp) respObj.optString("tag", null) else null

        val decrypted = if (usesGcmResp) {
            error("GCM non supportato su questo JDK")
        } else {
            GreeCrypto.decryptEcb(pack, key)
        }
        val resp = JSONObject(decrypted)
        return resp.optInt("r", -1) == 200
    }

    /** Accendi / spegni. */
    fun power(ip: String, on: Boolean): Boolean =
        command(ip, listOf("Pow"), listOf(if (on) 1 else 0))

    /** Imposta temperatura. */
    fun temperature(ip: String, celsius: Int): Boolean =
        command(ip, listOf("TemUn", "SetTem"), listOf(0, celsius))

    /** Imposta modalità. */
    fun mode(ip: String, mode: Mode): Boolean =
        command(ip, listOf("Mod"), listOf(mode.code))

    /** Imposta velocità ventola. */
    fun fan(ip: String, fan: FanSpeed): Boolean =
        command(ip, listOf("WdSpd"), listOf(fan.code))

    // ---------- internals ----------

    private fun mapStatus(resp: JSONObject): ClimateState {
        // NB: JSONArray.toList() NON esiste su Android -> iteriamo a mano.
        fun stringList(name: String): List<String> {
            val arr = resp.optJSONArray(name) ?: return emptyList()
            val out = mutableListOf<String>()
            for (i in 0 until arr.length()) out.add(arr.optString(i))
            return out
        }
        val cols = stringList("cols")
        val dat = stringList("dat").map { it.toIntOrNull() ?: 0 }

        fun get(name: String): Int? {
            val idx = cols.indexOf(name)
            return if (idx in dat.indices) dat[idx] else null
        }

        val pow = (get("Pow") ?: 0) == 1
        val mode = modeFromCode(get("Mod") ?: 0)
        val setTem = get("SetTem") ?: 0
        val temUn = get("TemUn") ?: 0
        val wdSpd = fanSpeedFromCode(get("WdSpd") ?: 0)
        val temSen = get("TemSen") ?: 0
        val lig = get("Lig") ?: 0
        val swUpDn = get("SwUpDn") ?: 0
        val quiet = get("Quiet") ?: 0
        val tur = get("Tur") ?: 0
        val svSt = get("SvSt") ?: 0

        return ClimateState(
            pow, mode, setTem, temUn, wdSpd, temSen, lig, swUpDn, quiet, tur, svSt,
            swingLfRig = get("SwingLfRig") ?: 0,
            health = get("Health") ?: 0,
            air = get("Air") ?: 0,
            swhSlp = get("SwhSlp") ?: 0,
            slpMod = get("SlpMod") ?: 0,
            blo = get("Blo") ?: 0,
            temRec = get("TemRec") ?: 0,
            supported = cols.toSet()
        )
    }
}

/**
 * Store delle chiavi personali dei dispositivi, indicizzate per MAC.
 */
interface DeviceKeyStore {
    fun get(mac: String): String?
    fun put(mac: String, key: String)
    /** Tutte le coppie MAC->chiave conosciute. */
    fun allEntries(): Map<String, String>
}

/** Implementazione in memoria (utile per i test). */
class InMemoryDeviceKeyStore : DeviceKeyStore {
    private val map = mutableMapOf<String, String>()
    override fun get(mac: String): String? = map[mac]
    override fun put(mac: String, key: String) { map[mac] = key }
    override fun allEntries(): Map<String, String> = map.toMap()
}

/** Implementazione su file JSON (per l'uso reale su JVM/PC). */
class JsonFileDeviceKeyStore(private val path: java.io.File) : DeviceKeyStore {
    private var cache: Map<String, String> = emptyMap()

    private fun load(): Map<String, String> {
        if (!path.exists()) return emptyMap()
        // NB: JSONObject.toMap() NON esiste su Android -> iteriamo le chiavi a mano.
        return try {
            val obj = JSONObject(path.readText(StandardCharsets.UTF_8))
            val out = LinkedHashMap<String, String>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                out[k] = obj.optString(k)
            }
            out
        } catch (e: Throwable) {
            emptyMap()
        }
    }

    private fun save() {
        try {
            path.parentFile.mkdirs()
            path.writeText(JSONObject(cache).toString(2), StandardCharsets.UTF_8)
        } catch (e: Throwable) {
            // Il salvataggio non deve far crashare l'app: le chiavi restano valide in memoria.
        }
    }

    override fun get(mac: String): String? {
        if (cache.isEmpty()) cache = load()
        return cache[mac]
    }

    override fun put(mac: String, key: String) {
        if (cache.isEmpty()) cache = load()
        cache = cache + (mac to key)
        save()
    }

    override fun allEntries(): Map<String, String> {
        if (cache.isEmpty()) cache = load()
        return cache
    }
}
