package com.example.myapplication

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HalamanLogin(onLoginBerhasil: () -> Unit) {
    val context = LocalContext.current

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
        Icon(
            imageVector = Icons.Default.Sensors,
            contentDescription = null,
            tint = Color(0xFF2563EB),
            modifier = Modifier.size(64.dp)
        )
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
                OutlinedTextField(
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
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Password kamu", color = WarnaTeksDim) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = if (showPass)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            Icon(
                                imageVector = if (showPass) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp).clickable { showPass = !showPass },
                                tint = WarnaTeksDim
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
                        OutlinedTextField(
                            value = konfirmPassword,
                            onValueChange = { konfirmPassword = it },
                            placeholder = { Text("Ulangi password", color = WarnaTeksDim) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // ── Password baru & konfirmasi (lupa_password) ──
                AnimatedVisibility(visible = mode == "lupa_password") {
                    Column {
                        Text("Password Baru", fontSize = 13.sp, color = WarnaTeksDim)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = passwordBaru,
                            onValueChange = { passwordBaru = it },
                            placeholder = { Text("Password baru", color = WarnaTeksDim) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            visualTransformation = if (showPassBaru)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            trailingIcon = {
                                Icon(
                                    imageVector = if (showPassBaru) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp).clickable { showPassBaru = !showPassBaru },
                                    tint = WarnaTeksDim
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Konfirmasi Password Baru", fontSize = 13.sp, color = WarnaTeksDim)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = konfirmPassword,
                            onValueChange = { konfirmPassword = it },
                            placeholder = { Text("Ulangi password baru", color = WarnaTeksDim) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
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
                                    Toast.makeText(
                                        context,
                                        "✅ Password berhasil direset",
                                        Toast.LENGTH_SHORT
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
