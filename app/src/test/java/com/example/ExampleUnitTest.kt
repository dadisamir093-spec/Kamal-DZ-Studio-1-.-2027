package com.example

import com.example.data.media.TimelineMediaEngine
import com.example.data.model.TimelineClip
import com.example.data.model.TimelineTrackType
import com.example.ui.components.studioNavTabs
import com.example.ui.components.twentyCameraMovements
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun verify_seven_studio_navigation_sections() {
        assertEquals(7, studioNavTabs.size)
        val tabNames = studioNavTabs.map { it.title }
        assertTrue(tabNames.contains("التوليد"))
        assertTrue(tabNames.contains("الشخصيات"))
        assertTrue(tabNames.contains("الصوت"))
        assertTrue(tabNames.contains("الموسيقى"))
        assertTrue(tabNames.contains("المونتاج"))
        assertTrue(tabNames.contains("المكتبة"))
        assertTrue(tabNames.contains("التصدير"))
    }

    @Test
    fun verify_twenty_camera_movements() {
        assertEquals(20, twentyCameraMovements.size)
        assertTrue(twentyCameraMovements.any { it.titleAr.contains("Dolly In") })
        assertTrue(twentyCameraMovements.any { it.titleAr.contains("Orbit Shot") })
        assertTrue(twentyCameraMovements.any { it.titleAr.contains("Smart Drone") })
    }

    @Test
    fun test_timeline_split_operation() {
        val engine = TimelineMediaEngine()
        val originalClip = TimelineClip(
            id = "test_clip_1",
            name = "مشهد سينمائي",
            sourcePath = "assets/clips/test1.mp4",
            inPointMs = 0L,
            outPointMs = 6000L,
            timelineStartMs = 1000L,
            trackType = TimelineTrackType.VIDEO
        )

        // Split at timeline position 3500ms (relative offset = 2500ms)
        val (parts, result) = engine.splitClip(originalClip, 3500L)

        assertNotNull(parts)
        assertTrue(result.success)
        assertEquals("SPLIT", result.actionType)
        assertTrue(result.ffmpegCommand.contains("ffmpeg -ss"))
        assertTrue(result.ffmpegCommand.contains("part1"))
        assertTrue(result.ffmpegCommand.contains("part2"))

        val (partA, partB) = parts!!
        assertEquals(2500L, partA.durationMs)
        assertEquals(3500L, partB.durationMs)
        assertEquals(1000L, partA.timelineStartMs)
        assertEquals(3500L, partB.timelineStartMs)
    }

    @Test
    fun test_timeline_cut_operation() {
        val engine = TimelineMediaEngine()
        val originalClip = TimelineClip(
            id = "test_clip_2",
            name = "مشهد القصبة",
            sourcePath = "assets/clips/test2.mp4",
            inPointMs = 0L,
            outPointMs = 5000L,
            timelineStartMs = 0L,
            trackType = TimelineTrackType.VIDEO
        )

        // Cut 600ms head, 400ms tail -> total trimmed = 1000ms, new duration = 4000ms
        val (trimmedClip, result) = engine.cutClip(originalClip, 600L, 400L)

        assertNotNull(trimmedClip)
        assertTrue(result.success)
        assertEquals("CUT", result.actionType)
        assertEquals(4000L, trimmedClip!!.durationMs)
        assertEquals(600L, trimmedClip.inPointMs)
        assertEquals(4600L, trimmedClip.outPointMs)
        assertTrue(result.ffmpegCommand.contains("-crf 18"))
        assertTrue(result.ffmpegCommand.contains("_trimmed.mp4"))
    }

    @Test
    fun test_timeline_merge_operation() {
        val engine = TimelineMediaEngine()
        val clipA = TimelineClip(
            id = "clip_a",
            name = "المشهد الأول",
            sourcePath = "assets/clips/a.mp4",
            inPointMs = 0L,
            outPointMs = 3000L,
            timelineStartMs = 0L
        )
        val clipB = TimelineClip(
            id = "clip_b",
            name = "المشهد الثاني",
            sourcePath = "assets/clips/b.mp4",
            inPointMs = 0L,
            outPointMs = 4000L,
            timelineStartMs = 3000L
        )

        val (mergedClip, result) = engine.mergeClips(clipA, clipB)

        assertNotNull(mergedClip)
        assertTrue(result.success)
        assertEquals("MERGE", result.actionType)
        assertEquals(7000L, mergedClip!!.durationMs)
        assertTrue(result.ffmpegCommand.contains("concat=n=2:v=1:a=1"))
        assertTrue(result.messageAr.contains("تم دمج المقطعين"))
    }

    @Test
    fun test_timeline_sequential_recalculation() {
        val engine = TimelineMediaEngine()
        val clips = listOf(
            TimelineClip("c1", "مقطع 1", inPointMs = 0L, outPointMs = 2000L, timelineStartMs = 9999L),
            TimelineClip("c2", "مقطع 2", inPointMs = 0L, outPointMs = 3500L, timelineStartMs = 9999L),
            TimelineClip("c3", "مقطع 3", inPointMs = 0L, outPointMs = 1500L, timelineStartMs = 9999L)
        )

        val sequenced = engine.recalculateTimelineStarts(clips)

        assertEquals(0L, sequenced[0].timelineStartMs)
        assertEquals(2000L, sequenced[1].timelineStartMs)
        assertEquals(5500L, sequenced[2].timelineStartMs)
        assertEquals(7000L, sequenced[2].timelineEndMs)
    }
}
