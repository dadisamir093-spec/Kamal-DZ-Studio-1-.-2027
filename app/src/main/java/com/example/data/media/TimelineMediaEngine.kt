package com.example.data.media

import android.content.Context
import com.example.data.model.TimelineClip
import com.example.data.model.TimelineOperationResult
import java.io.File
import java.util.Locale

class TimelineMediaEngine(private val context: Context? = null) {

    /**
     * Splits a given clip at the timeline playhead position into two distinct clips.
     */
    fun splitClip(
        clip: TimelineClip,
        splitPlayheadMs: Long
    ): Pair<Pair<TimelineClip, TimelineClip>?, TimelineOperationResult> {
        val relativeOffsetMs = splitPlayheadMs - clip.timelineStartMs

        if (relativeOffsetMs <= 200L || relativeOffsetMs >= (clip.durationMs - 200L)) {
            val failResult = TimelineOperationResult(
                success = false,
                actionType = "SPLIT",
                messageAr = "موضع المؤشر (${splitPlayheadMs}ms) قريب جداً من بداية أو نهاية المقطع. يُشترط هامش 200ms على الأقل.",
                ffmpegCommand = "# Split rejected: playhead out of valid inner range",
                affectedClipsCount = 1
            )
            return Pair(null, failResult)
        }

        val clipAId = "${clip.id}_part1_${System.currentTimeMillis() % 10000}"
        val clipBId = "${clip.id}_part2_${System.currentTimeMillis() % 10000}"

        val partA = clip.copy(
            id = clipAId,
            name = "${clip.name} (جزء 1)",
            inPointMs = clip.inPointMs,
            outPointMs = clip.inPointMs + relativeOffsetMs,
            timelineStartMs = clip.timelineStartMs,
            isSelected = true
        )

        val partB = clip.copy(
            id = clipBId,
            name = "${clip.name} (جزء 2)",
            inPointMs = clip.inPointMs + relativeOffsetMs,
            outPointMs = clip.outPointMs,
            timelineStartMs = splitPlayheadMs,
            isSelected = false
        )

        val ffmpegCmd = String.format(
            Locale.US,
            "ffmpeg -ss %.2f -to %.2f -i \"%s\" -c copy \"%s\" && ffmpeg -ss %.2f -to %.2f -i \"%s\" -c copy \"%s\"",
            clip.inPointMs / 1000.0,
            (clip.inPointMs + relativeOffsetMs) / 1000.0,
            clip.sourcePath,
            "${partA.id}.mp4",
            (clip.inPointMs + relativeOffsetMs) / 1000.0,
            clip.outPointMs / 1000.0,
            clip.sourcePath,
            "${partB.id}.mp4"
        )

        recordManifestOperation("SPLIT", clip.name, ffmpegCmd)

        val successResult = TimelineOperationResult(
            success = true,
            actionType = "SPLIT",
            messageAr = "تم تقسيم المقطع «${clip.name}» إلى جزأين عند ${splitPlayheadMs / 1000.0}s بنجاح",
            ffmpegCommand = ffmpegCmd,
            affectedClipsCount = 2
        )

        return Pair(Pair(partA, partB), successResult)
    }

    /**
     * Cuts (Trims) in/out points of the specified clip.
     */
    fun cutClip(
        clip: TimelineClip,
        trimHeadMs: Long,
        trimTailMs: Long
    ): Pair<TimelineClip?, TimelineOperationResult> {
        val newInPoint = clip.inPointMs + trimHeadMs
        val newOutPoint = clip.outPointMs - trimTailMs

        if (newOutPoint - newInPoint < 300L) {
            val failResult = TimelineOperationResult(
                success = false,
                actionType = "CUT",
                messageAr = "القص المطلوب يؤدي لمقطع مدته أقل من 300ms. يرجى تعديل نقاط الاقتطاع.",
                ffmpegCommand = "# Cut rejected: remaining duration too short",
                affectedClipsCount = 1
            )
            return Pair(null, failResult)
        }

        val trimmedClip = clip.copy(
            inPointMs = newInPoint,
            outPointMs = newOutPoint,
            name = if (clip.name.contains("مقصوص")) clip.name else "${clip.name} [مقصوص]"
        )

        val ffmpegCmd = String.format(
            Locale.US,
            "ffmpeg -ss %.2f -t %.2f -i \"%s\" -c:v libx264 -crf 18 -preset fast -c:a aac \"%s_trimmed.mp4\"",
            newInPoint / 1000.0,
            trimmedClip.durationMs / 1000.0,
            clip.sourcePath,
            clip.id
        )

        recordManifestOperation("CUT", clip.name, ffmpegCmd)

        val successResult = TimelineOperationResult(
            success = true,
            actionType = "CUT",
            messageAr = "تم قص المقطع «${clip.name}» واقتطاع ${trimHeadMs + trimTailMs}ms بنجاح",
            ffmpegCommand = ffmpegCmd,
            affectedClipsCount = 1
        )

        return Pair(trimmedClip, successResult)
    }

    /**
     * Merges two adjacent clips into one continuous media clip.
     */
    fun mergeClips(
        firstClip: TimelineClip,
        secondClip: TimelineClip
    ): Pair<TimelineClip?, TimelineOperationResult> {
        val mergedId = "merged_${System.currentTimeMillis() % 100000}"
        val totalCombinedDuration = firstClip.durationMs + secondClip.durationMs

        val mergedClip = TimelineClip(
            id = mergedId,
            name = "مدمج: ${firstClip.name.take(12)} + ${secondClip.name.take(12)}",
            sourcePath = "assets/clips/$mergedId.mp4",
            drawableResId = firstClip.drawableResId ?: secondClip.drawableResId,
            inPointMs = 0L,
            outPointMs = totalCombinedDuration,
            timelineStartMs = firstClip.timelineStartMs,
            trackType = firstClip.trackType,
            colorHex = 0xFF0D9488, // Teal merged accent
            isSelected = true
        )

        val ffmpegCmd = String.format(
            Locale.US,
            "ffmpeg -i \"%s\" -i \"%s\" -filter_complex \"[0:v][0:a][1:v][1:a]concat=n=2:v=1:a=1[v][a]\" -map \"[v]\" -map \"[a]\" \"%s.mp4\"",
            firstClip.sourcePath,
            secondClip.sourcePath,
            mergedId
        )

        recordManifestOperation("MERGE", "${firstClip.name} & ${secondClip.name}", ffmpegCmd)

        val successResult = TimelineOperationResult(
            success = true,
            actionType = "MERGE",
            messageAr = "تم دمج المقطعين «${firstClip.name}» و «${secondClip.name}» في مقطع واحد مدته ${mergedClip.formatDuration()}",
            ffmpegCommand = ffmpegCmd,
            affectedClipsCount = 2
        )

        return Pair(mergedClip, successResult)
    }

    /**
     * Records operations to a local manifest file for verification and project integrity.
     */
    private fun recordManifestOperation(action: String, clipName: String, command: String) {
        try {
            val dir = context?.filesDir ?: File("/tmp")
            if (!dir.exists()) dir.mkdirs()
            val manifestFile = File(dir, "timeline_processing_log.txt")
            manifestFile.appendText("[$action] [${System.currentTimeMillis()}] Clip: $clipName | FFmpeg: $command\n")
        } catch (_: Exception) {
            // Ignore filesystem write errors during headless test runs
        }
    }

    /**
     * Recalculates timeline start positions sequentially so clips sit seamlessly back-to-back.
     */
    fun recalculateTimelineStarts(clips: List<TimelineClip>): List<TimelineClip> {
        var currentStart = 0L
        return clips.map { clip ->
            val updated = clip.copy(timelineStartMs = currentStart)
            currentStart += updated.durationMs
            updated
        }
    }
}
