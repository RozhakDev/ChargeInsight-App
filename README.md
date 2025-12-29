# ChargeInsight-App

ChargeInsight adalah aplikasi desktop berbasis Python untuk **menganalisis kesehatan baterai laptop Windows** secara objektif dan explainable.

Aplikasi ini tidak hanya menampilkan persentase baterai, tetapi menghitung **battery health**, **status degradasi**, dan **confidence score** berdasarkan data nyata dari sistem dan reasoning berbasis aturan.

## ✨ Fitur Utama

- Analisis **Battery Health (%)** dari design vs full charge capacity  
- **Confidence Score** berbasis constraint reasoning (bukan tebakan)
- Penjelasan alasan (explainable reasoning)
- Monitoring berkala otomatis
- Klasifikasi kondisi baterai: **Healthy / Degrading / Worn**

## 🧠 Pendekatan

ChargeInsight menggunakan:

- Data sistem asli Windows (`powercfg /batteryreport`)
- Model deterministik dan explainable
- Parsing & normalisasi data baterai
- Tanpa manipulasi data, tanpa guesswork

AI **tidak digunakan sebagai sumber data**, hanya disiapkan sebagai kemungkinan layer interpretasi di masa depan.

## 🛠️ Tech Stack

- **Python**
- **Poetry** (dependency & environment management)
- **PySide6** (desktop GUI)
- **BeautifulSoup** (HTML parsing)
- **psutil** (system utilities)

## 🚀 Menjalankan Aplikasi

```bash
poetry install
poetry run python -m chargeinsight.main
```

> Aplikasi akan otomatis mengambil data baterai dan melakukan monitoring berkala.

## 📌 Catatan

* Aplikasi bersifat **read-only** terhadap sistem
* Data disimpan **lokal** di direktori user
* Dirancang sebagai proyek pembelajaran, portfolio, dan fondasi pengembangan lanjutan

## 📄 Lisensi

MIT License