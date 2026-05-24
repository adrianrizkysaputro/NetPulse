package com.example.myapplication

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

data class PerangkatResponse(
    val id: Int,
    val nama: String,
    val ip: String,
    val kategori: String,
    val status: String,
    val latency: Int
)

data class LogResponse(
    val perangkat: String,
    val pesan: String,
    val waktu: String,
    val tanggal: String,
    val isOnline: Boolean
)

data class TambahRequest(
    val nama: String,
    val ip: String,
    val kategori: String
)

data class PingResponse(
    val status: String,
    val latency: Int
)

data class DiagnosisResponse(
    val level: String,
    val judul: String,
    val icon: String,
    val kemungkinan: List<String>,
    val solusi: List<String>,
    val info: String? = null
)

// FCM - sesuaikan dengan field yang diterima server
data class FcmTokenRequest(
    val token: String,
    val platform: String = "android"   // ← tambah platform, server butuh ini
)

data class FcmTokenResponse(
    val ok: Boolean,
    val message: String? = null
)

data class FcmTokensInfoResponse(
    val username: String,
    val jumlah_token: Int
)

interface ApiService {
    @GET("devices")
    suspend fun getDevices(
        @Header("X-Username") username: String = "default"
    ): List<PerangkatResponse>

    @GET("logs")
    suspend fun getLogs(
        @Header("X-Username") username: String = "default"
    ): List<LogResponse>

    @POST("devices/tambah")
    suspend fun tambahDevice(
        @Body data: TambahRequest,
        @Header("X-Username") username: String = "default"
    ): Map<String, Boolean>

    @DELETE("devices/{id}")
    suspend fun hapusDevice(
        @Path("id") id: Int,
        @Header("X-Username") username: String = "default"
    ): Map<String, Boolean>

    @GET("ping/{ip}")
    suspend fun cekKoneksi(
        @Path("ip") ip: String
    ): PingResponse

    @GET("diagnosis")
    suspend fun getDiagnosis(
        @Header("X-Username") username: String = "default"
    ): List<DiagnosisResponse>

    @GET("iot/status/{ip}")
    suspend fun getIotStatus(@Path("ip") ip: String): Map<String, Any>

    @DELETE("logs/hapus-semua")
    suspend fun hapusSemuaLog(
        @Header("X-Username") username: String = "default"
    ): Map<String, Boolean>

    // ─── FCM ───────────────────────────────────────────
    // PERBAIKAN: path harus /fcm/register bukan /fcm/token
    @POST("fcm/register")
    suspend fun registerFcmToken(
        @Body data: FcmTokenRequest,
        @Header("X-Username") username: String = "default"
    ): FcmTokenResponse

    // Hapus token saat logout
    @HTTP(method = "DELETE", path = "fcm/unregister", hasBody = true)
    suspend fun unregisterFcmToken(
        @Body data: FcmTokenRequest,
        @Header("X-Username") username: String = "default"
    ): FcmTokenResponse

    // Cek berapa token terdaftar
    @GET("fcm/tokens")
    suspend fun getFcmTokensInfo(
        @Header("X-Username") username: String = "default"
    ): FcmTokensInfoResponse

    // Test kirim notifikasi
    @POST("fcm/test")
    suspend fun testNotifikasi(
        @Header("X-Username") username: String = "default"
    ): Map<String, Any>
}

object RetrofitClient {
    var BASE_URL = "http://192.168.1.5:8000/"
        set(value) {
            field = value
            _instance = null
        }

    private var _instance: ApiService? = null

    val instance: ApiService
        get() {
            if (_instance == null) {
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()

                _instance = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService::class.java)
            }
            return _instance!!
        }
}