package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
        setContent {
            MyApplicationTheme {
                val context = androidx.compose.ui.platform.LocalContext.current
                var isLoggedIn by remember { mutableStateOf(AppPrefs.isLoggedIn(context)) }
                var fcmToken by remember { mutableStateOf("") }
                
                LaunchedEffect(Unit) {
                    RetrofitClient.BASE_URL = AppPrefs.getServerUrl(context)

                    com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                        .addOnSuccessListener { token ->
                            fcmToken = token
                            android.util.Log.d("FCM_TOKEN", "Token: $token")
                            lifecycleScope.launch(Dispatchers.IO) {
                                try {
                                    val username = AppPrefs.getActiveUsername(context)
                                    RetrofitClient.instance.registerFcmToken(
                                        FcmTokenRequest(token = token, platform = "android"),
                                        username
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                    com.google.firebase.messaging.FirebaseMessaging.getInstance()
                        .subscribeToTopic("netpulse")
                }

                if (!isLoggedIn) {
                    HalamanLogin(onLoginBerhasil = { isLoggedIn = true })
                } else {
                    var isScanning by remember { mutableStateOf(false) }
                    var devices by remember { mutableStateOf(listOf<Perangkat>()) }
                    var logAktivitas by remember { mutableStateOf(listOf<AktivitasLog>()) }
                    var selectedTab by remember { mutableIntStateOf(0) }
                    var isLoading by remember { mutableStateOf(false) }
                    var errorMsg by remember { mutableStateOf("") }
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
                            delay(30000)
                        }
                    }

                    Scaffold(
                        bottomBar = {
                            NavigationBar(
                                containerColor = WarnaCard,
                                tonalElevation = 8.dp
                            ) {
                                NavigationBarItem(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    icon = { Icon(Icons.Filled.Home, contentDescription = "Dashboard") },
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
                                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Device") },
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
                                    icon = { Icon(Icons.Filled.History, contentDescription = "History") },
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
                                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
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
                        
                        LaunchedEffect(Unit) {
                            while (true) {
                                try {
                                    val diag = withContext(Dispatchers.IO) {
                                        val username = AppPrefs.getActiveUsername(context)
                                        RetrofitClient.instance.getDiagnosis(username)
                                    }
                                    diagnosisJaringan = diag
                                } catch (e: Exception) { }
                                delay(30000)
                            }
                        }

                        when (selectedTab) {
                            0 -> HalamanDashboard(
                                devices = devices,
                                logAktivitas = logAktivitas,
                                diagnosisJaringan = diagnosisJaringan,
                                isLoading = isLoading,
                                isScanning = isScanning,
                                errorMsg = errorMsg,
                                onRefresh = { isScanning = true },
                                paddingValues = paddingValues
                            )
                            1 -> HalamanDevice(
                                devices = devices,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                paddingValues = paddingValues,
                                onFetchDevices = { fetchDevices() }
                            )
                            2 -> HalamanHistory(
                                logAktivitas = logAktivitas,
                                paddingValues = paddingValues,
                                onFetchDevices = { fetchDevices() }
                            )
                            3 -> HalamanSettings(
                                fcmToken = fcmToken,
                                paddingValues = paddingValues
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getCurrentDate(): String {
    val now = java.util.Calendar.getInstance()
    return "%02d/%02d/%04d".format(
        now.get(java.util.Calendar.DAY_OF_MONTH),
        now.get(java.util.Calendar.MONTH) + 1,
        now.get(java.util.Calendar.YEAR)
    )
}
