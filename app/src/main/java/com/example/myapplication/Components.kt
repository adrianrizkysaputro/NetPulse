package com.example.myapplication

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

val WarnaHijau      = Color(0xFF22C55E)
val WarnaMerah      = Color(0xFFEF4444)
val WarnaBg         = Color(0xFFF1F6FB)
val WarnaTeksDim    = Color(0xFF64748B)
val WarnaTeksUtama  = Color(0xFF1E293B)
val WarnaKuning     = Color(0xFFF59E0B)
val WarnaCard       = Color(0xFFFFFFFF)
val WarnaBorder     = Color(0xFFE2E8F0)
val WarnaBrand      = Color(0xFF2563EB)
val WarnaBrandAkhir = Color(0xFF06B6D4)

data class Perangkat(
    val id: Int = 0,
    val nama: String,
    val ip: String,
    val port: String = "80",
    val protokol: String = "HTTP",
    val username: String = "",
    val password: String = "",
    val topic: String = "",
    val kategori: String = "Jaringan",
    val status: String,
    var latency: Int = 0
)

data class AktivitasLog(
    val perangkat: String,
    val pesan: String,
    val waktu: String,
    val tanggal: String,
    val isOnline: Boolean
)

@Composable
fun InfoText(teks: String) {
    Text(
        text = teks,
        fontSize = 14.sp,
        color = WarnaTeksDim,
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth()
    )
}

/** Dialog seragam NetPulse: ikon di lingkaran, judul, deskripsi, isi bebas, tombol Batal + aksi. */
@Composable
fun DialogNetPulse(
    onDismiss: () -> Unit,
    icon: ImageVector,
    judul: String,
    deskripsi: String? = null,
    warna: Color = WarnaBrand,
    teksAksi: String? = null,          // null = tanpa tombol aksi (mis. dialog pilihan)
    warnaAksi: Color = warna,
    aksiAktif: Boolean = true,
    onAksi: () -> Unit = {},
    teksBatal: String = "Batal",
    content: @Composable ColumnScope.() -> Unit = {}
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = WarnaCard),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(warna.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = warna, modifier = Modifier.size(30.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(judul, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama, textAlign = TextAlign.Center)
                if (deskripsi != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(deskripsi, fontSize = 13.sp, color = WarnaTeksDim, textAlign = TextAlign.Center)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.fillMaxWidth(), content = content)
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(WarnaBg)
                            .clickable { onDismiss() }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(teksBatal, fontSize = 14.sp, color = WarnaTeksDim, fontWeight = FontWeight.Bold)
                    }
                    if (teksAksi != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (aksiAktif) warnaAksi else warnaAksi.copy(alpha = 0.35f))
                                .clickable(enabled = aksiAktif) { onAksi() }
                                .padding(vertical = 13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(teksAksi, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/** Kolom input seragam NetPulse (sudut 14dp, fokus biru). */
@Composable
fun FieldNetPulse(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    password: Boolean = false,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    showPassword: Boolean = false,                 // dipakai bersama onToggleShow untuk ikon mata
    onToggleShow: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        placeholder = if (placeholder != null) {
            { Text(placeholder, fontSize = 13.sp, color = WarnaTeksDim) }
        } else null,
        leadingIcon = if (leadingIcon != null) {
            { Icon(leadingIcon, contentDescription = null, tint = WarnaTeksDim, modifier = Modifier.size(20.dp)) }
        } else null,
        trailingIcon = if (password && onToggleShow != null) {
            {
                Icon(
                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = WarnaTeksDim,
                    modifier = Modifier.size(20.dp).clickable { onToggleShow() }
                )
            }
        } else null,
        visualTransformation = if (password && !showPassword) androidx.compose.ui.text.input.PasswordVisualTransformation()
        else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = WarnaBrand,
            unfocusedBorderColor = WarnaBorder,
            focusedLabelColor = WarnaBrand
        )
    )
}

/** Header gradient reusable untuk semua tab. Isi (judul, sapaan, dll) dikirim lewat content. */
@Composable
fun GradientHeader(
    modifier: Modifier = Modifier,
    bottomPadding: androidx.compose.ui.unit.Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                androidx.compose.ui.graphics.Brush.linearGradient(listOf(WarnaBrand, WarnaBrandAkhir)),
                RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = bottomPadding),
        content = content
    )
}

@Composable
fun HeaderApp() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "NetPulse",
            color = WarnaTeksUtama,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CardRingkasan(
    angka: String,
    label: String,
    warna: Color,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = WarnaCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(warna.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = warna,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = angka,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = warna,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = WarnaTeksDim,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AktivitasItem(log: AktivitasLog) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (log.isOnline) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (log.isOnline) WarnaHijau else WarnaMerah,
                modifier = Modifier.size(16.dp)
            )
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = WarnaTeksUtama)) {
                        append(log.perangkat + " ")
                    }
                    withStyle(SpanStyle(color = WarnaTeksDim)) {
                        append(log.pesan)
                    }
                },
                fontSize = 13.sp
            )
        }
        Text(text = log.waktu, fontSize = 12.sp, color = WarnaTeksDim)
    }
}

@Composable
fun KartuPerangkat(perangkat: Perangkat) {
    var isExpanded by remember { mutableStateOf(false) }
    val warnaStatus = if (perangkat.status == "ONLINE") WarnaHijau else WarnaMerah

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = WarnaCard),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Ikon Kompak
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
                            tint = if (perangkat.kategori == "IoT") Color(0xFF22C55E) else Color(0xFF2563EB),
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
                            // Badge Kategori Mini
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

                val warnaStatusTxt = if (perangkat.status == "ONLINE") WarnaHijau else WarnaMerah
                Text(
                    text = if (perangkat.status == "ONLINE") "Normal" else "Down",
                    fontSize = 11.sp,
                    color = warnaStatusTxt,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(warnaStatusTxt.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = WarnaBorder)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "IP", fontSize = 13.sp, color = WarnaTeksDim)
                        Text(text = perangkat.ip, fontSize = 13.sp, color = WarnaTeksUtama, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Latency", fontSize = 13.sp, color = WarnaTeksDim)
                        Text(
                            text = if (perangkat.latency == 0) "TIMEOUT" else "${perangkat.latency}ms",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                perangkat.latency == 0 -> WarnaMerah
                                perangkat.latency > 100 -> WarnaKuning
                                else -> WarnaHijau
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Status", fontSize = 13.sp, color = WarnaTeksDim)
                        Text(text = perangkat.status, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = warnaStatus)
                    }
                }
            }
        }
    }
}

@Composable
fun CardDiagnosisJaringan(diagnosis: DiagnosisResponse) {
    val warnaLevel = when (diagnosis.level) {
        "KRITIS" -> Color(0xFFEF4444)
        "TINGGI" -> Color(0xFFF97316)
        "SEDANG" -> Color(0xFFF59E0B)
        else     -> Color(0xFF22C55E)
    }
    val warnaBg = when (diagnosis.level) {
        "KRITIS" -> Color(0xFFFFEDED)
        "TINGGI" -> Color(0xFFFFF3ED)
        "SEDANG" -> Color(0xFFFFFBED)
        else     -> Color(0xFFEDFFF3)
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = warnaBg)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = when (diagnosis.icon) {
                        "✅", "🟢" -> Icons.Default.CheckCircle
                        "⚠️", "🟡" -> Icons.Default.Warning
                        "❌", "🔴" -> Icons.Default.Error
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = warnaLevel,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(text = diagnosis.level, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = warnaLevel)
                    Text(text = diagnosis.judul, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
                }
            }
            if (diagnosis.info != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = diagnosis.info, fontSize = 13.sp, color = WarnaTeksDim)
            }
            if (diagnosis.kemungkinan.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = WarnaBorder)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Kemungkinan penyebab:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
                diagnosis.kemungkinan.forEach { Text(text = "• $it", fontSize = 13.sp, color = WarnaTeksDim) }
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = WarnaBorder)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Solusi:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
                diagnosis.solusi.forEach {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "✅", fontSize = 12.sp)
                        Text(text = it, fontSize = 13.sp, color = WarnaTeksUtama)
                    }
                }
            }
        }
    }
}

data class DiagnosisHasil(
    val problem: String,
    val kemungkinan: List<String>,
    val solusi: List<String>
)

fun diagnosisPerangkat(perangkat: Perangkat): DiagnosisHasil? {
    return when {
        perangkat.status == "OFFLINE" -> DiagnosisHasil(
            problem = "Perangkat tidak merespon",
            kemungkinan = listOf(
                "Perangkat mati atau dimatikan",
                "Kabel LAN tercabut",
                "IP address berubah",
                "Firewall memblokir koneksi"
            ),
            solusi = listOf(
                "Cek koneksi kabel LAN",
                "Pastikan perangkat menyala",
                "Verifikasi IP di admin panel",
                "Cek pengaturan firewall"
            )
        )
        perangkat.latency > 100 -> DiagnosisHasil(
            problem = "Latency tinggi (${perangkat.latency}ms)",
            kemungkinan = listOf(
                "Jaringan padat atau overload",
                "Sinyal WiFi lemah",
                "Interferensi sinyal dari perangkat lain"
            ),
            solusi = listOf(
                "Restart router atau AP",
                "Pindah ke channel WiFi yang lebih sepi",
                "Kurangi jumlah device yang terkoneksi"
            )
        )
        perangkat.latency in 50..100 -> DiagnosisHasil(
            problem = "Latency tidak stabil (${perangkat.latency}ms)",
            kemungkinan = listOf(
                "Koneksi mulai terganggu",
                "Sinyal WiFi kurang optimal"
            ),
            solusi = listOf(
                "Pantau latency secara berkala",
                "Pertimbangkan restart AP jika memburuk"
            )
        )
        else -> null
    }
}

@Composable
fun CardDiagnosis(diagnosis: DiagnosisHasil) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF7ED)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFB45309),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Gangguan Terdeteksi",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB45309)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Problem:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = WarnaTeksUtama
            )
            Text(
                text = diagnosis.problem,
                fontSize = 13.sp,
                color = WarnaMerah
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Kemungkinan penyebab:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = WarnaTeksUtama
            )
            diagnosis.kemungkinan.forEach {
                Text(
                    text = "• $it",
                    fontSize = 13.sp,
                    color = WarnaTeksDim
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Solusi yang disarankan:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = WarnaTeksUtama
            )
            diagnosis.solusi.forEach {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WarnaHijau, modifier = Modifier.size(12.dp))
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        color = WarnaTeksUtama
                    )
                }
            }
        }
    }
}

@Composable
fun CardIotDetail(iotData: Map<String, Any>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = WarnaCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Pets, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                Text(text = "Detail Pakan Burung", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = WarnaBorder)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = WarnaTeksDim, modifier = Modifier.size(16.dp))
                    Text(text = "Level Pakan", fontSize = 14.sp, color = WarnaTeksDim)
                }
                val pakan = iotData["persentasePakan"]?.toString()?.toDoubleOrNull() ?: 0.0
                Text(
                    text = "${pakan.toInt()}%",
                    fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = when { pakan >= 70 -> WarnaHijau; pakan >= 40 -> WarnaKuning; else -> WarnaMerah }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Status: ${iotData["statusPakan"] ?: "-"}", fontSize = 13.sp, color = WarnaTeksDim)
            Text(text = "Jarak: ${iotData["jarakPakan"]?.toString()?.toDoubleOrNull()?.toInt() ?: 0} cm", fontSize = 13.sp, color = WarnaTeksDim)
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = WarnaBorder)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.WaterDrop, contentDescription = null, tint = WarnaTeksDim, modifier = Modifier.size(16.dp))
                    Text(text = "Level Air", fontSize = 14.sp, color = WarnaTeksDim)
                }
                val air = iotData["persentaseAir"]?.toString()?.toDoubleOrNull() ?: 0.0
                Text(
                    text = "${air.toInt()}%",
                    fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = when { air >= 70 -> WarnaHijau; air >= 40 -> WarnaKuning; else -> WarnaMerah }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Status: ${iotData["statusAir"] ?: "-"}", fontSize = 13.sp, color = WarnaTeksDim)
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = WarnaBorder)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = WarnaTeksDim, modifier = Modifier.size(16.dp))
                    Text(text = "Aktuator", fontSize = 14.sp, color = WarnaTeksDim)
                }
                val servoOpen = iotData["servoOpen"]?.toString() == "true"
                Box(
                    modifier = Modifier
                        .background(if (servoOpen) WarnaHijau.copy(alpha = 0.1f) else WarnaMerah.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (servoOpen) "OPEN" else "CLOSE",
                        fontSize = 12.sp, color = if (servoOpen) WarnaHijau else WarnaMerah, fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}