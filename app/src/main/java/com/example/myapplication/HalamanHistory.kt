package com.example.myapplication

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HalamanHistory(
    logAktivitas: List<AktivitasLog>,
    paddingValues: PaddingValues,
    onFetchDevices: suspend () -> Unit,
    onKeDashboard: () -> Unit = {}   // opsional: tombol di empty state
) {
    val context = LocalContext.current
    var filterDevice by remember { mutableStateOf("Semua") }
    var filterTanggal by remember { mutableStateOf("Semua") }
    var showFilterDevice by remember { mutableStateOf(false) }
    var showFilterTanggal by remember { mutableStateOf(false) }
    var showDialogHapusHistory by remember { mutableStateOf(false) }
    var searchHistory by remember { mutableStateOf("") }
    var showFilterDropdown by remember { mutableStateOf(false) }

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
        DialogNetPulse(
            onDismiss = { showDialogHapusHistory = false },
            icon = Icons.Default.DeleteSweep,
            judul = "Hapus Semua History?",
            deskripsi = "Semua history aktivitas akan dihapus permanen.",
            warna = WarnaMerah,
            teksAksi = "Hapus Semua",
            onAksi = {
                @Suppress("OPT_IN_USAGE")
                GlobalScope.launch {
                    try {
                        val username = AppPrefs.getActiveUsername(context)
                        RetrofitClient.instance.hapusSemuaLog(username)
                        onFetchDevices()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "✅ History berhasil dihapus", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "❌ Gagal hapus history", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                showDialogHapusHistory = false
            }
        )
    }

    val filterAktif = filterDevice != "Semua" || filterTanggal != "Semua"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = paddingValues.calculateBottomPadding())
    ) {
        // Teks header. Bar biru-nya digambar MainActivity di belakang (tingginya beranimasi antar tab)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(84.dp)
                .padding(top = 12.dp, start = 20.dp, end = 20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("History", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = when {
                        logAktivitas.isEmpty() -> "Belum ada aktivitas"
                        filterAktif || searchHistory.isNotEmpty() -> "${filteredLog.size} dari ${logAktivitas.size} aktivitas"
                        else -> "${logAktivitas.size} aktivitas tercatat"
                    },
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
            }
            if (logAktivitas.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable { showDialogHapusHistory = true }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hapus Semua", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Search + tombol filter (menimpa tepi bar biru)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .zIndex(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(containerColor = WarnaCard)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = searchHistory,
                            onValueChange = { searchHistory = it },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = WarnaTeksUtama),
                            decorationBox = { innerTextField: @Composable () -> Unit ->
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = WarnaTeksDim, modifier = Modifier.size(18.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (searchHistory.isEmpty()) {
                                            Text("Cari aktivitas...", fontSize = 14.sp, color = WarnaTeksDim)
                                        }
                                        innerTextField()
                                    }
                                    if (searchHistory.isNotEmpty()) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Hapus",
                                            tint = WarnaTeksDim,
                                            modifier = Modifier.size(18.dp).clickable { searchHistory = "" }
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                // Tombol filter
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(6.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (filterAktif) WarnaBrand else WarnaCard)
                        .clickable { showFilterDropdown = !showFilterDropdown },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = if (filterAktif) Color.White else WarnaTeksUtama,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Dropdown filter
            if (showFilterDropdown) {
                androidx.compose.ui.window.Popup(
                    alignment = Alignment.TopEnd,
                    offset = androidx.compose.ui.unit.IntOffset(
                        0,
                        with(androidx.compose.ui.platform.LocalDensity.current) { 56.dp.roundToPx() }
                    )
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
                                                if (dipilih) WarnaBrand else WarnaBg,
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
                                                if (dipilih) WarnaBrand else WarnaBg,
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
        if (filterAktif) {
            Text(
                text = "Menampilkan ${filteredLog.size} aktivitas",
                fontSize = 12.sp,
                color = WarnaTeksDim,
                modifier = Modifier.padding(start = 20.dp, bottom = 4.dp)
            )
        }

        if (logAktivitas.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Box(
                        modifier = Modifier.size(88.dp).clip(CircleShape).background(WarnaBrand.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = WarnaBrand, modifier = Modifier.size(44.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Belum Ada Riwayat", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Riwayat aktivitas jaringan kamu\nakan muncul di sini.",
                        fontSize = 13.sp,
                        color = WarnaTeksDim,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onKeDashboard,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WarnaBrand)
                    ) {
                        Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ke Dashboard untuk cek jaringan", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        } else if (filteredLog.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.SearchOff, contentDescription = null, tint = WarnaTeksDim, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tidak ada history\nsesuai filter yang dipilih",
                        fontSize = 14.sp,
                        color = WarnaTeksDim,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(60.dp))
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
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
                        color = WarnaBrand,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 6.dp)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
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
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}