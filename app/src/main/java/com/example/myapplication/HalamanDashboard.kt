package com.example.myapplication

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HalamanDashboard(
    devices: List<Perangkat>,
    logAktivitas: List<AktivitasLog>,
    diagnosisJaringan: List<DiagnosisResponse>,
    isLoading: Boolean,
    isScanning: Boolean,
    errorMsg: String,
    onRefresh: () -> Unit,
    paddingValues: PaddingValues,
    onTambahDevice: () -> Unit = {}   // opsional: dipakai tombol di empty state
) {
    val context = LocalContext.current
    val namaPengguna = AppPrefs.getNamaPengguna(context).replaceFirstChar { it.uppercase() }
    val onlineCount = devices.count { it.status == "ONLINE" }
    val offlineCount = devices.count { it.status == "OFFLINE" }
    val totalCount = devices.size

    var isDeviceExpanded by remember { mutableStateOf(false) }
    var isAktivitasExpanded by remember { mutableStateOf(true) }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = onRefresh
    )

    // Header gradient gelap -> ikon status bar jadi putih, balik normal saat pindah tab
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.isAppearanceLightStatusBars = false
        onDispose { }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = paddingValues.calculateBottomPadding())
    ) {
        // Teks header. Bar biru-nya digambar MainActivity di belakang (tingginya beranimasi antar tab)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(124.dp)
                .padding(top = 12.dp, start = 20.dp, end = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MonitorHeart,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "NetPulse",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Halo, $namaPengguna!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.WavingHand,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "Berikut status jaringan kamu.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
        RingkasanCard(
            online = onlineCount,
            offline = offlineCount,
            total = totalCount,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

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

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    if (errorMsg.isNotEmpty()) {
                        ErrorBanner(onCobaLagi = onRefresh, enabled = !isScanning && !isLoading)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // ---------- Diagnosis ----------
                    SectionTitle("Diagnosis Jaringan")
                    if (diagnosisJaringan.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(2.dp),
                            colors = CardDefaults.cardColors(containerColor = WarnaCard)
                        ) {
                            Text(
                                text = if (errorMsg.isNotEmpty())
                                    "Diagnosis belum tersedia karena server belum terhubung."
                                else "Mengambil data diagnosis...",
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

                    // ---------- Status perangkat ----------
                    Spacer(modifier = Modifier.height(20.dp))
                    SectionTitle("Status Perangkat")

                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = WarnaBrand)
                        }
                    } else if (devices.isEmpty()) {
                        EmptyPerangkat(onTambah = onTambahDevice)
                    } else {
                        val tampilDevices = if (isDeviceExpanded) devices else devices.take(3)
                        tampilDevices.forEach { perangkat ->
                            KartuPerangkat(perangkat = perangkat)
                        }
                        if (devices.size > 3) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isDeviceExpanded = !isDeviceExpanded }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = if (isDeviceExpanded) "Sembunyikan"
                                        else "Lihat ${devices.size - 3} perangkat lainnya",
                                        fontSize = 13.sp,
                                        color = WarnaBrand,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Icon(
                                        imageVector = if (isDeviceExpanded) Icons.Filled.KeyboardArrowUp
                                        else Icons.Filled.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = WarnaBrand,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ---------- Aktivitas terbaru ----------
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isAktivitasExpanded = !isAktivitasExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionTitle("Aktivitas Terbaru", bottomSpace = 0)
                        Icon(
                            imageVector = if (isAktivitasExpanded) Icons.Filled.KeyboardArrowUp
                            else Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
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
                                    logAktivitas.take(3).forEachIndexed { index, log ->
                                        AktivitasItem(log = log)
                                        if (index < minOf(3, logAktivitas.size) - 1) {
                                            HorizontalDivider(color = WarnaBorder)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            PullRefreshIndicator(
                refreshing = isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = Color.White,
                contentColor = WarnaBrand
            )
        }

        Button(
            onClick = onRefresh,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isScanning && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = WarnaBrand,
                contentColor = Color.White
            )
        ) {
            Text(
                if (isScanning || isLoading) "Mengecek..." else "Cek Semua Jaringan",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ======================= Komponen khusus Dashboard =======================

@Composable
private fun SectionTitle(teks: String, bottomSpace: Int = 8) {
    Text(
        text = teks,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = WarnaTeksUtama
    )
    if (bottomSpace > 0) Spacer(modifier = Modifier.height(bottomSpace.dp))
}

@Composable
private fun RingkasanCard(online: Int, offline: Int, total: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = WarnaCard)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RingkasanItem(online.toString(), "Normal", WarnaHijau, Icons.Default.CheckCircle, Modifier.weight(1f))
            RingkasanItem(offline.toString(), "Down", WarnaMerah, Icons.Default.Warning, Modifier.weight(1f))
            RingkasanItem(total.toString(), "Perangkat", WarnaBrand, Icons.Default.Devices, Modifier.weight(1f))
        }
    }
}

@Composable
private fun RingkasanItem(
    angka: String,
    label: String,
    warna: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(42.dp).clip(CircleShape).background(warna.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = warna, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(angka, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
        Text(label, fontSize = 12.sp, color = WarnaTeksDim, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ErrorBanner(onCobaLagi: () -> Unit, enabled: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEDED))
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(WarnaMerah.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CloudOff, contentDescription = null, tint = WarnaMerah, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Server tidak bisa dihubungi", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WarnaMerah)
                Text("Cek koneksi atau URL server di Settings.", fontSize = 12.sp, color = WarnaTeksDim)
            }
            TextButton(onClick = onCobaLagi, enabled = enabled) {
                Text("Coba lagi", color = WarnaMerah, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun EmptyPerangkat(onTambah: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape).background(WarnaBrand.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Devices, contentDescription = null, tint = WarnaBrand, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("Belum ada perangkat", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
        Text(
            "Tambahkan perangkat pertamamu untuk mulai memantau.",
            fontSize = 13.sp,
            color = WarnaTeksDim,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        FilledTonalButton(onClick = onTambah, shape = RoundedCornerShape(14.dp)) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Tambah perangkat")
        }
    }
}