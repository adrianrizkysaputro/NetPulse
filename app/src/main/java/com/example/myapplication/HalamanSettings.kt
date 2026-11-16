package com.example.myapplication

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HalamanSettings(
    fcmToken: String,
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    var serverUrl by remember { mutableStateOf(AppPrefs.getServerUrl(context)) }
    var namaPengguna by remember { mutableStateOf(AppPrefs.getNamaPengguna(context)) }
    var intervalScan by remember { mutableStateOf(AppPrefs.getIntervalScan(context)) }

    // State for Dialogs
    var showUrlDialog by remember { mutableStateOf(false) }
    var showIntervalDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    StatusBarIkonPutih()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = paddingValues.calculateBottomPadding())
    ) {
        // Teks header + avatar. Bar biru-nya digambar MainActivity di belakang (tingginya beranimasi antar tab)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(200.dp)
                .padding(top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Pengaturan", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 6.dp,
                modifier = Modifier.size(84.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = namaPengguna.take(1).uppercase(),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarnaBrand
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(namaPengguna, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                "@" + AppPrefs.getActiveUsername(context),
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // --- Sections ---
            SettingsGroup(title = "Koneksi & Server") {
                SettingsItem(
                    icon = Icons.Default.Dns,
                    title = "URL Server",
                    subtitle = serverUrl,
                    onClick = { showUrlDialog = true }
                )
            }

            SettingsGroup(title = "Monitoring") {
                val labelInterval = when (intervalScan) {
                    30 -> "30 detik"
                    60 -> "1 menit"
                    300 -> "5 menit"
                    600 -> "10 menit"
                    else -> "$intervalScan detik"
                }
                SettingsItem(
                    icon = Icons.Default.Speed,
                    title = "Interval Scan",
                    subtitle = labelInterval,
                    onClick = { showIntervalDialog = true }
                )
            }

            SettingsGroup(title = "Akun & Keamanan") {
                SettingsItem(
                    icon = Icons.Default.Person,
                    title = "Profil Pengguna",
                    subtitle = "Ubah nama tampilan",
                    onClick = { showProfileDialog = true }
                )
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Ganti Password",
                    subtitle = "Keamanan akun",
                    onClick = { showPasswordDialog = true }
                )
            }

            SettingsGroup(title = "Aplikasi") {
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifikasi",
                    subtitle = if (fcmToken.isNotEmpty()) "Aktif" else "Menghubungkan...",
                    onClick = { /* Could show token info if needed */ }
                )
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Tentang NetPulse",
                    subtitle = "Versi 1.0.0",
                    onClick = { showAboutDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Keluar
            Card(
                onClick = {
                    AppPrefs.setLoggedIn(context, false)
                    (context as Activity).recreate()
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEDED)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = WarnaMerah)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Keluar dari Akun", color = WarnaMerah, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // --- Dialogs ---

    if (showUrlDialog) {
        var tempUrl by remember { mutableStateOf(serverUrl) }
        DialogNetPulse(
            onDismiss = { showUrlDialog = false },
            icon = Icons.Default.Dns,
            judul = "URL Server",
            deskripsi = "Alamat server untuk sinkronisasi data.",
            teksAksi = "Simpan",
            onAksi = {
                AppPrefs.setServerUrl(context, tempUrl)
                RetrofitClient.BASE_URL = tempUrl
                serverUrl = tempUrl
                showUrlDialog = false
                Toast.makeText(context, "✅ URL disimpan", Toast.LENGTH_SHORT).show()
            }
        ) {
            FieldNetPulse(value = tempUrl, onValueChange = { tempUrl = it }, label = "URL Server", placeholder = "http://192.168.1.5:8000/")
        }
    }

    if (showIntervalDialog) {
        DialogNetPulse(
            onDismiss = { showIntervalDialog = false },
            icon = Icons.Default.Speed,
            judul = "Interval Scan",
            deskripsi = "Seberapa sering NetPulse mengecek jaringan.",
            warna = Color(0xFF8B5CF6)
        ) {
            listOf(30 to "30 detik", 60 to "1 menit", 300 to "5 menit", 600 to "10 menit").forEach { (v, l) ->
                val dipilih = intervalScan == v
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (dipilih) WarnaBrand.copy(alpha = 0.1f) else WarnaBg)
                        .clickable {
                            intervalScan = v
                            AppPrefs.setIntervalScan(context, v)
                            showIntervalDialog = false
                            Toast.makeText(context, "✅ Interval diubah", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = l,
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp,
                        fontWeight = if (dipilih) FontWeight.Bold else FontWeight.Medium,
                        color = if (dipilih) WarnaBrand else WarnaTeksUtama
                    )
                    if (dipilih) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WarnaBrand, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }

    if (showProfileDialog) {
        var tempNama by remember { mutableStateOf(namaPengguna) }
        DialogNetPulse(
            onDismiss = { showProfileDialog = false },
            icon = Icons.Default.Person,
            judul = "Profil",
            deskripsi = "Nama ini tampil di sapaan Dashboard.",
            warna = Color(0xFF06B6D4),
            teksAksi = "Simpan",
            onAksi = {
                AppPrefs.setNamaPengguna(context, tempNama)
                namaPengguna = tempNama
                showProfileDialog = false
                Toast.makeText(context, "✅ Profil diperbarui", Toast.LENGTH_SHORT).show()
            }
        ) {
            FieldNetPulse(value = tempNama, onValueChange = { tempNama = it }, label = "Nama Tampilan")
        }
    }

    if (showPasswordDialog) {
        var pLama by remember { mutableStateOf("") }
        var pBaru by remember { mutableStateOf("") }
        var pKonfirm by remember { mutableStateOf("") }
        var error by remember { mutableStateOf("") }

        DialogNetPulse(
            onDismiss = { showPasswordDialog = false },
            icon = Icons.Default.Lock,
            judul = "Ganti Password",
            deskripsi = "Gunakan minimal 6 karakter.",
            warna = Color(0xFFF59E0B),
            teksAksi = "Ganti",
            onAksi = {
                val activeUser = AppPrefs.getActiveUsername(context)
                error = when {
                    pLama != AppPrefs.getPassword(context, activeUser) -> "Password lama salah"
                    pBaru.length < 6 -> "Minimal 6 karakter"
                    pBaru != pKonfirm -> "Password tidak cocok"
                    else -> {
                        AppPrefs.gantiPassword(context, activeUser, pBaru)
                        showPasswordDialog = false
                        Toast.makeText(context, "✅ Password diganti", Toast.LENGTH_SHORT).show()
                        ""
                    }
                }
            }
        ) {
            FieldNetPulse(value = pLama, onValueChange = { pLama = it }, label = "Password Lama", password = true)
            Spacer(modifier = Modifier.height(8.dp))
            FieldNetPulse(value = pBaru, onValueChange = { pBaru = it }, label = "Password Baru", password = true)
            Spacer(modifier = Modifier.height(8.dp))
            FieldNetPulse(value = pKonfirm, onValueChange = { pKonfirm = it }, label = "Konfirmasi", password = true)
            if (error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(error, color = WarnaMerah, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }

    if (showAboutDialog) {
        DialogNetPulse(
            onDismiss = { showAboutDialog = false },
            icon = Icons.Default.MonitorHeart,
            judul = "NetPulse",
            deskripsi = "Network & IoT Monitoring System",
            teksBatal = "Tutup"
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(WarnaBg)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BarisAbout("Versi", "1.0.0")
                BarisAbout("Developer", "Adrian")
                BarisAbout("Institusi", "Unpam Serang")
            }
        }
    }
}

@Composable
private fun BarisAbout(label: String, nilai: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = WarnaTeksDim)
        Text(nilai, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
    }
}

@Composable
fun StatusBarIkonPutih() {
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.isAppearanceLightStatusBars = false
        onDispose { }
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = WarnaTeksDim,
            modifier = Modifier.padding(start = 8.dp, top = 20.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = WarnaCard)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val warna = when (icon) {
        Icons.Default.Dns -> Color(0xFF2563EB)
        Icons.Default.Speed -> Color(0xFF8B5CF6)
        Icons.Default.Person -> Color(0xFF06B6D4)
        Icons.Default.Lock -> Color(0xFFF59E0B)
        Icons.Default.Notifications -> Color(0xFF22C55E)
        Icons.Default.Info -> Color(0xFF64748B)
        else -> WarnaBrand
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(warna.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = warna)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = WarnaTeksUtama)
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = WarnaTeksDim,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = WarnaTeksDim.copy(alpha = 0.5f)
        )
    }
}