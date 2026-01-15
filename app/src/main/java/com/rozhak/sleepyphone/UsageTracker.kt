package com.rozhak.sleepyphone

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context

/**
 * Utilitas pelacak aktivitas penggunaan aplikasi pada perangkat.
 *
 * Menyediakan fungsi untuk menganalisis riwayat interaksi aplikasi di latar depan (foreground).
 * **Izin Diperlukan:** `android.permission.PACKAGE_USAGE_STATS`.
 */
object UsageTracker {
    /**
     * Mengambil nama paket aplikasi terakhir yang diinteraksi oleh pengguna.
     *
     * Fungsi ini memindai kejadian `MOVE_TO_FOREGROUND` dalam rentang waktu 2 jam terakhir
     * dan mengabaikan nama paket aplikasi ini sendiri.
     *
     * @param context Konteks aplikasi untuk mengakses [UsageStatsManager].
     * @return Nama paket aplikasi terakhir yang terdeteksi, atau "Unknown" jika data tidak tersedia.
     */
    fun getTopPackageName(context: Context): String {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()

        val startTime = endTime - (1000 * 60 * 60 * 2)

        val events = usm.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()

        var lastAppPackage = "Unknown"

        while (events.hasNextEvent()) {
            events.getNextEvent(event)

            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                if (event.packageName != context.packageName) {
                    lastAppPackage = event.packageName
                }
            }
        }
        return lastAppPackage
    }
}