package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                    var detailDeviceAktif by remember { mutableStateOf(false) }

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

                        // Ikon status bar putih di semua tab (semua tab sudah pakai bar biru)
                        val view = LocalView.current
                        SideEffect {
                            val window = (view.context as? Activity)?.window
                            if (window != null) {
                                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                            }
                        }

                        // Tinggi bar biru per tab (di bawah status bar)
                        val tinggiStatusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                        val targetTinggiBar = when (selectedTab) {
                            0 -> 200.dp + tinggiStatusBar   // Dashboard
                            1 -> (if (detailDeviceAktif) 170.dp else 108.dp) + tinggiStatusBar   // Device (lebih tinggi di halaman detail)
                            2 -> 108.dp + tinggiStatusBar   // History
                            3 -> 216.dp + tinggiStatusBar   // Settings
                            else -> 0.dp
                        }
                        val tinggiBar by animateDpAsState(
                            targetValue = targetTinggiBar,
                            animationSpec = tween(450, easing = FastOutSlowInEasing),
                            label = "tinggiBar"
                        )

                        Box(modifier = Modifier.fillMaxSize().background(WarnaBg)) {
                            // Satu bar biru untuk semua tab -> tingginya berubah ("morph") saat pindah tab
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(tinggiBar)
                                    .background(
                                        Brush.linearGradient(listOf(WarnaBrand, WarnaBrandAkhir)),
                                        RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                                    )
                            )

                            // Transisi antar tab: geser tipis + fade, arah mengikuti posisi tab
                            AnimatedContent(
                                targetState = selectedTab,
                                transitionSpec = {
                                    val arah = if (targetState > initialState) 1 else -1
                                    (slideInHorizontally(tween(300)) { it / 8 * arah } + fadeIn(tween(300)))
                                        .togetherWith(
                                            slideOutHorizontally(tween(300)) { -it / 8 * arah } + fadeOut(tween(150))
                                        )
                                },
                                label = "tab"
                            ) { tab ->
                                when (tab) {
                                    0 -> HalamanDashboard(
                                        devices = devices,
                                        logAktivitas = logAktivitas,
                                        diagnosisJaringan = diagnosisJaringan,
                                        isLoading = isLoading,
                                        isScanning = isScanning,
                                        errorMsg = errorMsg,
                                        onRefresh = { isScanning = true },
                                        paddingValues = paddingValues,
                                        onTambahDevice = { selectedTab = 1 }
                                    )
                                    1 -> HalamanDevice(
                                        devices = devices,
                                        searchQuery = searchQuery,
                                        onSearchQueryChange = { searchQuery = it },
                                        paddingValues = paddingValues,
                                        onFetchDevices = { fetchDevices() },
                                        onDetailAktif = { detailDeviceAktif = it }
                                    )
                                    2 -> HalamanHistory(
                                        logAktivitas = logAktivitas,
                                        paddingValues = paddingValues,
                                        onFetchDevices = { fetchDevices() },
                                        onKeDashboard = { selectedTab = 0 }
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