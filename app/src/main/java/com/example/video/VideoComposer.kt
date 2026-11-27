package com.example.video

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.media.Image
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max
import kotlin.math.min

/**
 * Encodes a sequence of images into a short MP4 clip with a gentle Ken Burns
 * (slow zoom) per image and cross-fade transitions, using MediaCodec +
 * MediaMuxer only - no paid or third-party libraries.
 */
object VideoComposer {

    data class VideoResult(
        val file: File,
        val durationMs: Int,
        val frameCount: Int,
        val width: Int,
        val height: Int
    )

    /** Conservative H.264 sizes per aspect ratio, big first then a smaller fallback. */
    private fun targetSizes(aspectRatio: String): List<Pair<Int, Int>> = when (aspectRatio.trim()) {
        "9:16" -> listOf(720 to 1280, 540 to 960)
        "1:1" -> listOf(720 to 720, 540 to 540)
        "4:5" -> listOf(720 to 900, 576 to 720)
        "21:9" -> listOf(1260 to 540, 840 to 360)
        "16:9" -> listOf(1280 to 720, 960 to 540)
        else -> listOf(1280 to 720, 960 to 540)
    }

    /**
     * @param images ordered bitmaps (2+)
     * @param msPerImage how long each image is on screen
     * @param aspectRatio slot aspect ratio ("16:9", "9:16", ...)
     */
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
        val framesPerImage = msPerImage * fps / 1000
        val fadeFrames = min(framesPerImage / 4, fps)
        var encoder: MediaCodec? = null
        var muxer: MediaMuxer? = null
        try {
            val enc = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            encoder = enc
            val colorFormat = pickColorFormat(enc)
            val format = MediaFormat.createVideoFormat(
                MediaFormat.MIMETYPE_VIDEO_AVC, outWidth, outHeight
            ).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat)
                setInteger(MediaFormat.KEY_BIT_RATE, 4_000_000)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }
            enc.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            enc.start()

            val mux = MediaMuxer(outFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            muxer = mux

            var trackIndex = -1
            var muxerStarted = false
            var frameIndex = 0
            val bufferInfo = MediaCodec.BufferInfo()
            val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)

            fun drawFrame(bitmap: Bitmap, zoom: Float): Bitmap {
                val out = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(out)
                // center-crop the source into the target frame so no aspect distortion
                val scale = max(
                    outWidth.toFloat() / bitmap.width,
                    outHeight.toFloat() / bitmap.height
                ) * zoom
                val dx = (outWidth - bitmap.width * scale) / 2f
                val dy = (outHeight - bitmap.height * scale) / 2f
                val m = Matrix().apply {
                    postScale(scale, scale)
                    postTranslate(dx, dy)
                }
                canvas.drawBitmap(bitmap, m, paint)
                return out
            }

            fun blend(prev: Bitmap, next: Bitmap, alpha: Float): Bitmap {
                val out = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(out)
                val pm = Paint(Paint.FILTER_BITMAP_FLAG).apply { this.alpha = ((1f - alpha) * 255).toInt() }
                val nm = Paint(Paint.FILTER_BITMAP_FLAG).apply { this.alpha = (alpha * 255).toInt() }
                canvas.drawBitmap(prev, 0f, 0f, pm)
                canvas.drawBitmap(next, 0f, 0f, nm)
                return out
            }

            fun queueFrame(bitmap: Bitmap, ptsUs: Long): Boolean {
                val inputIndex = enc.dequeueInputBuffer(20_000)
                if (inputIndex < 0) return false
                val yuvSize = outWidth * outHeight * 3 / 2
                val image: Image? = enc.getInputImage(inputIndex)
                if (image != null) {
                    try {
                        if (image.width != outWidth || image.height != outHeight) {
                            throw IllegalStateException(
                                "أبعاد صورة المشفّر غير مطابقة (${image.width}x${image.height} != ${outWidth}x$outHeight)"
                            )
                        }
                        fillImage(image, bitmap)
                    } finally {
                        image.close()
                    }
                } else {
                    val buf = enc.getInputBuffer(inputIndex)
                        ?: throw IllegalStateException("تعذّر الحصول على مخزن إدخال المشفّر")
                    val nv12 = bitmapToNv12(bitmap)
                    if (nv12.size > buf.capacity()) {
                        throw IllegalStateException(
                            "حجم إطار YUV أكبر من المخزن (${nv12.size} > ${buf.capacity()})"
                        )
                    }
                    buf.clear()
                    buf.put(nv12)
                }
                enc.queueInputBuffer(inputIndex, 0, yuvSize, ptsUs, 0)
                return true
            }

            fun drainEncoder(endOfStream: Boolean) {
                while (true) {
                    val outIndex = enc.dequeueOutputBuffer(bufferInfo, if (endOfStream) 10_000 else 0)
                    if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        if (!endOfStream) break
                        continue
                    }
                    if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        trackIndex = mux.addTrack(enc.outputFormat)
                        mux.start()
                        muxerStarted = true
                        continue
                    }
                    if (outIndex < 0) continue
                    val encoded = enc.getOutputBuffer(outIndex) ?: continue
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                        bufferInfo.size = 0
                    }
                    if (bufferInfo.size > 0 && muxerStarted) {
                        encoded.position(bufferInfo.offset)
                        encoded.limit(bufferInfo.offset + bufferInfo.size)
                        mux.writeSampleData(trackIndex, encoded, bufferInfo)
                    }
                    enc.releaseOutputBuffer(outIndex, false)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) break
                }
            }

            var prevFrame: Bitmap? = null
            for (bitmap in images) {
                for (f in 0 until framesPerImage) {
                    val t = f.toFloat() / framesPerImage
                    val zoom = 1.0f + 0.12f * t
                    val frame = drawFrame(bitmap, zoom)
                    val previous = prevFrame
                    val toEncode = if (previous != null && f < fadeFrames) {
                        blend(previous, frame, f.toFloat() / fadeFrames.coerceAtLeast(1))
                    } else {
                        frame
                    }
                    if (queueFrame(toEncode, frameIndex * 1_000_000L / fps)) frameIndex++
                    if (toEncode !== frame) toEncode.recycle()
                    drainEncoder(false)
                    if (previous != null && previous !== frame) previous.recycle()
                    prevFrame = frame
                }
            }
            prevFrame?.recycle()

            enc.signalEndOfInputStream()
            drainEncoder(true)

            val totalFrames = frameIndex
            return VideoResult(
                file = outFile,
                durationMs = totalFrames * 1000 / fps,
                frameCount = totalFrames,
                width = outWidth,
                height = outHeight
            )
        } finally {
            try { encoder?.stop() } catch (_: Exception) {}
            try { encoder?.release() } catch (_: Exception) {}
            try { muxer?.stop() } catch (_: Exception) {}
            try { muxer?.release() } catch (_: Exception) {}
        }
    }

    private fun pickColorFormat(encoder: MediaCodec): Int {
        val formats = runCatching {
            encoder.codecInfo.getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_AVC)
                .colorFormats.toSet()
        }.getOrNull()
        return when {
            formats == null -> MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar in formats ->
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible in formats ->
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
            else -> formats.first()
        }
    }

    /** Writes Y/U/V into a MediaCodec flexible YUV image, honouring row/pixel strides. */
    private fun fillImage(image: Image, bitmap: Bitmap) {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val planes = image.planes
        val yPlane = planes[0]
        val uPlane = planes[1]
        val vPlane = planes[2]
        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer
        for (y in 0 until h) {
            val rowStart = y * w
            var yIndex = y * yPlane.rowStride
            for (x in 0 until w) {
                val p = pixels[rowStart + x]
                val r = (p shr 16) and 0xFF
                val g = (p shr 8) and 0xFF
                val b = p and 0xFF
                val yv = (0.299f * r + 0.587f * g + 0.114f * b).toInt().coerceIn(0, 255)
                yBuffer.put(yIndex, yv.toByte())
                yIndex += yPlane.pixelStride
                if (y % 2 == 0 && x % 2 == 0) {
                    val u = ((-0.169f * r - 0.331f * g + 0.5f * b) + 128).toInt().coerceIn(0, 255)
                    val v = ((0.5f * r - 0.419f * g - 0.081f * b) + 128).toInt().coerceIn(0, 255)
                    val chromaIndex = (y / 2) * uPlane.rowStride + (x / 2) * uPlane.pixelStride
                    uBuffer.put(chromaIndex, u.toByte())
                    vBuffer.put((y / 2) * vPlane.rowStride + (x / 2) * vPlane.pixelStride, v.toByte())
                }
            }
        }
    }

    private fun bitmapToNv12(bitmap: Bitmap): ByteArray {
        val w = bitmap.width
        val h = bitmap.height
        val ySize = w * h
        val out = ByteArray(ySize * 3 / 2)
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        var yi = 0
        var ui = ySize
        var vi = ySize + ySize / 4
        for (y in 0 until h) {
            for (x in 0 until w) {
                val p = pixels[y * w + x]
                val r = (p shr 16) and 0xFF
                val g = (p shr 8) and 0xFF
                val b = p and 0xFF
                val yv = (0.299f * r + 0.587f * g + 0.114f * b).toInt().coerceIn(0, 255)
                out[yi++] = yv.toByte()
                if (y % 2 == 0 && x % 2 == 0) {
                    val u = ((-0.169f * r - 0.331f * g + 0.5f * b) + 128).toInt().coerceIn(0, 255)
                    val v = ((0.5f * r - 0.419f * g - 0.081f * b) + 128).toInt().coerceIn(0, 255)
                    out[ui++] = u.toByte()
                    out[vi++] = v.toByte()
                }
            }
        }
        return out
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
