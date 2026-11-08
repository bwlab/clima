#!/usr/bin/env python3
"""
gree_cli.py - Menu interattivo di controllo di un climatizzatore Gree/EWPE
da shell (usa il motore di gree_test.py: bind automatico, chiavi salvate).

Uso:
    python gree_cli.py                # menu: chiede/scansiona, scegli il clima
    python gree_cli.py 192.168.0.50   # parte direttamente su quell'IP

Funzioni nel menu:
  stato, on/off, temperatura, modalita', ventola, swing, luci display,
  quiet, turbo, lettura grezza, rescansione dispositivi.
"""
import json
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))

from gree_test import (MODES, FANS, scan, bind, get_status, send_cmd, load_keys)

LAST_FILE = HERE / "gree_cli_last.json"

MODE_INV = {v: k for k, v in MODES.items()}
FAN_INV = {v: k for k, v in FANS.items()}

# (label, nome campo del protocollo, mappa scelta->valore)
PNL_MODE = {"auto": 0, "cool": 1, "dry": 2, "ventola": 3, "heat": 4}
PNL_FAN = {"auto": 0, "low": 1, "medlow": 2, "med": 3, "medhigh": 4, "high": 5}
PNL_BIN = {"no": 0, "si": 1}
PNL_QUIET = {"no": 0, "quiet": 1, "auto-quiet": 2}
PNL_SWING = {"fermo": 0, "swing": 1}


def ask_choice(prompt, mapping):
    """Chiede una voce di `mapping` (etichette testuali); None per annullare."""
    opts = " | ".join(mapping.keys())
    while True:
        v = input(f"{prompt} [{opts}] (INVIO per annullare): ").strip().lower()
        if not v:
            return None
        if v in mapping:
            return mapping[v]
        print(f"  Valore non valido. Scelte: {opts}")


def pick_device():
    """Scansiona e fa scegliere il clima; conserva la scelta in gree_cli_last.json."""
    # ricordo l'ultimo usato
    last_ip = None
    if LAST_FILE.exists():
        last_ip = json.loads(LAST_FILE.read_text()).get("ip")

    # climi gia' noti (gia' in bound) + scan
    keys = load_keys()
    known = list(keys.keys())
    if known:
        print("\nClimi gia' registrati in gree_keys.json:")
        for i, ip in enumerate(known, 1):
            d = keys[ip]
            print(f"  {i}) {ip:15}  mac={d['mac']}  fw={d.get('ver','')}")

    print("\nScansiono la rete (spetta broadcast + directed)...")
    found = scan("255.255.255.255") or []
    if not found:
        # directed broadcast delle sottorete corrente
        import socket
        try:
            s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            s.connect(("8.8.8.8", 80))
            own = s.getsockname()[0]
            s.close()
            bcast = own.rsplit(".", 1)[0] + ".255"
            found = scan(bcast)
        except Exception:
            pass
    if found:
        print("Trovati:")
        for i, d in enumerate(found, 1):
            offset = len(known)
            print(f"  {offset + i}) {d['ip']:15}  mac={d['mac']}  nome={d['name']}  "
                  f"fw={d['ver']}  cifratura={'GCM' if d['gcm'] else 'ECB'}")
    else:
        print("Nessun clima trovato allo scan.")

    choices = known + [d["ip"] for d in found]
    if not choices:
        return None
    default = f" [INVIO = {last_ip}]" if last_ip in choices else ""
    raw = input(f"\nnumero (1..{len(choices)}) o IP{default}: ").strip()
    if not raw:
        if last_ip in choices:
            chosen = last_ip
        else:
            return None
    elif raw.isdigit() and 1 <= int(raw) <= len(choices):
        chosen = choices[int(raw) - 1]
    else:
        chosen = raw
    LAST_FILE.write_text(json.dumps({"ip": chosen}))
    print(f"-> uso {chosen}\n")
    return chosen


# ---------- azioni ----------

def show_status(ip):
    st = get_status(ip)
    t = st.get("TemSen")
    if isinstance(t, int) and t >= 40:
        st["TemSen"] = t - 40
    print(f"""\
  ╔══════════════════════════════════  {ip}  ══════════════════════════
   Acceso         : {'SI' if st.get('Pow') else 'NO'}
   Modalita'      : {MODE_INV.get(st.get('Mod'), st.get('Mod'))}
   Temperatura    : {st.get('SetTem')} °C (ambiente {st.get('TemSen')} °C)
   Ventola        : {FAN_INV.get(st.get('WdSpd'), st.get('WdSpd'))}
   Swing vert.    : {'swing' if st.get('SwUpDn') else 'fermo'}
   Luci display   : {'SI' if st.get('Lig') else 'no'}
   Quiet          : {st.get('Quiet')}
   Turbo          : {st.get('Tur')}
   Standby/eco    : {st.get('SvSt')}
  ╚════════════════════════════════════════════════════════════════════""")
    return st


def change_value(ip, label, col, options):
    v = ask_choice(label, options)
    if v is None:
        print("  annullato")
        return
    send_cmd(ip, **{col: v})


def action_temp(ip):
    raw = input("temperatura 16..30 °C (INVIO per annullare): ").strip()
    if not raw:
        print("  annullato"); return
    try:
        t = int(raw)
    except ValueError:
        print("  non e' un numero"); return
    if not 16 <= t <= 30:
        print("  fuori range"); return
    send_cmd(ip, TemUn=0, SetTem=t)


MENU = """
  ─────────────────────────────────────────
   1)  stato                              (lettura completa)
   2)  ACCENDI
   3)  SPEGNI
   4)  temperatura (SetTem 16..30 °C)
   5)  modalita' (Mod)
   6)  ventola (WdSpd)
   7)  swing verticale (SwUpDn)
   8)  luci display (Lig)
   9)  quiet (Quiet)
  10)  turbo (Tur)
  11)  cambio clima (risearch)
  12)  uscita
  ─────────────────────────────────────────
"""


def run_menu():
    ip = None
    if len(sys.argv) > 1:
        ip = sys.argv[1]
    else:
        ip = pick_device()
        if not ip:
            sys.exit("Nessun clima selezionato: riprova (serve essere sulla stessa rete).")
    print(f"Controllo di {ip} — CTRL+C o 'q' per uscire\n")
    try:
        show_status(ip)
    except Exception as e:
        print(f"W: lettura iniziale fallita: {e}\n")

    while True:
        print(MENU)
        v = input("scelta: ").strip().lower()
        if v in ("q", "12", ""):
            break
        try:
            if v == "1":
                show_status(ip)
            elif v == "2":
                send_cmd(ip, Pow=1)
            elif v == "3":
                send_cmd(ip, Pow=0)
            elif v == "4":
                action_temp(ip)
            elif v == "5":
                change_value(ip, "modalita'", "Mod", PNL_MODE)
            elif v == "6":
                change_value(ip, "ventola", "WdSpd", PNL_FAN)
            elif v == "7":
                change_value(ip, "swing", "SwUpDn", PNL_SWING)
            elif v == "8":
                change_value(ip, "luci display", "Lig", PNL_BIN)
            elif v == "9":
                change_value(ip, "quiet", "Quiet", PNL_QUIET)
            elif v == "10":
                change_value(ip, "turbo", "Tur", PNL_BIN)
            elif v == "11":
                new = pick_device()
                if new:
                    ip = new
            else:
                print("  scelta non valida")
        except KeyboardInterrupt:
            print("\n  (azione interrotta)")
        except SystemExit as e:
            print(f"  errore: {e}")


if __name__ == "__main__":
    try:
        run_menu()
    except KeyboardInterrupt:
        print("\nBye")
    except SystemExit as e:
        print(e)
