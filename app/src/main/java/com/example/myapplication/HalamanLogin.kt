package com.example.myapplication

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HalamanLogin(onLoginBerhasil: () -> Unit) {
    val context = LocalContext.current
    StatusBarIkonPutih()

    // Mode: "login", "register", "lupa_password"
    var mode by remember { mutableStateOf("login") }

    var username    by remember { mutableStateOf("") }
    var password    by remember { mutableStateOf("") }
    var passwordBaru by remember { mutableStateOf("") }
    var konfirmPassword by remember { mutableStateOf("") }
    var showPass    by remember { mutableStateOf(false) }
    var showPassBaru by remember { mutableStateOf(false) }
    var errorMsg    by remember { mutableStateOf("") }

    // Aksi tombol utama (logika asli, tidak diubah)
    val aksiUtama: () -> Unit = aksi@{
        errorMsg = ""
        when (mode) {
            "login" -> {
                if (username.isBlank() || password.isBlank()) {
                    errorMsg = "Username dan password tidak boleh kosong"
                    return@aksi
                }
                if (!AppPrefs.akuuAdaUsername(context, username)) {
                    errorMsg = "Akun tidak ditemukan"
                    return@aksi
                }
                if (password != AppPrefs.getPassword(context, username)) {
                    errorMsg = "Password salah, coba lagi"
                    password = ""
                    return@aksi
                }
                AppPrefs.setActiveUsername(context, username)
                AppPrefs.setLoggedIn(context, true)
                onLoginBerhasil()
            }
            "register" -> {
                if (username.isBlank()) {
                    errorMsg = "Username tidak boleh kosong"
                    return@aksi
                }
                if (password.length < 6) {
                    errorMsg = "Password minimal 6 karakter"
                    return@aksi
                }
                if (password != konfirmPassword) {
                    errorMsg = "Password tidak cocok"
                    return@aksi
                }
                if (AppPrefs.akuuAdaUsername(context, username)) {
                    errorMsg = "Username sudah digunakan"
                    return@aksi
                }
                AppPrefs.simpanAkun(context, username, password)
                AppPrefs.setActiveUsername(context, username)
                AppPrefs.setLoggedIn(context, true)
                onLoginBerhasil()
            }
            "lupa_password" -> {
                if (username.isBlank()) {
                    errorMsg = "Masukkan username kamu"
                    return@aksi
                }
                if (!AppPrefs.akuuAdaUsername(context, username)) {
                    errorMsg = "Username tidak ditemukan"
                    return@aksi
                }
                if (passwordBaru.length < 6) {
                    errorMsg = "Password baru minimal 6 karakter"
                    return@aksi
                }
                if (passwordBaru != konfirmPassword) {
                    errorMsg = "Password baru tidak cocok"
                    return@aksi
                }
                AppPrefs.gantiPassword(context, username, passwordBaru)
                mode = "login"
                errorMsg = ""
                password = ""
                passwordBaru = ""
                konfirmPassword = ""
                Toast.makeText(
                    context,
                    "✅ Password berhasil direset",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(WarnaBg)) {
        // ---------- Hero gradient ----------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.linearGradient(listOf(WarnaBrand, WarnaBrandAkhir)),
                    RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(76.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Sensors, contentDescription = null, tint = Color.White, modifier = Modifier.size(42.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("NetPulse", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Network & IoT Monitor", fontSize = 14.sp, color = Color.White.copy(alpha = 0.85f))
            }
        }

        // ---------- Kartu form (menimpa hero) ----------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            Spacer(modifier = Modifier.height(230.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(12.dp),
                colors = CardDefaults.cardColors(containerColor = WarnaCard)
            ) {
                Column(modifier = Modifier.animateContentSize().padding(22.dp)) {
                    Text(
                        text = when (mode) {
                            "register"      -> "Buat Akun Baru"
                            "lupa_password" -> "Reset Password"
                            else            -> "Selamat Datang"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarnaTeksUtama
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (mode) {
                            "register"      -> "Daftarkan akun baru untuk mulai memantau."
                            "lupa_password" -> "Masukkan username dan password baru."
                            else            -> "Masuk untuk melihat status jaringanmu."
                        },
                        fontSize = 13.sp,
                        color = WarnaTeksDim
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    // Username (semua mode)
                    FieldNetPulse(
                        value = username,
                        onValueChange = { username = it.trim() },
                        label = "Username",
                        placeholder = "contoh: adrian",
                        leadingIcon = Icons.Default.Person
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Password
                    if (mode != "lupa_password") {
                        FieldNetPulse(
                            value = password,
                            onValueChange = { password = it },
                            label = "Password",
                            password = true,
                            leadingIcon = Icons.Default.Lock,
                            showPassword = showPass,
                            onToggleShow = { showPass = !showPass }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Konfirmasi password (register)
                    AnimatedVisibility(visible = mode == "register") {
                        Column {
                            FieldNetPulse(
                                value = konfirmPassword,
                                onValueChange = { konfirmPassword = it },
                                label = "Konfirmasi Password",
                                password = true,
                                leadingIcon = Icons.Default.Lock
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // Password baru + konfirmasi (lupa password)
                    AnimatedVisibility(visible = mode == "lupa_password") {
                        Column {
                            FieldNetPulse(
                                value = passwordBaru,
                                onValueChange = { passwordBaru = it },
                                label = "Password Baru",
                                password = true,
                                leadingIcon = Icons.Default.Lock,
                                showPassword = showPassBaru,
                                onToggleShow = { showPassBaru = !showPassBaru }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            FieldNetPulse(
                                value = konfirmPassword,
                                onValueChange = { konfirmPassword = it },
                                label = "Konfirmasi Password Baru",
                                password = true,
                                leadingIcon = Icons.Default.Lock
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // Pesan error
                    if (errorMsg.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(WarnaMerah.copy(alpha = 0.1f))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = WarnaMerah, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(errorMsg, fontSize = 13.sp, color = WarnaMerah, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Tombol utama
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(listOf(WarnaBrand, WarnaBrandAkhir)))
                            .clickable { aksiUtama() }
                            .padding(vertical = 15.dp),
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // Link navigasi bawah
                    when (mode) {
                        "login" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Buat akun baru",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WarnaBrand,
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
                                    fontWeight = FontWeight.SemiBold,
                                    color = WarnaTeksDim,
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
                                fontWeight = FontWeight.SemiBold,
                                color = WarnaBrand,
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
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}