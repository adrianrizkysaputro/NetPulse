package com.example.myapplication

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    if (selectedDevice != null) {
        val device = selectedDevice!!
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
            .background(WarnaBg)
            .padding(paddingValues)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WarnaCard)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clickable { onBack() }
                    .background(WarnaBg, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = WarnaTeksUtama,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(device.nama, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
            Spacer(modifier = Modifier.weight(1f))
            val warnaStatus = if (device.status == "ONLINE") WarnaHijau else WarnaMerah
            Box(
                modifier = Modifier
                    .background(warnaStatus.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
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
                .padding(16.dp)
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
            if (device.kategori == "IoT") {
                if (iotData.isNotEmpty()) {
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
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = WarnaCard)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = WarnaTeksDim, modifier = Modifier.size(24.dp))
                            Column {
                                Text("Menunggu Data", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
                                Text("Perangkat belum mengirim data sensor.", fontSize = 12.sp, color = WarnaTeksDim)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- DIAGNOSIS UNIVERSAL (Optimized: No Duplicates) ---
                val diagnosisMap = mutableMapOf<String, Pair<String, String>>() // Key: Kategori (air, pakan, suhu, dll)

                iotData.forEach { (key, value) ->
                    val valStr = value.toString()
                    val valNum = valStr.toDoubleOrNull()
                    val keyLower = key.lowercase()

                    when {
                        // 1. Deteksi Berdasarkan Nilai Numerik
                        valNum != null -> {
                            when {
                                (keyLower.contains("suhu") || keyLower.contains("temp")) && valNum > 35 -> {
                                    diagnosisMap["suhu_panas"] = "Suhu terlalu panas (${valNum.toInt()}°C)" to "Gunakan pendingin atau cek sirkulasi udara"
                                }
                                (keyLower.contains("suhu") || keyLower.contains("temp")) && valNum < 15 -> {
                                    diagnosisMap["suhu_dingin"] = "Suhu terlalu dingin (${valNum.toInt()}°C)" to "Cek pemanas atau isolasi ruangan"
                                }
                                (keyLower.contains("air") || keyLower.contains("water")) && valNum < 20 -> {
                                    diagnosisMap["air"] = "Air hampir habis (${valNum.toInt()}%)" to "Isi ulang Air segera"
                                }
                                (keyLower.contains("pakan") || keyLower.contains("feed")) && valNum < 20 -> {
                                    diagnosisMap["pakan"] = "Pakan hampir habis (${valNum.toInt()}%)" to "Isi ulang Pakan segera"
                                }
                                keyLower.contains("tegangan") && valNum < 10 -> {
                                    diagnosisMap["tegangan"] = "Tegangan drop (${valNum}V)" to "Cek sumber daya atau baterai"
                                }
                                keyLower.contains("gas") && valNum > 400 -> {
                                    diagnosisMap["gas"] = "Kebocoran Gas/Asap terdeteksi!" to "Buka ventilasi dan matikan sumber api"
                                }
                            }
                        }
                        // 2. Deteksi Berdasarkan Status Teks/Boolean
                        valStr.contains("Habis", true) || valStr.contains("Critical", true) || (valStr == "true" && (keyLower == "alert" || keyLower == "warning")) -> {
                            // Tentukan kategori dari nama key
                            val category = when {
                                keyLower.contains("air") || keyLower.contains("water") -> "air"
                                keyLower.contains("pakan") || keyLower.contains("feed") -> "pakan"
                                keyLower == "alert" || keyLower == "warning" -> "umum"
                                else -> "sensor_$key"
                            }

                            // Hanya tambahkan jika kategori ini belum ada diagnosisnya (prioritas numerik)
                            if (!diagnosisMap.containsKey(category)) {
                                if (category == "umum") {
                                    // Hanya muncul jika tidak ada diagnosis spesifik lain
                                    val hasSpecific = diagnosisMap.keys.any { it in listOf("air", "pakan", "suhu_panas", "suhu_dingin", "gas") }
                                    if (!hasSpecific) {
                                        diagnosisMap["umum"] = "Kondisi kritis pada perangkat" to "Cek fisik perangkat dan sensor segera"
                                    }
                                } else {
                                    val label = key.replaceFirstChar { it.uppercase() }
                                    diagnosisMap[category] = "Kondisi kritis pada: $label" to "Cek fisik perangkat dan sensor segera"
                                }
                            }
                        }
                    }
                }

                if (diagnosisMap.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(20.dp))
                                Text("Diagnosis Sistem", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            diagnosisMap.values.forEach { (problem, solusi) ->
                                Text("Problem: $problem", fontSize = 13.sp, color = WarnaMerah, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WarnaHijau, modifier = Modifier.size(14.dp))
                                    Text("Solusi: $solusi", fontSize = 13.sp, color = WarnaTeksUtama)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarnaBg)
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Device", color = WarnaTeksUtama, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .clickable { onTambah() }
                    .background(Color(0xFF2563EB), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("+ Tambah", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, WarnaBorder, RoundedCornerShape(12.dp))
                .background(WarnaCard, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.CenterStart
        ) {
            androidx.compose.foundation.text.BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = WarnaTeksUtama),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = WarnaTeksDim, modifier = Modifier.size(16.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) Text("Cari perangkat...", fontSize = 13.sp, color = WarnaTeksDim)
                            innerTextField()
                        }
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Hapus",
                                tint = WarnaTeksDim,
                                modifier = Modifier.size(16.dp).clickable { onSearchQueryChange("") }
                            )
                        }
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (devices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Devices, contentDescription = null, tint = WarnaTeksDim, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Belum ada perangkat.", fontSize = 14.sp, color = WarnaTeksDim)
                    Text("Tap '+ Tambah' untuk mulai.", fontSize = 13.sp, color = WarnaTeksDim)
                }
            }
        } else {
            val filteredDevices = if (searchQuery.isEmpty()) devices
            else devices.filter {
                it.nama.contains(searchQuery, ignoreCase = true) ||
                        it.ip.contains(searchQuery, ignoreCase = true) ||
                        it.kategori.contains(searchQuery, ignoreCase = true)
            }

            if (filteredDevices.isEmpty() && searchQuery.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = WarnaTeksDim, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tidak ada perangkat\nyang cocok dengan \"$searchQuery\"",
                            fontSize = 14.sp, color = WarnaTeksDim, textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                filteredDevices.forEach { perangkat ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(1.dp),
                        colors = CardDefaults.cardColors(containerColor = WarnaCard),
                        onClick = { onPilihDevice(perangkat) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                // Ikon lebih kecil
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            if (perangkat.kategori == "IoT") Color(0xFFF0FDF4) else Color(0xFFEFF6FF),
                                            RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (perangkat.nama.uppercase()) {
                                            "ROUTER" -> Icons.Default.Router
                                            "AP1", "AP2" -> Icons.Default.Wifi
                                            "CCTV" -> Icons.Default.CameraAlt
                                            "SENSOR" -> Icons.Default.Thermostat
                                            "SMART DOOR" -> Icons.Default.MeetingRoom
                                            "SMART LAMP" -> Icons.Default.Lightbulb
                                            "SMART PLUG" -> Icons.Default.Power
                                            "PAKAN BURUNG" -> Icons.Default.Pets
                                            else -> if (perangkat.kategori == "IoT") Icons.Default.Smartphone else Icons.Default.Wifi
                                        },
                                        contentDescription = null,
                                        tint = if (perangkat.kategori == "IoT") WarnaHijau else Color(0xFF2563EB),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = perangkat.nama,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WarnaTeksUtama,
                                            maxLines = 1
                                        )
                                        // Badge Kategori Mini di samping Nama
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = (if (perangkat.kategori == "IoT") WarnaHijau else Color(0xFF2563EB)).copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                text = perangkat.kategori,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (perangkat.kategori == "IoT") WarnaHijau else Color(0xFF2563EB),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = perangkat.ip,
                                        fontSize = 12.sp,
                                        color = WarnaTeksDim
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val warnaStatus = if (perangkat.status == "ONLINE") WarnaHijau else WarnaMerah
                                Text(
                                    text = if (perangkat.status == "ONLINE") "Normal" else "Down",
                                    fontSize = 11.sp,
                                    color = warnaStatus,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(warnaStatus.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                )

                                IconButton(
                                    onClick = { deviceToDelete = perangkat; showDialogHapus = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = WarnaTeksDim, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
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
