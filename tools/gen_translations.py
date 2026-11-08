#!/usr/bin/env python3
"""Genera values-xx/strings.xml per tutte le 31 lingue europee.
Tutte le chiavi del values/ sono presenti in ogni cartella (fallback inglese
per le lingue senza traduzione certa) -> Lint MissingTranslation a zero.
"""
import xml.etree.ElementTree as ET
import os
import html

ROOT = "android-app/src/main/res"

tree = ET.parse(os.path.join(ROOT, "values/strings.xml"))
root = tree.getroot()

entries = []
for child in root:
    if child.tag == "string":
        entries.append((child.attrib.get("name"), child.text or ""))
    elif child.tag == "comment":
        entries.append(("__comment__", child.text.strip()))

# Dizionario traduzioni certe (chiave -> testo) per le lingue principali.
# Le lingue non elencate ricevono tutte le chiavi in inglese (fallback esplicito).
TR = {}

def add(code, d):
    TR.setdefault(code, {}).update(d)

# ---------------- ITALIANO ----------------
add("it", {
 "status_connected":"Connesso","status_connecting":"Connessione...","status_unreachable":"Non raggiungibile",
 "network_home_wifi":"Wi-Fi di casa","network_vpn":"VPN",
 "label_target":"Impostata","label_room":"Ambiente",
 "action_power_on":"Acceso","action_power_off":"Spento","power_toggle_on":"Accendi","power_toggle_off":"Spegni",
 "device_off_hint":"Unità spenta: modalità, ventola e funzioni restano modificabili ma attenuate.",
 "section_mode":"Modalità","mode_auto":"Auto","mode_cool":"Raffresca","mode_dry":"Deumidifica","mode_fan":"Ventila","mode_heat":"Riscalda",
 "section_fan":"Ventola","fan_auto":"Auto","fan_level_1":"Livello 1","fan_level_2":"Livello 2","fan_level_3":"Livello 3","fan_level_4":"Livello 4","fan_level_5":"Livello 5",
 "section_features":"Funzioni","feature_turbo":"Turbo","feature_quiet":"Silenzioso","feature_sleep":"Notte","feature_light":"Luce","feature_health":"Salute","feature_fresh_air":"Ricambio aria","feature_x_fan":"X-Fan",
 "section_airflow":"Flusso d'aria","airflow_vertical":"Alette verticali","airflow_horizontal":"Alette orizzontali",
 "airflow_cd_vertical":"Cambia la direzione verticale del flusso d'aria","airflow_cd_horizontal":"Cambia la direzione orizzontale del flusso d'aria",
 "louvre_fixed":"Fissa (posizione attuale)","louvre_swing_full":"Oscillazione completa","louvre_position":"Posizione %1$d","louvre_swing_partial":"Oscillazione parziale %1$d",
 "section_timer":"Timer","timer_on":"Accensione","timer_off":"Spegnimento","timer_once":"Una volta","timer_daily":"Ogni giorno","timer_weekdays":"Giorni feriali","timer_custom_days":"Giorni personalizzati",
 "timer_add":"Aggiungi timer","timer_delete":"Elimina timer","timer_note":"Il telefono dev'essere connesso alla rete di casa o alla VPN all'ora prevista.",
 "timer_daily_at":"Ogni giorno alle %1$s","timer_once_at":"Una volta alle %1$s","timer_time":"Orario","timer_repeat":"Ripeti","timer_save":"Salva timer",
 "updated_now":"Aggiornato ora","refresh":"Aggiorna",
 "day_mon_short":"Lun","day_tue_short":"Mar","day_wed_short":"Mer","day_thu_short":"Gio","day_fri_short":"Ven","day_sat_short":"Sab","day_sun_short":"Dom",
 "screen_devices":"Climatizzatori","section_saved":"Salvati","section_found":"Trovati in rete","action_add":"Aggiungi","action_add_manual":"Aggiungi manualmente",
 "field_ip":"Indirizzo IP","field_ip_hint":"es. 192.168.47.30","invalid_ip":"IP non valido",
 "device_state_on":"%1$s · Acceso","device_state_off":"%1$s · Spento","device_unreachable":"Non raggiungibile","device_rename":"Rinomina","device_delete":"Elimina","device_name":"Nome","new_device":"Nuovo",
 "note_vpn":"La ricerca automatica non passa tramite la VPN: il router blocca la scansione di rete. Le unità salvate restano raggiungibili tramite IP. Si consiglia un IP fisso (prenotazione DHCP).",
 "action_scan":"Cerca in rete","scan_running":"Ricerca in rete...","scan_empty":"Nessuna unità trovata","scan_failed":"Ricerca fallita","bind_failed":"L'unità non ha accettato la connessione",
 "command_failed":"Comando non consegnato","error_generic":"Si è verificato un errore",
 "screen_settings":"Impostazioni","settings_language":"Lingua","settings_language_current":"%1$s (%2$s)","language_system_default":"Predefinita di sistema",
 "language_description":"L'app segue la lingua del telefono. Puoi scegliere un'altra lingua solo per questa app.","search_language":"Cerca lingua","settings_units":"Unità",
 "unit_celsius":"°C","unit_fahrenheit":"°F","settings_auto_refresh":"Aggiornamento automatico","settings_auto_refresh_desc":"Aggiorna lo stato ogni 5 secondi mentre l'app è aperta",
 "settings_theme":"Tema","theme_system":"Sistema","theme_light":"Chiaro","theme_dark":"Scuro","about_version":"Versione %1$s",
 "cd_back":"Indietro","cd_settings":"Impostazioni","cd_devices":"Climatizzatori","cd_refresh_scan":"Cerca unità in rete",
 "cd_temperature_decrease":"Abbassa temperatura impostata","cd_temperature_increase":"Alza temperatura impostata","cd_dial":"%1$s",
 "a11y_temperature":"%1$s gradi temperatura impostata","a11y_room_temperature":"Temperatura ambiente %1$s gradi","mode_cd":"Modalità %1$s","fan_cd":"Ventola: %1$s","chip_on":"%1$s attivo",
 "action_cancel":"Annulla","action_save":"Salva","action_close":"Chiudi",
 "temp_celsius":"%1$s °C","temp_fahrenheit":"%1$s °F","timer_not_set":"Non impostato",
})

# ---------------- TEDESCO ----------------
add("de", {
 "status_connected":"Verbunden","status_connecting":"Verbindung...","status_unreachable":"Nicht erreichbar",
 "network_home_wifi":"WLAN","network_vpn":"VPN",
 "label_target":"Soll","label_room":"Raum",
 "action_power_on":"An","action_power_off":"Aus","power_toggle_on":"Einschalten","power_toggle_off":"Ausschalten",
 "device_off_hint":"Gerät aus: Modus, Lüfter und Funktionen bleiben einstellbar, aber ausgegraut.",
 "section_mode":"Betriebsart","mode_auto":"Auto","mode_cool":"Kühlen","mode_dry":"Entfeuchten","mode_fan":"Lüften","mode_heat":"Heizen",
 "section_fan":"Lüfter","fan_auto":"Auto","fan_level_1":"Stufe 1","fan_level_2":"Stufe 2","fan_level_3":"Stufe 3","fan_level_4":"Stufe 4","fan_level_5":"Stufe 5",
 "section_features":"Funktionen","feature_turbo":"Turbo","feature_quiet":"Leise","feature_sleep":"Nacht","feature_light":"Licht","feature_health":"Gesundheit","feature_fresh_air":"Frischluft","feature_x_fan":"X-Fan",
 "section_airflow":"Luftstrom","airflow_vertical":"Vertikale Lamellen","airflow_horizontal":"Horizontale Lamellen",
 "airflow_cd_vertical":"Vertikale Luftflussrichtung ändern","airflow_cd_horizontal":"Horizontale Luftflussrichtung ändern",
 "louvre_fixed":"Fest (aktuelle Position)","louvre_swing_full":"Volle Schwingung","louvre_position":"Position %1$d","louvre_swing_partial":"Teilschwung %1$d",
 "section_timer":"Timer","timer_on":"Einschalten","timer_off":"Ausschalten","timer_once":"Einmalig","timer_daily":"Täglich","timer_weekdays":"Werktage","timer_custom_days":"Benutzerdefinierte Tage",
 "timer_add":"Timer hinzufügen","timer_delete":"Timer löschen","timer_note":"Das Telefon muss zum Zeitpunkt zum Heimnetzwerk oder zur VPN verbunden sein.",
 "timer_daily_at":"Täglich um %1$s","timer_once_at":"Einmal um %1$s","timer_time":"Uhrzeit","timer_repeat":"Wiederholen","timer_save":"Timer speichern",
 "updated_now":"Gerade aktualisiert","refresh":"Aktualisieren",
 "day_mon_short":"Mo","day_tue_short":"Di","day_wed_short":"Mi","day_thu_short":"Do","day_fri_short":"Fr","day_sat_short":"Sa","day_sun_short":"So",
 "screen_devices":"Klimageräte","section_saved":"Gespeichert","section_found":"Im Netzwerk gefunden","action_add":"Hinzufügen","action_add_manual":"Manuell hinzufügen",
 "field_ip":"IP-Adresse","field_ip_hint":"z. B. 192.168.47.30","invalid_ip":"Ungültige IP-Adresse",
 "device_state_on":"%1$s · An","device_state_off":"%1$s · Aus","device_unreachable":"Nicht erreichbar","device_rename":"Umbenennen","device_delete":"Löschen","device_name":"Name","new_device":"Neu",
 "note_vpn":"Die automatische Suche funktioniert nicht über VPN; der Router blockiert die Netzwerksuche. Gespeicherte Geräte bleiben über ihre IP erreichbar. Eine feste IP (DHCP-Reservierung) wird empfohlen.",
 "action_scan":"Netzwerk durchsuchen","scan_running":"Suche im Netzwerk...","scan_empty":"Kein Gerät gefunden","scan_failed":"Netzwerksuche fehlgeschlagen","bind_failed":"Das Gerät hat die Verbindung nicht akzeptiert",
 "command_failed":"Befehl nicht zugestellt","error_generic":"Etwas ist schiefgelaufen",
 "screen_settings":"Einstellungen","settings_language":"Sprache","settings_language_current":"%1$s (%2$s)","language_system_default":"Systemstandard",
 "language_description":"Die App folgt der Systemsprache. Sie können eine andere Sprache nur für diese App wählen.","search_language":"Sprache suchen","settings_units":"Einheit",
 "unit_celsius":"°C","unit_fahrenheit":"°F","settings_auto_refresh":"Automatische Aktualisierung","settings_auto_refresh_desc":"Alle 5 Sekunden aktualisieren, während die App geöffnet ist",
 "settings_theme":"Design","theme_system":"System","theme_light":"Hell","theme_dark":"Dunkel","about_version":"Version %1$s",
 "cd_back":"Zurück","cd_settings":"Einstellungen","cd_devices":"Klimageräte","cd_refresh_scan":"Geräte im Netzwerk suchen",
 "cd_temperature_decrease":"Zieltemperatur verringern","cd_temperature_increase":"Zieltemperatur erhöhen","cd_dial":"%1$s",
 "a11y_temperature":"%1$s Grad Zieltemperatur","a11y_room_temperature":"Raumtemperatur %1$s Grad","mode_cd":"Modus %1$s","fan_cd":"Lüfter: %1$s","chip_on":"%1$s aktiv",
 "action_cancel":"Abbrechen","action_save":"Speichern","action_close":"Schließen",
 "temp_celsius":"%1$s °C","temp_fahrenheit":"%1$s °F","timer_not_set":"Nicht eingestellt",
})

# ---------------- FRANCESE ----------------
add("fr", {
 "status_connected":"Connecté","status_connecting":"Connexion...","status_unreachable":"Injoignable",
 "network_home_wifi":"Wi-Fi maison","network_vpn":"VPN",
 "label_target":"Consigne","label_room":"Pièce",
 "action_power_on":"Marche","action_power_off":"Arrêt","power_toggle_on":"Allumer","power_toggle_off":"Éteindre",
 "device_off_hint":"Unité éteinte : mode, ventilateur et fonctions restent réglables mais atténués.",
 "section_mode":"Mode","mode_auto":"Auto","mode_cool":"Froid","mode_dry":"Déshumidification","mode_fan":"Ventilation","mode_heat":"Chaud",
 "section_fan":"Ventilateur","fan_auto":"Auto","fan_level_1":"Niveau 1","fan_level_2":"Niveau 2","fan_level_3":"Niveau 3","fan_level_4":"Niveau 4","fan_level_5":"Niveau 5",
 "section_features":"Fonctions","feature_turbo":"Turbo","feature_quiet":"Silencieux","feature_sleep":"Nuit","feature_light":"Lumière","feature_health":"Santé","feature_fresh_air":"Air frais","feature_x_fan":"X-Fan",
 "section_airflow":"Flux d'air","airflow_vertical":"Vannes verticales","airflow_horizontal":"Vannes horizontales",
 "airflow_cd_vertical":"Modifier la direction verticale du flux d'air","airflow_cd_horizontal":"Modifier la direction horizontale du flux d'air",
 "louvre_fixed":"Fixe (position actuelle)","louvre_swing_full":"Oscillation complète","louvre_position":"Position %1$d","louvre_swing_partial":"Oscillation partielle %1$d",
 "section_timer":"Minuterie","timer_on":"Mise sous tension","timer_off":"Mise hors tension","timer_once":"Une fois","timer_daily":"Tous les jours","timer_weekdays":"Jours ouvrés","timer_custom_days":"Jours personnalisés",
 "timer_add":"Ajouter une minuterie","timer_delete":"Supprimer la minuterie","timer_note":"Le téléphone doit être connecté au réseau local ou au VPN à l'heure prévue.",
 "timer_daily_at":"Tous les jours à %1$s","timer_once_at":"Une fois à %1$s","timer_time":"Heure","timer_repeat":"Répéter","timer_save":"Enregistrer la minuterie",
 "updated_now":"Mis à jour à l'instant","refresh":"Actualiser",
 "day_mon_short":"Lun","day_tue_short":"Mar","day_wed_short":"Mer","day_thu_short":"Jeu","day_fri_short":"Ven","day_sat_short":"Sam","day_sun_short":"Dim",
 "screen_devices":"Climatiseurs","section_saved":"Enregistrés","section_found":"Trouvés sur le réseau","action_add":"Ajouter","action_add_manual":"Ajouter manuellement",
 "field_ip":"Adresse IP","field_ip_hint":"p. ex. 192.168.47.30","invalid_ip":"IP invalide",
 "device_state_on":"%1$s · Marche","device_state_off":"%1$s · Arrêt","device_unreachable":"Injoignable","device_rename":"Renommer","device_delete":"Supprimer","device_name":"Nom","new_device":"Nouveau",
 "note_vpn":"La recherche automatique ne passe pas par le VPN ; le router bloque la recherche réseau. Les unités enregistrées restent joignables via leur IP. Une IP fixe (réservation DHCP) est recommandée.",
 "action_scan":"Rechercher le réseau","scan_running":"Recherche sur le réseau...","scan_empty":"Aucune unité trouvée","scan_failed":"Échec de la recherche réseau","bind_failed":"L'unité n'a pas accepté la connexion",
 "command_failed":"Commande non délivrée","error_generic":"Une erreur est survenue",
 "screen_settings":"Réglages","settings_language":"Langue","settings_language_current":"%1$s (%2$s)","language_system_default":"Par défaut du système",
 "language_description":"L'application suit la langue du téléphone. Vous pouvez choisir une autre langue uniquement pour cette application.","search_language":"Rechercher une langue","settings_units":"Unités",
 "unit_celsius":"°C","unit_fahrenheit":"°F","settings_auto_refresh":"Actualisation automatique","settings_auto_refresh_desc":"Actualiser le statut toutes les 5 secondes quand l'app est ouverte",
 "settings_theme":"Thème","theme_system":"Système","theme_light":"Clair","theme_dark":"Sombre","about_version":"Version %1$s",
 "cd_back":"Retour","cd_settings":"Réglages","cd_devices":"Climatiseurs","cd_refresh_scan":"Chercher des unités sur le réseau",
 "cd_temperature_decrease":"Diminuer la température cible","cd_temperature_increase":"Augmenter la température cible","cd_dial":"%1$s",
 "a11y_temperature":"%1$s degrés température cible","a11y_room_temperature":"Température ambiante %1$s degrés","mode_cd":"Mode %1$s","fan_cd":"Ventilateur : %1$s","chip_on":"%1$s actif",
 "action_cancel":"Annuler","action_save":"Enregistrer","action_close":"Fermer",
 "temp_celsius":"%1$s °C","temp_fahrenheit":"%1$s °F","timer_not_set":"Non défini",
})

# ---------------- SPAGNOLO ----------------
add("es", {
 "status_connected":"Conectado","status_connecting":"Conectando...","status_unreachable":"No alcanzable",
 "network_home_wifi":"Wi-Fi de casa","network_vpn":"VPN",
 "label_target":"Objetivo","label_room":"Ambiente",
 "action_power_on":"Encendido","action_power_off":"Apagado","power_toggle_on":"Encender","power_toggle_off":"Apagar",
 "device_off_hint":"Unidad apagada: modo, ventilador y funciones siguen siendo ajustables, pero atenuados.",
 "section_mode":"Modo","mode_auto":"Auto","mode_cool":"Frío","mode_dry":"Deshumidificar","mode_fan":"Ventilar","mode_heat":"Calor",
 "section_fan":"Ventilador","fan_auto":"Auto","fan_level_1":"Nivel 1","fan_level_2":"Nivel 2","fan_level_3":"Nivel 3","fan_level_4":"Nivel 4","fan_level_5":"Nivel 5",
 "section_features":"Funciones","feature_turbo":"Turbo","feature_quiet":"Silencioso","feature_sleep":"Noche","feature_light":"Luz","feature_health":"Salud","feature_fresh_air":"Aire fresco","feature_x_fan":"X-Fan",
 "section_airflow":"Flujo de aire","airflow_vertical":"Aletas verticales","airflow_horizontal":"Aletas horizontales",
 "airflow_cd_vertical":"Cambiar la dirección vertical del flujo de aire","airflow_cd_horizontal":"Cambiar la dirección horizontal del flujo de aire",
 "louvre_fixed":"Fijo (posición actual)","louvre_swing_full":"Oscilación completa","louvre_position":"Posición %1$d","louvre_swing_partial":"Oscilación parcial %1$d",
 "section_timer":"Temporizador","timer_on":"Encendido","timer_off":"Apagado","timer_once":"Una vez","timer_daily":"Todos los días","timer_weekdays":"Días laborables","timer_custom_days":"Días personalizados",
 "timer_add":"Añadir temporizador","timer_delete":"Eliminar temporizador","timer_note":"El teléfono debe estar conectado a la red local o a la VPN a la hora prevista.",
 "timer_daily_at":"Todos los días a las %1$s","timer_once_at":"Una vez a las %1$s","timer_time":"Hora","timer_repeat":"Repetir","timer_save":"Guardar temporizador",
 "updated_now":"Actualizado justo ahora","refresh":"Actualizar",
 "day_mon_short":"Lun","day_tue_short":"Mar","day_wed_short":"Mié","day_thu_short":"Jue","day_fri_short":"Vie","day_sat_short":"Sáb","day_sun_short":"Dom",
 "screen_devices":"Aire acondicionado","section_saved":"Guardados","section_found":"Encontrados en red","action_add":"Añadir","action_add_manual":"Añadir manualmente",
 "field_ip":"Dirección IP","field_ip_hint":"ej. 192.168.47.30","invalid_ip":"IP inválida",
 "device_state_on":"%1$s · Encendido","device_state_off":"%1$s · Apagado","device_unreachable":"No alcanzable","device_rename":"Renombrar","device_delete":"Eliminar","device_name":"Nombre","new_device":"Nuevo",
 "note_vpn":"La búsqueda automática no pasa por la VPN; el router bloquea la búsqueda en red. Las unidades guardadas siguen siendo alcanzables mediante su IP. Se recomienda una IP fija (reserva DHCP).",
 "action_scan":"Buscar en red","scan_running":"Buscando en la red...","scan_empty":"No se encontró ninguna unidad","scan_failed":"Error al buscar en la red","bind_failed":"La unidad no aceptó la conexión",
 "command_failed":"Comando no entregado","error_generic":"Algo salió mal",
 "screen_settings":"Ajustes","settings_language":"Idioma","settings_language_current":"%1$s (%2$s)","language_system_default":"Predeterminado del sistema",
 "language_description":"La aplicación sigue el idioma del teléfono. Puedes elegir otro idioma solo para esta aplicación.","search_language":"Buscar idioma","settings_units":"Unidades",
 "unit_celsius":"°C","unit_fahrenheit":"°F","settings_auto_refresh":"Actualización automática","settings_auto_refresh_desc":"Actualizar el estado cada 5 segundos mientras la app está abierta",
 "settings_theme":"Tema","theme_system":"Sistema","theme_light":"Claro","theme_dark":"Oscuro","about_version":"Versión %1$s",
 "cd_back":"Atrás","cd_settings":"Ajustes","cd_devices":"Aire acondicionado","cd_refresh_scan":"Buscar unidades en la red",
 "cd_temperature_decrease":"Disminuir temperatura objetivo","cd_temperature_increase":"Aumentar temperatura objetivo","cd_dial":"%1$s",
 "a11y_temperature":"%1$s grados temperatura objetivo","a11y_room_temperature":"Temperatura ambiente %1$s grados","mode_cd":"Modo %1$s","fan_cd":"Ventilador: %1$s","chip_on":"%1$s activo",
 "action_cancel":"Cancelar","action_save":"Guardar","action_close":"Cerrar",
 "temp_celsius":"%1$s °C","temp_fahrenheit":"%1$s °F","timer_not_set":"No configurado",
})

# ---------------- PORTOGHESE ----------------
add("pt", {
 "status_connected":"Conectado","status_connecting":"Conectando...","status_unreachable":"Inalcançável",
 "network_home_wifi":"Wi-Fi de casa","network_vpn":"VPN",
 "label_target":"Alvo","label_room":"Ambiente",
 "action_power_on":"Ligado","action_power_off":"Desligado","power_toggle_on":"Ligar","power_toggle_off":"Desligar",
 "device_off_hint":"Unidade desligada: modo, ventilador e funções permanecem ajustáveis, mas atenuados.",
 "section_mode":"Modo","mode_auto":"Auto","mode_cool":"Frio","mode_dry":"Desumidificar","mode_fan":"Ventilar","mode_heat":"Calor",
 "section_fan":"Ventilador","fan_auto":"Auto","fan_level_1":"Nível 1","fan_level_2":"Nível 2","fan_level_3":"Nível 3","fan_level_4":"Nível 4","fan_level_5":"Nível 5",
 "section_features":"Funções","feature_turbo":"Turbo","feature_quiet":"Silencioso","feature_sleep":"Sono","feature_light":"Luz","feature_health":"Saúde","feature_fresh_air":"Ar fresco","feature_x_fan":"X-Fan",
 "section_airflow":"Fluxo de ar","airflow_vertical":"Aletas verticais","airflow_horizontal":"Aletas horizontais",
 "airflow_cd_vertical":"Alterar a direção vertical do fluxo de ar","airflow_cd_horizontal":"Alterar a direção horizontal do fluxo de ar",
 "louvre_fixed":"Fixo (posição atual)","louvre_swing_full":"Oscilação completa","louvre_position":"Posição %1$d","louvre_swing_partial":"Oscilação parcial %1$d",
 "section_timer":"Timer","timer_on":"Ligar","timer_off":"Desligar","timer_once":"Uma vez","timer_daily":"Todos os dias","timer_weekdays":"Dias úteis","timer_custom_days":"Dias personalizados",
 "timer_add":"Adicionar timer","timer_delete":"Eliminar timer","timer_note":"O telefone deve estar conectado à rede local ou à VPN no horário previsto.",
 "timer_daily_at":"Todos os dias às %1$s","timer_once_at":"Uma vez às %1$s","timer_time":"Hora","timer_repeat":"Repetir","timer_save":"Salvar timer",
 "updated_now":"Atualizado agora","refresh":"Atualizar",
 "day_mon_short":"Seg","day_tue_short":"Ter","day_wed_short":"Qua","day_thu_short":"Qui","day_fri_short":"Sex","day_sat_short":"Sáb","day_sun_short":"Dom",
 "screen_devices":"Ar-condicionado","section_saved":"Salvos","section_found":"Encontrados na rede","action_add":"Adicionar","action_add_manual":"Adicionar manualmente",
 "field_ip":"Endereço IP","field_ip_hint":"ex. 192.168.47.30","invalid_ip":"IP inválido",
 "device_state_on":"%1$s · Ligado","device_state_off":"%1$s · Desligado","device_unreachable":"Inalcançável","device_rename":"Renomear","device_delete":"Excluir","device_name":"Nome","new_device":"Novo",
 "note_vpn":"A busca automática não passa pela VPN; o roteador bloqueia a busca na rede. As unidades salvas permanecem alcançáveis via IP. É recomendado um IP fixo (reserva DHCP).",
 "action_scan":"Buscar na rede","scan_running":"Buscando na rede...","scan_empty":"Nenhuma unidade encontrada","scan_failed":"Falha ao buscar na rede","bind_failed":"A unidade não aceitou a conexão",
 "command_failed":"Comando não entregue","error_generic":"Algo deu errado",
 "screen_settings":"Ajustes","settings_language":"Idioma","settings_language_current":"%1$s (%2$s)","language_system_default":"Padrão do sistema",
 "language_description":"O aplicativo segue o idioma do telefone. Você pode escolher outro idioma apenas para este aplicativo.","search_language":"Buscar idioma","settings_units":"Unidades",
 "unit_celsius":"°C","unit_fahrenheit":"°F","settings_auto_refresh":"Atualização automática","settings_auto_refresh_desc":"Atualizar o status a cada 5 segundos enquanto o app está aberto",
 "settings_theme":"Tema","theme_system":"Sistema","theme_light":"Claro","theme_dark":"Escuro","about_version":"Versão %1$s",
 "cd_back":"Voltar","cd_settings":"Ajustes","cd_devices":"Ar-condicionado","cd_refresh_scan":"Buscar unidades na rede",
 "cd_temperature_decrease":"Diminuir temperatura alvo","cd_temperature_increase":"Aumentar temperatura alvo","cd_dial":"%1$s",
 "a11y_temperature":"%1$s graus temperatura alvo","a11y_room_temperature":"Temperatura ambiente %1$s graus","mode_cd":"Modo %1$s","fan_cd":"Ventilador: %1$s","chip_on":"%1$s ativo",
 "action_cancel":"Cancelar","action_save":"Salvar","action_close":"Fechar",
 "temp_celsius":"%1$s °C","temp_fahrenheit":"%1$s °F","timer_not_set":"Não configurado",
})

# ---------------- OLANDESE ----------------
add("nl", {
 "status_connected":"Verbonden","status_connecting":"Verbinden...","status_unreachable":"Niet bereikbaar",
 "network_home_wifi":"Thuis-Wi-Fi","network_vpn":"VPN",
 "label_target":"Doel","label_room":"Kamer",
 "action_power_on":"Aan","action_power_off":"Uit","power_toggle_on":"Aanzetten","power_toggle_off":"Uitschakelen",
 "device_off_hint":"Apparaat uit: modus, ventilator en functies blijven instelbaar, maar gedimd.",
 "section_mode":"Modus","mode_auto":"Auto","mode_cool":"Koelen","mode_dry":"Ontvochtigen","mode_fan":"Ventileren","mode_heat":"Verwarmen",
 "section_fan":"Ventilator","fan_auto":"Auto","fan_level_1":"Niveau 1","fan_level_2":"Niveau 2","fan_level_3":"Niveau 3","fan_level_4":"Niveau 4","fan_level_5":"Niveau 5",
 "section_features":"Functies","feature_turbo":"Turbo","feature_quiet":"Stil","feature_sleep":"Nacht","feature_light":"Licht","feature_health":"Gezondheid","feature_fresh_air":"Verse lucht","feature_x_fan":"X-Fan",
 "section_airflow":"Luchtstroom","airflow_vertical":"Verticale lamellen","airflow_horizontal":"Horizontale lamellen",
 "airflow_cd_vertical":"Verticale richting luchtstroom wijzigen","airflow_cd_horizontal":"Horizontale richting luchtstroom wijzigen",
 "louvre_fixed":"Vast (huidige positie)","louvre_swing_full":"Volledige oscillatie","louvre_position":"Positie %1$d","louvre_swing_partial":"Gedeeltelijke oscillatie %1$d",
 "section_timer":"Timer","timer_on":"Aanzetten","timer_off":"Uitschakelen","timer_once":"Een keer","timer_daily":"Elke dag","timer_weekdays":"Weekdagen","timer_custom_days":"Aangepaste dagen",
 "timer_add":"Timer toevoegen","timer_delete":"Timer verwijderen","timer_note":"De telefoon moet op het geplande tijdstip verbonden zijn met het thuisnetwerk of de VPN.",
 "timer_daily_at":"Elke dag om %1$s","timer_once_at":"Een keer om %1$s","timer_time":"Tijd","timer_repeat":"Herhalen","timer_save":"Timer opslaan",
 "updated_now":"Net bijgewerkt","refresh":"Vernieuwen",
 "day_mon_short":"Ma","day_tue_short":"Di","day_wed_short":"Wo","day_thu_short":"Do","day_fri_short":"Vr","day_sat_short":"Za","day_sun_short":"Zo",
 "screen_devices":"Airco's","section_saved":"Opgeslagen","section_found":"Gevonden op netwerk","action_add":"Toevoegen","action_add_manual":"Handmatig toevoegen",
 "field_ip":"IP-adres","field_ip_hint":"bijv. 192.168.47.30","invalid_ip":"Ongeldig IP-adres",
 "device_state_on":"%1$s · Aan","device_state_off":"%1$s · Uit","device_unreachable":"Niet bereikbaar","device_rename":"Hernoemen","device_delete":"Verwijderen","device_name":"Naam","new_device":"Nieuw",
 "note_vpn":"Automatisch zoeken werkt niet via VPN; de router blokkeert de netwerkzoekopdracht. Opgeslagen eenheden blijven bereikbaar via hun IP. Een vast IP (DHCP-reservatie) wordt aanbevolen.",
 "action_scan":"Netwerk doorzoeken","scan_running":"Zoeken in netwerk...","scan_empty":"Geen eenheid gevonden","scan_failed":"Netwerkzoekopdracht mislukt","bind_failed":"De eenheid heeft de verbinding niet geaccepteerd",
 "command_failed":"Opdracht niet afgeleverd","error_generic":"Er is iets foutgegaan",
 "screen_settings":"Instellingen","settings_language":"Taal","settings_language_current":"%1$s (%2$s)","language_system_default":"Standaard systeem",
 "language_description":"De app volgt de taal van de telefoon. Je kunt een andere taal alleen voor deze app kiezen.","search_language":"Zoek taal","settings_units":"Eenheden",
 "unit_celsius":"°C","unit_fahrenheit":"°F","settings_auto_refresh":"Automatische vernieuwing","settings_auto_refresh_desc":"Status elke 5 seconden vernieuwen terwijl de app open is",
 "settings_theme":"Thema","theme_system":"Systeem","theme_light":"Licht","theme_dark":"Donker","about_version":"Versie %1$s",
 "cd_back":"Terug","cd_settings":"Instellingen","cd_devices":"Airco's","cd_refresh_scan":"Zoek eenheden in het netwerk",
 "cd_temperature_decrease":"Doeltemperatuur verlagen","cd_temperature_increase":"Doeltemperatuur verhogen","cd_dial":"%1$s",
 "a11y_temperature":"%1$s graden doeltemperatuur","a11y_room_temperature":"Kamertemperatuur %1$s graden","mode_cd":"Modus %1$s","fan_cd":"Ventilator: %1$s","chip_on":"%1$s actief",
 "action_cancel":"Annuleren","action_save":"Opslaan","action_close":"Sluiten",
 "temp_celsius":"%1$s °C","temp_fahrenheit":"%1$s °F","timer_not_set":"Niet ingesteld",
})

# ---------------- DANESE ----------------
add("da", {
 "status_connected":"Forbundet","status_connecting":"Forbinder...","status_unreachable":"Ikke tilgængelig",
 "network_home_wifi":"Hjemme-Wi-Fi","network_vpn":"VPN",
 "label_target":"Mål","label_room":"Rum",
 "action_power_on":"Tændt","action_power_off":"Slukket","power_toggle_on":"Tænd","power_toggle_off":"Sluk",
 "device_off_hint":"Enhed slukket: tilstand, blæser og funktioner kan stadig ændres, men er dæmpet.",
 "section_mode":"Tilstand","mode_auto":"Auto","mode_cool":"Køl","mode_dry":"Affugt","mode_fan":"Ventilér","mode_heat":"Varme",
 "section_fan":"Blæser","fan_auto":"Auto","fan_level_1":"Niveau 1","fan_level_2":"Niveau 2","fan_level_3":"Niveau 3","fan_level_4":"Niveau 4","fan_level_5":"Niveau 5",
 "section_features":"Funktioner","feature_turbo":"Turbo","feature_quiet":"Stille","feature_sleep":"Nat","feature_light":"Lys","feature_health":"Sundhed","feature_fresh_air":"Frisk luft","feature_x_fan":"X-Fan",
 "section_airflow":"Luftstrøm","airflow_vertical":"Lodrette lameller","airflow_horizontal":"Vandrette lameller",
 "airflow_cd_vertical":"Ændr den lodrette luftstrømsretning","airflow_cd_horizontal":"Ændr den vandrette luftstrømsretning",
 "louvre_fixed":"Fast (nuværende position)","louvre_swing_full":"Fuld oscillation","louvre_position":"Position %1$d","louvre_swing_partial":"Delvis oscillation %1$d",
 "section_timer":"Timer","timer_on":"Tænd","timer_off":"Sluk","timer_once":"Engang","timer_daily":"Hver dag","timer_weekdays":"Hverdage","timer_custom_days":"Brugerdefinerede dage",
 "timer_add":"Tilføj timer","timer_delete":"Slet timer","timer_note":"Telefonen skal være tilsluttet hjemmenetværket eller VPN på det fastsatte tidspunkt.",
 "timer_daily_at":"Hver dag kl. %1$s","timer_once_at":"Engang kl. %1$s","timer_time":"Tidspunkt","timer_repeat":"Gentag","timer_save":"Gem timer",
 "updated_now":"Opdateret lige nu","refresh":"Opdater",
 "day_mon_short":"Man","day_tue_short":"Tir","day_wed_short":"Ons","day_thu_short":"Tor","day_fri_short":"Fre","day_sat_short":"Lør","day_sun_short":"Søn",
 "screen_devices":"Aircondition","section_saved":"Gemte","section_found":"Fundet på netværk","action_add":"Tilføj","action_add_manual":"Tilføj manuelt",
 "field_ip":"IP-adresse","field_ip_hint":"f.eks. 192.168.47.30","invalid_ip":"Ugyldig IP-adresse",
 "device_state_on":"%1$s · Tændt","device_state_off":"%1$s · Slukket","device_unreachable":"Ikke tilgængelig","device_rename":"Omdøb","device_delete":"Slet","device_name":"Navn","new_device":"Ny",
 "note_vpn":"Automatisk søgning virker ikke via VPN; routeren blokerer netværkssøgning. Gemte enheder forbliver tilgængelige via deres IP. En fast IP (DHCP-reservation) anbefales.",
 "action_scan":"Søg i netværk","scan_running":"Søger i netværk...","scan_empty":"Ingen enhed fundet","scan_failed":"Netværkssøgning mislykkedes","bind_failed":"Enheden accepterede ikke forbindelsen",
 "command_failed":"Kommando ikke leveret","error_generic":"Der opstod en fejl",
 "screen_settings":"Indstillinger","settings_language":"Sprog","settings_language_current":"%1$s (%2$s)","language_system_default":"Systemstandard",
 "language_description":"Appen følger telefonens sprog. Du kan vælge et andet sprog kun for denne app.","search_language":"Søg sprog","settings_units":"Enheder",
 "unit_celsius":"°C","unit_fahrenheit":"°F","settings_auto_refresh":"Automatisk opdatering","settings_auto_refresh_desc":"Opdater status hver 5. sekund, mens appen er åben",
 "settings_theme":"Tema","theme_system":"System","theme_light":"Lyst","theme_dark":"Mørkt","about_version":"Version %1$s",
 "cd_back":"Tilbage","cd_settings":"Indstillinger","cd_devices":"Aircondition","cd_refresh_scan":"Søg efter enheder i netværket",
 "cd_temperature_decrease":"Sænk mål temperatur","cd_temperature_increase":"Hæv mål temperatur","cd_dial":"%1$s",
 "a11y_temperature":"%1$s grader mål temperatur","a11y_room_temperature":"Rumtemperatur %1$s grader","mode_cd":"Tilstand %1$s","fan_cd":"Blæser: %1$s","chip_on":"%1$s aktiv",
 "action_cancel":"Annuller","action_save":"Gem","action_close":"Luk",
 "temp_celsius":"%1$s °C","temp_fahrenheit":"%1$s °F","timer_not_set":"Ikke indstillet",
})

# ---------------- SVEDESE ----------------
add("sv", {
 "status_connected":"Ansluten","status_connecting":"Ansluter...","status_unreachable":"Ej nåbar",
 "network_home_wifi":"Hemma-Wi-Fi","network_vpn":"VPN",
 "label_target":"Mål","label_room":"Rum",
 "action_power_on":"På","action_power_off":"Av","power_toggle_on":"Slå på","power_toggle_off":"Stäng av",
 "device_off_hint":"Enhet avstängd: läge, fläkt och funktioner kan ändras men är nedtonade.",
 "section_mode":"Läge","mode_auto":"Auto","mode_cool":"Kyla","mode_dry":"Avfukta","mode_fan":"Fläkt","mode_heat":"Värme",
 "section_fan":"Fläkt","fan_auto":"Auto","fan_level_1":"Nivå 1","fan_level_2":"Nivå 2","fan_level_3":"Nivå 3","fan_level_4":"Nivå 4","fan_level_5":"Nivå 5",
 "section_features":"Funktioner","feature_turbo":"Turbo","feature_quiet":"Tyst","feature_sleep":"Natt","feature_light":"Ljus","feature_health":"Hälsa","feature_fresh_air":"Frisk luft","feature_x_fan":"X-Fan",
 "section_airflow":"Luftflöde","airflow_vertical":"Vertikala lameller","airflow_horizontal":"Horisontella lameller",
 "airflow_cd_vertical":"Ändra den vertikala luftflödesriktningen","airflow_cd_horizontal":"Ändra den horisontella luftflödesriktningen",
 "louvre_fixed":"Fast (nuvarande position)","louvre_swing_full":"Full oscillation","louvre_position":"Position %1$d","louvre_swing_partial":"Delvis oscillation %1$d",
 "section_timer":"Timer","timer_on":"Slå på","timer_off":"Stäng av","timer_once":"En gång","timer_daily":"Varje dag","timer_weekdays":"Veckodagar","timer_custom_days":"Anpassade dagar",
 "timer_add":"Lägg till timer","timer_delete":"Ta bort timer","timer_note":"Telefonen måste vara ansluten till hemnätverket eller VPN vid den angivna tiden.",
 "timer_daily_at":"Varje dag kl. %1$s","timer_once_at":"En gång kl. %1$s","timer_time":"Tid","timer_repeat":"Upprepa","timer_save":"Spara timer",
 "updated_now":"Uppdaterad precis nu","refresh":"Uppdatera",
 "day_mon_short":"Mån","day_tue_short":"Tis","day_wed_short":"Ons","day_thu_short":"Tor","day_fri_short":"Fre","day_sat_short":"Lör","day_sun_short":"Sön",
 "screen_devices":"Luftkonditionering","section_saved":"Sparade","section_found":"Hittade på nätverk","action_add":"Lägg till","action_add_manual":"Lägg till manuellt",
 "field_ip":"IP-adress","field_ip_hint":"t.ex. 192.168.47.30","invalid_ip":"Ogiltig IP-adress",
 "device_state_on":"%1$s · På","device_state_off":"%1$s · Av","device_unreachable":"Ej nåbar","device_rename":"Byt namn","device_delete":"Ta bort","device_name":"Namn","new_device":"Ny",
 "note_vpn":"Automatisk sökning fungerar inte via VPN; routern blockerar nätverkssökning. Sparade enheter förblir nåbara via sin IP. En fast IP (DHCP-reservation) rekommenderas.",
 "action_scan":"Sök i nätverk","scan_running":"Söker i nätverk...","scan_empty":"Ingen enhet hittad","scan_failed":"Nätverkssökning misslyckades","bind_failed":"Enheten accepterade inte anslutningen",
 "command_failed":"Kommando inte levererat","error_generic":"Något gick fel",
 "screen_settings":"Inställningar","settings_language":"Språk","settings_language_current":"%1$s (%2$s)","language_system_default":"Systemstandard",
 "language_description":"Appen följer telefonens språk. Du kan välja ett annat språk bara för denna app.","search_language":"Sök språk","settings_units":"Enheter",
 "unit_celsius":"°C","unit_fahrenheit":"°F","settings_auto_refresh":"Automatisk uppdatering","settings_auto_refresh_desc":"Uppdatera status var 5:e sekund medan appen är öppen",
 "settings_theme":"Tema","theme_system":"System","theme_light":"Ljust","theme_dark":"Mörkt","about_version":"Version %1$s",
 "cd_back":"Tillbaka","cd_settings":"Inställningar","cd_devices":"Luftkonditionering","cd_refresh_scan":"Sök efter enheter i nätverket",
 "cd_temperature_decrease":"Sänk måltemperatur","cd_temperature_increase":"Höj måltemperatur","cd_dial":"%1$s",
 "a11y_temperature":"%1$s grader måltemperatur","a11y_room_temperature":"Rumstemperatur %1$s grader","mode_cd":"Läge %1$s","fan_cd":"Fläkt: %1$s","chip_on":"%1$s aktiv",
 "action_cancel":"Avbryt","action_save":"Spara","action_close":"Stäng",
 "temp_celsius":"%1$s °C","temp_fahrenheit":"%1$s °F","timer_not_set":"Ej inställt",
})

# ---------------- FINLANDESE ----------------
add("fi", {
 "status_connected":"Yhdistetty","status_connecting":"Yhdistetään...","status_unreachable":"Tavoittamattomissa",
 "network_home_wifi":"Kodin Wi-Fi","network_vpn":"VPN",
 "label_target":"Asetuslämpötila","label_room":"Huone",
 "action_power_on":"Päällä","action_power_off":"Pois päältä","power_toggle_on":"Kytke päälle","power_toggle_off":"Sammuta",
 "device_off_hint":"Laite on pois päältä: tila, puhallin ja toiminnot ovat edelleen säädettävissä, mutta himmennettyinä.",
 "section_mode":"Tila","mode_auto":"Auto","mode_cool":"Jäähdytys","mode_dry":"Kuivaus","mode_fan":"Tuuletus","mode_heat":"Lämmitys",
 "section_fan":"Puhallin","fan_auto":"Auto","fan_level_1":"Taso 1","fan_level_2":"Taso 2","fan_level_3":"Taso 3","fan_level_4":"Taso 4","fan_level_5":"Taso 5",
 "section_features":"Toiminnot","feature_turbo":"Turbo","feature_quiet":"Hiljainen","feature_sleep":"Yö","feature_light":"Valo","feature_health":"Terveys","feature_fresh_air":"Raitis ilma","feature_x_fan":"X-Fan",
 "section_airflow":"Ilmavirta","airflow_vertical":"Pystysuorat säleet","airflow_horizontal":"Vaakasuorat säleet",
 "airflow_cd_vertical":"Muuta ilmavirran pystysuunta","airflow_cd_horizontal":"Muuta ilmavirran vaakasuunta",
 "louvre_fixed":"Kiinteä (nykyinen asento)","louvre_swing_full":"Koko heilunta","louvre_position":"Asento %1$d","louvre_swing_partial":"Osittainen heilunta %1$d",
 "section_timer":"Ajastin","timer_on":"Käynnistä","timer_off":"Sammuta","timer_once":"Kerta","timer_daily":"Joka päivä","timer_weekdays":"Arkipäivät","timer_custom_days":"Mukautetut päivät",
 "timer_add":"Lisää ajastin","timer_delete":"Poista ajastin","timer_note":"Puhelimen on oltava kotiverkossa tai VPN:ssä määritettynä aikana.",
 "timer_daily_at":"Joka päivä klo %1$s","timer_once_at":"Kerran klo %1$s","timer_time":"Aika","timer_repeat":"Toista","timer_save":"Tallenna ajastin",
 "updated_now":"Päivitetty juuri nyt","refresh":"Päivitä",
 "day_mon_short":"Ma","day_tue_short":"Ti","day_wed_short":"Ke","day_thu_short":"To","day_fri_short":"Pe","day_sat_short":"La","day_sun_short":"Su",
 "screen_devices":"Ilmastointilaitteet","section_saved":"Tallennetut","section_found":"Löydetty verkosta","action_add":"Lisää","action_add_manual":"Lisää manuaalisesti",
 "field_ip":"IP-osoite","field_ip_hint":"esim. 192.168.47.30","invalid_ip":"Virheellinen IP-osoite",
 "device_state_on":"%1$s · Päällä","device_state_off":"%1$s · Pois päältä","device_unreachable":"Tavoittamattomissa","device_rename":"Nimeä uudelleen","device_delete":"Poista","device_name":"Nimi","new_device":"Uusi",
 "note_vpn":"Automaattinen haku ei toimi VPN:n kautta; reititin estää verkon etsinnän. Tallennetut laitteet pysyvät tavoitettavissa IP-osoitteellaan. Kiinteä IP (DHCP-varaus) suositeltava.",
 "action_scan":"Etsi verkosta","scan_running":"Etsitään verkosta...","scan_empty":"Ei laitetta löytynyt","scan_failed":"Verkon etsintä epäonnistui","bind_failed":"Laite ei hyväksynyt yhteyttä",
 "command_failed":"Komentoa ei toimitettu","error_generic":"Jotain meni pieleen",
 "screen_settings":"Asetukset","settings_language":"Kieli","settings_language_current":"%1$s (%2$s)","language_system_default":"Järjestelmän oletus",
 "language_description":"Sovellus noudattaa puhelimen kieltä. Voit valita toisen kielen vain tätä sovellusta varten.","search_language":"Etsi kieli","settings_units":"Yksiköt",
 "unit_celsius":"°C","unit_fahrenheit":"°F","settings_auto_refresh":"Automaattinen päivitys","settings_auto_refresh_desc":"Päivitä tila 5 sekunnin välein, kun sovellus on auki",
 "settings_theme":"Teema","theme_system":"Järjestelmä","theme_light":"Vaalea","theme_dark":"Tumma","about_version":"Versio %1$s",
 "cd_back":"Takaisin","cd_settings":"Asetukset","cd_devices":"Ilmastointilaitteet","cd_refresh_scan":"Etsi laitteet verkosta",
 "cd_temperature_decrease":"Laske kohdelämpötilaa","cd_temperature_increase":"Nosta kohdelämpötilaa","cd_dial":"%1$s",
 "a11y_temperature":"%1$s astetta kohdelämpötila","a11y_room_temperature":"Huonelämpötila %1$s astetta","mode_cd":"Tila %1$s","fan_cd":"Puhallin: %1$s","chip_on":"%1$s aktiivinen",
 "action_cancel":"Peruuta","action_save":"Tallenna","action_close":"Sulje",
 "temp_celsius":"%1$s °C","temp_fahrenheit":"%1$s °F","timer_not_set":"Ei asetettu",
})

# ---------------- UNGHERESE ----------------
add("hu", {
 "status_connected":"Csatlakoztatva","status_connecting":"Kapcsolódás...","status_unreachable":"Nem elérhető",
 "network_home_wifi":"Otthoni Wi-Fi","network_vpn":"VPN",
 "label_target":"Cél","label_room":"Szoba",
 "action_power_on":"Bekapcsolva","action_power_off":"Kikapcsolva","power_toggle_on":"Bekapcsol","power_toggle_off":"Kikapcsol",
 "device_off_hint":"Az eszköz ki van kapcsolva: mód, ventilátor és funkciók továbbra is beállíthatók, de homályosak.",
 "section_mode":"Mód","mode_auto":"Auto","mode_cool":"Hűtés","mode_dry":"Szárítás","mode_fan":"Szüvétezés","mode_heat":"Fűtés",
 "section_fan":"Ventilátor","fan_auto":"Auto","fan_level_1":"Szint 1","fan_level_2":"Szint 2","fan_level_3":"Szint 3","fan_level_4":"Szint 4","fan_level_5":"Szint 5",
 "section_features":"Funkciók","feature_turbo":"Turbo","feature_quiet":"Csendes","feature_sleep":"Éjszaka","feature_light":"Világítás","feature_health":"Egészség","feature_fresh_air":"Friss levegő","feature_x_fan":"X-Fan",
 "section_airflow":"Légáramlás","airflow_vertical":"Függőleges lapelék","airflow_horizontal":"Vízszintes lapelék",
 "airflow_cd_vertical":"Módosítsa a légáramlást függőlegesen","airflow_cd_horizontal":"Módosítsa a légáramlást vízszintesen",
 "louvre_fixed":"Rögzített (jelenlegi pozíció)","louvre_swing_full":"Teljes osciláció","louvre_position":"Pozíció %1$d","louvre_swing_partial":"Részleges osciláció %1$d",
 "section_timer":"Időzítő","timer_on":"Bekapcsolás","timer_off":"Kikapcsolás","timer_once":"Egyszer","timer_daily":"Minden nap","timer_weekdays":"Hétköznapok","timer_custom_days":"Egyéni napok",
 "timer_add":"Időzítő hozzáadása","timer_delete":"Időzítő törlése","timer_note":"A telefonnak hálózati vagy VPN-kapcsolatra van szüksége a megadott időpontban.",
 "timer_daily_at":"Minden nap %1$s-kor","timer_once_at":"Egyszer %1$s-kor","timer_time":"Időpont","timer_repeat":"Ismétlés","timer_save":"Időzítő mentése",
 "updated_now":"Frissítve éppen most","refresh":"Frissítés",
 "day_mon_short":"Hétfő","day_tue_short":"Kedd","day_wed_short":"Szerda","day_thu_short":"Csütörtök","day_fri_short":"Péntek","day_sat_short":"Szombat","day_sun_short":"Vasárnap",
 "screen_devices":"Klíma","section_saved":"Mentettek","section_found":"Hálózaton talált","action_add":"Hozzáadás","action_add_manual":"Kézi hozzáadás",
 "field_ip":"IP-cím","field_ip_hint":"pl. 192.168.47.30","invalid_ip":"Érvénytelen IP-cím",
 "device_state_on":"%1$s · Bekapcsolva","device_state_off":"%1$s · Kikapcsolva","device_unreachable":"Nem elérhető","device_rename":"Átnevezés","device_delete":"Törlés","device_name":"Név","new_device":"Új",
 "note_vpn":"Az automatikus keresés nem érhető el VPN-en; a router blokkolja a hálózati keresést. A mentett eszközök IP-címeikön elérhetőek maradnak. Statikus IP (DHCP-foglalás) javasolt.",
 "action_scan":"Hálózat keresése","scan_running":"Keresés a hálózatban...","scan_empty":"Nincs eszköz találva","scan_failed":"Hálózati keresés sikertelen","bind_failed":"Az eszköz nem fogadta el a kapcsolatot",
 "command_failed":"Parancs nem teljesült","error_generic":"Valami hiba történt",
 "screen_settings":"Beállítások","settings_language":"Nyelv","settings_language_current":"%1$s (%2$s)","language_system_default":"Rendszeralapértelmezés",
 "language_description":"Az alkalmazás a telefon nyelvét követi. Más nyelvet csak erre az alkalmazásra választhat ki.","search_language":"Nyelv keresése","settings_units":"Mértékegységek",
 "unit_celsius":"°C","unit_fahrenheit":"°F","settings_auto_refresh":"Automatikus frissítés","settings_auto_refresh_desc":"Frissítse az állapotot minden 5 másodpercben, mielőtt az alkalmazás nyitva van",
 "settings_theme":"Téma","theme_system":"Rendszer","theme_light":"Világos","theme_dark":"Sötét","about_version":"Verzió %1$s",
 "cd_back":"Vissza","cd_settings":"Beállítások","cd_devices":"Klíma","cd_refresh_scan":"Eszközök keresése a hálózatban",
 "cd_temperature_decrease":"Célhőmérséklet csökkentése","cd_temperature_increase":"Célhőmérséklet növelése","cd_dial":"%1$s",
 "a11y_temperature":"%1$s fok célhőmérséklet","a11y_room_temperature":"Szobahőmérséklet %1$s fok","mode_cd":"Mód %1$s","fan_cd":"Ventilátor: %1$s","chip_on":"%1$s aktív",
 "action_cancel":"Mégsem","action_save":"Mentés","action_close":"Bezárás",
 "temp_celsius":"%1$s °C","temp_fahrenheit":"%1$s °F","timer_not_set":"Nem beállítva",
})

# ---------------- POLACCO ----------------
add("pl", {
 "status_connected":"Połączono","status_connecting":"Łączenie...","status_unreachable":"Niedostępne",
 "network_home_wifi":"Wi-Fi domowe","network_vpn":"VPN",
 "label_target":"Ustawiona","label_room":"Pomieszczenie",
 "action_power_on":"Włączone","action_power_off":"Wyłączone","power_toggle_on":"Włącz","power_toggle_off":"Wyłącz",
 "device_off_hint":"Urządzenie wyłączone: tryb, wentylator i funkcje pozostają dostępne, ale są przygaszone.",
 "section_mode":"Tryb","mode_auto":"Auto","mode_cool":"Chłodzenie","mode_dry":"Osuszanie","mode_fan":"Wentylacja","mode_heat":"Ogrzewanie",
 "section_fan":"Wentylator","fan_auto":"Auto","fan_level_1":"Poziom 1","fan_level_2":"Poziom 2","fan_level_3":"Poziom 3","fan_level_4":"Poziom 4","fan_level_5":"Poziom 5",
 "section_features":"Funkcje","feature_turbo":"Turbo","feature_quiet":"Cichy","feature_sleep":"Noc","feature_light":"Światło","feature_health":"Zdrowie","feature_fresh_air":"Świeże powietrze","feature_x_fan":"X-Fan",
 "section_airflow":"Przepływ powietrza","airflow_vertical":"Żaluzje pionowe","airflow_horizontal":"Żaluzje poziome",
 "airflow_cd_vertical":"Zmień pionowy kierunek przepływu powietrza","airflow_cd_horizontal":"Zmień poziomy kierunek przepływu powietrza",
 "louvre_fixed":"Stałe (obecna pozycja)","louvre_swing_full":"Pełne oscylowanie","louvre_position":"Pozycja %1$d","louvre_swing_partial":"Częściowe oscylowanie %1$d",
 "section_timer":"Timer","timer_on":"Włączenie","timer_off":"Wyłączenie","timer_once":"Raz","timer_daily":"Codziennie","timer_weekdays":"Dni robocze","timer_custom_days":"Wybrane dni",
 "timer_add":"Dodaj timer","timer_delete":"Usuń timer","timer_note":"Telefon musi być połączony z siecią domową lub VPN o zaplanowanej godzinie.",
 "timer_daily_at":"Codziennie o %1$s","timer_once_at":"Raz o %1$s","timer_time":"Godzina","timer_repeat":"Powtarzaj","timer_save":"Zapisz timer",
 "updated_now":"Zaktualizowano przed chwilą","refresh":"Odśwież",
 "day_mon_short":"Pn","day_tue_short":"Wt","day_wed_short":"Śr","day_thu_short":"Cz","day_fri_short":"Pt","day_sat_short":"So","day_sun_short":"Nd",
 "screen_devices":"Klimatyzatory","section_saved":"Zapisane","section_found":"Znalezione w sieci","action_add":"Dodaj","action_add_manual":"Dodaj ręcznie",
 "field_ip":"Adres IP","field_ip_hint":"np. 192.168.47.30","invalid_ip":"Nieprawidłowy adres IP",
 "device_state_on":"%1$s · Włączone","device_state_off":"%1$s · Wyłączone","device_unreachable":"Niedostępne","device_rename":"Zmień nazwę","device_delete":"Usuń","device_name":"Nazwa","new_device":"Nowy",
 "note_vpn":"Automatyczne wyszukiwanie nie działa przez VPN; router blokuje skanowanie sieci. Zapisane urządzenia pozostają dostępne przez IP. Zalecany stały adres IP (rezerwacja DHCP).",
 "action_scan":"Szukaj w sieci","scan_running":"Szukanie w sieci...","scan_empty":"Nie znaleziono urządzenia","scan_failed":"Wyszukiwanie nie powiodło się","bind_failed":"Urządzenie nie zaakceptowało połączenia",
 "command_failed":"Polecenie nie zostało dostarczone","error_generic":"Coś poszło nie tak",
 "screen_settings":"Ustawienia","settings_language":"Język","settings_language_current":"%1$s (%2$s)","language_system_default":"Domyślny systemu",
 "language_description":"Aplikacja podąża za językiem telefonu. Możesz wybrać inny język tylko dla tej aplikacji.","search_language":"Szukaj języka","settings_units":"Jednostki",
 "unit_celsius":"°C","unit_fahrenheit":"°F","settings_auto_refresh":"Automatyczne odświeżanie","settings_auto_refresh_desc":"Odświeżaj stan co 5 sekund, gdy aplikacja jest otwarta",
 "settings_theme":"Motyw","theme_system":"System","theme_light":"Jasny","theme_dark":"Ciemny","about_version":"Wersja %1$s",
 "cd_back":"Wstecz","cd_settings":"Ustawienia","cd_devices":"Klimatyzatory","cd_refresh_scan":"Szukaj urządzeń w sieci",
 "cd_temperature_decrease":"Obniż temperaturę docelową","cd_temperature_increase":"Podnieś temperaturę docelową","cd_dial":"%1$s",
 "a11y_temperature":"%1$s stopni temperatura docelowa","a11y_room_temperature":"Temperatura pomieszczenia %1$s stopni","mode_cd":"Tryb %1$s","fan_cd":"Wentylator: %1$s","chip_on":"%1$s aktywne",
 "action_cancel":"Anuluj","action_save":"Zapisz","action_close":"Zamknij",
 "temp_celsius":"%1$s °C","temp_fahrenheit":"%1$s °F","timer_not_set":"Nie ustawiono","devices_empty":"Nie zapisano jeszcze klimatyzatora",
})

# ---------------- CECO ----------------
add("cs", {
 "status_connected":"Připojeno","status_connecting":"Připojování...","status_unreachable":"Nedostupné",
 "network_home_wifi":"Domácí Wi-Fi","network_vpn":"VPN",
 "label_target":"Nastavená","label_room":"Místnost",
 "action_power_on":"Zapnuto","action_power_off":"Vypnuto","power_toggle_on":"Zapnout","power_toggle_off":"Vypnout",
 "device_off_hint":"Jednotka je vypnutá: režim, ventilátor a funkce zůstávají nastavitelné, ale jsou ztlumené.",
 "section_mode":"Režim","mode_auto":"Auto","mode_cool":"Chlazení","mode_dry":"Odvlhčování","mode_fan":"Ventilace","mode_heat":"Topení",
 "section_fan":"Ventilátor","fan_auto":"Auto","fan_level_1":"Úroveň 1","fan_level_2":"Úroveň 2","fan_level_3":"Úroveň 3","fan_level_4":"Úroveň 4","fan_level_5":"Úroveň 5",
 "section_features":"Funkce","feature_turbo":"Turbo","feature_quiet":"Tichý","feature_sleep":"Noc","feature_light":"Světlo","feature_health":"Zdraví","feature_fresh_air":"Čerstvý vzduch","feature_x_fan":"X-Fan",
 "section_airflow":"Proudění vzduchu","airflow_vertical":"Svislé lamely","airflow_horizontal":"Vodorovné lamely",
 "airflow_cd_vertical":"Změnit svislý směr proudění vzduchu","airflow_cd_horizontal":"Změnit vodorovný směr proudění vzduchu",
 "louvre_fixed":"Pevné (aktuální poloha)","louvre_swing_full":"Plné kyvání","louvre_position":"Poloha %1$d","louvre_swing_partial":"Částečné kyvání %1$d",
 "section_timer":"Časovač","timer_on":"Zapnutí","timer_off":"Vypnutí","timer_once":"Jednou","timer_daily":"Každý den","timer_weekdays":"Pracovní dny","timer_custom_days":"Vlastní dny",
 "timer_add":"Přidat časovač","timer_delete":"Smazat časovač","timer_note":"Telefon musí být v naplánovaný čas připojen k domácí síti nebo VPN.",
 "timer_daily_at":"Každý den v %1$s","timer_once_at":"Jednou v %1$s","timer_time":"Čas","timer_repeat":"Opakovat","timer_save":"Uložit časovač",
 "updated_now":"Právě aktualizováno","refresh":"Obnovit",
 "day_mon_short":"Po","day_tue_short":"Út","day_wed_short":"St","day_thu_short":"Čt","day_fri_short":"Pá","day_sat_short":"So","day_sun_short":"Ne",
 "screen_devices":"Klimatizace","section_saved":"Uložené","section_found":"Nalezené v síti","action_add":"Přidat","action_add_manual":"Přidat ručně",
 "field_ip":"IP adresa","field_ip_hint":"např. 192.168.47.30","invalid_ip":"Neplatná IP adresa",
 "device_state_on":"%1$s · Zapnuto","device_state_off":"%1$s · Vypnuto","device_unreachable":"Nedostupné","device_rename":"Přejmenovat","device_delete":"Smazat","device_name":"Název","new_device":"Nové",
 "note_vpn":"Automatické vyhledávání nefunguje přes VPN; router blokuje skenování sítě. Uložené jednotky zůstávají dostupné přes IP. Doporučena pevná IP (rezervace DHCP).",
 "action_scan":"Hledat v síti","scan_running":"Hledání v síti...","scan_empty":"Žádná jednotka nenalezena","scan_failed":"Hledání v síti selhalo","bind_failed":"Jednotka nepřijala připojení",
 "command_failed":"Příkaz nebyl doručen","error_generic":"Něco se pokazilo",
 "screen_settings":"Nastavení","settings_language":"Jazyk","settings_language_current":"%1$s (%2$s)","language_system_default":"Výchozí systému",
 "language_description":"Aplikace následuje jazyk telefonu. Můžete zvolit jiný jazyk jen pro tuto aplikaci.","search_language":"Hledat jazyk","settings_units":"Jednotky",
 "unit_celsius":"°C","unit_fahrenheit":"°F","settings_auto_refresh":"Automatická aktualizace","settings_auto_refresh_desc":"Aktualizovat stav každých 5 sekund, když je aplikace otevřená",
 "settings_theme":"Motiv","theme_system":"Systém","theme_light":"Světlý","theme_dark":"Tmavý","about_version":"Verze %1$s",
 "cd_back":"Zpět","cd_settings":"Nastavení","cd_devices":"Klimatizace","cd_refresh_scan":"Hledat jednotky v síti",
 "cd_temperature_decrease":"Snížit cílovou teplotu","cd_temperature_increase":"Zvýšit cílovou teplotu","cd_dial":"%1$s",
 "a11y_temperature":"%1$s stupňů cílová teplota","a11y_room_temperature":"Teplota místnosti %1$s stupňů","mode_cd":"Režim %1$s","fan_cd":"Ventilátor: %1$s","chip_on":"%1$s aktivní",
 "action_cancel":"Zrušit","action_save":"Uložit","action_close":"Zavřít",
 "temp_celsius":"%1$s °C","temp_fahrenheit":"%1$s °F","timer_not_set":"Nenastaveno","devices_empty":"Zatím není uložena žádná klimatizace",
})

# ---------------- SLOVACCO ----------------
add("sk", {
 "status_connected":"Pripojené","status_connecting":"Pripájanie...","status_unreachable":"Nedostupné",
 "network_home_wifi":"Domáce Wi-Fi","network_vpn":"VPN",
 "label_target":"Nastavená","label_room":"Miestnosť",
 "action_power_on":"Zapnuté","action_power_off":"Vypnuté","power_toggle_on":"Zapnúť","power_toggle_off":"Vypnúť",
 "device_off_hint":"Jednotka je vypnutá: režim, ventilátor a funkcie zostávajú nastaviteľné, ale sú stlmené.",
 "section_mode":"Režim","mode_auto":"Auto","mode_cool":"Chladenie","mode_dry":"Odvlhčovanie","mode_fan":"Ventilácia","mode_heat":"Kúrenie",
 "section_fan":"Ventilátor","fan_auto":"Auto","fan_level_1":"Úroveň 1","fan_level_2":"Úroveň 2","fan_level_3":"Úroveň 3","fan_level_4":"Úroveň 4","fan_level_5":"Úroveň 5",
 "section_features":"Funkcie","feature_turbo":"Turbo","feature_quiet":"Tichý","feature_sleep":"Noc","feature_light":"Svetlo","feature_health":"Zdravie","feature_fresh_air":"Čerstvý vzduch","feature_x_fan":"X-Fan",
 "section_airflow":"Prúdenie vzduchu","airflow_vertical":"Zvislé lamely","airflow_horizontal":"Vodorovné lamely",
 "airflow_cd_vertical":"Zmeniť zvislý smer prúdenia vzduchu","airflow_cd_horizontal":"Zmeniť vodorovný smer prúdenia vzduchu",
 "louvre_fixed":"Pevné (aktuálna poloha)","louvre_swing_full":"Plné kyvanie","louvre_position":"Poloha %1$d","louvre_swing_partial":"Čiastočné kyvanie %1$d",
 "section_timer":"Časovač","timer_on":"Zapnutie","timer_off":"Vypnutie","timer_once":"Raz","timer_daily":"Každý deň","timer_weekdays":"Pracovné dni","timer_custom_days":"Vlastné dni",
 "timer_add":"Pridať časovač","timer_delete":"Odstrániť časovač","timer_note":"Telefón musí byť v naplánovanom čase pripojený k domácej sieti alebo VPN.",
 "timer_daily_at":"Každý deň o %1$s","timer_once_at":"Raz o %1$s","timer_time":"Čas","timer_repeat":"Opakovať","timer_save":"Uložiť časovač",
 "updated_now":"Práve aktualizované","refresh":"Obnoviť",
 "day_mon_short":"Po","day_tue_short":"Ut","day_wed_short":"St","day_thu_short":"Št","day_fri_short":"Pi","day_sat_short":"So","day_sun_short":"Ne",
 "screen_devices":"Klimatizácie","section_saved":"Uložené","section_found":"Nájdené v sieti","action_add":"Pridať","action_add_manual":"Pridať ručne",
 "field_ip":"IP adresa","field_ip_hint":"napr. 192.168.47.30","invalid_ip":"Neplatná IP adresa",
 "device_state_on":"%1$s · Zapnuté","device_state_off":"%1$s · Vypnuté","device_unreachable":"Nedostupné","device_rename":"Premenovať","device_delete":"Odstrániť","device_name":"Názov","new_device":"Nové",
 "note_vpn":"Automatické vyhľadávanie nefunguje cez VPN; router blokuje skenovanie siete. Uložené jednotky zostávajú dostupné cez IP. Odporúča sa pevná IP (rezervácia DHCP).",
 "action_scan":"Hľadať v sieti","scan_running":"Hľadanie v sieti...","scan_empty":"Nenašla sa žiadna jednotka","scan_failed":"Hľadanie v sieti zlyhalo","bind_failed":"Jednotka neprijala pripojenie",
 "command_failed":"Príkaz nebol doručený","error_generic":"Niečo sa pokazilo",
 "screen_settings":"Nastavenia","settings_language":"Jazyk","settings_language_current":"%1$s (%2$s)","language_system_default":"Predvolené systému",
 "language_description":"Aplikácia nasleduje jazyk telefónu. Môžete zvoliť iný jazyk len pre túto aplikáciu.","search_language":"Hľadať jazyk","settings_units":"Jednotky",
 "unit_celsius":"°C","unit_fahrenheit":"°F","settings_auto_refresh":"Automatická aktualizácia","settings_auto_refresh_desc":"Aktualizovať stav každých 5 sekúnd, keď je aplikácia otvorená",
 "settings_theme":"Téma","theme_system":"Systém","theme_light":"Svetlá","theme_dark":"Tmavá","about_version":"Verzia %1$s",
 "cd_back":"Späť","cd_settings":"Nastavenia","cd_devices":"Klimatizácie","cd_refresh_scan":"Hľadať jednotky v sieti",
 "cd_temperature_decrease":"Znížiť cieľovú teplotu","cd_temperature_increase":"Zvýšiť cieľovú teplotu","cd_dial":"%1$s",
 "a11y_temperature":"%1$s stupňov cieľová teplota","a11y_room_temperature":"Teplota miestnosti %1$s stupňov","mode_cd":"Režim %1$s","fan_cd":"Ventilátor: %1$s","chip_on":"%1$s aktívne",
 "action_cancel":"Zrušiť","action_save":"Uložiť","action_close":"Zavrieť",
 "temp_celsius":"%1$s °C","temp_fahrenheit":"%1$s °F","timer_not_set":"Nenastavené","devices_empty":"Zatiaľ nie je uložená žiadna klimatizácia",
})

# Lingue con traduzione dedicata vs fallback inglese (da rivedere in TRANSLATIONS.md)
LANGUAGES = {
    "it": "Italiano", "de": "Deutsch", "fr": "Français", "es": "Español", "pt": "Português",
    "nl": "Nederlands", "da": "Dansk", "sv": "Svenska", "fi": "Suomi", "hu": "Magyar",
    "pl": "Polski", "cs": "Čeština", "sk": "Slovenčina",
}
FALLBACK_LANGS = ["ro","lt","lv","et","ga","mt","hr","sl","sr","bs","sq","mk","bg","el","uk","ca","nb","is"]

# Assicura devices_empty in ogni dizionario dedicato
_DEVICES_EMPTY = {
    "it": "Nessun climatizzatore salvato",
    "de": "Noch kein Klimagerät gespeichert",
    "fr": "Aucun climatiseur enregistré",
    "es": "Ningún aire acondicionado guardado",
    "pt": "Nenhum ar-condicionado salvo",
    "nl": "Nog geen airco opgeslagen",
    "da": "Ingen aircondition gemt endnu",
    "sv": "Ingen luftkonditionering sparad ännu",
    "fi": "Ei tallennettua ilmastointilaitetta",
    "hu": "Még nincs mentett klíma",
    "pl": "Nie zapisano jeszcze klimatyzatora",
    "cs": "Zatím není uložena žádná klimatizace",
    "sk": "Zatiaľ nie je uložená žiadna klimatizácia",
}
for _code, _text in _DEVICES_EMPTY.items():
    TR.setdefault(_code, {})["devices_empty"] = _text

if __name__ == "__main__":
    # Ordine stabile: dedicate poi fallback (niente doppioni)
    all_codes = list(dict.fromkeys(list(TR.keys()) + FALLBACK_LANGS))
    print("Languages:", ", ".join(all_codes), f"({len(all_codes)})")
    print("Dedicated:", ", ".join(sorted(LANGUAGES)))
    print("Fallback EN:", ", ".join(FALLBACK_LANGS))
    for code in all_codes:
        outdir = os.path.join(ROOT, f"values-{code}")
        os.makedirs(outdir, exist_ok=True)
        d = TR.get(code, {})
        lines = ['<?xml version="1.0" encoding="utf-8"?>', "<resources>"]
        for key, text in entries:
            if key == "__comment__":
                lines.append(f"    <!-- {text} -->")
                continue
            translated = d.get(key)
            if not translated:
                translated = text  # fallback inglese esplicito
            # Escappa XML + converti ogni carattere non-ASCII in entita'
            # numerica (file pure-ASCII, evita problemi AAPT2 con UTF-8)
            out=[]
            for ch in translated:
                o=ord(ch)
                if o<128:
                    if ch=='&': out.append('&amp;')
                    elif ch=='<': out.append('&lt;')
                    elif ch=='>': out.append('&gt;')
                    elif ch=='"': out.append('\\"')
                    elif ch=="'": out.append("\\'")
                    else: out.append(ch)
                else:
                    out.append(f'&#{o};')
            translated=''.join(out)
            lines.append(f'    <string name="{key}">{translated}</string>')
        lines.append("</resources>")
        with open(os.path.join(outdir, "strings.xml"), "w", encoding="utf-8") as f:
            f.write("\n".join(lines) + "\n")
    print(f"Generated {len(all_codes)} language folders under {ROOT}")
