package com.rozhak.sleepyphone

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rozhak.sleepyphone.databinding.ActivityMainBinding

/**
 * Titik entri utama aplikasi (Dashboard) untuk konfigurasi dan kontrol pengguna.
 *
 * Activity ini menangani manajemen izin sistem (Overlay dan Usage Stats),
 * input parameter monitoring, serta orkestrasi siklus hidup [MonitoringService].
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    /**
     * Inisialisasi komponen view binding dan pengaturan dasar antarmuka.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()
    }

    /**
     * Memvalidasi ulang status izin setiap kali aplikasi kembali ke latar depan.
     * Hal ini memastikan UI selalu sinkron dengan perubahan yang dilakukan pengguna
     * di menu Pengaturan Sistem.
     */
    override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    /**
     * Konfigurasi event listener untuk seluruh elemen interaktif pada UI.
     * Mengelola alur navigasi ke pengaturan sistem dan pengiriman data ke [MonitoringService].
     */
    private fun setupButtons() {
        binding.btnOverlayPermission.setOnClickListener {
            if (!hasOverlayPermission()) {
                try {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Cari menu 'Display Over Other Apps' manual", Toast.LENGTH_LONG).show()
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            }
        }

        binding.btnUsagePermission.setOnClickListener {
            if (!hasUsageStatsPermission()) {
                try {
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                } catch (e: Exception) {
                    Toast.makeText(this, "Cari menu 'Usage Access' manual", Toast.LENGTH_LONG).show()
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            }
        }

        binding.btnStartService.setOnClickListener {
            if (hasOverlayPermission() && hasUsageStatsPermission()) {
                val targetPackage = binding.etTargetPackage.text.toString()
                if (targetPackage.isBlank()) {
                    Toast.makeText(this, "Package Name tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val timeLimitString = binding.etTimeLimit.text.toString()
                val timeLimitMinutes = timeLimitString.toLongOrNull() ?: 1L

                if (timeLimitString.isEmpty()) {
                    Toast.makeText(this, "Batas waktu kosong, diatur ke 1 menit", Toast.LENGTH_SHORT).show()
                }

                val timeLimitMillis = timeLimitMinutes * 60 * 1000

                val intent = Intent(this, MonitoringService::class.java).apply {
                    putExtra(MonitoringService.EXTRA_TARGET_PACKAGE, targetPackage)
                    putExtra(MonitoringService.EXTRA_MAX_USAGE_LIMIT, timeLimitMillis)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
            } else {
                Toast.makeText(this, "Mohon izinkan semua akses dulu!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnStopService.setOnClickListener {
            stopService(Intent(this, MonitoringService::class.java))
        }
    }

    /**
     * Melakukan audit status izin dan memperbarui representasi visual pada UI.
     * Mengatur warna teks, ikon status, dan visibilitas tombol aksi berdasarkan status akses.
     */
    private fun checkPermissions() {
        if (hasOverlayPermission()) {
            binding.tvOverlayStatus.text = "Izin Tampil di Atas: Diberikan"
            binding.tvOverlayStatus.setTextColor(getColor(R.color.success))
            binding.tvOverlayStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_status_granted, 0, 0, 0)
            binding.tvOverlayStatus.compoundDrawableTintList = getColorStateList(R.color.success)
            binding.btnOverlayPermission.visibility = View.GONE
        } else {
            binding.tvOverlayStatus.text = "Izin Tampil di Atas: Ditolak"
            binding.tvOverlayStatus.setTextColor(getColor(R.color.error))
            binding.tvOverlayStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_status_denied, 0, 0, 0)
            binding.tvOverlayStatus.compoundDrawableTintList = getColorStateList(R.color.error)
            binding.btnOverlayPermission.visibility = View.VISIBLE
            binding.btnOverlayPermission.text = "Buka"
        }

        if (hasUsageStatsPermission()) {
            binding.tvUsageStatus.text = "Akses Penggunaan: Diberikan"
            binding.tvUsageStatus.setTextColor(getColor(R.color.success))
            binding.tvUsageStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_status_granted, 0, 0, 0)
            binding.tvUsageStatus.compoundDrawableTintList = getColorStateList(R.color.success)
            binding.btnUsagePermission.visibility = View.GONE
        } else {
            binding.tvUsageStatus.text = "Akses Penggunaan: Ditolak"
            binding.tvUsageStatus.setTextColor(getColor(R.color.error))
            binding.tvUsageStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_status_denied, 0, 0, 0)
            binding.tvUsageStatus.compoundDrawableTintList = getColorStateList(R.color.error)
            binding.btnUsagePermission.visibility = View.VISIBLE
            binding.btnUsagePermission.text = "Buka"
        }
    }

    /**
     * Memeriksa apakah aplikasi memiliki izin `SYSTEM_ALERT_WINDOW`.
     * @return Boolean yang menunjukkan ketersediaan izin overlay.
     */
    private fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    /**
     * Memeriksa apakah aplikasi memiliki akses ke `PACKAGE_USAGE_STATS`.
     * Menggunakan [AppOpsManager] untuk validasi lintas versi Android.
     * @return Boolean yang menunjukkan ketersediaan izin statistik penggunaan.
     */
    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }
}