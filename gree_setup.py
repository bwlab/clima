#!/usr/bin/env python3
"""
gree_setup.py - Aggancia il climatizzatore Gree/EWPE alla rete Wi-Fi di casa.

FASE 1 (sei attaccato all'hotspot del clima, es. SSID "1e538ca2"):
    python gree_setup.py
    invia {"psw": ..., "ssid": ..., "t": "wlan"} a 192.168.1.1 UDP 7000
    (ripetuto N volte: quando il modulo lo riceve, lascia l'AP e si
     aggancia alla rete di casa; l'hotspot sparira' entro qualche istante)

FASE 2 (sei attaccato alla rete di casa):
    python gree_setup.py
    fa uno scan Gree su tutta la sottorete: se il clima ha accettato le
    credenziali, compare col suo IP.

Puoi ripetere lo script quante volte vuoi; capisce da solo in quale fase sei.
Credenziali: la prima volta le chiede e le salva in gree_wifi.json.

Uso rapido:
    python gree_setup.py                 # fa la fase giusta per dove sei
    python gree_setup.py --repeat 20     # prova piu' a lungo (fase 1)
    python gree_setup.py --ssid XXX --psw YYY   # forza le credenziali

Suggerimento per il Wi-Fi di casa: password da fornire a mano; non salvare in git —
    nmcli -s -g 802-11-wireless-security.psk connection show <NOME-SSID>
"""
import argparse
import json
import socket
import subprocess
import sys
import time
from pathlib import Path

HERE = Path(__file__).resolve().parent
CONF_FILE = HERE / "gree_wifi.json"

AP_IP = "192.168.1.1"          # IP del clima quando e' in modalita' configurazione
PORT = 7000


# ---------- infra ----------

def udp_send_recv(ip, msg, timeout=3):
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.settimeout(timeout)
    try:
        s.sendto(msg, (ip, PORT))
        return s.recvfrom(4096)[0]
    except socket.timeout:
        return None
    finally:
        s.close()


def ping(ip):
    return subprocess.run(["ping", "-c", "1", "-W", "1", ip],
                          stdout=subprocess.DEVNULL,
                          stderr=subprocess.DEVNULL).returncode == 0


def arp_mac_for(ip):
    if not ping(ip):
        return None
    for line in Path("/proc/net/arp").read_text().splitlines()[1:]:
        f = line.split()
        if len(f) >= 6 and f[0] == ip:
            return f[3].lower()
    return None


def load_conf():
    if CONF_FILE.exists():
        return json.loads(CONF_FILE.read_text())
    return {}


def save_conf(conf):
    CONF_FILE.write_text(json.dumps(conf, indent=2))


# ---------- fase 1: provisioning sulle credenziali ----------

def on_ac_hotspot():
    """True se il gateway e' il clima (MAC del modulo Gree, OUI f4:91:1e)."""
    mac = arp_mac_for(AP_IP)
    return mac is not None and mac.startswith("f4:91:1e")


def request_credentials_interactive(defaults):
    ssid = input(f"SSID Wi-Fi di casa [{defaults.get('ssid', '')}]: ").strip() \
        or defaults.get("ssid")
    psw = input(f"Password Wi-Fi di casa [{ '*' * 8 if defaults.get('psw') else '' }]: ").strip() \
        or defaults.get("psw")
    if not ssid:
        sys.exit("Serve l'SSID.")
    if not psw:
        ans = input("Password vuota: la rete di casa e' aperta? [s/N]: ").strip().lower()
        if ans != "s":
            sys.exit("Serve la password (o conferma rete aperta con 's').")
    return ssid, psw


def phase_provisioning(ssid, psw, repeat):
    msg = json.dumps({"psw": psw, "ssid": ssid, "t": "wlan"}).encode()
    print(f"Invio credenziali per '{ssid}' a {AP_IP}:{PORT} "
          f"({repeat} tentativi, 2s di distanza)...")
    print("Quando il modulo le accetta, l'hotspot del clima sparira' "
          "dal Wi-Fi e andra' a classificarlo in rete di casa.\n")
    got = None
    for i in range(1, repeat + 1):
        if not on_ac_hotspot():
            if arp_mac_for(AP_IP) is None:
                print(f"[{i}/{repeat}] Il clima {AP_IP} non risponde piu' qui: "
                      "probabilmente l'aggancio e' riuscito ed ha lasciato l'AP.")
                break
        resp = udp_send_recv(AP_IP, msg)
        if resp:
            got = resp
            try:
                print(f"[{i}/{repeat}]   <- {json.loads(resp.decode())}")
            except Exception:
                print(f"[{i}/{repeat}]   <- {resp!r}")
        else:
            print(f"[{i}/{repeat}]   <- (nessun)")
        time.sleep(2)
    if got:
        print("\nIl modulo ha risposto: credenziali in fase di apprendimento.")
    print("\nOra: verifica che l'hotspot del clima sia sparito, rientra nella "
          "rete Wi-Fi di casa e rilancia  python gree_setup.py")


# ---------- fase 2: verifica aggancio su rete di casa ----------

def scan_broadcast(timeout=3):
    found = []
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    s.settimeout(timeout)
    s.sendto(b'{"t":"scan"}', ("255.255.255.255", PORT))
    try:
        while True:
            data, addr = s.recvfrom(65535)
            found.append(addr[0])
    except socket.timeout:
        pass
    return found


def phase_verify(subnet):
    # prima broadcast...
    found = scan_broadcast()
    if found:
        print(f"Rispondono allo scan: {', '.join(found)}")
    else:
        # ...poi uno scan diretto a ogni IP della /24 (alcuni moduli
        # rispondono al diretto e non al broadcast)
        print("Broadcast muto: provo lo scan diretto a ogni IP del "
              f"{subnet}/24 (circa 20 s)...")
        prefix = subnet.rsplit(".", 1)[0]
        for last in range(1, 255):
            ip = f"{prefix}.{last}"
            resp = udp_send_recv(ip, b'{"t":"scan"}', timeout=0.35)
            if resp:
                found.append(ip)
                print(f"  -> trova {ip}: {resp.decode(errors='replace')[:120]}")
    if not found:
        print("\nNessun clima ancora agganciato a questa rete.")
        print("Rimedi:")
        print("  - riporta il clima in modalita' configurazione")
        print("    (telecomando: premi WIFI e tieni premuto 10 s, oppure")
        print("     sul pannello del clima, FIN + WIFI per 10 s)")
        print("  - poi rientra nell'hotspot '1e538ca2' (psw 12345678)")
        print("  - e rilancia  python gree_setup.py")
    else:
        print("\nIl clima e' agganciato! Per provarlo:")
        for ip in found:
            print(f"  python gree_test.py status {ip}")
            print(f"  python gree_test.py on     {ip}")
        print("  (all'avvio del primo comando lo script fa il 'bind' e salva la chiave)")


# ---------- CLI ----------

def main():
    p = argparse.ArgumentParser(description="Setup Gree: aggancio Wi-Fi di casa")
    p.add_argument("--ssid", help="SSID Wi-Fi di casa (fase 1)")
    p.add_argument("--psw", help="Password Wi-Fi di casa (fase 1)")
    p.add_argument("--repeat", type=int, default=10,
                   help="quanti tentativi di invio credenziali (default 10)")
    p.add_argument("--subnet", default=None,
                   help="sottorete casa, es. 192.168.1.1 (per la verifica)")
    a = p.parse_args()

    if on_ac_hotspot():
        print("=== FASE 1: ti sei sicuramente attaccato all'hotspot del "
              "climatizzatore (gateway f4:91:1e:...) ===\n")
        conf = load_conf()
        if a.ssid and a.psw:
            ssid, psw = a.ssid, a.psw
        elif a.ssid:
            ssid = a.ssid
            psw = conf.get("psw") or input("Password Wi-Fi di casa: ")
        else:
            ssid, psw = request_credentials_interactive(conf)
        conf.update({"ssid": ssid, "psw": psw})
        save_conf(conf)
        print("(credenziali salvate in gree_wifi.json)\n")
        phase_provisioning(ssid, psw, a.repeat)
        return

    print("=== FASE 2: sei su una rete normale -> verifico se il clima "
          "e' agganciato ===\n")
    subnet = a.subnet
    if not subnet:
        ip = socket.gethostbyname(socket.gethostname()) if False else None
        out = subprocess.run(["ip", "-4", "-o", "addr", "show", "scope", "global"],
                             capture_output=True, text=True).stdout
        for line in out.splitlines():
            f = line.split()
            if f and f[2].split("/")[0].startswith("192.168."):
                subnet = f[2].split("/")[0] if "." in f[2] else None
        if not subnet:
            sys.exit("Non ho capito la sottorete; passa --subnet 192.168.1.1")
    phase_verify(subnet)


if __name__ == "__main__":
    main()
