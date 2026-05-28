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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarnaBg)
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        // --- Custom Header ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF2563EB).copy(alpha = 0.1f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = namaPengguna.take(1).uppercase(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = namaPengguna,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarnaTeksUtama
                )
                Text(
                    text = AppPrefs.getActiveUsername(context),
                    fontSize = 14.sp,
                    color = WarnaTeksDim
                )
            }
        }

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
        
        // Logout Item
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    AppPrefs.setLoggedIn(context, false)
                    (context as Activity).recreate()
                },
            color = WarnaCard
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = WarnaMerah)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Keluar dari Akun", color = WarnaMerah, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }

    // --- Dialogs ---

    if (showUrlDialog) {
        var tempUrl by remember { mutableStateOf(serverUrl) }
        AlertDialog(
            onDismissRequest = { showUrlDialog = false },
            title = { Text("URL Server", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Alamat server untuk sinkronisasi data.", fontSize = 13.sp, color = WarnaTeksDim)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempUrl,
                        onValueChange = { tempUrl = it },
                        placeholder = { Text("http://192.168.1.5:8000/") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    AppPrefs.setServerUrl(context, tempUrl)
                    RetrofitClient.BASE_URL = tempUrl
                    serverUrl = tempUrl
                    showUrlDialog = false
                    Toast.makeText(context, "✅ URL disimpan", Toast.LENGTH_SHORT).show()
                }) { Text("Simpan", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showUrlDialog = false }) { Text("Batal") }
            }
        )
    }

    if (showIntervalDialog) {
        AlertDialog(
            onDismissRequest = { showIntervalDialog = false },
            title = { Text("Interval Scan", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf(30 to "30 detik", 60 to "1 menit", 300 to "5 menit", 600 to "10 menit").forEach { (v, l) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    intervalScan = v
                                    AppPrefs.setIntervalScan(context, v)
                                    showIntervalDialog = false
                                    Toast.makeText(context, "✅ Interval diubah", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = intervalScan == v, onClick = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(l)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showIntervalDialog = false }) { Text("Batal") }
            }
        )
    }

    if (showProfileDialog) {
        var tempNama by remember { mutableStateOf(namaPengguna) }
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("Profil", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = tempNama,
                    onValueChange = { tempNama = it },
                    label = { Text("Nama Tampilan") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    AppPrefs.setNamaPengguna(context, tempNama)
                    namaPengguna = tempNama
                    showProfileDialog = false
                    Toast.makeText(context, "✅ Profil diperbarui", Toast.LENGTH_SHORT).show()
                }) { Text("Simpan", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) { Text("Batal") }
            }
        )
    }

    if (showPasswordDialog) {
        var pLama by remember { mutableStateOf("") }
        var pBaru by remember { mutableStateOf("") }
        var pKonfirm by remember { mutableStateOf("") }
        var error by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Ganti Password", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = pLama, onValueChange = { pLama = it }, label = { Text("Password Lama") }, visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = pBaru, onValueChange = { pBaru = it }, label = { Text("Password Baru") }, visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = pKonfirm, onValueChange = { pKonfirm = it }, label = { Text("Konfirmasi") }, visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(), shape = RoundedCornerShape(12.dp))
                    if (error.isNotEmpty()) Text(error, color = WarnaMerah, fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
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
                }) { Text("Ganti", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) { Text("Batal") }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("NetPulse", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Versi 1.0.0", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Network & IoT Monitoring System", fontSize = 13.sp, color = WarnaTeksDim)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Developer : Adrian", fontSize = 13.sp)
                    Text("Institusi : Unpam Serang", fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("Tutup") }
            }
        )
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2563EB),
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
        )
        Surface(color = WarnaCard, modifier = Modifier.fillMaxWidth()) {
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(WarnaBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = WarnaTeksUtama)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = WarnaTeksUtama)
            Text(text = subtitle, fontSize = 13.sp, color = WarnaTeksDim)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = WarnaBorder
        )
    }
}
