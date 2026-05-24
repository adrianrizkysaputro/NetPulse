package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // PERBAIKAN: harus sama persis dengan server monitor.py dan AndroidManifest
    private val CHANNEL_ID = "monitor_alerts"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: "Monitor Alert"
        val body  = remoteMessage.notification?.body  ?: ""

        // Ambil data payload dari server jika ada (type, device, dll)
        val type   = remoteMessage.data["type"]   ?: ""
        val device = remoteMessage.data["device"] ?: ""

        tampilkanNotifikasi(title, body, type, device)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val username = AppPrefs.getActiveUsername(applicationContext)
                // PERBAIKAN: pakai registerFcmToken (bukan simpanFcmToken)
                // dan sertakan platform = "android"
                RetrofitClient.instance.registerFcmToken(
                    FcmTokenRequest(token = token, platform = "android"),
                    username
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun tampilkanNotifikasi(
        title: String,
        body: String,
        type: String = "",
        device: String = ""
    ) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Buat channel sekali (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Monitor Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi status perangkat jaringan"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tap notifikasi → buka MainActivity
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Kirim info ke MainActivity agar bisa langsung buka tab yang relevan
            putExtra("notif_type", type)
            putExtra("notif_device", device)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Pilih ikon kecil berdasarkan tipe notifikasi
        val smallIcon = when {
            type.contains("offline", ignoreCase = true) -> android.R.drawable.ic_delete
            type.contains("online",  ignoreCase = true) -> android.R.drawable.ic_menu_send
            type.contains("latency", ignoreCase = true) -> android.R.drawable.ic_dialog_alert
            type.contains("internet",ignoreCase = true) -> android.R.drawable.ic_dialog_info
            else                                         -> android.R.drawable.ic_dialog_alert
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(smallIcon)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            // Tampilkan teks penuh jika panjang
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}