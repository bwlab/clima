# Climatizzatore Gree/EWPE Smart — Protocollo locale, aggancio Wi-Fi e porting

> Documento di riferimento per il controllo **locale (senza cloud)** di un climatizzatore
> Gree protocollo EWPE Smart — in questo caso una **Viessmann Vitoclima 232-S**
> (OEM Gree, app ufficiale "EWPE Smart").
>
> Ultimo aggiornamento: 07/11/2026 — stato: **controllo completo verificato dal vivo** ✅

---

## Indice

1. [Ambiente di test](#1-ambiente-di-test)
2. [Il protocollo — visione d'insieme](#2-il-protocollo--visione-dinsieme)
3. [Dettagli crittografia](#3-dettagli-crittografia)
4. [Messaggi del protocollo](#4-messaggi-del-protocollo)
5. [Procedura di aggancio alla rete Wi-Fi (provisioning)](#5-procedura-di-aggancio-alla-rete-wi-fi-provisioning)
6. [Procedura dopo un RESET del modulo Wi-Fi](#6-procedura-dopo-un-reset-del-modulo-wi-fi)
7. [Script a disposizione](#7-script-a-disposizione)
8. [Test eseguiti e risultati](#8-test-eseguiti-e-risultati)
9. [Linee guida per il porting in Kotlin](#9-linee-guida-per-il-porting-in-kotlin)
10. [Troubleshooting](#10-troubleshooting)

---

## 1. Ambiente di test

| Elemento | Valore |
|---|---|
| Climatizzatore | Viessmann Vitoclima 232-S (Gree EWPE) |
| Modulo Wi-Fi | MAC `F4:91:1E:53:8C:A2`, firmware **V1.2.1** |
| Cifratura iniziale testata | **AES-128 ECB** (fw vecchio) |
| Rete di casa | `HomeWiFi-SSID` (2,4 GHz, canale 5) — password in `nmcli` |
| PC di test | `bwlx-007`, Ubuntu, IP `192.168.1.100/24`, `wlp0s20f3` |
| Tools | `gree_test.py`, `gree_setup.py` (Python 3, `pycryptodome`) |
| Dato importante | **Il modulo Gree funziona solo su 2,4 GHz** — la Vodafone 5 GHz (`HomeWiFi-5GHz`) NON va usata nel provisioning |

Struttura della cartella:

```
climatizzatre/
├── gree_test.py        # client: scan/bind/status/on/off/temp/mode/fan
├── gree_setup.py       # provisioning Wi-Fi + verifica aggancio
├── gree_keys.json      # chiavi personali dei dispositivi (per IP)
├── gree_wifi.json      # SSID/password di casa usati nel provisioning
├── DOCUMENTAZIONE.md   # questo documento
└── .venv/              # ambiente Python con pycryptodome
```

---

## 2. Il protocollo — visione d'insieme

Tutto avviene su **UDP, porta 7000**, con scambio di **oggetti JSON** (UTF-8).

I messaggi utilitari (scan, provisioning) viaggiano **in chiaro**.
I messaggi verso il climatizzatore (bind, status, cmd) hanno il campo **`pack`**
contenente JSON **cifrato AES-128** e codificato **base64**, dentro un envelope comune:

```json
{
  "cid":  "app",
  "i":    1,
  "t":    "pack",
  "uid":  0,
  "tcid": "aabbccddeeff",          // MAC del clima
  "pack": "<base64 del payload cifrato>",
  "tag":  "<base64 del tag GCM, solo fw recenti>"
}
```

Campi:

| Campo | Significato |
|---|---|
| `cid` | chi invia (`"app"` per l'app locale) |
| `tcid` | destinatario (MAC del clima) |
| `i` | `1` = pacchetto di bind, `0` = comando dati |
| `t` | tipo: `pack`, `scan` |
| `pack` | payload cifrato base64 |
| `tag` | tag AES-GCM (solo firmware "nuovi") |

Esistono **due varianti di cifratura** a seconda del firmware del modulo:

* **Fw vecchi (come ours V1.2.1): AES-128-ECB, padding PKCS7**, chiave generica per scan/bind
* **Fw nuovi: AES-128-GCM**, chiave generica diversa, IV fisso, AAD `qualcomm-test`, tag base64 nel campo `tag`

**Come riconoscere la variante dalla risposta:** se il messaggio contiene il
campo `tag` → GCM; altrimenti → ECB.

Scan e bind usano la **chiave generica**; tutti i comandi successivi usano la
**chiave personale del dispositivo**, che si ottiene **con il bind** e si memorizza
(ora: `gree_keys.json`).

---

## 3. Dettagli crittografia

### Variante ECB (fw vecchi — la nostra)

```
Algoritmo : AES-128-ECB, padding PKCS7
Chiave    : a3K8Bx%2r8Y7#xDh      (16 byte, generica di fabbrica)
```

* `encrypt(payload)`: cifro tutto lo stream e base64 → `pack`
* `decrypt(pack)`: base64 → decrypt → **troncare al persistere `}`** (il padding
  può sporcare la coda): `text[: text.rfind("}") + 1]`

### Variante GCM (fw recenti, es. moduli "Qualcomm")

```
Algoritmo : AES-128-GCM
Chiave    : {yxAHAY_Lm6pbC/<      (16 byte, generica)
IV/nonce  : 5440 7844 4967 5A51 6C5E 6313    (12 byte, FISSO)
AAD       : qualcomm-test
```

* `encrypt(payload)`: `ct, tag` → base64 separatamente → `pack` e `tag`
* `decrypt(pack, tag)`: verifica del tag con `decrypt_and_verify`

### Chiave personale del dispositivo

Ottenuta nel **bind** (campo `key` della risposta, stringa). Tutti i comandi
posteriori usano QUESTA chiave, con la stessa cifra ECB o GCM del modulo.

---

## 4. Messaggi del protocollo

Tutti inviati/ricevuti su UDP 7000 con timeout consigliato 3–5 s.

### 4.1 Scan (in chiaro!)

```
->  {"t":"scan"}
```
Da unicast diretto all'IP del clima, o broadcast `255.255.255.255` / directed
broadcast `192.168.1.255` (alcuni moduli rispondono solo al diretto).

Risposta: envelope con `pack` cifrato con la chiave generica (ECB o GCM), che once
decifrato contiene:

```json
{ "mac": "aabbccddeeff", "name": "1e538ca2", "ver": "V1.2.1", ... }
```

> Osservazione dal vivo: la risposta allo scan del nostro modulo ha `name` uguale
> al MAC short; è normale. Il campo per i rapporti è `mac`.

### 4.2 Bind (prima interazione con un IP)

Payload cifrato con la chiave generica:

```json
{ "mac": "aabbccddeeff", "t": "bind", "uid": 0 }
```

Risposta (decifrata con la stessa chiave generica):

```json
{ "t": "bindok", "key": "<chiave personale del dispositivo>", ... }
```

Salvare `key` (nel nostro caso per IP in `gree_keys.json`).

### 4.3 Status

Payload cifrato con la chiave personale:

```json
{ "cols": ["Pow","Mod","SetTem","TemUn","WdSpd","TemSen","Lig","SwUpDn","Quiet","Tur","SvSt"],
  "mac": "aabbccddeeff",
  "t": "status" }
```

Risposta:

```json
{ "t": "dat", "cols": [...come sopra...], "dat": [ 0, 4, 24, 0, 0, 18, 1, 0, 0, 0, 0 ] }
```

Significato delle colonne:

| Col | Significato | Esempio |
|---|---|---|
| `Pow` | accesa sì/no | `1` accesa |
| `Mod` | modalità: 0 auto, 1 cool, 2 dry, 3 fan, 4 heat | `4` = heat |
| `SetTem` | temperatura impostata (°C) | `24` |
| `TemUn` | unità: 0 °C, 1 °F | `0` |
| `WdSpd` | ventola: 0 auto, 1 low, 2 medlow, 3 med, 4 medhigh, 5 high | `0` |
| `TemSen` | temperatura ambiente (⚠ molti fw sommano +40: se ≥40, sottrarre 40) | `18` |
| `Lig` | display LED: 1 acceso, 0 spento | `1` |
| `SwUpDn` | swing verticale | `0` |
| `Quiet` | modalità silenziosa | `0` |
| `Tur` | turbo | `0` |
| `SvSt` | standby/eco | `0` |

Il nostro clima (07/11/2026, prima di tutti i test):
`Pow=0, Mod=4 (heat), SetTem=24, WdSpd=0 (auto), TemSen=18` → ambiente 18 °C
(Novembre!). *N.B. qui TemSen era 18 già "pulito" — il caso +40 non生气a but compared.*

### 4.4 Comando (write)

Payload cifrato con la chiave personale:

```json
{ "opt": ["Pow"],      "p": [1], "t": "cmd" }        // accendere
{ "opt": ["SetTem","TemUn"], "p": [24,0], "t": "cmd" }       // 24 °C
{ "opt": ["Mod"],      "p": [4], "t": "cmd" }        // heat
{ "opt": ["WdSpd"],    "p": [5], "t": "cmd" }        // ventola high
```

Risposta:

```json
{ "p": [...", "r": 200, ... }    // r=200 -> comando accettato
```

> Osservazione dal vivo: il clima **accetta i comandi anche da spento**
> (dormiente); l'impostazione `SetTem` su clima spento si conserva.

### 4.5 Provisioning Wi-Fi (AGGANCIO — solo quando il clima è in AP mode!)

Pacchetto **in chiaro** (non cifrato!), UDP 7000, all'IP del clima mentre il
modulo è in AP mode (vedi §5):

```json
{"psw": "password-casa", "ssid": "HomeWiFi-SSID", "t": "wlan"}
```

Risposta attesa dalla documentazione (non sempre arriva!):

```json
{"t":"ret","r":200}
```

> Osservazione dal vivo: il nostro modulo **non ha risposto** ai pacchetti wlan
> e ha Gemini agganciato mono: non dimenticare che "nessuna risposta" NON vuol dire FAILURE.
> In più e sensibile: dopo le credenziali il modulo lascia l'hotspot entro pochi secondi–minuti.

---

## 5. Procedura di aggancio alla rete Wi-Fi (provisioning)

⏭ **RIASSUNTO in breve:** il modulo, in modalità configurazione, crea un
**hotspot Wi-Fi** il cui SSID è una stringa a 8 caratteri derivata dal MAC
(esempio: `1e538ca2` = ultimi 4 byte del MAC). Password dell'hotspot: `12345678`.
Sull'hotspot il clima ha l'IP **192.168.1.1**. Si li passa SSID+password di casa
via UDP 7000 e lui abbandona l'AP per attaccarsi alla rete di casa.

### Step-by-step (VERIFICATA — funzionata).

1. **Porta il clima in modalità configurazione**
   * Telecomando: tieni premuto **WIFI** ~10 s fino al doppio beep
     (oppure **MODE+WIFI** ins., o **FIN+WIFI** sul pannello per modi senza tasto)
   * L'icona Wi-Fi a display lampeggia → AP attivo
   * Dal PC: `nmcli dev wifi list` → deve apparire l'SSID a 8 caratteri hex

2. **Collega il PC all'hotspot del clima** (GUI Wi-Fi di Ubuntu)

   ⚠ REACH delicato: il NetworkManager **autoconnect può "rubare" la
   connessione** e riattach a casa, interrompendo l'aggancio. Occorre
   preventivamente distruggere l'autoconnect se vuoi "Reduced risk":

   ```bash
   nmcli con modify HomeWiFi-SSID connection.autoconnect no
   nmcli con modify HomeWiFi-5GHz connection.autoconnect no
   # (lasciare così fino a fine procedura, poi ripristinare "yes")
   ```

3. **Verifica di essere sull'AP del clima** (NON quello del router di casa!):

   ```bash
   ping -c1 192.168.1.1 && arp -n 192.168.1.1
   # 192.168.1.1 deve avere MAC f4:91:1e:...  (il clima)
   # se mostra 80:16:05:... è ancora il ROUTER di casa: non ci siamo ancora collecati!
   ```

   -> Il PC di solito ottiene IP `192.168.1.2`; il clima è `192.168.1.1`.

4. **Invia le credenziali di casa**:

   ```bash
   .venv/bin/python gree_setup.py
   ```

   (`gree_setup.py` rileva automaticamente la fase; accetta `--ssid/--psw` oppure
   le chiede una volta e le salva in `gree_wifi.json`;
   `--repeat N` — default 10 — spara il pacchetto N volte ogni 2 s)

5. **Eventi attesi**: l'hotspot del clima sparisce dal Wi-Fi (o smette di rispondere
   a `192.168.1.1`) → il modulo sta attaccando la rete di casa (قا انتظار decine di s).

6. **Rientra sulla rete di casa** e riabilita l'autoconnect (se l'avevi tolto):

   ```bash
   nmcli con modify HomeWiFi-SSID connection.autoconnect yes
   nmcli con modify HomeWiFi-5GHz connection.autoconnect yes
   nmcli dev wifi connect HomeWiFi-SSID
   ```

7. **VERIFICA aggancio** (di nuovo `gree_setup.py`, questa volta in fase 2):

   ```bash
   .venv/bin/python gree_setup.py
   # oppure
   .venv/bin/python gree_test.py scan 192.168.1.255
   ```

   Output esperienza:

   ```
   192.168.1.100     mac=aabbccddeeff  nome=1e538ca2  fw=V1.2.1  cifratura=ECB
   ```

8. **Bind + controllo**:

   ```bash
   .venv/bin/python gree_test.py status 192.168.1.100
   ```
   (La prima occupazione automaticamente fa il bind e salva la chiave in `gree_keys.json`)

> **Il modulo Gree si attacca SOLO a rete 2,4 GHz.** Se la casa ha SSID 5GHz vs 2.4GHz
> separati, usare SEMPRE quello a 2,4 nel provisioning.

---

## 6. Procedura dopo un RESET del modulo Wi-Fi

Scenario: il modulo WK non ricorda più la rete (track parametri fabbrica o
ripristino WIFI). La procedura è IDENTICA alla §5 con due differenze:

1. **L'IP può cambiare**: non scommettere su `192.168.1.100`; fare alltid scan.
2. **La chiave può essere rigenerata**: se dopo il reset lo status non risponde
   o non si capisce mai — REMOVERA la cache delle chiavi:

   ```bash
   rm gree_keys.json
   .venv/bin/python gree_test.py status <IP-NUOVO>
   # rifà il bind da zero
   ```

Checklist rapida:

```text
[ ] reset del modulo (10 s tasto WIFI)          -> doppio beep
[ ] hotspot 1e538ca2 visibile                    -> nmcli dev wifi list
[ ] autoconnect casa DISABLED                    -> nmcli con modify ... no
[ ] PC on hotspot (psw 12345678)                 -> arp -n 192.168.1.1 = f4:91:1e:...
[ ] gree_setup.py                                -> credenziali inviate
[ ] hotspot sparito                              -> attendere 10-60 s
[ ] re-PC su rete di casa + autoconnect re-enabled
[ ] gree_setup.py / gree_test.py scan            -> AGGANCIATO con nuovo IP
[ ] rm gree_keys.json (se server) + bind automatico
```

---

## 7. Script a disposizione

### `gree_test.py` — controllo del clima

```bash
python gree_test.py scan                      # broadcast scan
python gree_test.py scan 192.168.1.255        # directed broadcast scan
python gree_test.py status 192.168.1.100       # bind automatico al primo colpo + lettura
python gree_test.py on    192.168.1.100        # accendere
python gree_test.py off   192.168.1.100        # spegnere
python gree_test.py temp  192.168.1.100 24     # 16..30 °C
python gree_test.py mode  192.168.1.100 cool   # auto|cool|dry|fan|heat
python gree_test.py fan   192.168.1.100 auto   # auto|low|medlow|med|medhigh|high
```

File sattelite: `gree_keys.json` = `{ "<ip>": { "mac", "name", "ver", "gcm", "key" } }`.

### `gree_setup.py` — aggancio Wi-Fi (provisioning)

```bash
python gree_setup.py                    # capisce da solo la fase:
                                        #   se waypoint MAC f4:91:1e → fase 1 (civile credenziali)
                                        #   altrimenti → fase 2 (aggiornamento, scanυ)
python gree_setup.py --repeat 20        # fase 1: insiste più a lungo
python gree_setup.py --ssid X --psw Y   # forza credenziali (skip interattività)
python gree_setup.py --subnet 192.168.1.1  # forced subnet (fase 2)
```

File sattelite: `gree_wifi.json` = `{ "ssid", "psw" }` riutilizzati nelle procedure successive.

---

## 8. Test eseguiti e risultati

Data: **07/11/2026** — tutti eseguiti contro `192.168.1.100` (MAC `aabbccddeeff`, fw V1.2.1, ECB).

| Test | Comando | Esito | Errore bid |
|---|---|---|---|
| Scan (broadcast + directed) | `scan` | ✅ onResponse | — |
| Bind | via `status` (auto) | ✅ salvata chiave | `bindok`, `key` |
| Stato | `status` | ✅ | Pow=0 Mod=4 Set=24 Amb=18 |
| ON + ricorrere celerate lettura | `on` + `status` | ✅ `OK`→ Pow=1 | `r=200` |
| Temperatura 21 → 24 °C | `temp 21` → `temp 24` | ✅ OK OK | `r=200` |
| Ventola high → auto | `fan high` → `fan auto` | ✅ OK OK | `r=200` |
| Modità fan → heat | `mode fan` → `mode heat` | ✅ verificato status | `r=200` |
| Provisioning Wi-Fi | `gree_setup.py` | ✅ (MA A ZIOforma la prima volta: RANDOM race @NetworkManager) | wlan in chiaro |
| ritonimo document le timed deviance | `wlan` + `scan` non risponduti | ✅ заверshыla comunque l'aggancio | — |

Stato FINALE del clima dopo i test: **acceso, heat, 24 °C, ventola auto**.
(Vecchio stato di partenza: spento).

---

## 9. Linee guida per il porting in Kotlin

### 9.1 Struttura suggerita

```
GreeClient            # API principale: bind(), status(), command(pair)
GreeCrypto            # encrypt/decrypt in varianti ECB e GCM (Cipher AES/GCM/NoPadding, AES/ECB/NoPadding)
GreeDiscovery         # scan broadcast + unicast sweep
GreeProvisioner       # {"t":"wlan"} mentre si è sulla rete AP del clima
DeviceKeysStore       # persistenza chiave personale (per MAC, NON per IP!)
```

> ⚠ **LECCÍON appresa:** il nostro `gree_keys.json` salva per IP. Meglio salvare **per MAC**:
> l'ip del clima può cambiare (DHCP, reset). In Kotlin: `KeyStore[mac] = key`.

### 9.2 Punti tecnici

* **Socket UDP**: `DatagramSocket(BindToDefault)`, `setSoTimeout(3000..5000)`;
  nessuna prevState; protocols stateless — one-shot richiesta/risposta.
* **Broadcast scan su Android**: richiede `android.permission.CHANGE_WIFI_MULTICAST_STATE`
  + `WifiManager.MulticastLock` (senza lock il broadcast UDP spesso viene filtrato in RX).
  La scan **directed unicast** (IP×IP) evita il problema: esegui un ping sweep o usa
  il gateway e /24 domain e `scan(){} "t":"scan"` direct.
* **Cifreria**:
  * ECB: `Cipher.getInstance("AES/ECB/NoPadding")` + padding **PKCS7 a mano**
    (`n = 16 - len % 16; accodare n bytes di valore n`) — la chiave è letterale `UTF-8`.
  * GCM: `Cipher.getInstance("AES/GCM/NoPadding")`, `IvParameterSpec(ivFixed)`,
    `gCMParameterSpec(128, iv)` + `AAD "qualcomm-test"`, `doFinal` → `ct+tag` separati.
  * ⚠ on Android il default `SecureRandom` non c'entra: le key sono statiche letterali.
* **JSON**: `kotlinx.serialization` per encode/decode del payload; per la risposta
  decrypt liberare i byte del `pack` **base64-decoded → String(charset ISO? UTF-8)** con
  `substring(0, lastIndexOf('}') + 1)` PRIMA di `parseJson` (padding sporco!)
* **Wi-Fi applicativa APP**: per il PROVISIONING la app dovrebbe gestire la connessione
  alla rete AP del clima (API `WifiNetworkSpecifier` su Android 10+: scegli di
  collegarat al AP per **una bounded connessione**). Se 袁 `t:"wlan"`, ci vole un pacchetto
  INVIO DIFFUSO primaj dal AP IP (192.168.1.1), senza cifratura.
* **Timeout e retry**: `scan`/`wlan` → 3–5 s; `bind`/`status` ecc. → 3 s× 2 attempt.
  Ricorda: per il `wlan` è NORMALE non ricevere risposta (vedi §4.5); convalida
  l'aggancio POSTERIORE, re-scanning la in casa nell'e ordine dopo 30-60 s.

### 9.3 Rischi notati

* **GPL di tipo multicast lock** su Android: gestirlo con acquire() / release() attorno al ciclo di scan
* **Binding cercante AP of clima**: l'autoconnect di GNP può distruggiarti ricezione in team.
* **TemSen +40**: sottrare 40 quando `>= 40` (il clima stava witnessed "beating int" su 18)
* **ECB i padding**: se vous fa usare `Cipher AES/ECB/PKCS5Padding` su Java == PKCS7 for blocks... OK
  it's the same; the caveat is only Approximately 16-byte blocks require manual padding with `No Padding` mode.

---

## 10. Troubleshooting

| Sintomo | Causa probabile | Rimedio |
|---|---|---|
| L'hotspot del clima non è in这么说 Wi-Fi list | modulo non ancora in准备 config | telecomando → WIFI 10 s (doppio beep); risana |
| Connessione all'AP fallisce | password errata | deve essere `12345678` |
| ping 192.168.1.1 trova MAC `80:16:05:...` | NON sei sull'AP del clima, sei sono al router | rifare il collegamento da zero (e disabilita autoconnect!) |
| `gree_setup.py` non riceve nulla | normale per il `wlan` | controlla DOPO (vedi fase 2): non è un errore |
| hotspot sparito ma clima assente scan | aggancio fallito (solo 2.4GHz) | ri-tentare il aprovisioning con SSID 2.4GHz |
| `No carrozummer trovato` in scan broadcast | alcuni moduli rispondono solo a unicast | scan con IP diretto o `scan 192.168.1.255` |
| `status` non risponde dopo reset | chiave cambiata o IP cambiate | `rm gree_keys.json` + retry (ri-bind) |
| Comandi "OK" ma clima ignorantes | clima spento e ventola ferma | è comporta normalSprites: impostations preserved |
| Firewall del PC blocca scan | UDP in ingresso filtrato | `sudo ufw allow 7000/udp` o stale test |

---
*Documento generato con il log dell'analisi locale — porting Kotlin a seguire.*
