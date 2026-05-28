package com.example.myapplication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    val namaPengguna = AppPrefs.getNamaPengguna(context)
    val onlineCount = devices.count { it.status == "ONLINE" }
    val offlineCount = devices.count { it.status == "OFFLINE" }
    val totalCount = devices.size

    var isDeviceExpanded by remember { mutableStateOf(false) }
    var isAktivitasExpanded by remember { mutableStateOf(true) }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = onRefresh
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarnaBg)
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
    ) {
        HeaderApp()
        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Halo $namaPengguna!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WarnaTeksUtama
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.WavingHand,
                contentDescription = null,
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = "Berikut status jaringan kamu.",
            fontSize = 13.sp,
            color = WarnaTeksDim
        )

        if (errorMsg.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEDED))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = WarnaMerah, modifier = Modifier.size(16.dp))
                    Text(
                        text = errorMsg,
                        fontSize = 12.sp,
                        color = WarnaMerah
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                CardRingkasan(onlineCount.toString(), "Normal", WarnaHijau, Icons.Default.CheckCircle)
            }
            Box(modifier = Modifier.weight(1f)) {
                CardRingkasan(offlineCount.toString(), "Down", WarnaMerah, Icons.Default.Warning)
            }
            Box(modifier = Modifier.weight(1f)) {
                CardRingkasan(totalCount.toString(), "Device", WarnaTeksUtama, Icons.Default.Devices)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

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
                Spacer(modifier = Modifier.height(12.dp))
                Text("Diagnosis Jaringan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
                Spacer(modifier = Modifier.height(4.dp))
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

                Spacer(modifier = Modifier.height(12.dp))
                Text("Status Perangkat", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
                Spacer(modifier = Modifier.height(4.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF2563EB))
                    }
                } else if (devices.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
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
                    val tampilDevices = if (isDeviceExpanded) devices else devices.take(3)
                    tampilDevices.forEach { perangkat ->
                        KartuPerangkat(perangkat = perangkat)
                    }
                    if (devices.size > 3) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isDeviceExpanded = !isDeviceExpanded }
                                .padding(vertical = 4.dp),
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
                                    color = Color(0xFF2563EB)
                                )
                                Icon(
                                    imageVector = if (isDeviceExpanded) Icons.Filled.KeyboardArrowUp
                                    else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isAktivitasExpanded = !isAktivitasExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Aktivitas Terbaru", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
                    Icon(
                        imageVector = if (isAktivitasExpanded) Icons.Filled.KeyboardArrowUp
                        else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = WarnaTeksDim
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
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
                Spacer(modifier = Modifier.height(8.dp))
            }

            PullRefreshIndicator(
                refreshing = isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = Color(0xFF2563EB)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onRefresh,
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