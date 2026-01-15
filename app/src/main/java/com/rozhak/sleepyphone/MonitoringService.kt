package com.rozhak.sleepyphone

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat

/**
 * Layanan latar depan (Foreground Service) untuk memantau durasi penggunaan aplikasi target.
 *
 * Service ini mengakumulasi waktu penggunaan aplikasi tertentu secara real-time.
 * Ketika durasi penggunaan mencapai ambang batas [maxUsageLimit], layanan akan
 * memicu [BlackScreenActivity] untuk membatasi akses pengguna.
 */
class MonitoringService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var isBlackScreenShown = false

    private var targetPackage = "com.google.android.youtube"
    private var maxUsageLimit = 60000L

    private var currentUsageDuration = 0L
    private var lastCheckTime = 0L

    companion object {
        /** Kunci Intent untuk menentukan nama paket aplikasi yang akan dipantau. */
        const val EXTRA_TARGET_PACKAGE = "TARGET_PACKAGE"

        /** Kunci Intent untuk menentukan batas maksimum penggunaan dalam milidetik. */
        const val EXTRA_MAX_USAGE_LIMIT = "MAX_USAGE_LIMIT"
    }

    /**
     * Komunikasi antar-proses (IPC) tidak didukung oleh service ini.
     * @return null karena binding tidak diimplementasikan.
     */
    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Inisialisasi awal service, menjalankan status foreground, dan memulai loop pemantauan.
     */
    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        startMonitoring()
    }

    /**
     * Memperbarui parameter operasional (target aplikasi dan batas waktu) dari Intent.
     * * @return [START_STICKY] untuk memastikan service dijalankan ulang oleh sistem jika terhenti secara paksa.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        targetPackage = intent?.getStringExtra(EXTRA_TARGET_PACKAGE) ?: targetPackage
        maxUsageLimit = intent?.getLongExtra(EXTRA_MAX_USAGE_LIMIT, maxUsageLimit) ?: maxUsageLimit

        currentUsageDuration = 0L
        lastCheckTime = System.currentTimeMillis()
        isBlackScreenShown = false

        val limitInMinutes = maxUsageLimit / 60000
        Toast.makeText(this, "Monitoring $targetPackage. Limit: $limitInMinutes menit", Toast.LENGTH_LONG).show()

        return START_STICKY
    }

    /**
     * Mengonfigurasi dan mengaktifkan notifikasi persisten.
     * * Hal ini diperlukan agar sistem Android memberikan prioritas tinggi pada service
     * dan mencegah penghentian otomatis di latar belakang.
     */
    private fun startForegroundService() {
        val channelId = "sleepy_monitor"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Sleepy Monitor", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("SleepyPhone Berjalan")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .build()
        startForeground(1, notification)
    }

    /**
     * Menjalankan siklus pemantauan berulang secara asinkron.
     * * Menggunakan [Handler] untuk melakukan pemeriksaan status aplikasi setiap 1000ms (1 detik).
     */
    private fun startMonitoring() {
        handler.post(object : Runnable {
            override fun run() {
                checkAppUsage()
                handler.postDelayed(this, 1000)
            }
        })
    }

    /**
     * Menganalisis aplikasi yang sedang berjalan dan menghitung akumulasi durasi.
     * * Jika terdeteksi bahwa aplikasi target sedang berada di latar depan, selisih waktu
     * akan ditambahkan ke total durasi hingga mencapai [maxUsageLimit].
     */
    private fun checkAppUsage() {
        if (isBlackScreenShown) {
            return
        }

        val currentTime = System.currentTimeMillis()
        val currentApp = UsageTracker.getTopPackageName(this)
        val timeDiff = currentTime - lastCheckTime
        lastCheckTime = currentTime

        if (currentApp == targetPackage) {
            currentUsageDuration += timeDiff

            if (currentUsageDuration >= maxUsageLimit) {
                showBlackScreen()
            }
        }
    }

    /**
     * Mengaktifkan layar proteksi melalui [BlackScreenActivity].
     * * Menggunakan flag `FLAG_ACTIVITY_NEW_TASK` karena Activity dimulai dari konteks Service.
     */
    private fun showBlackScreen() {
        if (isBlackScreenShown) return

        isBlackScreenShown = true
        val intent = Intent(this, BlackScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    /**
     * Mengirimkan sinyal siaran (broadcast) untuk menutup layar proteksi.
     * * Digunakan untuk mereset status pemantauan dan membersihkan UI proteksi.
     */
    private fun hideBlackScreen() {
        if (!isBlackScreenShown) return

        isBlackScreenShown = false
        currentUsageDuration = 0L
        val intent = Intent(BlackScreenActivity.ACTION_FINISH)
        sendBroadcast(intent)
    }

    /**
     * Callback saat aplikasi dihapus dari daftar aktivitas terbaru (Recent Apps).
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }

    /**
     * Membersihkan seluruh sumber daya, menghentikan callback, dan menghapus proteksi
     * saat service dimatikan.
     */
    override fun onDestroy() {
        super.onDestroy()
        hideBlackScreen()
        handler.removeCallbacksAndMessages(null)
        Toast.makeText(this, "SleepyPhone Service Stopped", Toast.LENGTH_SHORT).show()
    }
}