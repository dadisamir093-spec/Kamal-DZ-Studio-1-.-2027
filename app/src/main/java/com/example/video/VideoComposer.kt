package com.example.video

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Encodes a sequence of images into a short MP4 clip with a gentle Ken Burns
 * (slow zoom) per image and cross-fade transitions, using MediaCodec +
 * MediaMuxer only - no paid or third-party libraries.
 */
object VideoComposer {

    data class VideoResult(val file: File, val durationMs: Int, val frameCount: Int)

    /**
     * @param images ordered bitmaps (3-6)
     * @param msPerImage how long each image is on screen
     */
    suspend fun compose(
        context: Context,
        images: List<Bitmap>,
        msPerImage: Int = 2000,
        fps: Int = 30,
        outWidth: Int = 1024,
        outHeight: Int = 576
    ): Result<VideoResult> = withContext(Dispatchers.IO) {
        try {
            if (images.size < 2) {
                return@withContext Result.failure(IllegalArgumentException("تحتاج صورة على الأقل لتركيب الفيديو"))
            }
            val framesPerImage = msPerImage * fps / 1000
            val fadeFrames = minOf(framesPerImage / 4, fps)
            val totalFrames = framesPerImage * images.size

            val moviesDir = java.io.File(
                android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_MOVIES
                ), "KamalDZStudio"
            )
            if (!moviesDir.exists()) moviesDir.mkdirs()
            val outFile = java.io.File(moviesDir, "KamalDZ_video_${System.currentTimeMillis()}.mp4")

            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, outWidth, outHeight).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
                setInteger(MediaFormat.KEY_BIT_RATE, 6_000_000)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }
            val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            encoder.start()

            val muxer = MediaMuxer(outFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var trackIndex = -1
            var muxerStarted = false
            var frameIndex = 0
            val bufferInfo = MediaCodec.BufferInfo()

            fun drawFrame(bitmap: Bitmap, zoom: Float, rotate: Matrix?): Bitmap {
                val scaled = Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, true)
                val canvasBitmap = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(canvasBitmap)
                val m = Matrix().apply { postScale(zoom, zoom, outWidth / 2f, outHeight / 2f) }
                if (rotate != null) m.preConcat(rotate)
                canvas.drawBitmap(scaled, m, null)
                return canvasBitmap
            }

            fun encodeFrame(bitmap: Bitmap, presentationTimeUs: Long) {
                val inputIndex = encoder.dequeueInputBuffer(10_000)
                if (inputIndex >= 0) {
                    val inputBuf = encoder.getInputBuffer(inputIndex) ?: return
                    // Convert bitmap to YUV420 via NV12 byte array
                    val nv12 = bitmapToNv12(bitmap)
                    inputBuf.clear()
                    inputBuf.put(nv12)
                    encoder.queueInputBuffer(inputIndex, 0, nv12.size, presentationTimeUs, 0)
                }
            }

            fun drainEncoder(endOfStream: Boolean) {
                while (true) {
                    val outIndex = encoder.dequeueOutputBuffer(bufferInfo, if (endOfStream) 10_000 else 0)
                    if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        if (!endOfStream) break
                        continue
                    }
                    if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        trackIndex = muxer.addTrack(encoder.outputFormat)
                        muxer.start()
                        muxerStarted = true
                        continue
                    }
                    if (outIndex < 0) continue
                    val encoded = encoder.getOutputBuffer(outIndex) ?: continue
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                        bufferInfo.size = 0
                    }
                    if (bufferInfo.size > 0 && muxerStarted) {
                        encoded.position(bufferInfo.offset)
                        encoded.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(trackIndex, encoded, bufferInfo)
                    }
                    encoder.releaseOutputBuffer(outIndex, false)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) break
                }
            }

            images.forEachIndexed { imgIndex, bitmap ->
                for (f in 0 until framesPerImage) {
                    val t = f.toFloat() / framesPerImage
                    // Slow zoom 1.0 -> 1.12 over each image
                    val zoom = 1.0f + 0.12f * t
                    val b = drawFrame(bitmap, zoom, null)
                    // Cross fade with previous image: blend during the first fadeFrames
                    if (imgIndex > 0 && f < fadeFrames) {
                        val alpha = f.toFloat() / fadeFrames
                        val prev = images[imgIndex - 1]
                        val blended = blend(prev, bitmap, alpha, zoom)
                        encodeFrame(blended, frameIndex * 1_000_000L / fps)
                        blended.recycle()
                    } else {
                        encodeFrame(b, frameIndex * 1_000_000L / fps)
                    }
                    drainEncoder(false)
                    frameIndex++
                    if (b !== bitmap) b.recycle()
                }
            }

            encoder.signalEndOfInputStream()
            drainEncoder(true)
            encoder.stop()
            encoder.release()
            muxer.stop()
            muxer.release()

            Result.success(VideoResult(outFile, totalFrames * 1000 / fps, totalFrames))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun blend(prev: Bitmap, next: Bitmap, alpha: Float, zoom: Float): Bitmap {
        val out = Bitmap.createBitmap(next.width, next.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val pm = android.graphics.Paint().apply { this.alpha = ((1f - alpha) * 255).toInt() }
        val nm = android.graphics.Paint().apply { this.alpha = (alpha * 255).toInt() }
        val scale = Matrix().apply { postScale(zoom, zoom, next.width / 2f, next.height / 2f) }
        canvas.drawBitmap(prev, scale, pm)
        canvas.drawBitmap(next, scale, nm)
        return out
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

    /** MediaScanner registration so the clip shows up in gallery/library apps. */
    fun registerInMediaStore(context: Context, file: File): Uri? {
        return try {
            val mediaScanIntent = android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = Uri.fromFile(file)
            context.sendBroadcast(mediaScanIntent)
            Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }
}
