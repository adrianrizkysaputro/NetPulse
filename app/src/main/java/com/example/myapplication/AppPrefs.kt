package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences

object AppPrefs {
    private const val PREF_NAME = "netpulse_prefs"

    fun get(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getServerUrl(context: Context): String =
        get(context).getString("server_url", "http://192.168.1.5:8000/") ?: "http://192.168.1.5:8000/"

    fun setServerUrl(context: Context, url: String) =
        get(context).edit().putString("server_url", url).apply()

    fun getIntervalScan(context: Context): Int =
        get(context).getInt("interval_scan", 60)

    fun setIntervalScan(context: Context, interval: Int) =
        get(context).edit().putInt("interval_scan", interval).apply()

    fun isLoggedIn(context: Context): Boolean =
        get(context).getBoolean("is_logged_in", false)

    fun setLoggedIn(context: Context, value: Boolean) =
        get(context).edit().putBoolean("is_logged_in", value).apply()

    fun getActiveUsername(context: Context): String =
        get(context).getString("active_username", "") ?: ""

    fun setActiveUsername(context: Context, username: String) =
        get(context).edit().putString("active_username", username).apply()

    fun simpanAkun(context: Context, username: String, password: String) {
        get(context).edit()
            .putString("akun_${username}_nama", username)
            .putString("akun_${username}_password", password)
            .apply()
    }

    fun getPassword(context: Context, username: String): String =
        get(context).getString("akun_${username}_password", "") ?: ""

    fun akuuAdaUsername(context: Context, username: String): Boolean =
        get(context).contains("akun_${username}_password")

    fun getNamaPengguna(context: Context): String =
        getActiveUsername(context).ifEmpty { "Pengguna" }

    fun setNamaPengguna(context: Context, nama: String) =
        get(context).edit().putString("active_username", nama).apply()

    fun gantiPassword(context: Context, username: String, passwordBaru: String) =
        simpanAkun(context, username, passwordBaru)
}
