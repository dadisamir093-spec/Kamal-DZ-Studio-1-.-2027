package com.example.data.model

import java.util.Locale

enum class TimelineTrackType(val titleAr: String) {
    VIDEO("مسار الفيديو الأساسي"),
    OVERLAY("مسار الطبقات والمؤثرات"),
    AUDIO("مسار الصوت والموسيقى"),
    SUBTITLE("مسار الترجمة والحوار")
}

data class TimelineClip(
    val id: String,
    val name: String,
    val sourcePath: String = "assets/clips/$id.mp4",
    val drawableResId: Int? = null,
    val inPointMs: Long = 0L,
    val outPointMs: Long,
    val timelineStartMs: Long = 0L,
    val trackType: TimelineTrackType = TimelineTrackType.VIDEO,
    val colorHex: Long = 0xFF1E3A8A,
    val isSelected: Boolean = false,
    val ffmpegFilter: String = "fps=30,scale=1920:1080"
) {
    val durationMs: Long
        get() = (outPointMs - inPointMs).coerceAtLeast(0L)

    val timelineEndMs: Long
        get() = timelineStartMs + durationMs

    fun formatDuration(): String {
        val totalSec = durationMs / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        val ms = (durationMs % 1000) / 100
        return String.format(Locale.US, "%02d:%02d.%d", min, sec, ms)
    }

    fun formatTimeRange(): String {
        val startSec = timelineStartMs / 1000f
        val endSec = timelineEndMs / 1000f
        return String.format(Locale.US, "%.1fs - %.1fs", startSec, endSec)
    }

    fun toFfmpegTrimCommand(outputName: String): String {
        val startSec = inPointMs / 1000.0
        val durSec = durationMs / 1000.0
        return String.format(
            Locale.US,
            "ffmpeg -ss %.2f -i \"%s\" -t %.2f -c:v libx264 -c:a aac \"%s\"",
            startSec, sourcePath, durSec, outputName
        )
    }
}

data class TimelineOperationResult(
    val success: Boolean,
    val actionType: String, // CUT, SPLIT, MERGE, IMPORT
    val messageAr: String,
    val ffmpegCommand: String,
    val affectedClipsCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)
