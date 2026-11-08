# Prompt per l'agente di sviluppo — Redesign UI + multilingua app "Clima"

> Copia tutto quello che segue nella chat del tuo agente, allegando le immagini in `mockup/` (00-panoramica.png + 01, 01b, 02…05).

---

## Contesto

Stai lavorando sull'app Android **Clima Gree**: controlla in rete locale un climatizzatore Gree/EWPE Smart (Viessmann Vitoclima 232-S) tramite il protocollo UDP porta 7000 con JSON cifrato AES (ECB e GCM). La logica di protocollo (scan, bind, status, comandi) **esiste già e funziona: non riscriverla**, separala solo dalla UI se oggi è mescolata.

Oggi l'app ha una sola schermata tecnica (campo IP, pulsanti Scan/Bind/Stato, pulsanti testuali per modalità e ventola, testi italiani hard-coded, pulsanti che vanno a capo tipo "HEA T"). Va rifatta seguendo i mockup allegati e resa multilingua.

## Obiettivi

1. Rifare le schermate come nei mockup (Material 3).
2. Tradurre l'app in tutte le lingue europee elencate sotto, con selezione **automatica della lingua del telefono** e possibilità di scelta manuale per la sola app.
3. Nascondere i passaggi tecnici (bind, lettura stato) all'utente: avvengono in automatico.

## Stack

- Kotlin. Se il progetto usa già Jetpack Compose, continua con Compose + Material 3. Se usa layout XML, **migra queste schermate a Compose + Material 3** (consigliato); in alternativa mantieni XML ma con componenti `Material3` equivalenti. Prima di iniziare, dimmi quale scelta fai e perché.
- Architettura: `ViewModel` + `StateFlow` per schermata, repository per il protocollo, `DataStore` per preferenze e dispositivi salvati (nome, IP, MAC, chiave AES, tipo cifratura).
- minSdk invariato; `targetSdk` all'ultima versione stabile.

## Schermate (vedi mockup)

### 1. Controllo (`01`, `01b`, `02`, `03`)
- **Top bar**: nome del climatizzatore (rinominabile), icona "Climatizzatori" e icona "Impostazioni".
- **Chip di stato** sotto il titolo: `Connesso` / `Non raggiungibile` / `Connessione…` (pallino verde/rosso/grigio) + chip rete: `Wi-Fi di casa` o `VPN` (rileva se è attiva una VPN tramite `ConnectivityManager` / `NetworkCapabilities.TRANSPORT_VPN`).
- **Card principale** con sfondo a gradiente che cambia colore in base alla modalità:
  - Raffresca / Deumidifica / Auto / Ventila → blu (`#2E86FF → #1A4BB0`)
  - Riscalda → arancio (`#FF8A3D → #C9430C`)
  - Spento → grigio neutro, quadrante disattivato
- **Quadrante circolare** (arco 270°, 16–30 °C, tacche ogni grado) trascinabile, con temperatura impostata grande al centro, etichetta "Impostata" sopra e pill "Ambiente xx °C" sotto. Pulsanti **−** / **+** ai lati (passo 1 °C). Invio comando con **debounce 400 ms**, aggiornamento ottimistico dell'UI e rollback se il comando fallisce.
- In basso nella card: riepilogo "Modalità · Ventola" e **pulsante Accendi/Spegni** unico (toggle) bianco a pillola.
- **Modalità**: 5 riquadri in griglia con icona + etichetta (Auto, Raffresca, Deumidifica, Ventila, Riscalda); selezionato = pieno col colore accent.
- **Ventola**: controllo segmentato con "Auto" + 5 livelli a barre crescenti (mappa sui valori `WdSpd` 0–5 del protocollo).
- **Funzioni**: chip attivabili Turbo, Silenzioso, Notte, Luce, Salute, Ricambio aria (vedi mappatura del telecomando sotto). Mostra solo quelle che il dispositivo supporta: se una chiave non torna nello status, nascondi la chip.
- **Schermata scorrevole (`01b`)**: scorrendo, la card grande si riduce a una barra compatta (temperatura · modalità + pulsante Acceso/Spento) che resta fissa in alto. Sotto compaiono:
  - **Flusso d'aria**: "Alette verticali" e "Alette orizzontali". Al tap si apre un bottom sheet con: Fisse (posizione attuale), Oscillazione completa, posizioni fisse 1–5 e oscillazioni parziali, solo quelle supportate.
  - **Timer**: accensione e spegnimento con interruttore, orario e ripetizione (una volta / ogni giorno / giorni della settimana), più "Aggiungi timer".

### Mappatura telecomando → app (obbligatoria)
Il telecomando fisico (Gree, tipo YAP1F/YAN1F) ha questi tasti. L'app deve offrire tutte le funzioni che hanno senso via Wi-Fi:

| Tasto telecomando | Nell'app | Chiave protocollo Gree |
|---|---|---|
| ON/OFF | Pulsante Acceso/Spento | `Pow` (0/1) |
| MODE | Riquadri Modalità | `Mod` (0 auto, 1 cool, 2 dry, 3 fan, 4 heat) |
| − / + | Quadrante + pulsanti −/+ | `SetTem` (+ `TemUn`, `TemRec` per 0,5° / °F) |
| FAN | Ventola Auto + 5 livelli | `WdSpd` (0–5) |
| TURBO | Chip Turbo | `Tur` |
| QUIET | Chip Silenzioso | `Quiet` |
| SLEEP | Chip Notte | `SwhSlp` (+ `SlpMod`) |
| LIGHT | Chip Luce (display dell'unità interna) | `Lig` |
| Albero / casa | Chip **Salute** (ionizzatore, albero) e chip **Ricambio aria** (casa) | `Health`, `Air` |
| Alette su/giù (icona flusso verso il basso) | Alette verticali | `SwUpDn` (0–11) |
| Alette sinistra/destra (icona flusso laterale) | Alette orizzontali | `SwingLfRig` (0–6) |
| TIMER ON / TIMER OFF | Sezione Timer | vedi nota |
| TEMP | Pill "Ambiente xx °C" sul quadrante | `TemSen` (sottrarre 40 se ≥ 40) |
| CLOCK | Non serve: l'app usa l'ora del telefono | — |
| IFEEL | Non applicabile: il sensore è nel telecomando | (`ifeel` solo se l'unità lo espone, altrimenti ometti) |
| WiFi | Non applicabile: serve a resettare il modulo Wi-Fi | — |

Se l'unità lo supporta, aggiungi anche **X-Fan** (`Blo`, asciugatura della batteria dopo lo spegnimento in Raffresca/Deumidifica) come chip, visibile solo in quelle modalità.

**Nota timer**: le librerie open source del protocollo locale (greeclimate, gree-remote) non gestiscono i timer dell'unità. Prima verifica se il firmware risponde a chiavi di timer/programmazione. Se non lo fa, implementa i timer lato app con `AlarmManager` (`setExactAndAllowWhileIdle`) o `WorkManager`, che inviano il comando all'ora prevista. In questo caso spiega nell'UI che il telefono dev'essere connesso alla rete di casa o alla VPN in quel momento.
- Footer: "Aggiornato 5 s fa" (tempo relativo localizzato).
- Polling dello stato ogni 5 s solo con app in primo piano; pull-to-refresh.
- Quando il climatizzatore è spento, modalità/ventola/funzioni restano modificabili ma visivamente attenuate.

### 2. Climatizzatori (`04`)
- Sezione **Salvati**: lista con nome, IP, stato sintetico, pallino raggiungibilità; tap → apre Controllo; long-press → rinomina/elimina.
- Sezione **Trovati in rete**: risultati dello scan broadcast non ancora salvati, pulsante "Aggiungi" (fa bind automatico e salva la chiave).
- **Aggiungi manualmente**: campo IP con validazione.
- Nota informativa sulla VPN (la ricerca automatica non passa via VPN; usare IP fisso).
- Pulsante principale in basso "Cerca in rete" con stato di caricamento.
- Al primo avvio senza dispositivi salvati, apri direttamente questa schermata.

### 3. Impostazioni (`05`)
- **Lingua** → bottom sheet con: "Predefinita di sistema (lingua attuale)" in cima e selezionata di default, separatore, poi tutte le lingue **ordinate alfabeticamente per nome nativo** (endonimo, es. "Deutsch") con il nome tradotto nella lingua corrente a destra (es. "Tedesco"), e campo di ricerca.
- **Unità**: °C / °F (conversione solo lato UI; al dispositivo si invia sempre °C, `TemUn=0`).
- **Aggiornamento automatico** on/off.
- Tema: Sistema / Chiaro / Scuro (prevedi la palette scura anche se i mockup sono in chiaro).
- Info: versione app.

## Multilingua

### Lingue (31)
Inglese come lingua di base (`values/`), più:

| Codice | Lingua | Codice | Lingua |
|---|---|---|---|
| `it` | Italiano | `lt` | Lietuvių |
| `fr` | Français | `lv` | Latviešu |
| `de` | Deutsch | `et` | Eesti |
| `es` | Español | `ga` | Gaeilge |
| `pt` | Português | `mt` | Malti |
| `nl` | Nederlands | `hr` | Hrvatski |
| `da` | Dansk | `sl` | Slovenščina |
| `sv` | Svenska | `sr` | Српски |
| `fi` | Suomi | `bs` | Bosanski |
| `nb` | Norsk bokmål | `sq` | Shqip |
| `is` | Íslenska | `mk` | Македонски |
| `pl` | Polski | `bg` | Български |
| `cs` | Čeština | `el` | Ελληνικά |
| `sk` | Slovenčina | `uk` | Українська |
| `hu` | Magyar | `ca` | Català |
| `ro` | Română | | |

(Tutte le 24 lingue ufficiali UE + norvegese, islandese, catalano, ucraino, serbo, bosniaco, albanese, macedone.)

### Requisiti tecnici
- **Nessuna stringa hard-coded**: tutto in `res/values*/strings.xml`, inclusi messaggi di errore, content description delle icone e testi dei Toast/Snackbar.
- Usa `plurals` dove serve (es. secondi/minuti) oppure `DateUtils.getRelativeTimeSpanString` per "Aggiornato X fa".
- Numeri e temperature formattati con il `Locale` corrente (`26,5 °C` in italiano/tedesco, `26.5 °C` in inglese).
- **Lingua automatica**: Android sceglie da solo la cartella `values-xx` in base alla lingua del telefono; fallback all'inglese.
- **Lingua per-app**:
  - `androidResources { generateLocaleConfig = true }` in `build.gradle.kts` + `res/resources.properties` con `unqualifiedResLocale=en`.
  - Cambio lingua con `AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("de"))`; "Predefinita di sistema" = `LocaleListCompat.getEmptyLocaleList()`.
  - Per Android < 13 aggiungi nel manifest il service `androidx.appcompat.app.AppLocalesMetadataHolderService` con `autoStoreLocales=true`. L'activity deve estendere `AppCompatActivity`.
  - Su Android 13+ la lingua deve comparire anche in *Impostazioni di sistema → App → Clima → Lingua*.
- **Layout robusti per lingue lunghe**: niente testo spezzato a metà parola (il bug "HEA T" di oggi). Etichette su max 2 righe con `maxLines=2`, ellissi solo come ultima risorsa, larghezze flessibili (`weight`/`Modifier.weight`) e `autoSize` dove serve. Verifica con tedesco, finlandese e ungherese, che sono le lingue più lunghe.
- Supporta anche le pseudo-locale `en-XA` (attiva `pseudoLocalesEnabled = true` nel build type debug) per testare troncamenti.
- Traduzioni: generale tu, con un tono breve da app (imperativi o sostantivi, niente frasi lunghe). Per i termini di dominio usa quelli dei telecomandi dei climatizzatori nella lingua (es. DE: Kühlen / Heizen / Entfeuchten / Lüften; FR: Froid / Chaud / Déshumidification / Ventilation). Aggiungi un file `TRANSLATIONS.md` con le stringhe non sicure, da far rivedere a un madrelingua.

### Stringhe di riferimento (chiavi suggerite)
| key | en | it | de |
|---|---|---|---|
| `status_connected` | Connected | Connesso | Verbunden |
| `status_unreachable` | Unreachable | Non raggiungibile | Nicht erreichbar |
| `network_home_wifi` | Home Wi-Fi | Wi-Fi di casa | WLAN |
| `network_vpn` | VPN | VPN | VPN |
| `label_target` | Target | Impostata | Soll |
| `label_room` | Room | Ambiente | Raum |
| `action_power_on` / `off` | On / Off | Acceso / Spento | An / Aus |
| `section_mode` | Mode | Modalità | Betriebsart |
| `mode_auto` | Auto | Auto | Auto |
| `mode_cool` | Cool | Raffresca | Kühlen |
| `mode_dry` | Dry | Deumidifica | Entfeuchten |
| `mode_fan` | Fan | Ventila | Lüften |
| `mode_heat` | Heat | Riscalda | Heizen |
| `section_fan` | Fan | Ventola | Lüfter |
| `section_features` | Features | Funzioni | Funktionen |
| `feature_turbo` | Turbo | Turbo | Turbo |
| `feature_quiet` | Quiet | Silenzioso | Leise |
| `feature_sleep` | Sleep | Notte | Nacht |
| `feature_light` | Light | Luce | Licht |
| `feature_health` | Health | Salute | Gesundheit |
| `feature_fresh_air` | Fresh air | Ricambio aria | Frischluft |
| `airflow_vertical` | Vertical louvres | Alette verticali | Vertikale Lamellen |
| `airflow_horizontal` | Horizontal louvres | Alette orizzontali | Horizontale Lamellen |
| `section_timer` | Timer | Timer | Timer |
| `timer_on` / `timer_off` | Turn on / Turn off | Accensione / Spegnimento | Einschalten / Ausschalten |
| `screen_devices` | Air conditioners | Climatizzatori | Klimageräte |
| `action_scan` | Search network | Cerca in rete | Netzwerk durchsuchen |
| `settings_language` | Language | Lingua | Sprache |
| `language_system_default` | System default | Predefinita di sistema | Systemstandard |

## Design system

- Font: Inter (o il default di sistema se preferisci non includere font).
- Sfondo `#F6F7FB`, superfici `#ECEEF4`, bordi `#D9DDE7`, testo `#14171F` / secondario `#5B6375`.
- Accent: blu `#1F6FEB` (soft `#DCE9FF`), arancio `#E8590C` (soft `#FFE6D5`) quando la modalità è Riscalda.
- Raggi: card 28 dp, riquadri modalità 18 dp, chip 12 dp, pulsanti a pillola.
- Target touch minimo 48 dp; contrasto AA; tutte le icone con `contentDescription` tradotta; supporto TalkBack sul quadrante (annuncia "Temperatura impostata 24 gradi", azioni incrementa/decrementa).
- Supporto tema scuro e dynamic color disattivato (i colori della modalità devono restare riconoscibili).

## Criteri di accettazione

- [ ] Le 3 schermate corrispondono ai mockup.
- [ ] Ogni tasto del telecomando nella tabella di mappatura ha il suo equivalente nell'app (o è escluso per il motivo indicato).
- [ ] Bind e lettura dello stato avvengono automaticamente; nessun pulsante "Bind" o "Stato" visibile.
- [ ] Cambiando la lingua del telefono, l'app si mostra nella nuova lingua senza reinstallazione; lingua non supportata → inglese.
- [ ] La scelta manuale della lingua funziona su Android 12 e 13+ e sopravvive al riavvio.
- [ ] Nessun testo spezzato o tagliato in DE, FI, HU, EL ed `en-XA` su schermo da 360 dp di larghezza.
- [ ] Lint `MissingTranslation` e `HardcodedText` a zero.
- [ ] Il protocollo esistente non è stato modificato nel comportamento (testa scan, on/off, temperatura, modalità, ventola su un dispositivo reale o sul simulatore).

## Modo di lavorare

1. Analizza il progetto e proponimi un piano breve (file da toccare, scelta Compose/XML) prima di scrivere codice.
2. Procedi a step con commit separati: (a) estrazione stringhe + base i18n, (b) design system/tema, (c) schermata Controllo, (d) Climatizzatori, (e) Impostazioni + selettore lingua, (f) traduzioni, (g) test e screenshot.
3. Alla fine fornisci gli screenshot delle 3 schermate in IT, EN e DE.
