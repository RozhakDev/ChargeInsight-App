package com.rozhak.sleepyphone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity yang berfungsi sebagai pelapis (overlay) proteksi layar penuh.
 *
 * Mengimplementasikan mode imersif untuk menyembunyikan UI sistem dan membatasi
 * navigasi pengguna. Komponen ini dirancang untuk memblokir akses perangkat
 * hingga urutan kunci rahasia dimasukkan.
 */
class BlackScreenActivity : AppCompatActivity() {
    private val secretSequence = listOf(
        KeyEvent.KEYCODE_VOLUME_UP,
        KeyEvent.KEYCODE_VOLUME_UP,
        KeyEvent.KEYCODE_VOLUME_DOWN
    )
    private val userInputSequence = mutableListOf<Int>()
    private val handler = Handler(Looper.getMainLooper())
    private val resetSequenceRunnable = Runnable {
        userInputSequence.clear()
    }
    private val SEQUENCE_TIMEOUT = 3000L

    /**
     * Receiver untuk menangani instruksi penutupan Activity secara eksternal.
     * Mendengarkan [ACTION_FINISH] yang dikirimkan oleh layanan latar belakang.
     */
    private val finishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_FINISH) {
                finish()
            }
        }
    }

    /**
     * Menginisialisasi komponen UI dan mendaftarkan pemantau instruksi eksternal.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_black_screen)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(finishReceiver, IntentFilter(ACTION_FINISH), RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(finishReceiver, IntentFilter(ACTION_FINISH))
        }
    }

    /**
     * Mengintersepsi penekanan tombol fisik untuk deteksi kode rahasia.
     * * Memblokir propagasi event tombol volume agar tidak memunculkan UI sistem
     * dan mencatat urutan input untuk divalidasi.
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            handler.removeCallbacks(resetSequenceRunnable)
            handler.postDelayed(resetSequenceRunnable, SEQUENCE_TIMEOUT)

            userInputSequence.add(keyCode)

            checkSequence()

            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * Memvalidasi apakah urutan input pengguna sesuai dengan [secretSequence].
     */
    private fun checkSequence() {
        if (userInputSequence.size > secretSequence.size) {
            val lastElements = userInputSequence.subList(userInputSequence.size - secretSequence.size, userInputSequence.size)
            if (lastElements == secretSequence) {
                unlockScreen()
            } else if (userInputSequence == secretSequence) {
                unlockScreen()
            }
        }
    }

    /**
     * Menghentikan proses pemantauan dan menutup layar proteksi.
     */
    private fun unlockScreen() {
        val stopIntent = Intent(this, MonitoringService::class.java)
        stopService(stopIntent)

        Toast.makeText(this, "Proteksi Dihentikan", Toast.LENGTH_SHORT).show()

        finish()
    }

    /**
     * Menjamin UI sistem tetap tersembunyi setiap kali Activity kembali ke latar depan.
     */
    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    /**
     * Mengonfigurasi tampilan layar penuh dengan menyembunyikan bilah status dan navigasi.
     * Mendukung penyesuaian API level untuk kompatibilitas [WindowInsetsController].
     */
    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowInsetsController = window.insetsController ?: return
            windowInsetsController.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            windowInsetsController.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
        }
    }

    /**
     * Melakukan pembersihan sumber daya untuk mencegah kebocoran memori (memory leak).
     */
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(finishReceiver)
        handler.removeCallbacks(resetSequenceRunnable)
    }

    /**
     * Mengabaikan perintah kembali untuk menjaga integritas kunci layar.
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { /* Sengaja dikosongkan untuk keamanan */ }

    companion object {
        /** Aksi Intent untuk memicu penutupan Activity secara terprogram. */
        const val ACTION_FINISH = "com.rozhak.sleepyphone.ACTION_FINISH"
    }
}