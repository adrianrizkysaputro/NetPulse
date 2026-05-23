package com.example.myapplication

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

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

interface ApiService {
    @GET("devices")
    suspend fun getDevices(
        @retrofit2.http.Header("X-Username") username: String = "default"
    ): List<PerangkatResponse>

    @GET("logs")
    suspend fun getLogs(
        @retrofit2.http.Header("X-Username") username: String = "default"
    ): List<LogResponse>

    @POST("devices/tambah")
    suspend fun tambahDevice(
        @Body data: TambahRequest,
        @retrofit2.http.Header("X-Username") username: String = "default"
    ): Map<String, Boolean>

    @DELETE("devices/{id}")
    suspend fun hapusDevice(
        @retrofit2.http.Path("id") id: Int,
        @retrofit2.http.Header("X-Username") username: String = "default"
    ): Map<String, Boolean>

    @GET("ping/{ip}")
    suspend fun cekKoneksi(
        @retrofit2.http.Path("ip") ip: String
    ): PingResponse

    @GET("diagnosis")
    suspend fun getDiagnosis(
        @retrofit2.http.Header("X-Username") username: String = "default"
    ): List<DiagnosisResponse>

    @GET("iot/status")
    suspend fun getIotStatus(): Map<String, Any>

    @DELETE("logs/hapus-semua")
    suspend fun hapusSemuaLog(
        @retrofit2.http.Header("X-Username") username: String = "default"
    ): Map<String, Boolean>
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
                _instance = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService::class.java)
            }
            return _instance!!
        }
}