package com.example.video

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.Surface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

/**
 * Encodes a sequence of images into a short MP4 clip (H.264) with a slow zoom
 * (Ken Burns) per image and cross-fades between them.
 *
 * The encoder uses a MediaCodec input Surface (createInputSurface) and draws
 * every frame with Canvas.lockCanvas, which avoids the YUV layout pitfalls of
 * the buffer-input path (that path raised IllegalStateException on device).
 */
object VideoComposer {

    data class VideoResult(
        val file: File,
        val durationMs: Int,
        val frameCount: Int,
        val width: Int,
        val height: Int
    )

    /** Exception class + message + up to 3 stack frames, safe for a status line. */
    fun describeError(e: Throwable): String {
        val frames = e.stackTrace
            .take(3)
            .joinToString(" <- ") { f -> "${f.fileName ?: "?"}:${f.lineNumber}" }
        val message = e.message?.takeIf { it.isNotBlank() } ?: "(no message)"
        val base = "${e.javaClass.simpleName}: $message"
        return if (frames.isBlank()) base else "$base | $frames"
    }

    /** Conservative H.264 sizes per aspect ratio, big first then a smaller fallback. */
    private fun targetSizes(aspectRatio: String): List<Pair<Int, Int>> = when (aspectRatio.trim()) {
        "1:1" -> listOf(720 to 720, 480 to 480)
        "9:16" -> listOf(720 to 1280, 540 to 960)
        "4:5" -> listOf(720 to 900, 576 to 720)
        "21:9" -> listOf(1260 to 540, 840 to 360)
        else -> listOf(1280 to 720, 960 to 540)
    }

    suspend fun compose(
        context: Context,
        images: List<Bitmap>,
        msPerImage: Int = 2000,
        fps: Int = 30,
        aspectRatio: String = "16:9"
    ): Result<VideoResult> = withContext(Dispatchers.IO) {
        try {
            if (images.size < 2) {
                return@withContext Result.failure(
                    IllegalArgumentException("تحتاج صورتين على الأقل لتركيب الفيديو")
                )
            }
            val buildDir = File(context.cacheDir, "video_build")
            if (!buildDir.exists()) buildDir.mkdirs()

            var lastError: Throwable? = null
            var success: VideoResult? = null
            for ((w, h) in targetSizes(aspectRatio)) {
                val outFile = File(buildDir, "KamalDZ_video_${System.currentTimeMillis()}.mp4")
                val attempt = runCatching { encodeToFile(images, msPerImage, fps, w, h, outFile) }
                val video = attempt.getOrNull()
                if (video != null && video.file.exists() && video.file.length() > 0L) {
                    success = video
                    break
                }
                lastError = attempt.exceptionOrNull()
                    ?: IllegalStateException("ملف الفيديو الناتج فارغ (${w}x$h)")
                if (outFile.exists()) outFile.delete()
            }
            val done = success
            if (done != null) Result.success(done)
            else Result.failure(lastError ?: IllegalStateException("فشل تركيب الفيديو"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun encodeToFile(
        images: List<Bitmap>,
        msPerImage: Int,
        fps: Int,
        outWidth: Int,
        outHeight: Int,
        outFile: File
    ): VideoResult {
        val framesPerImage = max(1, msPerImage * fps / 1000)
        val fadeFrames = min(framesPerImage / 4, fps)
        var encoder: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var inputSurface: Surface? = null
        var totalFrames = 0
        try {
            val enc = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            encoder = enc
            val format = MediaFormat.createVideoFormat(
                MediaFormat.MIMETYPE_VIDEO_AVC, outWidth, outHeight
            ).apply {
                setInteger(
                    MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
                )
                setInteger(MediaFormat.KEY_BIT_RATE, 4_000_000)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }
            enc.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val surface = enc.createInputSurface()
            inputSurface = surface
            enc.start()

            val mux = MediaMuxer(outFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            muxer = mux

            var trackIndex = -1
            var muxerStarted = false
            val bufferInfo = MediaCodec.BufferInfo()

            fun drainEncoder(endOfStream: Boolean) {
                var idleRounds = 0
                while (true) {
                    val outIndex = enc.dequeueOutputBuffer(
                        bufferInfo,
                        if (endOfStream) 10_000L else 0L
                    )
                    if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        if (!endOfStream) return
                        idleRounds++
                        if (idleRounds > 300) {
                            throw IllegalStateException(
                                "انتهت المهلة دون استلام إشارة نهاية الترميز (EOS)"
                            )
                        }
                        continue
                    }
                    idleRounds = 0
                    if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (muxerStarted) {
                            throw IllegalStateException("تغيّر تنسيق الخرج بعد بدء الـmuxer")
                        }
                        trackIndex = mux.addTrack(enc.outputFormat)
                        mux.start()
                        muxerStarted = true
                        continue
                    }
                    if (outIndex < 0) continue
                    try {
                        val encoded = enc.getOutputBuffer(outIndex)
                        if (encoded != null) {
                            if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                                bufferInfo.size = 0
                            }
                            if (bufferInfo.size > 0) {
                                if (!muxerStarted) {
                                    throw IllegalStateException("خرج مشفّر قبل بدء الـmuxer")
                                }
                                encoded.position(bufferInfo.offset)
                                encoded.limit(bufferInfo.offset + bufferInfo.size)
                                mux.writeSampleData(trackIndex, encoded, bufferInfo)
                            }
                        }
                    } finally {
                        enc.releaseOutputBuffer(outIndex, false)
                    }
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) return
                }
            }

            val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
            val frameDeadlineNs = 1_000_000_000L / fps
            val startNs = System.nanoTime()
            var frameIndex = 0

            fun drawFitted(canvas: Canvas, bitmap: Bitmap, zoom: Float, alpha: Int) {
                val scale = max(
                    canvas.width.toFloat() / bitmap.width,
                    canvas.height.toFloat() / bitmap.height
                ) * zoom
                val dx = (canvas.width - bitmap.width * scale) / 2f
                val dy = (canvas.height - bitmap.height * scale) / 2f
                paint.alpha = alpha.coerceIn(0, 255)
                canvas.save()
                canvas.translate(dx, dy)
                canvas.scale(scale, scale)
                canvas.drawBitmap(bitmap, 0f, 0f, paint)
                canvas.restore()
            }

            for ((imageIndex, bitmap) in images.withIndex()) {
                for (f in 0 until framesPerImage) {
                    var canvas: Canvas? = null
                    try {
                        canvas = surface.lockCanvas(null)
                        if (canvas == null) {
                            throw IllegalStateException("تعذّر الحصول على Canvas من سطح المشفّر")
                        }
                        canvas.drawColor(Color.BLACK)
                        val t = f.toFloat() / framesPerImage
                        drawFitted(canvas, bitmap, 1.0f + 0.12f * t, 255)
                        if (imageIndex > 0 && f < fadeFrames && fadeFrames > 0) {
                            // previous image fades out over the current one
                            val alpha = ((1f - f.toFloat() / fadeFrames) * 255).toInt()
                            drawFitted(canvas, images[imageIndex - 1], 1.0f, alpha)
                        }
                    } finally {
                        if (canvas != null) {
                            try {
                                surface.unlockCanvasAndPost(canvas)
                                frameIndex++
                            } catch (e: Exception) {
                                throw IllegalStateException(
                                    "فشل إرسال الإطار إلى المشفّر: ${describeError(e)}"
                                )
                            }
                        }
                    }
                    drainEncoder(false)
                    val targetNs = startNs + (frameIndex.toLong() * frameDeadlineNs)
                    val sleepMs = (targetNs - System.nanoTime()) / 1_000_000
                    if (sleepMs > 0) Thread.sleep(sleepMs)
                }
            }
            totalFrames = frameIndex

            enc.signalEndOfInputStream()
            drainEncoder(true)

            return VideoResult(
                file = outFile,
                durationMs = totalFrames * 1000 / fps,
                frameCount = totalFrames,
                width = outWidth,
                height = outHeight
            )
        } finally {
            try { inputSurface?.release() } catch (_: Exception) {}
            try { encoder?.stop() } catch (_: Exception) {}
            try { encoder?.release() } catch (_: Exception) {}
            try { muxer?.stop() } catch (_: Exception) {}
            try { muxer?.release() } catch (_: Exception) {}
        }
    }

    /**
     * Saves the generated bitmaps to the gallery (Pictures/KamalDZStudio).
     * Used as a fallback when video composition fails so the user still gets output.
     */
    fun saveImagesToGallery(context: Context, images: List<Bitmap>): Result<List<Uri>> {
        return runCatching {
            images.mapIndexed { index, bitmap ->
                val name = "KamalDZ_${System.currentTimeMillis()}_${index + 1}.png"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveImageMediaStore(context, bitmap, name)
                } else {
                    saveImageLegacy(context, bitmap, name)
                }
            }
        }
    }

    /** Keeps the current run's frames in cacheDir so a retry need not regenerate them. */
    fun cacheRunImages(context: Context, images: List<Bitmap>): List<File> {
        val dir = File(context.cacheDir, "video_run")
        if (!dir.exists()) dir.mkdirs()
        dir.listFiles()?.forEach { it.delete() }
        val saved = mutableListOf<File>()
        images.forEachIndexed { index, bitmap ->
            val file = File(dir, "frame_${index + 1}.png")
            runCatching {
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                saved += file
            }
        }
        return saved
    }

    private fun saveImageMediaStore(context: Context, bitmap: Bitmap, name: String): Uri {
        val resolver = context.contentResolver
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/KamalDZStudio")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(collection, values)
            ?: error("تعذّر إنشاء سجل الصورة في المعرض")
        try {
            val out = resolver.openOutputStream(uri)
                ?: error("تعذّر فتح ملف المعرض للكتابة")
            out.use { os ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)) {
                    error("فشل ترميز الصورة PNG")
                }
            }
            val done = ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }
            resolver.update(uri, done, null, null)
            return uri
        } catch (e: Exception) {
            runCatching { resolver.delete(uri, null, null) }
            throw e
        }
    }

    private fun saveImageLegacy(context: Context, bitmap: Bitmap, name: String): Uri {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "KamalDZStudio"
        )
        if (!dir.exists()) dir.mkdirs()
        val target = File(dir, name)
        FileOutputStream(target).use { out ->
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                error("فشل ترميز الصورة PNG")
            }
        }
        context.sendBroadcast(
            android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                .setData(Uri.fromFile(target))
        )
        return Uri.fromFile(target)
    }

    /**
     * Publishes the composed clip so it is visible in gallery/library apps.
     * Android 10+ writes through MediaStore (scoped storage); Android 9 and
     * below keeps the legacy direct file in Movies/KamalDZStudio.
     * Errors are returned instead of being swallowed so the caller can show them.
     */
    fun registerInMediaStore(context: Context, file: File): Result<Uri> {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                publishViaMediaStore(context, file)
            } else {
                publishLegacy(context, file)
            }
        }
    }

    private fun publishViaMediaStore(context: Context, file: File): Uri {
        val resolver = context.contentResolver
        val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/KamalDZStudio")
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(collection, values)
            ?: error("تعذّر إنشاء سجل الفيديو في المعرض")
        try {
            val out = resolver.openOutputStream(uri)
                ?: error("تعذّر فتح ملف المعرض للكتابة")
            out.use { os -> file.inputStream().use { it.copyTo(os) } }
            val done = ContentValues().apply { put(MediaStore.Video.Media.IS_PENDING, 0) }
            resolver.update(uri, done, null, null)
            file.delete()
            return uri
        } catch (e: Exception) {
            runCatching { resolver.delete(uri, null, null) }
            throw e
        }
    }

    private fun publishLegacy(context: Context, file: File): Uri {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            "KamalDZStudio"
        )
        if (!dir.exists()) dir.mkdirs()
        val target = File(dir, file.name)
        if (file.absolutePath != target.absolutePath) {
            file.copyTo(target, overwrite = true)
            file.delete()
        }
        context.sendBroadcast(
            android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                .setData(Uri.fromFile(target))
        )
        return Uri.fromFile(target)
    }
}
