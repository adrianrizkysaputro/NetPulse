package com.example.myapplication

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme

@Composable
fun SectionTemplateESP(
    namaDevice: String,
    serverUrl: String
) {
    // ── Daftar sensor umum ──
    data class SensorItem(
        val key: String,       // nama field di payload
        val label: String,     // label yang ditampilkan
        val icon: ImageVector, // ikon (ImageVector)
        val contohVar: String, // nama variabel contoh di kode
        val tipe: String = "float" // tipe data
    )

    val sensorUmum = listOf(
        SensorItem("suhu",        "Suhu (DHT11/DHT22)",     Icons.Default.Thermostat, "nilaiSuhu"),
        SensorItem("kelembaban",  "Kelembaban (DHT11/DHT22)",Icons.Default.WaterDrop, "nilaiKelembaban"),
        SensorItem("jarak",       "Ultrasonik (jarak)",      Icons.Default.Straighten, "nilaiJarak"),
        SensorItem("waterLevel",  "Water Level",             Icons.Default.Opacity,    "nilaiAir"),
        SensorItem("tegangan",    "Tegangan (Voltmeter)",    Icons.Default.Bolt,       "nilaiTegangan"),
        SensorItem("cahaya",      "Cahaya (LDR)",            Icons.Default.Lightbulb,  "nilaiCahaya"),
        SensorItem("gas",         "Gas (MQ-2)",              Icons.Default.Air,        "nilaiGas"),
        SensorItem("gerak",       "Gerak (PIR)",             Icons.Default.Person, "nilaiGerak", "int"),
    )

    var sensorDipilih by remember { mutableStateOf(setOf<String>()) }
    var customFields by remember { mutableStateOf(listOf<Pair<String, String>>()) } // key, varName
    var showTemplate by remember { mutableStateOf(false) }
    var templateCode by remember { mutableStateOf("") }

    // Untuk input custom field
    var inputNamaField by remember { mutableStateOf("") }
    var inputNamaVar   by remember { mutableStateOf("") }
    var errorCustom    by remember { mutableStateOf("") }

    // Generate kode template
    fun generateTemplate(): String {
        val host = serverUrl.trimEnd('/')
        val sb = StringBuilder()

        sb.appendLine("#include <ESP8266HTTPClient.h>")
        sb.appendLine("#include <WiFiClient.h>")
        sb.appendLine()
        sb.appendLine("const char* serverUrl = \"$host/iot/update\";")
        sb.appendLine()

        // Deklarasi variabel sensor yang dipilih
        val allFields = sensorUmum.filter { it.key in sensorDipilih } +
                customFields.map {
                    SensorItem(it.first, it.first, Icons.Default.Drafts, it.second)
                }

        if (allFields.isNotEmpty()) {
            sb.appendLine("// Deklarasikan variabel sensor kamu di sini")
            allFields.forEach { sensor ->
                sb.appendLine("float ${sensor.contohVar} = 0;")
            }
            sb.appendLine()
        }

        sb.appendLine("void kirimKeServer() {")
        sb.appendLine("    if (WiFi.status() != WL_CONNECTED) return;")
        sb.appendLine()
        sb.appendLine("    WiFiClient client;")
        sb.appendLine("    HTTPClient http;")
        sb.appendLine()
        sb.appendLine("    http.begin(client, serverUrl);")
        sb.appendLine("    http.addHeader(\"Content-Type\", \"application/json\");")
        sb.appendLine()
        sb.appendLine("    String payload = \"{\";")
        sb.appendLine("    payload += \"\\\"device\\\":\\\"$namaDevice\\\",\"; // ← jangan diubah")
        sb.appendLine("    payload += \"\\\"ip\\\":\\\"\" + WiFi.localIP().toString() + \"\\\",\"; // ← jangan diubah")

        // Field sensor yang dipilih
        allFields.forEachIndexed { index, sensor ->
            val comma = if (index < allFields.size - 1) " + \",\"" else ""
            sb.appendLine("    payload += \"\\\"${sensor.key}\\\":\" + String(${sensor.contohVar})$comma;")
        }

        sb.appendLine("    payload += \"}\";")
        sb.appendLine()
        sb.appendLine("    int httpCode = http.POST(payload);")
        sb.appendLine("    if (httpCode > 0) {")
        sb.appendLine("        Serial.println(\"✅ Data terkirim ke server\");")
        sb.appendLine("    } else {")
        sb.appendLine("        Serial.println(\"❌ Gagal kirim ke server: \" + String(httpCode));")
        sb.appendLine("    }")
        sb.appendLine("    http.end();")
        sb.appendLine("}")
        sb.appendLine()
        sb.appendLine("void loop() {")
        sb.appendLine("    // Isi logika sensor kamu di sini")
        sb.appendLine("    // Contoh: nilaiSuhu = dht.readTemperature();")
        sb.appendLine()
        sb.appendLine("    kirimKeServer();")
        sb.appendLine("    delay(5000); // kirim tiap 5 detik")
        sb.appendLine("}")

        return sb.toString()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = WarnaCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Header ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF2563EB).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Generate Template ESP",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarnaTeksUtama
                    )
                    Text(
                        text = "Copy kode sensor ke Arduino IDE",
                        fontSize = 12.sp,
                        color = WarnaTeksDim
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = WarnaBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // ── Pilih Sensor Umum ──
            Text(
                text = "Pilih Sensor",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = WarnaTeksUtama
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Menggunakan FlowRow untuk tata letak Grid/Chip
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sensorUmum.forEach { sensor ->
                    val dipilih = sensor.key in sensorDipilih
                    Surface(
                        onClick = {
                            sensorDipilih = if (dipilih)
                                sensorDipilih - sensor.key
                            else
                                sensorDipilih + sensor.key
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (dipilih) Color(0xFF2563EB) else WarnaBg,
                        border = BorderStroke(
                            1.dp,
                            if (dipilih) Color(0xFF2563EB) else WarnaBorder
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = sensor.icon,
                                contentDescription = null,
                                tint = if (dipilih) Color.White else Color(0xFF2563EB),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = sensor.label.split(" ").first(), // Ambil kata pertama saja agar ringkas
                                fontSize = 12.sp,
                                color = if (dipilih) Color.White else WarnaTeksUtama,
                                fontWeight = if (dipilih) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = WarnaBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // ── Custom Field ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Custom Sensor",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarnaTeksUtama
                )
                if (customFields.isNotEmpty()) {
                    Text(
                        text = "${customFields.size} field",
                        fontSize = 11.sp,
                        color = Color(0xFF2563EB)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Tampil custom field yang sudah ditambah
            if (customFields.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    customFields.forEachIndexed { index, field ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2563EB).copy(alpha = 0.05f),
                            border = BorderStroke(1.dp, Color(0xFF2563EB).copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = field.first,
                                    fontSize = 11.sp,
                                    color = Color(0xFF2563EB),
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable {
                                            customFields = customFields.toMutableList().also { it.removeAt(index) }
                                        },
                                    tint = WarnaMerah
                                )
                            }
                        }
                    }
                }
            }

            // Input tambah custom field
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputNamaField,
                    onValueChange = { inputNamaField = it.trim() },
                    placeholder = { Text("Field (ex: co2)", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = WarnaBorder
                    ),
                    textStyle = TextStyle(fontSize = 12.sp)
                )
                OutlinedTextField(
                    value = inputNamaVar,
                    onValueChange = { inputNamaVar = it.trim() },
                    placeholder = { Text("Variabel", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = WarnaBorder
                    ),
                    textStyle = TextStyle(fontSize = 12.sp)
                )
                IconButton(
                    onClick = {
                        if (inputNamaField.isBlank() || inputNamaVar.isBlank()) {
                            errorCustom = "Wajib diisi"
                        } else {
                            customFields = customFields + Pair(inputNamaField, inputNamaVar)
                            inputNamaField = ""
                            inputNamaVar = ""
                            errorCustom = ""
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFF2563EB), RoundedCornerShape(10.dp))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                }
            }

            if (errorCustom.isNotEmpty()) {
                Text(
                    errorCustom,
                    fontSize = 11.sp,
                    color = WarnaMerah,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Tombol Generate ──
            Button(
                onClick = {
                    templateCode = generateTemplate()
                    showTemplate = true
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Code", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            // ── Hasil Template ──
            AnimatedVisibility(visible = showTemplate && templateCode.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = WarnaBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kode Siap Pakai",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarnaTeksUtama
                        )
                        val context = LocalContext.current
                        TextButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Template ESP", templateCode)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "✅ Berhasil disalin!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = WarnaHijau)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salin Kode", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tampil kode dengan Scroll
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        BasicTextField(
                            value = templateCode,
                            onValueChange = {},
                            readOnly = true,
                            textStyle = TextStyle(
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 18.sp
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 Salin ke Arduino IDE dan upload ke modul ESP kamu.",
                            fontSize = 11.sp,
                            color = Color(0xFF166534),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSectionTemplateESP() {
    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            SectionTemplateESP(namaDevice = "ESP32_MONITOR", serverUrl = "http://192.168.1.5:8000")
        }
    }
}
