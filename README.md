# ReZero Indonesia Reader — proyek APK

Aplikasi Android sederhana yang membuka halaman bacaan Re:Zero dari:
https://darktranslation.com/novel/baca-novel-rezero-bahasa-indonesia/

## Fitur
- Membuka website dalam WebView.
- Tombol Beranda.
- Tombol kembali Android mengikuti riwayat halaman.
- Indikator pemuatan.
- Tidak memakai GPT/OpenAI API, sehingga aplikasi ini tidak menghasilkan biaya token OpenAI.
- Membutuhkan internet; tidak mengunduh atau menyimpan novel untuk penggunaan offline.

## Cara menghasilkan APK tanpa memasang Android Studio
1. Buat akun GitHub bila belum punya.
2. Buat repository baru, misalnya `ReZeroIndonesiaReader`.
3. Unggah seluruh isi ZIP ini ke repository, termasuk folder `.github`.
4. Buka tab **Actions** dan pilih workflow **Build Android APK**.
5. Tekan **Run workflow** (atau lakukan push ke branch `main`).
6. Setelah proses selesai, buka run yang berstatus berhasil.
7. Di bagian **Artifacts**, unduh `ReZeroIndonesiaReader-debug-apk`.
8. Ekstrak ZIP artifact lalu pasang `app-debug.apk` di HP Android.

APK debug cocok untuk instalasi pribadi dan pengujian. Android mungkin meminta izin memasang aplikasi dari sumber yang digunakan. Jangan membagikan APK debug sebagai rilis publik tanpa proses signing rilis yang layak.

## Jika build gagal
- Pastikan file `settings.gradle`, `build.gradle`, `gradle.properties`, dan folder `app` berada di root repository.
- Pastikan folder `.github/workflows/build-apk.yml` ikut terunggah.
- Buka log workflow untuk melihat langkah yang gagal.

## Catatan kompatibilitas
Aplikasi ini membungkus website live, bukan aplikasi resmi Re:Zero. Kompatibilitas penuh belum teruji pada seluruh halaman. Jika situs memblokir WebView, memerlukan mekanisme khusus, atau mengubah kebijakannya, sebagian fungsi mungkin tidak berjalan.
