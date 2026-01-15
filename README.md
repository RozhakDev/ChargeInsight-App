# SleepyPhone

SleepyPhone adalah aplikasi Android berbasis Kotlin untuk mengontrol durasi penggunaan aplikasi tertentu secara terprogram dan non-intrusif.

Aplikasi ini bekerja dengan memantau aplikasi yang aktif di latar depan. Ketika durasi penggunaan melewati batas yang ditentukan, SleepyPhone mengaktifkan proteksi layar hitam penuh yang memblok seluruh input hingga dilepas oleh pemilik perangkat. Pendekatan ini tidak mematikan perangkat, tidak menggunakan root, dan tidak melakukan modifikasi sistem.

## 🎬 Preview

![SleepyPhone Preview](demo_sleepyphone.gif)

## ✨ Fitur Utama

- Proteksi layar hitam penuh berbasis activity yang memblok input pengguna
- Monitoring durasi penggunaan aplikasi target
- Pelepasan proteksi oleh pemilik perangkat melalui kombinasi tombol fisik tertentu

## 🧠 Pendekatan

SleepyPhone menggunakan:

- Statistik penggunaan aplikasi Android (`UsageStats`)
- Foreground service untuk monitoring berkelanjutan
- Activity fullscreen sebagai mekanisme proteksi
- Pendekatan deterministik tanpa eksploitasi sistem

Aplikasi tidak melakukan manipulasi sistem dan seluruh kontrol tetap berada di tangan pemilik perangkat.

## 🛠️ Tech Stack

| Komponen | Peran |
|--------|------|
| Kotlin | Bahasa utama aplikasi |
| Android SDK | Platform dan API sistem |
| Foreground Service | Monitoring berkelanjutan |
| UsageStatsManager | Data penggunaan aplikasi |

## 🚀 Menjalankan Aplikasi

Setelah aplikasi dijalankan, pengguna perlu memberikan izin Usage Access dan Display Over Other Apps. Aplikasi target serta batas waktu ditentukan melalui antarmuka utama, kemudian monitoring dapat dijalankan.

Proteksi layar akan aktif otomatis saat batas waktu tercapai.

## 📌 Catatan

> Proyek ini dibuat untuk penggunaan pribadi dan eksplorasi teknis. SleepyPhone tidak dirancang sebagai solusi parental control komersial serta tidak memerlukan root atau modifikasi sistem Android.

## 📄 Lisensi

MIT License