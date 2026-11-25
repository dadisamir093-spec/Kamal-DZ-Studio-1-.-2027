package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File

object ImageExport {
    private const val DIR = "KamalDZStudio"

    private class Format(val mime: String, val extension: String)

    private fun detectFormat(source: File): Format {
        val header = ByteArray(12)
        val read = source.inputStream().use { it.read(header) }
        fun startsWith(vararg bytes: Int) =
            read >= bytes.size && bytes.indices.all { header[it] == bytes[it].toByte() }
        return when {
            startsWith(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A) -> Format("image/png", "png")
            startsWith(0xFF, 0xD8, 0xFF) -> Format("image/jpeg", "jpg")
            read >= 12 && startsWith(0x52, 0x49, 0x46, 0x46) &&
                header[8] == 0x57.toByte() && header[9] == 0x45.toByte() &&
                header[10] == 0x42.toByte() && header[11] == 0x50.toByte() -> Format("image/webp", "webp")
            else -> error("صيغة الصورة غير معروفة (ليست PNG أو JPEG أو WEBP)")
        }
    }

    fun saveToGallery(context: Context, source: File): Result<String> = runCatching {
        require(source.exists() && source.length() > 0L) { "ملف الصورة غير موجود أو فارغ" }
        val format = detectFormat(source)
        val name = "KamalDZ_${System.currentTimeMillis()}.${format.extension}"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, name)
                put(MediaStore.Images.Media.MIME_TYPE, format.mime)
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$DIR")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("رفض نظام Android إنشاء الملف في المعرض")
            try {
                val out = resolver.openOutputStream(uri) ?: error("تعذّر فتح ملف المعرض للكتابة")
                out.use { o -> source.inputStream().use { it.copyTo(o) } }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                throw e
            }
        } else {
            @Suppress("DEPRECATION")
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), DIR)
            if (!dir.exists() && !dir.mkdirs()) error("تعذّر إنشاء مجلد Pictures/$DIR")
            val dest = File(dir, name)
            source.copyTo(dest)
            MediaScannerConnection.scanFile(context, arrayOf(dest.absolutePath), arrayOf(format.mime), null)
        }
        "Pictures/$DIR/$name"
    }

    fun share(context: Context, source: File): Result<Unit> = runCatching {
        require(source.exists() && source.length() > 0L) { "ملف الصورة غير موجود أو فارغ" }
        val format = detectFormat(source)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", source)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = format.mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "مشاركة الصورة").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
