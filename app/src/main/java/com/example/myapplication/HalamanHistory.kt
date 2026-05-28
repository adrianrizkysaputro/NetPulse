package com.example.myapplication

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HalamanHistory(
    logAktivitas: List<AktivitasLog>,
    paddingValues: PaddingValues,
    onFetchDevices: suspend () -> Unit
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarnaBg)
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
    ) {
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = WarnaMerah, modifier = Modifier.size(14.dp))
                    Text("Hapus Semua", fontSize = 12.sp, color = WarnaMerah, fontWeight = FontWeight.Bold)
                }
            }
        }

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
                                Icon(Icons.Default.Search, contentDescription = null, tint = WarnaTeksDim, modifier = Modifier.size(16.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (searchHistory.isEmpty()) {
                                        Text("Cari aktivitas...", fontSize = 13.sp, color = WarnaTeksDim)
                                    }
                                    innerTextField()
                                }
                                if (searchHistory.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Hapus",
                                        tint = WarnaTeksDim,
                                        modifier = Modifier.size(16.dp).clickable { searchHistory = "" }
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
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = if (filterDevice != "Semua" || filterTanggal != "Semua") Color.White else WarnaTeksUtama,
                        modifier = Modifier.size(20.dp)
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, tint = WarnaTeksDim, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Belum Ada Riwayat",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarnaTeksUtama
                    )
                    Text(
                        text = "Riwayat aktivitas jaringan kamu\nakan muncul di sini",
                        fontSize = 14.sp,
                        color = WarnaTeksDim,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF2563EB).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                            Text(
                                text = "Tekan 'Cek Semua Jaringan'\ndi tab Dashboard",
                                fontSize = 13.sp,
                                color = Color(0xFF2563EB),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else if (filteredLog.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.SearchOff, contentDescription = null, tint = WarnaTeksDim, modifier = Modifier.size(48.dp))
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
