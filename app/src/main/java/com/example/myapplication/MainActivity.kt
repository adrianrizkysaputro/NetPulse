package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.foundation.clickable
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.widget.Toast
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.width
import androidx.compose.ui.zIndex

val WarnaHijau      = Color(0xFF22C55E)  // hijau modern
val WarnaMerah      = Color(0xFFEF4444)  // merah modern
val WarnaBg         = Color(0xFFF8FAFC)  // putih keabuan
val WarnaTeksDim    = Color(0xFF64748B)  // abu-abu
val WarnaTeksUtama  = Color(0xFF1E293B)  // hitam gelap
val WarnaKuning     = Color(0xFFF59E0B)  // kuning modern
val WarnaCard       = Color(0xFFFFFFFF)  // putih card
val WarnaBorder     = Color(0xFFE2E8F0)  // border abu tipis

data class Perangkat(
    val id: Int = 0,
    val nama: String,
    val ip: String,
    val port: String = "80",
    val protokol: String = "HTTP",
    val username: String = "",
    val password: String = "",
    val topic: String = "",        // khusus MQTT
    val kategori: String = "Jaringan",
    val status: String,
    var latency: Int = 0
)

data class AktivitasLog(
    val perangkat: String,
    val pesan: String,
    val waktu: String,
    val tanggal: String,
    val isOnline: Boolean
)
val daftarPerangkat = listOf(
    Perangkat(1, "ROUTER",     "192.168.1.1",   port = "80",   protokol = "HTTP",  kategori = "Jaringan", status = "ONLINE",  latency = 4),
    Perangkat(2, "AP1",        "192.168.1.2",   port = "80",   protokol = "HTTP",  kategori = "Jaringan", status = "ONLINE",  latency = 7),
    Perangkat(3, "IOT DEVICE", "192.168.1.100", port = "1883", protokol = "MQTT",  kategori = "IoT",      status = "OFFLINE", latency = 0),
    Perangkat(4, "SMART LAMP", "192.168.1.101", port = "1883", protokol = "MQTT",  kategori = "IoT",      status = "ONLINE",  latency = 12),
    Perangkat(5, "SMART PLUG", "192.168.1.102", port = "1883", protokol = "MQTT",  kategori = "IoT",      status = "ONLINE",  latency = 15),
    Perangkat(6, "CCTV",       "192.168.1.103", port = "554",  protokol = "HTTP",  kategori = "IoT",      status = "ONLINE",  latency = 18),
    Perangkat(7, "SENSOR",     "192.168.1.104", port = "1883", protokol = "MQTT",  kategori = "IoT",      status = "ONLINE",  latency = 22),
    Perangkat(8, "SMART DOOR", "192.168.1.105", port = "1883", protokol = "MQTT",  kategori = "IoT",      status = "OFFLINE", latency = 0),
)
fun simulasiScan(perangkat: Perangkat): Perangkat {
    val stausBaru = if (Random.nextInt(100) < 50) {
        "OFFLINE"
    } else {
        "ONLINE"
    }
    val latencyBaru = if (stausBaru == "OFFLINE") {
        0
    } else {
        Random.nextInt(5, 50)
    }
    return perangkat.copy(
        status = stausBaru,
        latency = latencyBaru
    )
}
@Composable
fun InfoText(teks: String) {
    Text(
        text = teks,
        fontSize = 14.sp,
        color = WarnaTeksDim,
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth()
    )
}
@Composable
fun HeaderApp() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "NetPulse",
            color = WarnaTeksUtama,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
@Composable
fun CardRingkasan(
    angka: String,
    label: String,
    warna: Color,
    emoji: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = WarnaCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Emoji di kiri
            Box(
                modifier = Modifier
                    .background(
                        warna.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {
                Text(text = emoji, fontSize = 20.sp)
            }

            // Angka & label di kanan
            Column {
                Text(
                    text = angka,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = warna
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = WarnaTeksDim
                )
            }
        }
    }
}
@Composable
fun AktivitasItem(log: AktivitasLog) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (log.isOnline) "✅" else "⚠️",
                fontSize = 16.sp
            )
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = WarnaTeksUtama)) {
                        append(log.perangkat + " ")
                    }
                    withStyle(SpanStyle(color = WarnaTeksDim)) {
                        append(log.pesan)
                    }
                },
                fontSize = 13.sp
            )
        }
        Text(
            text = log.waktu,
            fontSize = 12.sp,
            color = WarnaTeksDim
        )
    }
}
@Composable
fun KartuPerangkat(perangkat: Perangkat) {
    var isExpanded by remember { mutableStateOf(false) }
    val warnaStatus = if (perangkat.status == "ONLINE") WarnaHijau else WarnaMerah
    val labelStatus = if (perangkat.status == "ONLINE") "Normal" else "Down"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = WarnaCard),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = when (perangkat.nama) {
                            "ROUTER"       -> "🔌"
                            "AP1", "AP2"   -> "📶"
                            "CCTV"         -> "📷"
                            "SENSOR"       -> "🌡️"
                            "SMART DOOR"   -> "🚪"
                            "SMART LAMP"   -> "💡"
                            "SMART PLUG"   -> "🔋"
                            "PAKAN BURUNG" -> "🐦"
                            else -> if (perangkat.kategori == "IoT") "📱" else "📶"
                        },
                        fontSize = 24.sp
                    )
                    Column {
                        Text(
                            text = perangkat.nama,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarnaTeksUtama
                        )
                        Text(
                            text = perangkat.ip,
                            fontSize = 12.sp,
                            color = WarnaTeksDim
                        )
                        Text(
                            text = perangkat.kategori,
                            fontSize = 11.sp,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
                val warnaStatus2 = if (perangkat.status == "ONLINE") WarnaHijau else WarnaMerah
                Box(
                    modifier = Modifier
                        .background(warnaStatus2.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (perangkat.status == "ONLINE") "Normal" else "Down",
                        fontSize = 12.sp,
                        color = warnaStatus2,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = WarnaBorder)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "IP", fontSize = 13.sp, color = WarnaTeksDim)
                        Text(text = perangkat.ip, fontSize = 13.sp, color = WarnaTeksUtama, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Latency", fontSize = 13.sp, color = WarnaTeksDim)
                        Text(
                            text = if (perangkat.latency == 0) "TIMEOUT" else "${perangkat.latency}ms",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                perangkat.latency == 0 -> WarnaMerah
                                perangkat.latency > 100 -> WarnaKuning
                                else -> WarnaHijau
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Status", fontSize = 13.sp, color = WarnaTeksDim)
                        Text(
                            text = perangkat.status,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = warnaStatus
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun KartuPerangkatDenganHapus(
    perangkat: Perangkat,
    onHapus: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showKonfirmasi by remember { mutableStateOf(false) }

    val warnaStatus = if (perangkat.status == "ONLINE") WarnaHijau else WarnaMerah
    val labelStatus = if (perangkat.status == "ONLINE") "Normal" else "Down"

    // Dialog konfirmasi hapus
    if (showKonfirmasi) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showKonfirmasi = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WarnaCard),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Hapus Perangkat?",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarnaTeksUtama
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Yakin ingin menghapus ${perangkat.nama}?",
                        fontSize = 13.sp,
                        color = WarnaTeksDim
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showKonfirmasi = false }
                                .background(WarnaBg, RoundedCornerShape(12.dp))
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Batal", fontSize = 14.sp, color = WarnaTeksDim, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    showKonfirmasi = false
                                    onHapus()
                                }
                                .background(WarnaMerah, RoundedCornerShape(12.dp))
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Hapus", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = WarnaCard),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Kiri - emoji + nama + ip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .background(warnaStatus.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = when (perangkat.nama) {
                                "ROUTER"     -> "🔌"
                                "AP1"        -> "📶"
                                "CCTV"       -> "📷"
                                "SENSOR"     -> "🌡️"
                                "SMART DOOR" -> "🚪"
                                "SMART LAMP" -> "💡"
                                "SMART PLUG" -> "🔋"
                                else         -> "📱"
                            },
                            fontSize = 20.sp
                        )
                    }
                    Column {
                        Text(perangkat.nama, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
                        Text(perangkat.ip, fontSize = 12.sp, color = WarnaTeksDim)
                    }
                }

                // Kanan - badge status + tombol hapus
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(warnaStatus.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(warnaStatus, RoundedCornerShape(3.dp)))
                            Text(labelStatus, fontSize = 12.sp, color = warnaStatus, fontWeight = FontWeight.Bold)
                        }
                    }

                    // ── Tombol Hapus ──
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(WarnaMerah.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .clickable { showKonfirmasi = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🗑️", fontSize = 14.sp)
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = WarnaBorder)
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoText("IP         : ${perangkat.ip}")
                    InfoText("Port       : ${if (perangkat.port == "0") "-" else perangkat.port}")
                    InfoText("Protokol   : ${perangkat.protokol}")
                    InfoText("Kategori   : ${perangkat.kategori}")
                    if (perangkat.topic.isNotBlank()) {
                        InfoText("Topic      : ${perangkat.topic}")
                    }
                    if (perangkat.username.isNotBlank()) {
                        InfoText("Username   : ${perangkat.username}")
                    }
                    InfoText("Latency    : ${if (perangkat.latency == 0) "TIMEOUT" else "${perangkat.latency}ms"}")
                    Text(
                        text = "Status     : ${perangkat.status}",
                        fontSize = 14.sp,
                        color = warnaStatus,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
@Composable
fun DialogTambahPerangkat(
    onDismiss: () -> Unit,
    onTambah: (Perangkat) -> Unit,
    currentSize: Int
) {
    var nama      by remember { mutableStateOf("") }
    var ip        by remember { mutableStateOf("") }
    var port      by remember { mutableStateOf("80") }
    var protokol  by remember { mutableStateOf("HTTP") }
    var username  by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var topic     by remember { mutableStateOf("") }
    var kategori  by remember { mutableStateOf("Jaringan") }
    var showPass  by remember { mutableStateOf(false) }
    var errorMsg  by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val daftarProtokol = listOf("HTTP", "HTTPS", "MQTT", "SSH", "Ping")

    // Auto port saat protokol berubah
    LaunchedEffect(protokol) {
        port = when (protokol) {
            "HTTP"  -> "80"
            "HTTPS" -> "443"
            "MQTT"  -> "1883"
            "SSH"   -> "22"
            "Ping"  -> "0"
            else    -> port
        }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WarnaCard),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Tambah Perangkat",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarnaTeksUtama
                )
                Spacer(modifier = Modifier.height(16.dp))

                // ── Nama ──
                Text("Nama Perangkat", fontSize = 13.sp, color = WarnaTeksDim)
                Spacer(modifier = Modifier.height(4.dp))
                androidx.compose.material3.OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    placeholder = { Text("contoh: AP2", color = WarnaTeksDim) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                // ── Kategori ──
                Text("Kategori", fontSize = 13.sp, color = WarnaTeksDim)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Jaringan", "IoT").forEach { pilihan ->
                        val dipilih = kategori == pilihan
                        Box(
                            modifier = Modifier
                                .clickable { kategori = pilihan }
                                .background(
                                    if (dipilih) Color(0xFF2563EB) else WarnaBg,
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = pilihan,
                                fontSize = 13.sp,
                                color = if (dipilih) Color.White else WarnaTeksDim,
                                fontWeight = if (dipilih) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // ── Protokol ──
                Text("Protokol", fontSize = 13.sp, color = WarnaTeksDim)
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(daftarProtokol.size) { i ->
                        val p = daftarProtokol[i]
                        val dipilih = protokol == p
                        Box(
                            modifier = Modifier
                                .clickable { protokol = p }
                                .background(
                                    if (dipilih) Color(0xFF2563EB) else WarnaBg,
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = p,
                                fontSize = 12.sp,
                                color = if (dipilih) Color.White else WarnaTeksDim,
                                fontWeight = if (dipilih) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // ── IP & Port dalam satu baris ──
                Text("Alamat IP & Port", fontSize = 13.sp, color = WarnaTeksDim)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.OutlinedTextField(
                        value = ip,
                        onValueChange = { ip = it },
                        placeholder = { Text("192.168.1.10", color = WarnaTeksDim) },
                        modifier = Modifier.weight(2f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        placeholder = { Text("Port", color = WarnaTeksDim) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        enabled = protokol != "Ping"
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // ── Topic (hanya MQTT) ──
                AnimatedVisibility(visible = protokol == "MQTT") {
                    Column {
                        Text("Topic MQTT", fontSize = 13.sp, color = WarnaTeksDim)
                        Spacer(modifier = Modifier.height(4.dp))
                        androidx.compose.material3.OutlinedTextField(
                            value = topic,
                            onValueChange = { topic = it },
                            placeholder = { Text("contoh: home/lamp/status", color = WarnaTeksDim) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // ── Username & Password (sembunyikan jika Ping) ──
                AnimatedVisibility(visible = protokol != "Ping") {
                    Column {
                        Text("Username (opsional)", fontSize = 13.sp, color = WarnaTeksDim)
                        Spacer(modifier = Modifier.height(4.dp))
                        androidx.compose.material3.OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            placeholder = { Text("contoh: admin", color = WarnaTeksDim) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Password (opsional)", fontSize = 13.sp, color = WarnaTeksDim)
                        Spacer(modifier = Modifier.height(4.dp))
                        androidx.compose.material3.OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("contoh: admin123", color = WarnaTeksDim) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            visualTransformation = if (showPass)
                                androidx.compose.ui.text.input.VisualTransformation.None
                            else
                                androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            trailingIcon = {
                                Text(
                                    text = if (showPass) "👁️" else "🙈",
                                    modifier = Modifier.clickable { showPass = !showPass },
                                    fontSize = 16.sp
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

// ── Pesan Error ──
                if (errorMsg.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFEDED), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "⚠️ $errorMsg",
                            fontSize = 13.sp,
                            color = WarnaMerah
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

// ── Tombol Batal & Tambah ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onDismiss() }
                            .background(WarnaBg, RoundedCornerShape(12.dp))
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Batal", fontSize = 14.sp, color = WarnaTeksDim, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                // Validasi dulu sebelum kirim
                                when {
                                    nama.isBlank() -> {
                                        errorMsg = "Nama perangkat tidak boleh kosong"
                                        return@clickable
                                    }
                                    ip.isBlank() -> {
                                        errorMsg = "IP tidak boleh kosong"
                                        return@clickable
                                    }
                                    !ip.matches(Regex("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")) -> {
                                        errorMsg = "Format IP tidak valid (contoh: 192.168.1.1)"
                                        return@clickable
                                    }
                                }
                                // Kalau lolos validasi
                                errorMsg = ""
                                isLoading = true
                                onTambah(
                                    Perangkat(
                                        id       = currentSize + 1,
                                        nama     = nama.uppercase(),
                                        ip       = ip,
                                        port     = port,
                                        protokol = protokol,
                                        username = username,
                                        password = password,
                                        topic    = topic,
                                        kategori = kategori,
                                        status   = "ONLINE",
                                        latency  = 0
                                    )
                                )
                                // onDismiss() DIHAPUS dari sini
                            }
                            .background(
                                if (isLoading) Color(0xFF93C5FD) else Color(0xFF2563EB),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isLoading) "Menambahkan..." else "Tambah",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
fun getCurrentTime(): String {
    val now = java.util.Calendar.getInstance()
    return "%02d:%02d".format(
        now.get(java.util.Calendar.HOUR_OF_DAY),
        now.get(java.util.Calendar.MINUTE)
    )
}

fun getCurrentDate(): String {
    val now = java.util.Calendar.getInstance()
    return "%02d/%02d/%04d".format(
        now.get(java.util.Calendar.DAY_OF_MONTH),
        now.get(java.util.Calendar.MONTH) + 1,
        now.get(java.util.Calendar.YEAR)
    )
}
@Composable
fun CardIotDetail(iotData: Map<String, Any>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = WarnaCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🐦 Detail Pakan Burung",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = WarnaTeksUtama
            )
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = WarnaBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // Pakan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "🍚 Level Pakan", fontSize = 14.sp, color = WarnaTeksDim)
                Text(
                    text = "${iotData["persentasePakan"]?.toString()?.toDoubleOrNull()?.toInt() ?: 0}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        (iotData["persentasePakan"]?.toString()?.toDoubleOrNull() ?: 0.0) >= 70 -> WarnaHijau
                        (iotData["persentasePakan"]?.toString()?.toDoubleOrNull() ?: 0.0) >= 40 -> WarnaKuning
                        else -> WarnaMerah
                    }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Status: ${iotData["statusPakan"] ?: "-"}",
                fontSize = 13.sp,
                color = WarnaTeksDim
            )
            Text(
                text = "Jarak: ${iotData["jarakPakan"]?.toString()?.toDoubleOrNull()?.toInt() ?: 0} cm",
                fontSize = 13.sp,
                color = WarnaTeksDim
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = WarnaBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // Air
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "💧 Level Air", fontSize = 14.sp, color = WarnaTeksDim)
                Text(
                    text = "${iotData["persentaseAir"]?.toString()?.toDoubleOrNull()?.toInt() ?: 0}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        (iotData["persentaseAir"]?.toString()?.toDoubleOrNull() ?: 0.0) >= 70 -> WarnaHijau
                        (iotData["persentaseAir"]?.toString()?.toDoubleOrNull() ?: 0.0) >= 40 -> WarnaKuning
                        else -> WarnaMerah
                    }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Status: ${iotData["statusAir"] ?: "-"}",
                fontSize = 13.sp,
                color = WarnaTeksDim
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = WarnaBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // Aktuator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "⚙️ Aktuator", fontSize = 14.sp, color = WarnaTeksDim)
                val servoOpen = iotData["servoOpen"]?.toString() == "true"
                Box(
                    modifier = Modifier
                        .background(
                            if (servoOpen) WarnaHijau.copy(alpha = 0.1f)
                            else WarnaMerah.copy(alpha = 0.1f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (servoOpen) "OPEN" else "CLOSE",
                        fontSize = 12.sp,
                        color = if (servoOpen) WarnaHijau else WarnaMerah,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
data class DiagnosisHasil(
    val problem: String,
    val kemungkinan: List<String>,
    val solusi: List<String>
)

fun diagnosisPerangkat(perangkat: Perangkat): DiagnosisHasil? {
    return when {
        perangkat.status == "OFFLINE" -> DiagnosisHasil(
            problem = "Perangkat tidak merespon",
            kemungkinan = listOf(
                "Perangkat mati atau dimatikan",
                "Kabel LAN tercabut",
                "IP address berubah",
                "Firewall memblokir koneksi"
            ),
            solusi = listOf(
                "Cek koneksi kabel LAN",
                "Pastikan perangkat menyala",
                "Verifikasi IP di admin panel",
                "Cek pengaturan firewall"
            )
        )
        perangkat.latency > 100 -> DiagnosisHasil(
            problem = "Latency tinggi (${perangkat.latency}ms)",
            kemungkinan = listOf(
                "Jaringan padat atau overload",
                "Sinyal WiFi lemah",
                "Interferensi sinyal dari perangkat lain"
            ),
            solusi = listOf(
                "Restart router atau AP",
                "Pindah ke channel WiFi yang lebih sepi",
                "Kurangi jumlah device yang terkoneksi"
            )
        )
        perangkat.latency in 50..100 -> DiagnosisHasil(
            problem = "Latency tidak stabil (${perangkat.latency}ms)",
            kemungkinan = listOf(
                "Koneksi mulai terganggu",
                "Sinyal WiFi kurang optimal"
            ),
            solusi = listOf(
                "Pantau latency secara berkala",
                "Pertimbangkan restart AP jika memburuk"
            )
        )
        else -> null
    }
}

@Composable
fun CardDiagnosis(diagnosis: DiagnosisHasil) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF7ED)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "⚠️", fontSize = 18.sp)
                Text(
                    text = "Gangguan Terdeteksi",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB45309)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Problem:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = WarnaTeksUtama
            )
            Text(
                text = diagnosis.problem,
                fontSize = 13.sp,
                color = WarnaMerah
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Kemungkinan penyebab:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = WarnaTeksUtama
            )
            diagnosis.kemungkinan.forEach {
                Text(
                    text = "• $it",
                    fontSize = 13.sp,
                    color = WarnaTeksDim
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Solusi yang disarankan:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = WarnaTeksUtama
            )
            diagnosis.solusi.forEach {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "✅", fontSize = 12.sp)
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        color = WarnaTeksUtama
                    )
                }
            }
        }
    }
}

@Composable
fun CardDiagnosisJaringan(diagnosis: DiagnosisResponse) {
    val warnaLevel = when (diagnosis.level) {
        "KRITIS" -> Color(0xFFEF4444)
        "TINGGI" -> Color(0xFFF97316)
        "SEDANG" -> Color(0xFFF59E0B)
        else     -> Color(0xFF22C55E)
    }
    val warnaBg = when (diagnosis.level) {
        "KRITIS" -> Color(0xFFFFEDED)
        "TINGGI" -> Color(0xFFFFF3ED)
        "SEDANG" -> Color(0xFFFFFBED)
        else     -> Color(0xFFEDFFF3)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = warnaBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = diagnosis.icon, fontSize = 20.sp)
                Column {
                    Text(
                        text = diagnosis.level,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = warnaLevel
                    )
                    Text(
                        text = diagnosis.judul,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarnaTeksUtama
                    )
                }
            }

            if (diagnosis.info != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = diagnosis.info,
                    fontSize = 13.sp,
                    color = WarnaTeksDim
                )
            }

            if (diagnosis.kemungkinan.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = WarnaBorder)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Kemungkinan penyebab:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarnaTeksUtama
                )
                diagnosis.kemungkinan.forEach {
                    Text(
                        text = "• $it",
                        fontSize = 13.sp,
                        color = WarnaTeksDim
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = WarnaBorder)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Solusi:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarnaTeksUtama
                )
                diagnosis.solusi.forEach {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "✅", fontSize = 12.sp)
                        Text(
                            text = it,
                            fontSize = 13.sp,
                            color = WarnaTeksUtama
                        )
                    }
                }
            }
        }
    }
}

object AppPrefs {
    private const val PREF_NAME = "netpulse_prefs"

    fun get(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // ── Server ──
    fun getServerUrl(context: Context): String =
        get(context).getString("server_url", "http://192.168.1.5:8000/") ?: "http://192.168.1.5:8000/"
    fun setServerUrl(context: Context, url: String) =
        get(context).edit().putString("server_url", url).apply()

    // ── Interval scan ──
    fun getIntervalScan(context: Context): Int =
        get(context).getInt("interval_scan", 60)
    fun setIntervalScan(context: Context, interval: Int) =
        get(context).edit().putInt("interval_scan", interval).apply()

    // ── Session ──
    fun isLoggedIn(context: Context): Boolean =
        get(context).getBoolean("is_logged_in", false)
    fun setLoggedIn(context: Context, value: Boolean) =
        get(context).edit().putBoolean("is_logged_in", value).apply()

    fun getActiveUsername(context: Context): String =
        get(context).getString("active_username", "") ?: ""
    fun setActiveUsername(context: Context, username: String) =
        get(context).edit().putString("active_username", username).apply()

    // ── Multi-akun: simpan akun sebagai "akun_<username>" ──
    fun simpanAkun(context: Context, username: String, password: String) {
        get(context).edit()
            .putString("akun_${username}_nama", username)
            .putString("akun_${username}_password", password)
            .apply()
    }

    fun getPassword(context: Context, username: String): String =
        get(context).getString("akun_${username}_password", "") ?: ""

    fun akuuAdaUsername(context: Context, username: String): Boolean =
        get(context).contains("akun_${username}_password")

    fun hapusAkun(context: Context, username: String) {
        get(context).edit()
            .remove("akun_${username}_nama")
            .remove("akun_${username}_password")
            .apply()
    }

    // ── Nama tampilan (dari username aktif) ──
    fun getNamaPengguna(context: Context): String =
        getActiveUsername(context).ifEmpty { "Pengguna" }

    fun setNamaPengguna(context: Context, nama: String) =
        get(context).edit().putString("active_username", nama).apply()

    // ── Ganti password ──
    fun gantiPassword(context: Context, username: String, passwordBaru: String) =
        simpanAkun(context, username, passwordBaru)
}

@Composable
fun HalamanLogin(onLoginBerhasil: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // Mode: "login", "register", "lupa_password"
    var mode by remember { mutableStateOf("login") }

    var username    by remember { mutableStateOf("") }
    var password    by remember { mutableStateOf("") }
    var passwordBaru by remember { mutableStateOf("") }
    var konfirmPassword by remember { mutableStateOf("") }
    var showPass    by remember { mutableStateOf(false) }
    var showPassBaru by remember { mutableStateOf(false) }
    var errorMsg    by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarnaBg)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "📡", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "NetPulse",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2563EB)
        )
        Text(
            text = "Network & IoT Monitor",
            fontSize = 14.sp,
            color = WarnaTeksDim
        )
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = WarnaCard)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // ── Judul berdasarkan mode ──
                Text(
                    text = when (mode) {
                        "register"      -> "Buat Akun Baru"
                        "lupa_password" -> "Reset Password"
                        else            -> "Masuk"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarnaTeksUtama
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (mode) {
                        "register"      -> "Daftarkan akun baru"
                        "lupa_password" -> "Masukkan username & password baru"
                        else            -> "Masukkan username dan password"
                    },
                    fontSize = 13.sp,
                    color = WarnaTeksDim
                )
                Spacer(modifier = Modifier.height(16.dp))

                // ── Input Username (semua mode) ──
                Text("Username", fontSize = 13.sp, color = WarnaTeksDim)
                Spacer(modifier = Modifier.height(4.dp))
                androidx.compose.material3.OutlinedTextField(
                    value = username,
                    onValueChange = { username = it.trim() },
                    placeholder = { Text("contoh: adrian", color = WarnaTeksDim) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                // ── Input Password ──
                if (mode != "lupa_password") {
                    Text("Password", fontSize = 13.sp, color = WarnaTeksDim)
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Password kamu", color = WarnaTeksDim) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = if (showPass)
                            androidx.compose.ui.text.input.VisualTransformation.None
                        else
                            androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        trailingIcon = {
                            Text(
                                text = if (showPass) "👁️" else "🙈",
                                modifier = Modifier.clickable { showPass = !showPass },
                                fontSize = 16.sp
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // ── Konfirmasi Password (register) ──
                AnimatedVisibility(visible = mode == "register") {
                    Column {
                        Text("Konfirmasi Password", fontSize = 13.sp, color = WarnaTeksDim)
                        Spacer(modifier = Modifier.height(4.dp))
                        androidx.compose.material3.OutlinedTextField(
                            value = konfirmPassword,
                            onValueChange = { konfirmPassword = it },
                            placeholder = { Text("Ulangi password", color = WarnaTeksDim) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // ── Password baru & konfirmasi (lupa_password) ──
                AnimatedVisibility(visible = mode == "lupa_password") {
                    Column {
                        Text("Password Baru", fontSize = 13.sp, color = WarnaTeksDim)
                        Spacer(modifier = Modifier.height(4.dp))
                        androidx.compose.material3.OutlinedTextField(
                            value = passwordBaru,
                            onValueChange = { passwordBaru = it },
                            placeholder = { Text("Password baru", color = WarnaTeksDim) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            visualTransformation = if (showPassBaru)
                                androidx.compose.ui.text.input.VisualTransformation.None
                            else
                                androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            trailingIcon = {
                                Text(
                                    text = if (showPassBaru) "👁️" else "🙈",
                                    modifier = Modifier.clickable { showPassBaru = !showPassBaru },
                                    fontSize = 16.sp
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Konfirmasi Password Baru", fontSize = 13.sp, color = WarnaTeksDim)
                        Spacer(modifier = Modifier.height(4.dp))
                        androidx.compose.material3.OutlinedTextField(
                            value = konfirmPassword,
                            onValueChange = { konfirmPassword = it },
                            placeholder = { Text("Ulangi password baru", color = WarnaTeksDim) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // ── Pesan error ──
                if (errorMsg.isNotEmpty()) {
                    Text(text = errorMsg, fontSize = 13.sp, color = WarnaMerah)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // ── Tombol aksi utama ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            errorMsg = ""
                            when (mode) {
                                "login" -> {
                                    if (username.isBlank() || password.isBlank()) {
                                        errorMsg = "Username dan password tidak boleh kosong"
                                        return@clickable
                                    }
                                    if (!AppPrefs.akuuAdaUsername(context, username)) {
                                        errorMsg = "Akun tidak ditemukan"
                                        return@clickable
                                    }
                                    if (password != AppPrefs.getPassword(context, username)) {
                                        errorMsg = "Password salah, coba lagi"
                                        password = ""
                                        return@clickable
                                    }
                                    AppPrefs.setActiveUsername(context, username)
                                    AppPrefs.setLoggedIn(context, true)
                                    onLoginBerhasil()
                                }
                                "register" -> {
                                    if (username.isBlank()) {
                                        errorMsg = "Username tidak boleh kosong"
                                        return@clickable
                                    }
                                    if (password.length < 6) {
                                        errorMsg = "Password minimal 6 karakter"
                                        return@clickable
                                    }
                                    if (password != konfirmPassword) {
                                        errorMsg = "Password tidak cocok"
                                        return@clickable
                                    }
                                    if (AppPrefs.akuuAdaUsername(context, username)) {
                                        errorMsg = "Username sudah digunakan"
                                        return@clickable
                                    }
                                    AppPrefs.simpanAkun(context, username, password)
                                    AppPrefs.setActiveUsername(context, username)
                                    AppPrefs.setLoggedIn(context, true)
                                    onLoginBerhasil()
                                }
                                "lupa_password" -> {
                                    if (username.isBlank()) {
                                        errorMsg = "Masukkan username kamu"
                                        return@clickable
                                    }
                                    if (!AppPrefs.akuuAdaUsername(context, username)) {
                                        errorMsg = "Username tidak ditemukan"
                                        return@clickable
                                    }
                                    if (passwordBaru.length < 6) {
                                        errorMsg = "Password baru minimal 6 karakter"
                                        return@clickable
                                    }
                                    if (passwordBaru != konfirmPassword) {
                                        errorMsg = "Password baru tidak cocok"
                                        return@clickable
                                    }
                                    AppPrefs.gantiPassword(context, username, passwordBaru)
                                    mode = "login"
                                    errorMsg = ""
                                    password = ""
                                    passwordBaru = ""
                                    konfirmPassword = ""
                                    android.widget.Toast.makeText(
                                        context,
                                        "✅ Password berhasil direset",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        .background(Color(0xFF2563EB), RoundedCornerShape(12.dp))
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (mode) {
                            "register"      -> "Buat Akun"
                            "lupa_password" -> "Reset Password"
                            else            -> "Masuk"
                        },
                        fontSize = 15.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Link navigasi bawah ──
                when (mode) {
                    "login" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Buat akun baru",
                                fontSize = 13.sp,
                                color = Color(0xFF2563EB),
                                modifier = Modifier.clickable {
                                    mode = "register"
                                    errorMsg = ""
                                    password = ""
                                    konfirmPassword = ""
                                }
                            )
                            Text(
                                text = "Lupa password?",
                                fontSize = 13.sp,
                                color = WarnaMerah,
                                modifier = Modifier.clickable {
                                    mode = "lupa_password"
                                    errorMsg = ""
                                    password = ""
                                    passwordBaru = ""
                                    konfirmPassword = ""
                                }
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = "← Kembali ke Login",
                            fontSize = 13.sp,
                            color = Color(0xFF2563EB),
                            modifier = Modifier.clickable {
                                mode = "login"
                                errorMsg = ""
                                password = ""
                                passwordBaru = ""
                                konfirmPassword = ""
                            }
                        )
                    }
                }
            }
        }
    }
}

// ── Ganti seluruh MainActivity ──
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = androidx.compose.ui.platform.LocalContext.current
            var isLoggedIn by remember { mutableStateOf(AppPrefs.isLoggedIn(context)) }
            LaunchedEffect(Unit) {
                RetrofitClient.BASE_URL = AppPrefs.getServerUrl(context)
            }
            if (!isLoggedIn) {
                HalamanLogin(onLoginBerhasil = { isLoggedIn = true })
            } else {
                var isScanning by remember { mutableStateOf(false) }
                var devices by remember { mutableStateOf(listOf<Perangkat>()) }
                var logAktivitas by remember { mutableStateOf(listOf<AktivitasLog>()) }
                var isAktivitasExpanded by remember { mutableStateOf(true) }
                var selectedTab by remember { mutableIntStateOf(0) }
                var isLoading by remember { mutableStateOf(false) }
                var errorMsg by remember { mutableStateOf("") }
                var iotData by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }
                var selectedDevice by remember { mutableStateOf<Perangkat?>(null) }
                var diagnosisJaringan by remember { mutableStateOf(listOf<DiagnosisResponse>()) }
                var searchQuery by remember { mutableStateOf("") }

                // Fungsi fetch dari server
                suspend fun fetchDevices() {
                    try {
                        isLoading = true
                        errorMsg = ""
                        val username = AppPrefs.getActiveUsername(context)
                        val response = withContext(Dispatchers.IO) {
                            RetrofitClient.instance.getDevices(username)
                        }
                        devices = response.map {
                            Perangkat(
                                id = it.id,
                                nama = it.nama,
                                ip = it.ip,
                                kategori = it.kategori,
                                status = it.status,
                                latency = it.latency
                            )
                        }
                        val logs = withContext(Dispatchers.IO) {
                            RetrofitClient.instance.getLogs(username)
                        }
                        logAktivitas = logs.map {
                            AktivitasLog(
                                perangkat = it.perangkat,
                                pesan = it.pesan,
                                waktu = it.waktu,
                                tanggal = it.tanggal,
                                isOnline = it.isOnline
                            )
                        }
                    } catch (e: Exception) {
                        errorMsg = "Gagal konek ke server: ${e.message}"
                    } finally {
                        isLoading = false
                        isScanning = false
                    }
                }

                LaunchedEffect(isScanning) {
                    if (isScanning) {
                        fetchDevices()
                    }
                }

                // Auto refresh tiap 30 detik
                LaunchedEffect(Unit) {
                    while (true) {
                        fetchDevices()
                        kotlinx.coroutines.delay(30000)
                    }
                }

                val onlineCount = devices.count { it.status == "ONLINE" }
                val offlineCount = devices.count { it.status == "OFFLINE" }
                val totalCount = devices.size

                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = WarnaCard,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = {
                                    Icon(
                                        Icons.Filled.Home,
                                        contentDescription = "Dashboard"
                                    )
                                },
                                label = { Text("Dashboard", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF2563EB),
                                    selectedTextColor = Color(0xFF2563EB),
                                    unselectedIconColor = WarnaTeksDim,
                                    unselectedTextColor = WarnaTeksDim
                                )
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = {
                                    Icon(
                                        Icons.AutoMirrored.Filled.List,
                                        contentDescription = "Device"
                                    )
                                },
                                label = { Text("Device", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF2563EB),
                                    selectedTextColor = Color(0xFF2563EB),
                                    unselectedIconColor = WarnaTeksDim,
                                    unselectedTextColor = WarnaTeksDim
                                )
                            )
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = {
                                    Icon(
                                        Icons.Filled.History,
                                        contentDescription = "History"
                                    )
                                },
                                label = { Text("History", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF2563EB),
                                    selectedTextColor = Color(0xFF2563EB),
                                    unselectedIconColor = WarnaTeksDim,
                                    unselectedTextColor = WarnaTeksDim
                                )
                            )
                            NavigationBarItem(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                icon = {
                                    Icon(
                                        Icons.Filled.Settings,
                                        contentDescription = "Settings"
                                    )
                                },
                                label = { Text("Settings", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF2563EB),
                                    selectedTextColor = Color(0xFF2563EB),
                                    unselectedIconColor = WarnaTeksDim,
                                    unselectedTextColor = WarnaTeksDim
                                )
                            )
                        }
                    }
                ) { paddingValues ->

                    // Auto fetch IoT data
                    LaunchedEffect(Unit) {
                        while (true) {
                            fetchDevices()
                            try {
                                val data = withContext(Dispatchers.IO) {
                                    RetrofitClient.instance.getIotStatus()
                                }
                                iotData = data
                            } catch (e: Exception) {
                                // ignore
                            }
                            try {
                                val diag = withContext(Dispatchers.IO) {
                                    val username = AppPrefs.getActiveUsername(context)
                                    RetrofitClient.instance.getDiagnosis(username)
                                }
                                diagnosisJaringan = diag
                            } catch (e: Exception) {
                                // ignore
                            }
                            kotlinx.coroutines.delay(30000)
                        }
                    }

                    when (selectedTab) {

                        // ── TAB DASHBOARD ──
                        0 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(WarnaBg)
                                    .padding(paddingValues)
                                    .padding(horizontal = 16.dp)
                            ) {
                                HeaderApp()
                                Spacer(modifier = Modifier.height(8.dp))
                                val context = androidx.compose.ui.platform.LocalContext.current
                                val namaPengguna = AppPrefs.getNamaPengguna(context)

                                Text(
                                    text = "Halo $namaPengguna! 👋",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarnaTeksUtama
                                )
                                Text(
                                    text = "Berikut status jaringan kamu.",
                                    fontSize = 13.sp,
                                    color = WarnaTeksDim
                                )

                                // Tampil error kalau gagal konek
                                if (errorMsg.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFFFFEDED)
                                        )
                                    ) {
                                        Text(
                                            text = "⚠️ $errorMsg",
                                            fontSize = 12.sp,
                                            color = WarnaMerah,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        CardRingkasan(
                                            onlineCount.toString(),
                                            "Normal",
                                            WarnaHijau,
                                            "✅"
                                        )
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        CardRingkasan(
                                            offlineCount.toString(),
                                            "Down",
                                            WarnaMerah,
                                            "⚠️"
                                        )
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        CardRingkasan(
                                            totalCount.toString(),
                                            "Device",
                                            WarnaTeksUtama,
                                            "🌐"
                                        )
                                    }
                                }

                                var isDeviceExpanded by remember { mutableStateOf(false) }

                                val pullRefreshState = rememberPullRefreshState(
                                    refreshing = isLoading,
                                    onRefresh = { isScanning = true }
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .pullRefresh(pullRefreshState)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                    ) {

                                        // Diagnosis Jaringan
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Diagnosis Jaringan",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WarnaTeksUtama
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        if (diagnosisJaringan.isEmpty()) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(16.dp),
                                                elevation = CardDefaults.cardElevation(2.dp),
                                                colors = CardDefaults.cardColors(containerColor = WarnaCard)
                                            ) {
                                                Text(
                                                    text = "Mengambil data diagnosis...",
                                                    fontSize = 13.sp,
                                                    color = WarnaTeksDim,
                                                    modifier = Modifier.padding(16.dp)
                                                )
                                            }
                                        } else {
                                            diagnosisJaringan.forEach { diagnosis ->
                                                CardDiagnosisJaringan(diagnosis = diagnosis)
                                            }
                                        }

                                        // Status Perangkat
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Status Perangkat",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WarnaTeksUtama
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        if (isLoading) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(32.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                androidx.compose.material3.CircularProgressIndicator(
                                                    color = Color(0xFF2563EB)
                                                )
                                            }
                                        } else if (devices.isEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(32.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Belum ada perangkat.\nTambah device di tab Device.",
                                                    fontSize = 14.sp,
                                                    color = WarnaTeksDim,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        } else {
                                            val tampilDevices =
                                                if (isDeviceExpanded) devices else devices.take(3)
                                            tampilDevices.forEach { perangkat ->
                                                KartuPerangkat(perangkat = perangkat)
                                            }
                                            if (devices.size > 3) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            isDeviceExpanded = !isDeviceExpanded
                                                        }
                                                        .padding(vertical = 8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            4.dp
                                                        )
                                                    ) {
                                                        Text(
                                                            text = if (isDeviceExpanded) "Sembunyikan"
                                                            else "Lihat ${devices.size - 3} perangkat lainnya",
                                                            fontSize = 13.sp,
                                                            color = Color(0xFF2563EB)
                                                        )
                                                        Icon(
                                                            imageVector = if (isDeviceExpanded)
                                                                Icons.Filled.KeyboardArrowUp
                                                            else
                                                                Icons.Filled.KeyboardArrowDown,
                                                            contentDescription = null,
                                                            tint = Color(0xFF2563EB),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    isAktivitasExpanded = !isAktivitasExpanded
                                                },
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Aktivitas Terbaru",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WarnaTeksUtama
                                            )
                                            Icon(
                                                imageVector = if (isAktivitasExpanded)
                                                    Icons.Filled.KeyboardArrowUp
                                                else
                                                    Icons.Filled.KeyboardArrowDown,
                                                contentDescription = "Toggle Aktivitas",
                                                tint = WarnaTeksDim
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        AnimatedVisibility(visible = isAktivitasExpanded) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(16.dp),
                                                elevation = CardDefaults.cardElevation(2.dp),
                                                colors = CardDefaults.cardColors(containerColor = WarnaCard)
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    if (logAktivitas.isEmpty()) {
                                                        Text(
                                                            text = "Belum ada aktivitas.",
                                                            fontSize = 13.sp,
                                                            color = WarnaTeksDim,
                                                            modifier = Modifier.padding(vertical = 4.dp)
                                                        )
                                                    } else {
                                                        logAktivitas.take(3)
                                                            .forEachIndexed { index, log ->
                                                                AktivitasItem(log = log)
                                                                if (index < minOf(
                                                                        3,
                                                                        logAktivitas.size
                                                                    ) - 1
                                                                ) {
                                                                    HorizontalDivider(color = WarnaBorder)
                                                                }
                                                            }
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    PullRefreshIndicator(
                                        refreshing = isLoading,
                                        state = pullRefreshState,
                                        modifier = Modifier.align(Alignment.TopCenter),
                                        contentColor = Color(0xFF2563EB)
                                    )
                                } // ← tutup Box di sini
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { if (!isScanning) isScanning = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !isScanning && !isLoading,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2563EB),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(
                                        if (isScanning || isLoading) "Mengecek..." else "Cek Semua Jaringan",
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        // ── TAB DEVICE ──
                        1 -> {
                            var showDialogTambah by remember { mutableStateOf(false) }
                            var selectedDevice by remember { mutableStateOf<Perangkat?>(null) }
                            var iotData by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }
                            val context = androidx.compose.ui.platform.LocalContext.current
                            var intervalScanMs by remember {
                                mutableStateOf(AppPrefs.getIntervalScan(context) * 1000L)
                            }

                            if (showDialogTambah) {
                                DialogTambahPerangkat(
                                    onDismiss = { showDialogTambah = false },
                                    onTambah = { perangkat ->
                                        kotlinx.coroutines.GlobalScope.launch {
                                            try {
                                                val username = AppPrefs.getActiveUsername(this@MainActivity)
                                                RetrofitClient.instance.tambahDevice(
                                                    TambahRequest(
                                                        nama = perangkat.nama,
                                                        ip = perangkat.ip,
                                                        kategori = perangkat.kategori
                                                    ),
                                                    username
                                                )
                                                fetchDevices()
                                                withContext(Dispatchers.Main) {
                                                    showDialogTambah = false  // tutup HANYA kalau berhasil
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        "✅ ${perangkat.nama} berhasil ditambahkan",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } catch (e: Exception) {
                                                withContext(Dispatchers.Main) {
                                                    // Dialog tidak ditutup, error muncul sebagai Toast
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        "❌ Gagal: ${e.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    },
                                    currentSize = devices.size
                                )
                            }

                            // Kalau ada device yang dipilih → tampil halaman detail
                            if (selectedDevice != null) {
                                val device = selectedDevice!!
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(WarnaBg)
                                        .padding(paddingValues)
                                ) {
                                    // Header dengan tombol back
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(WarnaCard)
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clickable { selectedDevice = null }
                                                .background(WarnaBg, RoundedCornerShape(8.dp))
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                text = "←",
                                                fontSize = 18.sp,
                                                color = WarnaTeksUtama
                                            )
                                        }
                                        Text(
                                            text = device.nama,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WarnaTeksUtama
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        val warnaStatus =
                                            if (device.status == "ONLINE") WarnaHijau else WarnaMerah
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    warnaStatus.copy(alpha = 0.1f),
                                                    RoundedCornerShape(20.dp)
                                                )
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (device.status == "ONLINE") "Normal" else "Down",
                                                fontSize = 12.sp,
                                                color = warnaStatus,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .verticalScroll(rememberScrollState())
                                            .padding(16.dp)
                                    ) {
                                        // Info dasar
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(16.dp),
                                            elevation = CardDefaults.cardElevation(2.dp),
                                            colors = CardDefaults.cardColors(containerColor = WarnaCard)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text(
                                                    text = "Informasi Perangkat",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = WarnaTeksUtama
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                HorizontalDivider(color = WarnaBorder)
                                                Spacer(modifier = Modifier.height(8.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "IP",
                                                        fontSize = 13.sp,
                                                        color = WarnaTeksDim
                                                    )
                                                    Text(
                                                        text = device.ip,
                                                        fontSize = 13.sp,
                                                        color = WarnaTeksUtama,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "Kategori",
                                                        fontSize = 13.sp,
                                                        color = WarnaTeksDim
                                                    )
                                                    Text(
                                                        text = device.kategori,
                                                        fontSize = 13.sp,
                                                        color = WarnaTeksUtama,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "Latency",
                                                        fontSize = 13.sp,
                                                        color = WarnaTeksDim
                                                    )
                                                    Text(
                                                        text = if (device.latency == 0) "TIMEOUT" else "${device.latency}ms",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when {
                                                            device.latency == 0 -> WarnaMerah
                                                            device.latency > 100 -> WarnaKuning
                                                            else -> WarnaHijau
                                                        }
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "Status",
                                                        fontSize = 13.sp,
                                                        color = WarnaTeksDim
                                                    )
                                                    Text(
                                                        text = device.status,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (device.status == "ONLINE") WarnaHijau else WarnaMerah
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Kalau IoT → tampil data sensor
                                        if (device.kategori == "IoT" && iotData.isNotEmpty()) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(16.dp),
                                                elevation = CardDefaults.cardElevation(2.dp),
                                                colors = CardDefaults.cardColors(containerColor = WarnaCard)
                                            ) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    Text(
                                                        text = "📊 Data Sensor",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = WarnaTeksUtama
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    HorizontalDivider(color = WarnaBorder)
                                                    Spacer(modifier = Modifier.height(8.dp))

                                                    // Tampil semua data dinamis
                                                    val skipKeys = listOf("device", "ip")
                                                    iotData.forEach { (key, value) ->
                                                        if (key !in skipKeys) {
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(vertical = 4.dp),
                                                                horizontalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                Text(
                                                                    text = key.replaceFirstChar { it.uppercase() },
                                                                    fontSize = 13.sp,
                                                                    color = WarnaTeksDim
                                                                )
                                                                Text(
                                                                    text = value.toString(),
                                                                    fontSize = 13.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = when {
                                                                        value.toString()
                                                                            .contains("Habis") -> WarnaMerah

                                                                        value.toString()
                                                                            .contains("Penuh") -> WarnaHijau

                                                                        value.toString()
                                                                            .contains("Sedikit") -> WarnaKuning

                                                                        value.toString() == "true" -> WarnaHijau
                                                                        value.toString() == "false" -> WarnaTeksDim
                                                                        else -> WarnaTeksUtama
                                                                    }
                                                                )
                                                            }
                                                            HorizontalDivider(color = WarnaBorder)
                                                        }
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            // Diagnosis IoT
                                            val persentasePakan =
                                                iotData["persentasePakan"]?.toString()
                                                    ?.toDoubleOrNull() ?: 0.0
                                            val persentaseAir = iotData["persentaseAir"]?.toString()
                                                ?.toDoubleOrNull() ?: 0.0

                                            if (persentasePakan < 20 || persentaseAir < 20) {
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(16.dp),
                                                    elevation = CardDefaults.cardElevation(2.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = Color(0xFFFFF7ED)
                                                    )
                                                ) {
                                                    Column(modifier = Modifier.padding(14.dp)) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(
                                                                8.dp
                                                            )
                                                        ) {
                                                            Text(text = "⚠️", fontSize = 18.sp)
                                                            Text(
                                                                text = "Gangguan Terdeteksi",
                                                                fontSize = 14.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFFB45309)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        if (persentasePakan < 20) {
                                                            Text(
                                                                text = "Problem: Pakan hampir habis (${persentasePakan.toInt()}%)",
                                                                fontSize = 13.sp,
                                                                color = WarnaMerah
                                                            )
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(
                                                                text = "Solusi:",
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = WarnaTeksUtama
                                                            )
                                                            Text(
                                                                text = "✅ Isi ulang pakan segera",
                                                                fontSize = 13.sp,
                                                                color = WarnaTeksUtama
                                                            )
                                                            Text(
                                                                text = "✅ Cek sensor ultrasonik",
                                                                fontSize = 13.sp,
                                                                color = WarnaTeksUtama
                                                            )
                                                        }
                                                        if (persentaseAir < 20) {
                                                            Spacer(modifier = Modifier.height(8.dp))
                                                            Text(
                                                                text = "Problem: Air hampir habis (${persentaseAir.toInt()}%)",
                                                                fontSize = 13.sp,
                                                                color = WarnaMerah
                                                            )
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(
                                                                text = "Solusi:",
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = WarnaTeksUtama
                                                            )
                                                            Text(
                                                                text = "✅ Isi ulang air segera",
                                                                fontSize = 13.sp,
                                                                color = WarnaTeksUtama
                                                            )
                                                            Text(
                                                                text = "✅ Cek sensor water level",
                                                                fontSize = 13.sp,
                                                                color = WarnaTeksUtama
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

// Kalau Jaringan → selalu tampil kondisi + diagnosis
                                        if (device.kategori == "Jaringan") {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(16.dp),
                                                elevation = CardDefaults.cardElevation(2.dp),
                                                colors = CardDefaults.cardColors(containerColor = WarnaCard)
                                            ) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    Text(
                                                        text = "📊 Kondisi Jaringan",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = WarnaTeksUtama
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    HorizontalDivider(color = WarnaBorder)
                                                    Spacer(modifier = Modifier.height(8.dp))

                                                    // Status
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(
                                                            text = "Status",
                                                            fontSize = 13.sp,
                                                            color = WarnaTeksDim
                                                        )
                                                        Text(
                                                            text = if (device.status == "ONLINE") "Normal" else "Down",
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (device.status == "ONLINE") WarnaHijau else WarnaMerah
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))

                                                    // Latency
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(
                                                            text = "Latency",
                                                            fontSize = 13.sp,
                                                            color = WarnaTeksDim
                                                        )
                                                        Text(
                                                            text = if (device.latency == 0) "TIMEOUT" else "${device.latency}ms",
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = when {
                                                                device.latency == 0 -> WarnaMerah
                                                                device.latency > 200 -> WarnaMerah
                                                                device.latency > 100 -> WarnaKuning
                                                                else -> WarnaHijau
                                                            }
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))

                                                    // Kualitas koneksi
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(
                                                            text = "Kualitas",
                                                            fontSize = 13.sp,
                                                            color = WarnaTeksDim
                                                        )
                                                        Text(
                                                            text = when {
                                                                device.status == "OFFLINE" -> "Tidak Terjangkau"
                                                                device.latency > 200 -> "Buruk"
                                                                device.latency > 100 -> "Kurang Baik"
                                                                device.latency > 50 -> "Cukup"
                                                                else -> "Baik"
                                                            },
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = when {
                                                                device.status == "OFFLINE" -> WarnaMerah
                                                                device.latency > 200 -> WarnaMerah
                                                                device.latency > 100 -> WarnaKuning
                                                                device.latency > 50 -> WarnaKuning
                                                                else -> WarnaHijau
                                                            }
                                                        )
                                                    }
                                                }
                                            }

                                            // Diagnosis — selalu tampil
                                            Spacer(modifier = Modifier.height(12.dp))
                                            val diagnosis = diagnosisPerangkat(device)
                                            if (diagnosis != null) {
                                                CardDiagnosis(diagnosis = diagnosis)
                                            } else {
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(16.dp),
                                                    elevation = CardDefaults.cardElevation(2.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = Color(0xFFEDFFF3)
                                                    )
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(16.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            8.dp
                                                        )
                                                    ) {
                                                        Text(text = "✅", fontSize = 20.sp)
                                                        Column {
                                                            Text(
                                                                text = "Tidak Ada Gangguan",
                                                                fontSize = 14.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = WarnaHijau
                                                            )
                                                            Text(
                                                                text = "Perangkat berjalan normal",
                                                                fontSize = 12.sp,
                                                                color = WarnaTeksDim
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            } else {
                                // Halaman list device (yang sudah ada)
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(WarnaBg)
                                        .padding(paddingValues)
                                        .padding(horizontal = 16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Device",
                                            color = WarnaTeksUtama,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clickable { showDialogTambah = true }
                                                .background(
                                                    Color(0xFF2563EB),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = "+ Tambah",
                                                fontSize = 13.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .border(1.dp, WarnaBorder, RoundedCornerShape(12.dp))
                                            .background(WarnaCard, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        androidx.compose.foundation.text.BasicTextField(
                                            value = searchQuery,
                                            onValueChange = { searchQuery = it },
                                            singleLine = true,
                                            textStyle = androidx.compose.ui.text.TextStyle(
                                                fontSize = 13.sp,
                                                color = WarnaTeksUtama
                                            ),
                                            decorationBox = { innerTextField: @Composable () -> Unit ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(horizontal = 12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text("🔍", fontSize = 13.sp)
                                                    Box(modifier = Modifier.weight(1f)) {
                                                        if (searchQuery.isEmpty()) {
                                                            Text("Cari perangkat...", fontSize = 13.sp, color = WarnaTeksDim)
                                                        }
                                                        innerTextField()
                                                    }
                                                    if (searchQuery.isNotEmpty()) {
                                                        Text(
                                                            text = "✕",
                                                            fontSize = 13.sp,
                                                            color = WarnaTeksDim,
                                                            modifier = Modifier.clickable { searchQuery = "" }
                                                        )
                                                    }
                                                }
                                            }
                                        )
                                    }

                                    if (devices.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(text = "🌐", fontSize = 48.sp)
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "Belum ada perangkat.",
                                                    fontSize = 14.sp,
                                                    color = WarnaTeksDim
                                                )
                                                Text(
                                                    text = "Tap '+ Tambah' untuk mulai.",
                                                    fontSize = 13.sp,
                                                    color = WarnaTeksDim
                                                )
                                            }
                                        }
                                    } else {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .verticalScroll(rememberScrollState())
                                        ) {

                                            var showDialogHapus by remember { mutableStateOf(false) }
                                            var deviceToDelete by remember {
                                                mutableStateOf<Perangkat?>(
                                                    null
                                                )
                                            }

                                            if (showDialogHapus && deviceToDelete != null) {
                                                androidx.compose.ui.window.Dialog(
                                                    onDismissRequest = { showDialogHapus = false }
                                                ) {
                                                    Card(
                                                        shape = RoundedCornerShape(16.dp),
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = WarnaCard
                                                        ),
                                                        elevation = CardDefaults.cardElevation(8.dp)
                                                    ) {
                                                        Column(modifier = Modifier.padding(20.dp)) {
                                                            Text(
                                                                text = "Hapus Perangkat",
                                                                fontSize = 18.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = WarnaTeksUtama
                                                            )
                                                            Spacer(modifier = Modifier.height(8.dp))
                                                            Text(
                                                                text = "Apakah kamu yakin ingin menghapus ${deviceToDelete!!.nama}?",
                                                                fontSize = 14.sp,
                                                                color = WarnaTeksDim
                                                            )
                                                            Spacer(modifier = Modifier.height(20.dp))
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.spacedBy(
                                                                    8.dp
                                                                )
                                                            ) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .weight(1f)
                                                                        .clickable {
                                                                            showDialogHapus = false
                                                                        }
                                                                        .background(
                                                                            WarnaBg,
                                                                            RoundedCornerShape(12.dp)
                                                                        )
                                                                        .padding(vertical = 12.dp),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Text(
                                                                        text = "Batal",
                                                                        fontSize = 14.sp,
                                                                        color = WarnaTeksDim,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                }
                                                                Box(
                                                                    modifier = Modifier
                                                                        .weight(1f)
                                                                        .clickable {
                                                                            kotlinx.coroutines.GlobalScope.launch {
                                                                                try {
                                                                                    val username = AppPrefs.getActiveUsername(this@MainActivity)
                                                                                    RetrofitClient.instance.hapusDevice(
                                                                                        deviceToDelete!!.id, username
                                                                                    )
                                                                                    fetchDevices()
                                                                                    withContext(
                                                                                        Dispatchers.Main
                                                                                    ) {
                                                                                        Toast.makeText(
                                                                                            this@MainActivity,
                                                                                            "✅ ${deviceToDelete!!.nama} berhasil dihapus",
                                                                                            Toast.LENGTH_SHORT
                                                                                        ).show()
                                                                                    }
                                                                                } catch (e: Exception) {
                                                                                    withContext(
                                                                                        Dispatchers.Main
                                                                                    ) {
                                                                                        Toast.makeText(
                                                                                            this@MainActivity,
                                                                                            "❌ Gagal hapus device",
                                                                                            Toast.LENGTH_SHORT
                                                                                        ).show()
                                                                                    }
                                                                                }
                                                                            }
                                                                            showDialogHapus = false
                                                                        }
                                                                        .background(
                                                                            WarnaMerah,
                                                                            RoundedCornerShape(12.dp)
                                                                        )
                                                                        .padding(vertical = 12.dp),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Text(
                                                                        text = "Hapus",
                                                                        fontSize = 14.sp,
                                                                        color = Color.White,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            val filteredDevices = if (searchQuery.isEmpty()) {
                                                devices
                                            } else {
                                                devices.filter {
                                                    it.nama.contains(searchQuery, ignoreCase = true) ||
                                                            it.ip.contains(searchQuery, ignoreCase = true) ||
                                                            it.kategori.contains(searchQuery, ignoreCase = true)
                                                }
                                            }

                                            if (filteredDevices.isEmpty() && searchQuery.isNotEmpty()) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(32.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(text = "🔍", fontSize = 32.sp)
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Text(
                                                            text = "Tidak ada perangkat\nyang cocok dengan \"$searchQuery\"",
                                                            fontSize = 14.sp,
                                                            color = WarnaTeksDim,
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }
                                                }
                                            }

                                            filteredDevices.forEach { perangkat ->
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp),
                                                    shape = RoundedCornerShape(16.dp),
                                                    elevation = CardDefaults.cardElevation(2.dp),
                                                    colors = CardDefaults.cardColors(containerColor = WarnaCard),
                                                    onClick = { selectedDevice = perangkat }
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(12.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(
                                                                12.dp
                                                            )
                                                        ) {
                                                            Text(
                                                                text = when (perangkat.nama) {
                                                                    "ROUTER" -> "🔌"
                                                                    "AP1", "AP2" -> "📶"
                                                                    "CCTV" -> "📷"
                                                                    "SENSOR" -> "🌡️"
                                                                    "SMART DOOR" -> "🚪"
                                                                    "SMART LAMP" -> "💡"
                                                                    "SMART PLUG" -> "🔋"
                                                                    "PAKAN BURUNG" -> "🐦"
                                                                    else -> if (perangkat.kategori == "IoT") "📱" else "📶"
                                                                },
                                                                fontSize = 24.sp
                                                            )
                                                            Column {
                                                                Text(
                                                                    text = perangkat.nama,
                                                                    fontSize = 14.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = WarnaTeksUtama
                                                                )
                                                                Text(
                                                                    text = perangkat.ip,
                                                                    fontSize = 12.sp,
                                                                    color = WarnaTeksDim
                                                                )
                                                                Text(
                                                                    text = perangkat.kategori,
                                                                    fontSize = 11.sp,
                                                                    color = Color(0xFF2563EB)
                                                                )
                                                            }
                                                        }
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(
                                                                8.dp
                                                            )
                                                        ) {
                                                            val warnaStatus =
                                                                if (perangkat.status == "ONLINE") WarnaHijau else WarnaMerah
                                                            Box(
                                                                modifier = Modifier
                                                                    .background(
                                                                        warnaStatus.copy(
                                                                            alpha = 0.1f
                                                                        ), RoundedCornerShape(20.dp)
                                                                    )
                                                                    .padding(
                                                                        horizontal = 10.dp,
                                                                        vertical = 4.dp
                                                                    )
                                                            ) {
                                                                Text(
                                                                    text = if (perangkat.status == "ONLINE") "Normal" else "Down",
                                                                    fontSize = 12.sp,
                                                                    color = warnaStatus,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                            Box(
                                                                modifier = Modifier
                                                                    .clickable {
                                                                        deviceToDelete = perangkat
                                                                        showDialogHapus = true
                                                                    }
                                                                    .background(
                                                                        WarnaMerah.copy(
                                                                            alpha = 0.1f
                                                                        ), RoundedCornerShape(8.dp)
                                                                    )
                                                                    .padding(8.dp)
                                                            ) {
                                                                Text(text = "🗑️", fontSize = 16.sp)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // ── TAB HISTORY ──
                        2 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(WarnaBg)
                                    .padding(paddingValues)
                                    .padding(horizontal = 16.dp)
                            ) {

                                var filterDevice by remember { mutableStateOf("Semua") }
                                var filterTanggal by remember { mutableStateOf("Semua") }
                                var showFilterDevice by remember { mutableStateOf(false) }
                                var showFilterTanggal by remember { mutableStateOf(false) }
                                var showDialogHapusHistory by remember { mutableStateOf(false) }
                                var searchHistory by remember { mutableStateOf("") } // ← TAMBAH INI

                                // Daftar device unik dari log
                                val daftarDevice = listOf("Semua") + logAktivitas.map { it.perangkat }.distinct().sorted()

                                // Daftar tanggal unik dari log
                                val daftarTanggal = listOf("Semua") + logAktivitas.map { it.tanggal }.distinct().sortedDescending()

                                val filteredLog = logAktivitas.filter { log ->
                                    (filterDevice == "Semua" || log.perangkat == filterDevice) &&
                                            (filterTanggal == "Semua" || log.tanggal == filterTanggal) &&
                                            (searchHistory.isEmpty() || log.perangkat.contains(searchHistory, ignoreCase = true) ||
                                                    log.pesan.contains(searchHistory, ignoreCase = true))
                                }

                                if (showDialogHapusHistory) {
                                    androidx.compose.ui.window.Dialog(
                                        onDismissRequest = { showDialogHapusHistory = false }
                                    ) {
                                        Card(
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = WarnaCard),
                                            elevation = CardDefaults.cardElevation(8.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(20.dp)) {
                                                Text(
                                                    text = "Hapus Semua History",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = WarnaTeksUtama
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "Semua history aktivitas akan dihapus permanen. Lanjutkan?",
                                                    fontSize = 14.sp,
                                                    color = WarnaTeksDim
                                                )
                                                Spacer(modifier = Modifier.height(20.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clickable { showDialogHapusHistory = false }
                                                            .background(WarnaBg, RoundedCornerShape(12.dp))
                                                            .padding(vertical = 12.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("Batal", fontSize = 14.sp, color = WarnaTeksDim, fontWeight = FontWeight.Bold)
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clickable {
                                                                kotlinx.coroutines.GlobalScope.launch {
                                                                    try {
                                                                        val username = AppPrefs.getActiveUsername(this@MainActivity)
                                                                        RetrofitClient.instance.hapusSemuaLog(username)
                                                                        fetchDevices()
                                                                        withContext(Dispatchers.Main) {
                                                                            Toast.makeText(this@MainActivity, "✅ History berhasil dihapus", Toast.LENGTH_SHORT).show()
                                                                        }
                                                                    } catch (e: Exception) {
                                                                        withContext(Dispatchers.Main) {
                                                                            Toast.makeText(this@MainActivity, "❌ Gagal hapus history", Toast.LENGTH_SHORT).show()
                                                                        }
                                                                    }
                                                                }
                                                                showDialogHapusHistory = false
                                                            }
                                                            .background(WarnaMerah, RoundedCornerShape(12.dp))
                                                            .padding(vertical = 12.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("Hapus Semua", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Dialog Filter Device
                                if (showFilterDevice) {
                                    androidx.compose.ui.window.Dialog(onDismissRequest = { showFilterDevice = false }) {
                                        Card(
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = WarnaCard),
                                            elevation = CardDefaults.cardElevation(8.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .padding(20.dp)
                                                    .verticalScroll(rememberScrollState())
                                            ) {
                                                Text("Filter Device", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
                                                Spacer(modifier = Modifier.height(12.dp))
                                                daftarDevice.forEach { device ->
                                                    val dipilih = filterDevice == device
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                filterDevice = device
                                                                showFilterDevice = false
                                                            }
                                                            .background(
                                                                if (dipilih) Color(0xFF2563EB).copy(alpha = 0.1f) else Color.Transparent,
                                                                RoundedCornerShape(8.dp)
                                                            )
                                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = device,
                                                                fontSize = 14.sp,
                                                                color = if (dipilih) Color(0xFF2563EB) else WarnaTeksUtama,
                                                                fontWeight = if (dipilih) FontWeight.Bold else FontWeight.Normal
                                                            )
                                                            if (dipilih) Text("✓", fontSize = 14.sp, color = Color(0xFF2563EB))
                                                        }
                                                    }
                                                    if (device != daftarDevice.last()) HorizontalDivider(color = WarnaBorder)
                                                }
                                            }
                                        }
                                    }
                                }

                                // Dialog Filter Tanggal
                                if (showFilterTanggal) {
                                    androidx.compose.ui.window.Dialog(onDismissRequest = { showFilterTanggal = false }) {
                                        Card(
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = WarnaCard),
                                            elevation = CardDefaults.cardElevation(8.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .padding(20.dp)
                                                    .verticalScroll(rememberScrollState())
                                            ) {
                                                Text("Filter Tanggal", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
                                                Spacer(modifier = Modifier.height(12.dp))
                                                val today = getCurrentDate()
                                                val calendar = java.util.Calendar.getInstance()
                                                calendar.add(java.util.Calendar.DAY_OF_MONTH, -1)
                                                val yesterday = "%02d/%02d/%04d".format(
                                                    calendar.get(java.util.Calendar.DAY_OF_MONTH),
                                                    calendar.get(java.util.Calendar.MONTH) + 1,
                                                    calendar.get(java.util.Calendar.YEAR)
                                                )
                                                daftarTanggal.forEach { tanggal ->
                                                    val dipilih = filterTanggal == tanggal
                                                    val label = when (tanggal) {
                                                        "Semua" -> "Semua"
                                                        today -> "Hari Ini ($tanggal)"
                                                        yesterday -> "Kemarin ($tanggal)"
                                                        else -> tanggal
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                filterTanggal = tanggal
                                                                showFilterTanggal = false
                                                            }
                                                            .background(
                                                                if (dipilih) Color(0xFF2563EB).copy(alpha = 0.1f) else Color.Transparent,
                                                                RoundedCornerShape(8.dp)
                                                            )
                                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = label,
                                                                fontSize = 14.sp,
                                                                color = if (dipilih) Color(0xFF2563EB) else WarnaTeksUtama,
                                                                fontWeight = if (dipilih) FontWeight.Bold else FontWeight.Normal
                                                            )
                                                            if (dipilih) Text("✓", fontSize = 14.sp, color = Color(0xFF2563EB))
                                                        }
                                                    }
                                                    if (tanggal != daftarTanggal.last()) HorizontalDivider(color = WarnaBorder)
                                                }
                                            }
                                        }
                                    }
                                }

                                // Header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("History", color = WarnaTeksUtama, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Box(
                                        modifier = Modifier
                                            .clickable { showDialogHapusHistory = true }
                                            .background(WarnaMerah.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("🗑️ Hapus Semua", fontSize = 12.sp, color = WarnaMerah, fontWeight = FontWeight.Bold)
                                    }
                                }

// ── Search + Filter ──
                                var showFilterDropdown by remember { mutableStateOf(false) }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .zIndex(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Search bar
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .border(1.dp, WarnaBorder, RoundedCornerShape(12.dp))
                                                .background(WarnaCard, RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            androidx.compose.foundation.text.BasicTextField(
                                                value = searchHistory,
                                                onValueChange = { searchHistory = it },
                                                singleLine = true,
                                                textStyle = androidx.compose.ui.text.TextStyle(
                                                    fontSize = 13.sp,
                                                    color = WarnaTeksUtama
                                                ),
                                                decorationBox = { innerTextField: @Composable () -> Unit ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(horizontal = 12.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Text("🔍", fontSize = 13.sp)
                                                        Box(modifier = Modifier.weight(1f)) {
                                                            if (searchHistory.isEmpty()) {
                                                                Text("Cari aktivitas...", fontSize = 13.sp, color = WarnaTeksDim)
                                                            }
                                                            innerTextField()
                                                        }
                                                        if (searchHistory.isNotEmpty()) {
                                                            Text(
                                                                text = "✕",
                                                                fontSize = 13.sp,
                                                                color = WarnaTeksDim,
                                                                modifier = Modifier.clickable { searchHistory = "" }
                                                            )
                                                        }
                                                    }
                                                }
                                            )
                                        }

                                        // Tombol filter kecil
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(
                                                    if (filterDevice != "Semua" || filterTanggal != "Semua")
                                                        Color(0xFF2563EB) else WarnaCard,
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (filterDevice != "Semua" || filterTanggal != "Semua")
                                                        Color(0xFF2563EB) else WarnaBorder,
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .clickable { showFilterDropdown = !showFilterDropdown },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "⚙️",
                                                fontSize = 18.sp
                                            )
                                        }
                                    }

// Dropdown filter
                                    if (showFilterDropdown) {
                                        androidx.compose.ui.window.Popup(
                                            alignment = Alignment.TopEnd,
                                            offset = androidx.compose.ui.unit.IntOffset(0, 130)
                                        ) {
                                        Card(
                                            modifier = Modifier.width(220.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            elevation = CardDefaults.cardElevation(8.dp),
                                            colors = CardDefaults.cardColors(containerColor = WarnaCard)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text("Filter", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
                                                Spacer(modifier = Modifier.height(8.dp))

                                                // Filter Device
                                                Text("Device", fontSize = 11.sp, color = WarnaTeksDim)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                androidx.compose.foundation.lazy.LazyRow(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    items(daftarDevice.size) { i ->
                                                        val d = daftarDevice[i]
                                                        val dipilih = filterDevice == d
                                                        Box(
                                                            modifier = Modifier
                                                                .clickable { filterDevice = d }
                                                                .background(
                                                                    if (dipilih) Color(0xFF2563EB) else WarnaBg,
                                                                    RoundedCornerShape(20.dp)
                                                                )
                                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                                        ) {
                                                            Text(
                                                                text = d,
                                                                fontSize = 11.sp,
                                                                color = if (dipilih) Color.White else WarnaTeksDim,
                                                                fontWeight = if (dipilih) FontWeight.Bold else FontWeight.Normal
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))

                                                // Filter Tanggal
                                                Text("Tanggal", fontSize = 11.sp, color = WarnaTeksDim)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                androidx.compose.foundation.lazy.LazyRow(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    items(daftarTanggal.size) { i ->
                                                        val t = daftarTanggal[i]
                                                        val dipilih = filterTanggal == t
                                                        val today = getCurrentDate()
                                                        val label = when (t) {
                                                            "Semua" -> "Semua"
                                                            today -> "Hari Ini"
                                                            else -> t
                                                        }
                                                        Box(
                                                            modifier = Modifier
                                                                .clickable { filterTanggal = t }
                                                                .background(
                                                                    if (dipilih) Color(0xFF2563EB) else WarnaBg,
                                                                    RoundedCornerShape(20.dp)
                                                                )
                                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                                        ) {
                                                            Text(
                                                                text = label,
                                                                fontSize = 11.sp,
                                                                color = if (dipilih) Color.White else WarnaTeksDim,
                                                                fontWeight = if (dipilih) FontWeight.Bold else FontWeight.Normal
                                                            )
                                                        }
                                                    }
                                                }

                                                // Reset filter
                                                if (filterDevice != "Semua" || filterTanggal != "Semua") {
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    HorizontalDivider(color = WarnaBorder)
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                filterDevice = "Semua"
                                                                filterTanggal = "Semua"
                                                            }
                                                            .background(WarnaMerah.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                                            .padding(vertical = 6.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("Reset Filter", fontSize = 12.sp, color = WarnaMerah, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                            }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Info jumlah hasil filter
                                if (filterDevice != "Semua" || filterTanggal != "Semua") {
                                    Text(
                                        text = "Menampilkan ${filteredLog.size} aktivitas",
                                        fontSize = 12.sp,
                                        color = WarnaTeksDim,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }

                                if (logAktivitas.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "Belum ada history.\nTekan 'Cek Semua Jaringan' dulu.",
                                            fontSize = 14.sp,
                                            color = WarnaTeksDim,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else if (filteredLog.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("🔍", fontSize = 32.sp)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Tidak ada history\nsesuai filter yang dipilih",
                                                fontSize = 14.sp,
                                                color = WarnaTeksDim,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                } else {
                                    val today = getCurrentDate()
                                    val calendar = java.util.Calendar.getInstance()
                                    calendar.add(java.util.Calendar.DAY_OF_MONTH, -1)
                                    val yesterday = "%02d/%02d/%04d".format(
                                        calendar.get(java.util.Calendar.DAY_OF_MONTH),
                                        calendar.get(java.util.Calendar.MONTH) + 1,
                                        calendar.get(java.util.Calendar.YEAR)
                                    )
                                    val groupedLog = filteredLog.groupBy { it.tanggal }
                                    Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                                        groupedLog.forEach { (tanggal, logs) ->
                                            val labelTanggal = when (tanggal) {
                                                today -> "Hari Ini"
                                                yesterday -> "Kemarin"
                                                else -> tanggal
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = labelTanggal,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2563EB),
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            )
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(16.dp),
                                                elevation = CardDefaults.cardElevation(2.dp),
                                                colors = CardDefaults.cardColors(containerColor = WarnaCard)
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    logs.forEachIndexed { index, log ->
                                                        AktivitasItem(log = log)
                                                        if (index < logs.size - 1) HorizontalDivider(color = WarnaBorder)
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }

                        // ── TAB SETTINGS ──
                        3 -> {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            var serverUrl by remember { mutableStateOf(AppPrefs.getServerUrl(context)) }
                            var namaPengguna by remember {
                                mutableStateOf(
                                    AppPrefs.getNamaPengguna(
                                        context
                                    )
                                )
                            }
                            var intervalScan by remember {
                                mutableStateOf(
                                    AppPrefs.getIntervalScan(
                                        context
                                    )
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(WarnaBg)
                                    .padding(paddingValues)
                                    .padding(horizontal = 16.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Settings",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WarnaTeksUtama
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                // ── Server ──
                                Text(
                                    text = "Server",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarnaTeksUtama
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(2.dp),
                                    colors = CardDefaults.cardColors(containerColor = WarnaCard)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "URL Server",
                                            fontSize = 13.sp,
                                            color = WarnaTeksDim
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        androidx.compose.material3.OutlinedTextField(
                                            value = serverUrl,
                                            onValueChange = { serverUrl = it },
                                            placeholder = {
                                                Text(
                                                    "http://192.168.1.5:8000/",
                                                    color = WarnaTeksDim
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Tombol Test Koneksi
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        kotlinx.coroutines.GlobalScope.launch {
                                                            try {
                                                                RetrofitClient.BASE_URL = serverUrl
                                                                RetrofitClient.instance.getDevices()
                                                                withContext(Dispatchers.Main) {
                                                                    Toast.makeText(
                                                                        context,
                                                                        "✅ Server berhasil terhubung",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                            } catch (e: Exception) {
                                                                withContext(Dispatchers.Main) {
                                                                    Toast.makeText(
                                                                        context,
                                                                        "❌ Gagal konek ke server",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                            }
                                                        }
                                                    }
                                                    .background(WarnaBg, RoundedCornerShape(12.dp))
                                                    .padding(vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Test Koneksi",
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF2563EB),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            // Tombol Simpan
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        AppPrefs.setServerUrl(context, serverUrl)
                                                        RetrofitClient.BASE_URL = serverUrl
                                                        Toast.makeText(
                                                            context,
                                                            "✅ URL server disimpan",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                    .background(
                                                        Color(0xFF2563EB),
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .padding(vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Simpan",
                                                    fontSize = 13.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // ── Notifikasi ──
                                Text(
                                    text = "Monitoring",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarnaTeksUtama
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(2.dp),
                                    colors = CardDefaults.cardColors(containerColor = WarnaCard)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Interval Scan",
                                            fontSize = 13.sp,
                                            color = WarnaTeksDim
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf(
                                                30 to "30 detik",
                                                60 to "1 menit",
                                                300 to "5 menit",
                                                600 to "10 menit"
                                            ).forEach { (nilai, label) ->
                                                val dipilih = intervalScan == nilai
                                                Box(
                                                    modifier = Modifier
                                                        .clickable {
                                                            intervalScan = nilai
                                                            AppPrefs.setIntervalScan(context, nilai)
                                                            Toast.makeText(
                                                                context,
                                                                "✅ Interval disimpan",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                        .background(
                                                            if (dipilih) Color(0xFF2563EB) else WarnaBg,
                                                            RoundedCornerShape(20.dp)
                                                        )
                                                        .padding(
                                                            horizontal = 12.dp,
                                                            vertical = 8.dp
                                                        )
                                                ) {
                                                    Text(
                                                        text = label,
                                                        fontSize = 12.sp,
                                                        color = if (dipilih) Color.White else WarnaTeksDim,
                                                        fontWeight = if (dipilih) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // ── Tampilan ──
                                Text(
                                    text = "Tampilan",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarnaTeksUtama
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(2.dp),
                                    colors = CardDefaults.cardColors(containerColor = WarnaCard)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Nama Pengguna",
                                            fontSize = 13.sp,
                                            color = WarnaTeksDim
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            androidx.compose.material3.OutlinedTextField(
                                                value = namaPengguna,
                                                onValueChange = { namaPengguna = it },
                                                placeholder = {
                                                    Text(
                                                        "contoh: Adrian",
                                                        color = WarnaTeksDim
                                                    )
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(12.dp),
                                                singleLine = true
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clickable {
                                                        AppPrefs.setNamaPengguna(
                                                            context,
                                                            namaPengguna
                                                        )
                                                        Toast.makeText(
                                                            context,
                                                            "✅ Nama disimpan",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                    .background(
                                                        Color(0xFF2563EB),
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Simpan",
                                                    fontSize = 13.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                // ── Keamanan ──
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Keamanan",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarnaTeksUtama
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                var passwordLama   by remember { mutableStateOf("") }
                                var passwordBaru2  by remember { mutableStateOf("") }
                                var konfirmPass2   by remember { mutableStateOf("") }
                                var showOld        by remember { mutableStateOf(false) }
                                var showNew        by remember { mutableStateOf(false) }
                                var errGantiPass   by remember { mutableStateOf("") }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(2.dp),
                                    colors = CardDefaults.cardColors(containerColor = WarnaCard)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Ganti Password", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text("Password Lama", fontSize = 13.sp, color = WarnaTeksDim)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        androidx.compose.material3.OutlinedTextField(
                                            value = passwordLama,
                                            onValueChange = { passwordLama = it },
                                            placeholder = { Text("Password saat ini", color = WarnaTeksDim) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true,
                                            visualTransformation = if (showOld)
                                                androidx.compose.ui.text.input.VisualTransformation.None
                                            else
                                                androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                            trailingIcon = {
                                                Text(
                                                    text = if (showOld) "👁️" else "🙈",
                                                    modifier = Modifier.clickable { showOld = !showOld },
                                                    fontSize = 16.sp
                                                )
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text("Password Baru", fontSize = 13.sp, color = WarnaTeksDim)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        androidx.compose.material3.OutlinedTextField(
                                            value = passwordBaru2,
                                            onValueChange = { passwordBaru2 = it },
                                            placeholder = { Text("Min. 6 karakter", color = WarnaTeksDim) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true,
                                            visualTransformation = if (showNew)
                                                androidx.compose.ui.text.input.VisualTransformation.None
                                            else
                                                androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                            trailingIcon = {
                                                Text(
                                                    text = if (showNew) "👁️" else "🙈",
                                                    modifier = Modifier.clickable { showNew = !showNew },
                                                    fontSize = 16.sp
                                                )
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text("Konfirmasi Password Baru", fontSize = 13.sp, color = WarnaTeksDim)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        androidx.compose.material3.OutlinedTextField(
                                            value = konfirmPass2,
                                            onValueChange = { konfirmPass2 = it },
                                            placeholder = { Text("Ulangi password baru", color = WarnaTeksDim) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true,
                                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                                        )

                                        if (errGantiPass.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(errGantiPass, fontSize = 13.sp, color = WarnaMerah)
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    val activeUser = AppPrefs.getActiveUsername(context)
                                                    errGantiPass = when {
                                                        passwordLama.isBlank() || passwordBaru2.isBlank() ->
                                                            "Semua field harus diisi"
                                                        passwordLama != AppPrefs.getPassword(context, activeUser) ->
                                                            "Password lama salah"
                                                        passwordBaru2.length < 6 ->
                                                            "Password baru minimal 6 karakter"
                                                        passwordBaru2 != konfirmPass2 ->
                                                            "Password baru tidak cocok"
                                                        else -> {
                                                            AppPrefs.gantiPassword(context, activeUser, passwordBaru2)
                                                            passwordLama = ""
                                                            passwordBaru2 = ""
                                                            konfirmPass2 = ""
                                                            Toast.makeText(context, "✅ Password berhasil diganti", Toast.LENGTH_SHORT).show()
                                                            ""
                                                        }
                                                    }
                                                }
                                                .background(Color(0xFF2563EB), RoundedCornerShape(12.dp))
                                                .padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Simpan Password", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // ── Informasi App ──
                                Text(
                                    text = "Informasi",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarnaTeksUtama
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(2.dp),
                                    colors = CardDefaults.cardColors(containerColor = WarnaCard)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Nama Aplikasi",
                                                fontSize = 13.sp,
                                                color = WarnaTeksDim
                                            )
                                            Text(
                                                text = "NetPulse",
                                                fontSize = 13.sp,
                                                color = WarnaTeksUtama,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider(color = WarnaBorder)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Versi",
                                                fontSize = 13.sp,
                                                color = WarnaTeksDim
                                            )
                                            Text(
                                                text = "1.0.0",
                                                fontSize = 13.sp,
                                                color = WarnaTeksUtama,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider(color = WarnaBorder)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Developer",
                                                fontSize = 13.sp,
                                                color = WarnaTeksDim
                                            )
                                            Text(
                                                text = "Adrian",
                                                fontSize = 13.sp,
                                                color = WarnaTeksUtama,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider(color = WarnaBorder)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Institusi",
                                                fontSize = 13.sp,
                                                color = WarnaTeksDim
                                            )
                                            Text(
                                                text = "UNPAM",
                                                fontSize = 13.sp,
                                                color = WarnaTeksUtama,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            AppPrefs.setLoggedIn(context, false)
                                            Toast.makeText(context, "Berhasil logout", Toast.LENGTH_SHORT).show()
                                            // Restart activity
                                            (context as? android.app.Activity)?.recreate()
                                        }
                                        .background(WarnaMerah.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Logout",
                                        fontSize = 14.sp,
                                        color = WarnaMerah,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
