from fastapi import FastAPI, Header, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from ping3 import ping
from datetime import datetime, timedelta
import ipaddress
import uvicorn
import sqlite3

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

DB_PATH = "monitor.db"
status_terakhir = {}
internet_terakhir = None
iot_data = {}
iot_last_seen = {}


# ─────────────────────────────────────────
# HELPER: Validasi IP
# ─────────────────────────────────────────
def ip_valid(ip: str) -> bool:
    try:
        ipaddress.ip_address(ip.strip())
        return True
    except ValueError:
        return False

def safe_ping(ip: str, timeout: int = 1):
    """Ping dengan validasi IP terlebih dahulu, return None jika IP invalid."""
    if not ip_valid(ip):
        return None
    return ping(ip.strip(), timeout=timeout)


# ─────────────────────────────────────────
# DATABASE
# ─────────────────────────────────────────
def init_db():
    conn = sqlite3.connect(DB_PATH)
    c = conn.cursor()
    c.execute('''CREATE TABLE IF NOT EXISTS perangkat
                 (id INTEGER PRIMARY KEY AUTOINCREMENT,
                  username TEXT DEFAULT 'default',
                  nama TEXT, ip TEXT, kategori TEXT)''')
    c.execute('''CREATE TABLE IF NOT EXISTS log_aktivitas
                 (id INTEGER PRIMARY KEY AUTOINCREMENT,
                  username TEXT DEFAULT 'default',
                  perangkat TEXT, pesan TEXT,
                  waktu TEXT, tanggal TEXT, isOnline INTEGER)''')
    try:
        c.execute("ALTER TABLE perangkat ADD COLUMN username TEXT DEFAULT 'default'")
    except:
        pass
    try:
        c.execute("ALTER TABLE log_aktivitas ADD COLUMN username TEXT DEFAULT 'default'")
    except:
        pass
    conn.commit()
    conn.close()

init_db()


def get_perangkat(username: str = "default"):
    conn = sqlite3.connect(DB_PATH)
    c = conn.cursor()
    c.execute("SELECT id, nama, ip, kategori FROM perangkat WHERE username=?", (username,))
    rows = c.fetchall()
    conn.close()
    return [{"id": r[0], "nama": r[1], "ip": r[2], "kategori": r[3]} for r in rows]


def tambah_log(perangkat, pesan, isOnline, username: str = "default"):
    conn = sqlite3.connect(DB_PATH)
    c = conn.cursor()
    c.execute("""INSERT INTO log_aktivitas
                 (username, perangkat, pesan, waktu, tanggal, isOnline)
                 VALUES (?,?,?,?,?,?)""",
              (username, perangkat, pesan,
               datetime.now().strftime("%H:%M"),
               datetime.now().strftime("%d/%m/%Y"),
               1 if isOnline else 0))
    c.execute("""DELETE FROM log_aktivitas
                 WHERE id NOT IN (
                     SELECT id FROM log_aktivitas
                     WHERE username=?
                     ORDER BY id DESC LIMIT 10000
                 ) AND username=?""", (username, username))
    conn.commit()
    conn.close()


def get_logs(username: str = "default"):
    conn = sqlite3.connect(DB_PATH)
    c = conn.cursor()
    c.execute("""SELECT perangkat, pesan, waktu, tanggal, isOnline
                 FROM log_aktivitas
                 WHERE username=?
                 ORDER BY id DESC LIMIT 50""", (username,))
    rows = c.fetchall()
    conn.close()
    return [{"perangkat": r[0], "pesan": r[1], "waktu": r[2],
             "tanggal": r[3], "isOnline": bool(r[4])} for r in rows]


# ─────────────────────────────────────────
# ENDPOINTS
# ─────────────────────────────────────────
@app.get("/devices")
def get_devices(x_username: str = Header(default="default")):
    perangkat = get_perangkat(x_username)
    hasil = []

    for p in perangkat:
        key = f"{x_username}_{p['nama']}"
        status_lama = status_terakhir.get(key)

        # Kalau IP tidak valid, langsung tandai ERROR dan skip ping
        if not ip_valid(p.get("ip", "")):
            hasil.append({
                "id": p["id"],
                "nama": p["nama"],
                "ip": p["ip"],
                "kategori": p["kategori"],
                "status": "ERROR",
                "latency": 0,
                "error": "Format IP tidak valid"
            })
            continue

        if p["kategori"] == "IoT":
            last_seen = iot_last_seen.get(p["nama"])
            if last_seen is None:
                status = "OFFLINE"
                lat = 0
            elif datetime.now() - last_seen > timedelta(seconds=15):
                status = "OFFLINE"
                lat = 0
            else:
                status = "ONLINE"
                latency = safe_ping(p["ip"], timeout=1)
                lat = int(latency * 1000) if latency else 0
        else:
            latency = safe_ping(p["ip"], timeout=1)
            if latency is None:
                status = "OFFLINE"
                lat = 0
            else:
                status = "ONLINE"
                lat = int(latency * 1000)
                if lat > 200:
                    tambah_log(p["nama"], f"Latency Tinggi ({lat}ms)", False, x_username)

        if status_lama != status:
            if status == "OFFLINE":
                tambah_log(p["nama"], "Tidak Merespon", False, x_username)
            elif status == "ONLINE" and status_lama == "OFFLINE":
                tambah_log(p["nama"], "Kembali Normal", True, x_username)
            status_terakhir[key] = status

        hasil.append({
            "id": p["id"],
            "nama": p["nama"],
            "ip": p["ip"],
            "kategori": p["kategori"],
            "status": status,
            "latency": lat
        })

    return hasil


@app.get("/logs")
def get_logs_endpoint(x_username: str = Header(default="default")):
    return get_logs(x_username)


@app.post("/devices/tambah")
def tambah_device(data: dict, x_username: str = Header(default="default")):
    nama = data.get("nama", "").strip()
    ip = data.get("ip", "").strip()
    kategori = data.get("kategori", "").strip()

    # Validasi field tidak boleh kosong
    if not nama or not ip or not kategori:
        raise HTTPException(status_code=400, detail="Field nama, ip, dan kategori wajib diisi")

    # Validasi format IP
    if not ip_valid(ip):
        raise HTTPException(status_code=400, detail=f"Format IP '{ip}' tidak valid")

    conn = sqlite3.connect(DB_PATH)
    c = conn.cursor()
    c.execute("INSERT INTO perangkat (username, nama, ip, kategori) VALUES (?,?,?,?)",
              (x_username, nama, ip, kategori))
    conn.commit()
    conn.close()
    tambah_log(nama, "Device ditambahkan", True, x_username)
    return {"ok": True}


@app.delete("/devices/{device_id}")
def hapus_device(device_id: int, x_username: str = Header(default="default")):
    conn = sqlite3.connect(DB_PATH)
    c = conn.cursor()
    c.execute("SELECT nama FROM perangkat WHERE id=? AND username=?", (device_id, x_username))
    row = c.fetchone()
    if row:
        tambah_log(row[0], "Device dihapus", False, x_username)
    c.execute("DELETE FROM perangkat WHERE id=? AND username=?", (device_id, x_username))
    conn.commit()
    conn.close()
    return {"ok": True}


@app.get("/ping/{ip}")
def cek_koneksi(ip: str):
    if not ip_valid(ip):
        raise HTTPException(status_code=400, detail=f"Format IP '{ip}' tidak valid")
    latency = safe_ping(ip, timeout=1)
    if latency is None:
        return {"status": "OFFLINE", "latency": 0}
    return {"status": "ONLINE", "latency": int(latency * 1000)}


@app.post("/iot/update")
def iot_update(data: dict):
    global iot_data
    iot_data = data
    device_name = data.get("device", "IoT")
    iot_last_seen[device_name] = datetime.now()
    pakan = data.get("persentasePakan", 100)
    air = data.get("persentaseAir", 100)
    if isinstance(pakan, (int, float)) and pakan < 20:
        tambah_log(device_name, f"Pakan Hampir Habis ({int(pakan)}%)", False)
    if isinstance(air, (int, float)) and air < 20:
        tambah_log(device_name, f"Air Hampir Habis ({int(air)}%)", False)
    return {"ok": True}


@app.get("/iot/status")
def iot_status():
    global iot_data
    for device_name, last_seen in iot_last_seen.items():
        if datetime.now() - last_seen > timedelta(seconds=15):
            iot_data["status"] = "OFFLINE"
            iot_data["latency"] = 0
            if status_terakhir.get(device_name) != "OFFLINE":
                tambah_log(device_name, "Tidak Merespon - Device mati", False)
                status_terakhir[device_name] = "OFFLINE"
        else:
            if status_terakhir.get(device_name) == "OFFLINE":
                tambah_log(device_name, "Kembali Normal", True)
                status_terakhir[device_name] = "ONLINE"
            iot_data["status"] = "ONLINE"
    return iot_data


@app.get("/diagnosis")
def get_diagnosis(x_username: str = Header(default="default")):
    global internet_terakhir
    hasil = []

    router = safe_ping("192.168.1.1", timeout=2)
    router_latency = int(router * 1000) if router else 0

    internet1 = safe_ping("8.8.8.8", timeout=2)
    internet2 = safe_ping("1.1.1.1", timeout=2)
    internet_ok = internet1 is not None or internet2 is not None

    if internet_terakhir is not None and internet_terakhir != internet_ok:
        if not internet_ok:
            tambah_log("SISTEM", "Koneksi Internet Terputus", False, x_username)
        else:
            tambah_log("SISTEM", "Koneksi Internet Kembali Normal", True, x_username)
    internet_terakhir = internet_ok

    if router is None:
        hasil.append({
            "level": "KRITIS",
            "judul": "Jaringan Lokal Terputus Total",
            "icon": "🔴",
            "kemungkinan": [
                "Router atau ONT mati",
                "Kabel power tercabut",
                "Listrik mati di area router"
            ],
            "solusi": [
                "Cek power router dan ONT",
                "Pastikan kabel power terpasang",
                "Cek MCB listrik"
            ]
        })
        tambah_log("SISTEM", "Jaringan Lokal Terputus Total", False, x_username)
    elif not internet_ok:
        hasil.append({
            "level": "TINGGI",
            "judul": "Koneksi Internet Terputus (Kemungkinan LOS)",
            "icon": "🟠",
            "kemungkinan": [
                "Fiber optik putus atau gangguan",
                "Lampu LOS di ONT menyala",
                "Gangguan dari provider IndiHome",
                "Kabel WAN router tercabut"
            ],
            "solusi": [
                "Cek lampu LOS di ONT",
                "Hubungi IndiHome di 147",
                "Cek kabel dari ONT ke router",
                "Jangan cabut kabel fiber sendiri"
            ]
        })
    elif router_latency > 100:
        hasil.append({
            "level": "SEDANG",
            "judul": f"Jaringan Tidak Stabil (Latency {router_latency}ms)",
            "icon": "🟡",
            "kemungkinan": [
                "Jaringan overload atau padat",
                "Router butuh restart",
                "Terlalu banyak device terkoneksi"
            ],
            "solusi": [
                "Restart router",
                "Kurangi device yang terkoneksi",
                "Hubungi ISP jika berlanjut"
            ]
        })
    else:
        internet_latency = int(internet1 * 1000) if internet1 else int(internet2 * 1000)
        hasil.append({
            "level": "NORMAL",
            "judul": "Jaringan Berjalan Normal",
            "icon": "🟢",
            "kemungkinan": [],
            "solusi": [],
            "info": f"Router: {router_latency}ms | Internet: {internet_latency}ms"
        })

    perangkat = get_perangkat(x_username)
    offline_devices = []
    for p in perangkat:
        if p["kategori"] == "Jaringan":
            cek = safe_ping(p["ip"], timeout=1)
            if cek is None:
                offline_devices.append(p["nama"])

    if offline_devices and router is not None:
        hasil.append({
            "level": "SEDANG",
            "judul": "Perangkat Tidak Merespon",
            "icon": "🟡",
            "kemungkinan": [
                f"{', '.join(offline_devices)} tidak dapat dijangkau",
                "Kabel LAN ke perangkat tercabut",
                "Perangkat mati atau dimatikan"
            ],
            "solusi": [
                "Cek kabel LAN ke perangkat",
                "Pastikan perangkat menyala",
                "Cek port switch/router"
            ]
        })

    return hasil


@app.delete("/logs/hapus-semua")
def hapus_semua_log(x_username: str = Header(default="default")):
    conn = sqlite3.connect(DB_PATH)
    c = conn.cursor()
    c.execute("DELETE FROM log_aktivitas WHERE username=?", (x_username,))
    conn.commit()
    conn.close()
    return {"ok": True}


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
