#!/usr/bin/env python3
"""
gree_test.py - Prova del protocollo locale Gree / EWPE Smart
(es. Viessmann Vitoclima 232-S con app EWPE Smart).

Requisiti: Python 3.8+  e  pip install pycryptodome
Va lanciato da un PC collegato alla STESSA rete Wi-Fi del climatizzatore.

Uso:
  python gree_test.py scan                     # trova i climatizzatori in rete
  python gree_test.py status 192.168.1.50      # legge lo stato
  python gree_test.py on 192.168.1.50          # accende
  python gree_test.py off 192.168.1.50         # spegne
  python gree_test.py temp 192.168.1.50 24     # imposta 24 °C
  python gree_test.py mode 192.168.1.50 cool   # auto|cool|dry|fan|heat
  python gree_test.py fan 192.168.1.50 auto    # auto|low|medlow|med|medhigh|high

Al primo comando verso un IP lo script fa il "bind": chiede al climatizzatore
la sua chiave AES personale e la salva in gree_keys.json, accanto allo script.

Protocollo in breve (utile per il porting in Kotlin):
  - UDP porta 7000, messaggi JSON
  - il campo "pack" e' JSON cifrato AES-128 e codificato in base64
  - firmware vecchi: AES-ECB, padding PKCS7, chiave generica "a3K8Bx%2r8Y7#xDh"
  - firmware nuovi:  AES-GCM, chiave generica "{yxAHAY_Lm6pbC/<",
                     IV fisso, AAD "qualcomm-test", tag in base64 nel campo "tag"
  - scan e bind usano la chiave generica, tutto il resto la chiave del dispositivo
"""
import argparse
import base64
import json
import socket
import sys
from pathlib import Path

try:
    from Crypto.Cipher import AES
except ImportError:
    sys.exit("Manca pycryptodome: esegui  pip install pycryptodome")

PORT = 7000
KEY_ECB = b"a3K8Bx%2r8Y7#xDh"
KEY_GCM = b"{yxAHAY_Lm6pbC/<"
GCM_IV = bytes([0x54, 0x40, 0x78, 0x44, 0x49, 0x67, 0x5A, 0x51, 0x6C, 0x5E, 0x63, 0x13])
GCM_AAD = b"qualcomm-test"
KEYS_FILE = Path(__file__).with_name("gree_keys.json")

MODES = {"auto": 0, "cool": 1, "dry": 2, "fan": 3, "heat": 4}
FANS = {"auto": 0, "low": 1, "medlow": 2, "med": 3, "medhigh": 4, "high": 5}
STATUS_COLS = ["Pow", "Mod", "SetTem", "TemUn", "WdSpd", "TemSen",
               "Lig", "SwUpDn", "Quiet", "Tur", "SvSt"]


# ---------- cifratura ----------

def encrypt(obj, key, gcm):
    data = json.dumps(obj, separators=(",", ":")).encode()
    if gcm:
        c = AES.new(key, AES.MODE_GCM, nonce=GCM_IV)
        c.update(GCM_AAD)
        ct, tag = c.encrypt_and_digest(data)
        return base64.b64encode(ct).decode(), base64.b64encode(tag).decode()
    n = 16 - len(data) % 16
    data += bytes([n]) * n
    return base64.b64encode(AES.new(key, AES.MODE_ECB).encrypt(data)).decode(), None


def decrypt(pack, key, tag=None):
    raw = base64.b64decode(pack)
    if tag:
        c = AES.new(key, AES.MODE_GCM, nonce=GCM_IV)
        c.update(GCM_AAD)
        data = c.decrypt_and_verify(raw, base64.b64decode(tag))
    else:
        data = AES.new(key, AES.MODE_ECB).decrypt(raw)
    text = data.decode(errors="ignore")
    return json.loads(text[: text.rfind("}") + 1])  # toglie padding/sporcizia finale


# ---------- rete ----------

def udp_request(ip, msg, timeout=5):
    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
        s.settimeout(timeout)
        s.sendto(json.dumps(msg).encode(), (ip, PORT))
        data, _ = s.recvfrom(65535)
    return json.loads(data)


def scan(target="255.255.255.255", timeout=3):
    found = []
    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
        s.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
        s.settimeout(timeout)
        s.sendto(b'{"t":"scan"}', (target, PORT))
        try:
            while True:
                data, addr = s.recvfrom(65535)
                resp = json.loads(data)
                gcm = "tag" in resp
                info = decrypt(resp["pack"], KEY_GCM if gcm else KEY_ECB, resp.get("tag"))
                found.append({"ip": addr[0], "mac": info.get("mac") or resp.get("cid"),
                              "name": info.get("name", ""), "ver": info.get("ver", ""),
                              "gcm": gcm})
        except socket.timeout:
            pass
    return found


def load_keys():
    if KEYS_FILE.exists():
        return json.loads(KEYS_FILE.read_text())
    return {}


def bind(ip):
    devs = scan(ip)
    if not devs:
        sys.exit(f"Nessuna risposta da {ip}: controlla IP e che il PC sia sulla stessa rete.")
    dev = devs[0]
    gkey = KEY_GCM if dev["gcm"] else KEY_ECB
    pack, tag = encrypt({"mac": dev["mac"], "t": "bind", "uid": 0}, gkey, dev["gcm"])
    msg = {"cid": "app", "i": 1, "t": "pack", "uid": 0, "tcid": dev["mac"], "pack": pack}
    if tag:
        msg["tag"] = tag
    resp = udp_request(ip, msg)
    data = decrypt(resp["pack"], gkey, resp.get("tag"))
    if data.get("t") != "bindok":
        sys.exit(f"Bind fallito: {data}")
    dev["key"] = data["key"]
    keys = load_keys()
    keys[ip] = dev
    KEYS_FILE.write_text(json.dumps(keys, indent=2))
    print(f"Bind ok con {dev['mac']} ({'GCM' if dev['gcm'] else 'ECB'}), chiave salvata.")
    return dev


def device_request(ip, payload):
    dev = load_keys().get(ip) or bind(ip)
    key = dev["key"].encode()
    pack, tag = encrypt(payload, key, dev["gcm"])
    msg = {"cid": "app", "i": 0, "t": "pack", "uid": 0, "tcid": dev["mac"], "pack": pack}
    if tag:
        msg["tag"] = tag
    resp = udp_request(ip, msg)
    return decrypt(resp["pack"], key, resp.get("tag"))


def get_status(ip):
    dev = load_keys().get(ip) or bind(ip)
    data = device_request(ip, {"cols": STATUS_COLS, "mac": dev["mac"], "t": "status"})
    return dict(zip(data["cols"], data["dat"]))


def send_cmd(ip, **values):
    data = device_request(ip, {"opt": list(values), "p": list(values.values()), "t": "cmd"})
    ok = data.get("r") == 200
    print("OK" if ok else "Risposta inattesa:", "" if ok else data)


# ---------- CLI ----------

def main():
    p = argparse.ArgumentParser(description="Test protocollo Gree/EWPE Smart")
    p.add_argument("cmd", choices=["scan", "status", "on", "off", "temp", "mode", "fan"])
    p.add_argument("ip", nargs="?")
    p.add_argument("value", nargs="?")
    a = p.parse_args()

    if a.cmd == "scan":
        devs = scan(a.ip or "255.255.255.255")
        if not devs:
            print("Nessun climatizzatore trovato. Prova: python gree_test.py scan 192.168.1.255")
        for d in devs:
            print(f"{d['ip']:15}  mac={d['mac']}  nome={d['name']}  fw={d['ver']}  "
                  f"cifratura={'GCM' if d['gcm'] else 'ECB'}")
        return

    if not a.ip:
        sys.exit("Serve l'IP del climatizzatore (lo trovi con: python gree_test.py scan)")

    if a.cmd == "status":
        st = get_status(a.ip)
        t = st.get("TemSen")
        if isinstance(t, int) and t >= 40:  # molti firmware aggiungono 40 alla temperatura
            st["TemSen"] = t - 40
        inv_modes = {v: k for k, v in MODES.items()}
        inv_fans = {v: k for k, v in FANS.items()}
        print(f"Acceso:           {'si' if st.get('Pow') else 'no'}")
        print(f"Modalita:         {inv_modes.get(st.get('Mod'), st.get('Mod'))}")
        print(f"Temp. impostata:  {st.get('SetTem')} °C")
        print(f"Temp. ambiente:   {st.get('TemSen')} °C")
        print(f"Ventola:          {inv_fans.get(st.get('WdSpd'), st.get('WdSpd'))}")
        print(f"Grezzo:           {st}")
    elif a.cmd == "on":
        send_cmd(a.ip, Pow=1)
    elif a.cmd == "off":
        send_cmd(a.ip, Pow=0)
    elif a.cmd == "temp":
        t = int(a.value)
        if not 16 <= t <= 30:
            sys.exit("Temperatura tra 16 e 30 °C")
        send_cmd(a.ip, TemUn=0, SetTem=t)
    elif a.cmd == "mode":
        if a.value not in MODES:
            sys.exit(f"Modalita valide: {', '.join(MODES)}")
        send_cmd(a.ip, Mod=MODES[a.value])
    elif a.cmd == "fan":
        if a.value not in FANS:
            sys.exit(f"Velocita valide: {', '.join(FANS)}")
        send_cmd(a.ip, WdSpd=FANS[a.value])


if __name__ == "__main__":
    main()
