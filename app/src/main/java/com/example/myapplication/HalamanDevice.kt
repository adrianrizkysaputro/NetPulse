package com.example.myapplication

import androidx.compose.foundation.BorderStroke
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HalamanDevice(
    devices: List<Perangkat>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    paddingValues: PaddingValues,
    onFetchDevices: suspend () -> Unit
) {
    val context = LocalContext.current
    var showDialogTambah by remember { mutableStateOf(false) }
    var selectedDevice by remember { mutableStateOf<Perangkat?>(null) }
    var iotData by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }

    LaunchedEffect(selectedDevice) {
        if (selectedDevice?.kategori == "IoT") {
            while (true) {
                try {
                    val data = withContext(Dispatchers.IO) {
                        RetrofitClient.instance.getIotStatus(selectedDevice?.ip ?: "")
                    }
                    iotData = data
                } catch (e: Exception) { }
                kotlinx.coroutines.delay(5000)
            }
        }
    }

    if (showDialogTambah) {
        DialogTambahPerangkat(
            onDismiss = { showDialogTambah = false },
            onTambah = { perangkat ->
                GlobalScope.launch {
                    try {
                        val username = AppPrefs.getActiveUsername(context)
                        RetrofitClient.instance.tambahDevice(
                            TambahRequest(nama = perangkat.nama, ip = perangkat.ip, kategori = perangkat.kategori),
                            username
                        )
                        onFetchDevices()
                        withContext(Dispatchers.Main) {
                            showDialogTambah = false
                            Toast.makeText(context, "✅ ${perangkat.nama} berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "❌ Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            currentSize = devices.size
        )
    }

    AnimatedContent(
        targetState = selectedDevice,
        transitionSpec = {
            if (targetState != null) {
                (slideInHorizontally(tween(300)) { it / 4 } + fadeIn(tween(300)))
                    .togetherWith(slideOutHorizontally(tween(300)) { -it / 4 } + fadeOut(tween(150)))
            } else {
                (slideInHorizontally(tween(300)) { -it / 4 } + fadeIn(tween(300)))
                    .togetherWith(slideOutHorizontally(tween(300)) { it / 4 } + fadeOut(tween(150)))
            }
        },
        label = "deviceDetail"
    ) { device ->
        if (device != null) {
            HalamanDetailDevice(
                device = device,
                iotData = iotData,
                paddingValues = paddingValues,
                onBack = { selectedDevice = null },
                onFetchDevices = onFetchDevices
            )
        } else {
            HalamanListDevice(
                devices = devices,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                paddingValues = paddingValues,
                onTambah = { showDialogTambah = true },
                onPilihDevice = { selectedDevice = it },
                onHapusDevice = { perangkat ->
                    GlobalScope.launch {
                        try {
                            val username = AppPrefs.getActiveUsername(context)
                            RetrofitClient.instance.hapusDevice(perangkat.id, username)
                            onFetchDevices()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "✅ ${perangkat.nama} berhasil dihapus", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "❌ Gagal hapus device", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun HalamanDetailDevice(
    device: Perangkat,
    iotData: Map<String, Any>,
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    onFetchDevices: suspend () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = paddingValues.calculateBottomPadding())
    ) {
        // Header (di atas bar biru yang digambar MainActivity)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clickable { onBack() }
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(device.nama, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.weight(1f))
            val warnaStatus = if (device.status == "ONLINE") WarnaHijau else WarnaMerah
            Box(
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (device.status == "ONLINE") "Normal" else "Down",
                    fontSize = 12.sp,
                    color = warnaStatus,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp)
        ) {
            // Info dasar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = WarnaCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Informasi Perangkat", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = WarnaBorder)
                    Spacer(modifier = Modifier.height(6.dp))
                    listOf(
                        "IP" to device.ip,
                        "Kategori" to device.kategori,
                        "Latency" to if (device.latency == 0) "TIMEOUT" else "${device.latency}ms",
                        "Status" to device.status
                    ).forEach { (label, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label, fontSize = 13.sp, color = WarnaTeksDim)
                            Text(
                                text = value,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    label == "Latency" && device.latency == 0 -> WarnaMerah
                                    label == "Latency" && device.latency > 100 -> WarnaKuning
                                    label == "Latency" -> WarnaHijau
                                    label == "Status" && value == "ONLINE" -> WarnaHijau
                                    label == "Status" -> WarnaMerah
                                    else -> WarnaTeksUtama
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // IoT data sensor
            if (device.kategori == "IoT" && iotData.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = WarnaCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.BarChart, contentDescription = null, tint = WarnaTeksUtama, modifier = Modifier.size(18.dp))
                            Text("Data Sensor", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = WarnaBorder)
                        Spacer(modifier = Modifier.height(6.dp))
                        val skipKeys = listOf("device", "ip")
                        iotData.forEach { (key, value) ->
                            if (key !in skipKeys) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(key.replaceFirstChar { it.uppercase() }, fontSize = 13.sp, color = WarnaTeksDim)
                                    Text(
                                        text = value.toString(),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            value.toString().contains("Habis") -> WarnaMerah
                                            value.toString().contains("Penuh") -> WarnaHijau
                                            value.toString().contains("Sedikit") -> WarnaKuning
                                            value.toString() == "true" -> WarnaHijau
                                            value.toString() == "false" -> WarnaTeksDim
                                            else -> WarnaTeksUtama
                                        }
                                    )
                                }
                                HorizontalDivider(color = WarnaBorder)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Diagnosis IoT
                val persentasePakan = iotData["persentasePakan"]?.toString()?.toDoubleOrNull() ?: 0.0
                val persentaseAir = iotData["persentaseAir"]?.toString()?.toDoubleOrNull() ?: 0.0
                if (persentasePakan < 20 || persentaseAir < 20) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(20.dp))
                                Text("Gangguan Terdeteksi", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (persentasePakan < 20) {
                                Text("Problem: Pakan hampir habis (${persentasePakan.toInt()}%)", fontSize = 13.sp, color = WarnaMerah)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Solusi:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WarnaHijau, modifier = Modifier.size(14.dp))
                                    Text("Isi ulang pakan segera", fontSize = 13.sp, color = WarnaTeksUtama)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WarnaHijau, modifier = Modifier.size(14.dp))
                                    Text("Cek sensor ultrasonik", fontSize = 13.sp, color = WarnaTeksUtama)
                                }
                            }
                            if (persentaseAir < 20) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Problem: Air hampir habis (${persentaseAir.toInt()}%)", fontSize = 13.sp, color = WarnaMerah)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Solusi:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WarnaHijau, modifier = Modifier.size(14.dp))
                                    Text("Isi ulang air segera", fontSize = 13.sp, color = WarnaTeksUtama)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WarnaHijau, modifier = Modifier.size(14.dp))
                                    Text("Cek sensor water level", fontSize = 13.sp, color = WarnaTeksUtama)
                                }
                            }
                        }
                    }
                }
            }

            // Template ESP
            if (device.kategori == "IoT") {
                Spacer(modifier = Modifier.height(12.dp))
                SectionTemplateESP(
                    namaDevice = device.nama,
                    serverUrl = AppPrefs.getServerUrl(context)
                )
            }

            // Jaringan
            if (device.kategori == "Jaringan") {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = WarnaCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.SignalCellularAlt, contentDescription = null, tint = WarnaTeksUtama, modifier = Modifier.size(18.dp))
                            Text("Kondisi Jaringan", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = WarnaBorder)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Status", fontSize = 13.sp, color = WarnaTeksDim)
                            Text(
                                text = if (device.status == "ONLINE") "Normal" else "Down",
                                fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                color = if (device.status == "ONLINE") WarnaHijau else WarnaMerah
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Latency", fontSize = 13.sp, color = WarnaTeksDim)
                            Text(
                                text = if (device.latency == 0) "TIMEOUT" else "${device.latency}ms",
                                fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                color = when {
                                    device.latency == 0 -> WarnaMerah
                                    device.latency > 200 -> WarnaMerah
                                    device.latency > 100 -> WarnaKuning
                                    else -> WarnaHijau
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Kualitas", fontSize = 13.sp, color = WarnaTeksDim)
                            Text(
                                text = when {
                                    device.status == "OFFLINE" -> "Tidak Terjangkau"
                                    device.latency > 200 -> "Buruk"
                                    device.latency > 100 -> "Kurang Baik"
                                    device.latency > 50 -> "Cukup"
                                    else -> "Baik"
                                },
                                fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                color = when {
                                    device.status == "OFFLINE" -> WarnaMerah
                                    device.latency > 200 -> WarnaMerah
                                    device.latency > 100 -> WarnaKuning
                                    device.latency > 50 -> WarnaKuning
                                    else -> WarnaHijau
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                val diagnosis = diagnosisPerangkat(device)
                if (diagnosis != null) {
                    CardDiagnosis(diagnosis = diagnosis)
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDFFF3))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WarnaHijau, modifier = Modifier.size(24.dp))
                            Column {
                                Text("Tidak Ada Gangguan", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WarnaHijau)
                                Text("Perangkat berjalan normal", fontSize = 12.sp, color = WarnaTeksDim)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun HalamanListDevice(
    devices: List<Perangkat>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    paddingValues: PaddingValues,
    onTambah: () -> Unit,
    onPilihDevice: (Perangkat) -> Unit,
    onHapusDevice: (Perangkat) -> Unit
) {
    var showDialogHapus by remember { mutableStateOf(false) }
    var deviceToDelete by remember { mutableStateOf<Perangkat?>(null) }

    if (showDialogHapus && deviceToDelete != null) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showDialogHapus = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WarnaCard),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Hapus Perangkat", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Apakah kamu yakin ingin menghapus ${deviceToDelete!!.nama}?", fontSize = 14.sp, color = WarnaTeksDim)
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showDialogHapus = false }
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
                                    onHapusDevice(deviceToDelete!!)
                                    showDialogHapus = false
                                }
                                .background(WarnaMerah, RoundedCornerShape(12.dp))
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Hapus", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    StatusBarIkonPutih()

    val onlineCount = devices.count { it.status == "ONLINE" }
    val downCount = devices.size - onlineCount
    val adaSearch = devices.isNotEmpty()
    var filterKategori by remember { mutableStateOf("Semua") }

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
                Text("Perangkat", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = if (devices.isEmpty()) "Belum ada perangkat"
                    else "${devices.size} perangkat • $onlineCount normal • $downCount down",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
            }
            Row(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .clickable { onTambah() }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = WarnaBrand, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tambah", color = WarnaBrand, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (adaSearch) {
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = WarnaCard)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = WarnaTeksUtama),
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = WarnaTeksDim, modifier = Modifier.size(18.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) Text("Cari perangkat...", fontSize = 14.sp, color = WarnaTeksDim)
                                    innerTextField()
                                }
                                if (searchQuery.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Hapus",
                                        tint = WarnaTeksDim,
                                        modifier = Modifier.size(18.dp).clickable { onSearchQueryChange("") }
                                    )
                                }
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (devices.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(88.dp).clip(CircleShape).background(WarnaBrand.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Devices, contentDescription = null, tint = WarnaBrand, modifier = Modifier.size(44.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Belum ada perangkat", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Tambahkan router, access point, atau perangkat IoT untuk mulai dipantau.",
                    fontSize = 13.sp, color = WarnaTeksDim, textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onTambah,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarnaBrand)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambah perangkat pertama", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        } else {
            // ---------- Filter chip ----------
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Semua", "Jaringan", "IoT", "Down").forEach { label ->
                    ChipFilter(label = label, selected = filterKategori == label) { filterKategori = label }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            val filteredDevices = devices.filter { d ->
                val cocokFilter = when (filterKategori) {
                    "Semua" -> true
                    "Down" -> d.status != "ONLINE"
                    else -> d.kategori == filterKategori
                }
                val cocokCari = searchQuery.isEmpty() ||
                        d.nama.contains(searchQuery, ignoreCase = true) ||
                        d.ip.contains(searchQuery, ignoreCase = true) ||
                        d.kategori.contains(searchQuery, ignoreCase = true)
                cocokFilter && cocokCari
            }

            if (filteredDevices.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = WarnaTeksDim, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Tidak ada perangkat\nyang cocok dengan \"$searchQuery\""
                            else "Tidak ada perangkat\ndi kategori ini",
                            fontSize = 14.sp, color = WarnaTeksDim, textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                filteredDevices.forEach { perangkat ->
                    KartuDeviceList(
                        perangkat = perangkat,
                        onClick = { onPilihDevice(perangkat) },
                        onHapus = { deviceToDelete = perangkat; showDialogHapus = true }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

// ======================= Komponen khusus tab Device =======================

@Composable
private fun ChipFilter(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (selected) Color.White else WarnaTeksDim,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) WarnaBrand else WarnaCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

private fun ikonPerangkat(perangkat: Perangkat): ImageVector =
    when (perangkat.nama.uppercase()) {
        "ROUTER" -> Icons.Default.Router
        "AP1", "AP2" -> Icons.Default.Wifi
        "CCTV" -> Icons.Default.CameraAlt
        "SENSOR" -> Icons.Default.Thermostat
        "SMART DOOR" -> Icons.Default.MeetingRoom
        "SMART LAMP" -> Icons.Default.Lightbulb
        "SMART PLUG" -> Icons.Default.Power
        "PAKAN BURUNG" -> Icons.Default.Pets
        else -> if (perangkat.kategori == "IoT") Icons.Default.Smartphone else Icons.Default.Wifi
    }

@Composable
private fun KartuDeviceList(
    perangkat: Perangkat,
    onClick: () -> Unit,
    onHapus: () -> Unit
) {
    val isOnline = perangkat.status == "ONLINE"
    val warnaStatus = if (isOnline) WarnaHijau else WarnaMerah
    val isIot = perangkat.kategori == "IoT"
    val warnaKategori = if (isIot) WarnaHijau else WarnaBrand
    val warnaLatency = when {
        perangkat.latency == 0 -> WarnaMerah
        perangkat.latency > 100 -> WarnaKuning
        else -> WarnaHijau
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = WarnaCard),
        border = if (isOnline) null else BorderStroke(1.dp, WarnaMerah.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(warnaKategori.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(ikonPerangkat(perangkat), contentDescription = null, tint = warnaKategori, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = perangkat.nama,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarnaTeksUtama,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = perangkat.ip, fontSize = 12.sp, color = WarnaTeksDim)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = perangkat.kategori,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = warnaKategori,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(warnaKategori.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(warnaLatency))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (perangkat.latency == 0) "Timeout" else "${perangkat.latency} ms",
                        fontSize = 11.sp,
                        color = warnaLatency,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isOnline) "Normal" else "Down",
                    fontSize = 11.sp,
                    color = warnaStatus,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(warnaStatus.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                IconButton(onClick = onHapus, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = WarnaTeksDim.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun DialogTambahPerangkat(
    onDismiss: () -> Unit,
    onTambah: (Perangkat) -> Unit,
    currentSize: Int
) {
    var nama by remember { mutableStateOf("") }
    var ip by remember { mutableStateOf("") }
    var kategori by remember { mutableStateOf("Jaringan") }
    val daftarKategori = listOf("Jaringan", "IoT")

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WarnaCard),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Tambah Perangkat",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarnaTeksUtama
                )

                Column {
                    Text("Nama Perangkat", fontSize = 13.sp, color = WarnaTeksDim)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = nama,
                        onValueChange = { nama = it },
                        placeholder = { Text("contoh: ROUTER, CCTV", fontSize = 13.sp, color = WarnaTeksDim) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Column {
                    Text("IP Address / Hostname", fontSize = 13.sp, color = WarnaTeksDim)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = ip,
                        onValueChange = { ip = it },
                        placeholder = { Text("contoh: 192.168.1.1", fontSize = 13.sp, color = WarnaTeksDim) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Column {
                    Text("Kategori", fontSize = 13.sp, color = WarnaTeksDim)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        daftarKategori.forEach { kat ->
                            val dipilih = kategori == kat
                            Box(
                                modifier = Modifier
                                    .clickable { kategori = kat }
                                    .background(
                                        if (dipilih) Color(0xFF2563EB) else WarnaBg,
                                        RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = kat,
                                    fontSize = 12.sp,
                                    color = if (dipilih) Color.White else WarnaTeksDim,
                                    fontWeight = if (dipilih) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onDismiss() }
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
                                if (nama.isNotBlank() && ip.isNotBlank()) {
                                    onTambah(
                                        Perangkat(
                                            id = 0,
                                            nama = nama.uppercase(),
                                            ip = ip,
                                            kategori = kategori,
                                            status = "OFFLINE"
                                        )
                                    )
                                }
                            }
                            .background(
                                if (nama.isNotBlank() && ip.isNotBlank()) Color(0xFF2563EB) else Color.Gray,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tambah", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}