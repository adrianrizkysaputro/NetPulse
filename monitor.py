from fastapi import FastAPI, Header, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from ping3 import ping
from datetime import datetime, timedelta
import ipaddress
import uvicorn
import sqlite3

# ─────────────────────────────────────────
# FIREBASE FCM INIT
# ─────────────────────────────────────────
try:
    import firebase_admin
    from firebase_admin import credentials, messaging

    cred = credentials.Certificate("firebase-key.json")
    firebase_admin.initialize_app(cred)
    FCM_ENABLED = True
    print("[FCM] Firebase berhasil diinisialisasi")
except Exception as e:
    FCM_ENABLED = False
    print(f"[FCM] Firebase tidak aktif: {e}")

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
iot_data = {}       # { "192.168.1.11": {...data...}, "192.168.1.12": {...data...} }
iot_last_seen = {}  # { "192.168.1.11": datetime, "192.168.1.12": datetime }

# ─────────────────────────────────────────
# COOLDOWN NOTIFIKASI (anti-spam)
# ─────────────────────────────────────────
notif_cooldown: dict = {}
COOLDOWN_MENIT = 10  # notif yang sama tidak akan muncul lagi dalam 10 menit

def boleh_kirim_notif(key: str) -> bool:
    """Cek apakah notif dengan key ini boleh dikirim (cooldown check)."""
    waktu_terakhir = notif_cooldown.get(key)
    if waktu_terakhir is None:
        notif_cooldown[key] = datetime.now()
        return True
    if datetime.now() - waktu_terakhir > timedelta(minutes=COOLDOWN_MENIT):
        notif_cooldown[key] = datetime.now()
        return True
    return False

def reset_cooldown(key: str):
    """Reset cooldown saat status berubah (misal OFFLINE → ONLINE)."""
    notif_cooldown.pop(key, None)


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


# ─────────────────────────────────────────
# DATABASE: FCM TOKEN
# ─────────────────────────────────────────
def init_fcm_table():
    conn = sqlite3.connect(DB_PATH)
    c = conn.cursor()
    c.execute('''CREATE TABLE IF NOT EXISTS fcm_tokens
                 (id INTEGER PRIMARY KEY AUTOINCREMENT,
                  username TEXT NOT NULL,
                  token TEXT NOT NULL,
                  platform TEXT DEFAULT 'web',
                  updated_at TEXT,
                  UNIQUE(username, token))''')
    conn.commit()
    conn.close()

init_fcm_table()


def simpan_fcm_token(username: str, token: str, platform: str = "web"):
    conn = sqlite3.connect(DB_PATH)
    c = conn.cursor()
    c.execute("""INSERT INTO fcm_tokens (username, token, platform, updated_at)
                 VALUES (?, ?, ?, ?)
                 ON CONFLICT(username, token) DO UPDATE SET updated_at=excluded.updated_at""",
              (username, token, platform, datetime.now().isoformat()))
    conn.commit()
    conn.close()


def hapus_fcm_token(username: str, token: str):
    conn = sqlite3.connect(DB_PATH)
    c = conn.cursor()
    c.execute("DELETE FROM fcm_tokens WHERE username=? AND token=?", (username, token))
    conn.commit()
    conn.close()


def get_fcm_tokens(username: str) -> list:
    conn = sqlite3.connect(DB_PATH)
    c = conn.cursor()
    c.execute("SELECT token FROM fcm_tokens WHERE username=?", (username,))
    rows = c.fetchall()
    conn.close()
    return [r[0] for r in rows]


# ─────────────────────────────────────────
# FCM: Kirim Notifikasi
# ─────────────────────────────────────────
def kirim_notif(username: str, judul: str, isi: str, data: dict = None):
    """Kirim push notification ke semua device milik username."""
    if not FCM_ENABLED:
        return

    tokens = get_fcm_tokens(username)
    if not tokens:
        return

    notif_data = {k: str(v) for k, v in (data or {}).items()}

    gagal = []
    for token in tokens:
        try:
            msg = messaging.Message(
                notification=messaging.Notification(title=judul, body=isi),
                data=notif_data,
                token=token,
                android=messaging.AndroidConfig(
                    priority="high",
                    notification=messaging.AndroidNotification(
                        sound="default",
                        channel_id="monitor_alerts"
                    )
                ),
                apns=messaging.APNSConfig(
                    payload=messaging.APNSPayload(
                        aps=messaging.Aps(sound="default")
                    )
                )
            )
            messaging.send(msg)
            print(f"[FCM] Notif terkirim ke {username}: {judul}")
        except messaging.UnregisteredError:
            print(f"[FCM] Token tidak valid, hapus: {token[:20]}...")
            gagal.append(token)
        except Exception as e:
            print(f"[FCM] Gagal kirim notif: {e}")

    # Hapus token yang sudah tidak valid
    for token in gagal:
        hapus_fcm_token(username, token)


def kirim_notif_semua(judul: str, isi: str, data: dict = None):
    """Kirim notifikasi ke semua username yang punya token (untuk IoT global)."""
    if not FCM_ENABLED:
        return
    conn = sqlite3.connect(DB_PATH)
    c = conn.cursor()
    c.execute("SELECT DISTINCT username FROM fcm_tokens")
    usernames = [r[0] for r in c.fetchall()]
    conn.close()
    for username in usernames:
        kirim_notif(username, judul, isi, data)


# ─────────────────────────────────────────
# DATABASE HELPERS
# ─────────────────────────────────────────
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
# ENDPOINTS: Device Monitoring
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
            last_seen = iot_last_seen.get(p["ip"])  # pakai IP bukan nama
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
                # Notif latency tinggi — dengan cooldown agar tidak spam
                if lat > 200:
                    cooldown_key = f"{x_username}_{p['nama']}_latency"
                    if boleh_kirim_notif(cooldown_key):
                        tambah_log(p["nama"], f"Latency Tinggi ({lat}ms)", False, x_username)
                        kirim_notif(
                            x_username,
                            f"⚠️ Latency Tinggi: {p['nama']}",
                            f"Latency {p['nama']} mencapai {lat}ms (>200ms)",
                            {"type": "high_latency", "device": p["nama"], "latency": str(lat)}
                        )

        if status_lama != status:
            if status == "OFFLINE":
                cooldown_key = f"{x_username}_{p['nama']}_offline"
                if boleh_kirim_notif(cooldown_key):
                    tambah_log(p["nama"], "Tidak Merespon", False, x_username)
                    kirim_notif(
                        x_username,
                        f"🔴 {p['nama']} OFFLINE",
                        f"Perangkat {p['nama']} ({p['ip']}) tidak merespon",
                        {"type": "device_offline", "device": p["nama"], "ip": p["ip"]}
                    )
            elif status == "ONLINE" and status_lama == "OFFLINE":
                # Device kembali online → reset cooldown offline & latency
                reset_cooldown(f"{x_username}_{p['nama']}_offline")
                reset_cooldown(f"{x_username}_{p['nama']}_latency")
                tambah_log(p["nama"], "Kembali Normal", True, x_username)
                kirim_notif(
                    x_username,
                    f"🟢 {p['nama']} Kembali Online",
                    f"Perangkat {p['nama']} ({p['ip']}) sudah kembali normal",
                    {"type": "device_online", "device": p["nama"], "ip": p["ip"]}
                )
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


# ─────────────────────────────────────────
# ENDPOINTS: IoT
# ─────────────────────────────────────────
@app.post("/iot/update")
def iot_update(data: dict):
    global iot_data
    ip = data.get("ip", "")
    device_name = data.get("device", "IoT")

    # Simpan data per IP — nama bebas, IP sebagai kunci
    if ip:
        iot_data[ip] = data
        iot_last_seen[ip] = datetime.now()
    else:
        # Fallback pakai nama kalau tidak ada IP
        iot_data[device_name] = data
        iot_last_seen[device_name] = datetime.now()

    pakan = data.get("persentasePakan", 100)
    air = data.get("persentaseAir", 100)

    if isinstance(pakan, (int, float)) and pakan < 20:
        cooldown_key = f"iot_{ip or device_name}_pakan"
        if boleh_kirim_notif(cooldown_key):
            tambah_log(device_name, f"Pakan Hampir Habis ({int(pakan)}%)", False)
            kirim_notif_semua(
                f"🍚 Pakan Hampir Habis!",
                f"Sisa pakan {device_name}: {int(pakan)}%",
                {"type": "pakan_low", "device": device_name, "value": str(int(pakan))}
            )

    if isinstance(air, (int, float)) and air < 20:
        cooldown_key = f"iot_{ip or device_name}_air"
        if boleh_kirim_notif(cooldown_key):
            tambah_log(device_name, f"Air Hampir Habis ({int(air)}%)", False)
            kirim_notif_semua(
                f"💧 Air Hampir Habis!",
                f"Sisa air {device_name}: {int(air)}%",
                {"type": "air_low", "device": device_name, "value": str(int(air))}
            )

    # Reset cooldown pakan/air jika sudah terisi kembali
    if isinstance(pakan, (int, float)) and pakan >= 20:
        reset_cooldown(f"iot_{ip or device_name}_pakan")
    if isinstance(air, (int, float)) and air >= 20:
        reset_cooldown(f"iot_{ip or device_name}_air")

    return {"ok": True}


@app.get("/iot/status")
def iot_status_all():
    """Ambil semua data IoT (untuk kompatibilitas lama)."""
    return iot_data

@app.get("/iot/status/{ip}")
def iot_status(ip: str):
    """Ambil data IoT berdasarkan IP device."""
    global iot_data
    data = iot_data.get(ip, {})
    last_seen = iot_last_seen.get(ip)

    if last_seen is None or datetime.now() - last_seen > timedelta(seconds=15):
        if status_terakhir.get(ip) != "OFFLINE":
            device_name = data.get("device", ip)
            cooldown_key = f"iot_{ip}_offline"
            if boleh_kirim_notif(cooldown_key):
                tambah_log(device_name, "Tidak Merespon - Device mati", False)
                kirim_notif_semua(
                    f"🔴 IoT {device_name} OFFLINE",
                    f"Perangkat IoT {device_name} tidak mengirim data lebih dari 15 detik",
                    {"type": "iot_offline", "device": device_name}
                )
            status_terakhir[ip] = "OFFLINE"
        return {**data, "status": "OFFLINE"}
    else:
        if status_terakhir.get(ip) == "OFFLINE":
            device_name = data.get("device", ip)
            reset_cooldown(f"iot_{ip}_offline")
            reset_cooldown(f"iot_{ip}_pakan")
            reset_cooldown(f"iot_{ip}_air")
            tambah_log(device_name, "Kembali Normal", True)
            kirim_notif_semua(
                f"🟢 IoT {device_name} Kembali Online",
                f"Perangkat IoT {device_name} sudah kembali mengirim data",
                {"type": "iot_online", "device": device_name}
            )
            status_terakhir[ip] = "ONLINE"
        return {**data, "status": "ONLINE"}


# ─────────────────────────────────────────
# ENDPOINTS: Diagnosis
# ─────────────────────────────────────────
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
            cooldown_key = f"{x_username}_internet_down"
            if boleh_kirim_notif(cooldown_key):
                kirim_notif(
                    x_username,
                    "🌐 Internet Terputus!",
                    "Koneksi internet tidak dapat dijangkau. Cek router atau hubungi ISP.",
                    {"type": "internet_down"}
                )
        else:
            # Internet kembali → reset cooldown
            reset_cooldown(f"{x_username}_internet_down")
            reset_cooldown(f"{x_username}_router_down")
            tambah_log("SISTEM", "Koneksi Internet Kembali Normal", True, x_username)
            kirim_notif(
                x_username,
                "🌐 Internet Kembali Normal",
                "Koneksi internet sudah pulih kembali.",
                {"type": "internet_up"}
            )
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
        cooldown_key = f"{x_username}_router_down"
        if boleh_kirim_notif(cooldown_key):
            kirim_notif(
                x_username,
                "🔴 Jaringan Lokal Terputus!",
                "Router tidak dapat dijangkau. Kemungkinan router mati atau kabel power tercabut.",
                {"type": "network_down"}
            )
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


# ─────────────────────────────────────────
# ENDPOINTS: Log
# ─────────────────────────────────────────
@app.delete("/logs/hapus-semua")
def hapus_semua_log(x_username: str = Header(default="default")):
    conn = sqlite3.connect(DB_PATH)
    c = conn.cursor()
    c.execute("DELETE FROM log_aktivitas WHERE username=?", (x_username,))
    conn.commit()
    conn.close()
    return {"ok": True}


# ─────────────────────────────────────────
# ENDPOINTS: FCM Token Management
# ─────────────────────────────────────────
@app.post("/fcm/register")
def register_fcm_token(data: dict, x_username: str = Header(default="default")):
    """Daftarkan FCM token untuk menerima push notification."""
    token = data.get("token", "").strip()
    platform = data.get("platform", "web")
    if not token:
        raise HTTPException(status_code=400, detail="Token FCM tidak boleh kosong")
    simpan_fcm_token(x_username, token, platform)
    return {"ok": True, "message": "Token FCM berhasil didaftarkan"}


@app.delete("/fcm/unregister")
def unregister_fcm_token(data: dict, x_username: str = Header(default="default")):
    """Hapus FCM token (saat logout atau ganti device)."""
    token = data.get("token", "").strip()
    if not token:
        raise HTTPException(status_code=400, detail="Token tidak boleh kosong")
    hapus_fcm_token(x_username, token)
    return {"ok": True, "message": "Token FCM berhasil dihapus"}


@app.get("/fcm/tokens")
def list_fcm_tokens(x_username: str = Header(default="default")):
    """Lihat jumlah token terdaftar untuk username ini."""
    tokens = get_fcm_tokens(x_username)
    return {"username": x_username, "jumlah_token": len(tokens)}


@app.post("/fcm/test")
def test_notif(x_username: str = Header(default="default")):
    """Test kirim push notification ke device terdaftar."""
    tokens = get_fcm_tokens(x_username)
    if not tokens:
        return {"ok": False, "message": "Tidak ada token FCM terdaftar untuk user ini"}
    kirim_notif(
        x_username,
        "🔔 Test Notifikasi",
        "Notifikasi Firebase FCM berhasil dikonfigurasi!",
        {"type": "test", "timestamp": datetime.now().isoformat()}
    )
    return {"ok": True, "tokens_terdaftar": len(tokens), "message": "Notifikasi test berhasil dikirim"}


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
