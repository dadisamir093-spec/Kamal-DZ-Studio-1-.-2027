package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.viewmodel.StudioViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ImportExportFunctionalityTest {

    private lateinit var viewModel: StudioViewModel

    @Before
    fun setup() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        viewModel = StudioViewModel(application)
    }

    @Test
    fun test_button1_import_from_device() {
        val initialCount = viewModel.timelineClips.value.size
        val success = viewModel.importFromDevice(
            name = "مشهد_كاميرا_الهاتف.mp4",
            uriString = "content://media/external/video/media/12345",
            mediaType = "VIDEO"
        )

        assertTrue("Import from device should succeed", success)
        val clips = viewModel.timelineClips.value
        assertEquals(initialCount + 1, clips.size)
        val importedClip = clips.last()
        assertEquals("مشهد_كاميرا_الهاتف.mp4", importedClip.name)
        assertTrue(importedClip.sourcePath.contains("12345"))

        val lastOp = viewModel.lastTimelineResult.value
        assertNotNull(lastOp)
        assertTrue(lastOp!!.success)
        assertEquals("IMPORT_DEVICE", lastOp.actionType)
        assertTrue(lastOp.messageAr.contains("مشهد_كاميرا_الهاتف.mp4"))
    }

    @Test
    fun test_button2_import_from_url_valid_and_invalid() {
        // 1. Invalid URL (empty or without scheme)
        val fail1 = viewModel.importFromUrl("")
        assertFalse("Empty URL should fail", fail1)

        val fail2 = viewModel.importFromUrl("ftp://invalid.com/file.mp4")
        assertFalse("Non-http/https URL should fail", fail2)

        // 2. Valid HTTPS URL
        val initialCount = viewModel.timelineClips.value.size
        val validUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        val success = viewModel.importFromUrl(validUrl)

        assertTrue("Valid https URL import should succeed", success)
        val clips = viewModel.timelineClips.value
        assertEquals(initialCount + 1, clips.size)
        val importedClip = clips.last()
        assertTrue(importedClip.name.contains("BigBuckBunny.mp4"))

        val lastOp = viewModel.lastTimelineResult.value
        assertNotNull(lastOp)
        assertTrue(lastOp!!.success)
        assertEquals("IMPORT_URL", lastOp.actionType)
    }

    @Test
    fun test_button3_import_from_google_drive() {
        val initialCount = viewModel.timelineClips.value.size

        // Test Drive share link
        val driveLink = "https://drive.google.com/file/d/1A2B3C4D5E6F7G8H/view?usp=sharing"
        val success = viewModel.importFromGoogleDrive(driveLink, isLink = true)

        assertTrue("Google Drive link import should succeed", success)
        val clips = viewModel.timelineClips.value
        assertEquals(initialCount + 1, clips.size)
        val importedClip = clips.last()
        assertTrue(importedClip.name.contains("Drive"))

        val lastOp = viewModel.lastTimelineResult.value
        assertNotNull(lastOp)
        assertTrue(lastOp!!.success)
        assertEquals("IMPORT_DRIVE", lastOp.actionType)
        assertTrue(lastOp.messageAr.contains("Google Drive"))
    }

    @Test
    fun test_button4_import_from_cloud_storage_dropbox_onedrive() {
        val initialCount = viewModel.timelineClips.value.size

        // Test Dropbox cloud import
        val dropboxLink = "https://www.dropbox.com/s/sample_scene.mp4?dl=0"
        val success = viewModel.importFromCloudStorage("Dropbox", dropboxLink, isLink = true)

        assertTrue("Cloud Storage import should succeed", success)
        val clips = viewModel.timelineClips.value
        assertEquals(initialCount + 1, clips.size)
        val importedClip = clips.last()
        assertTrue(importedClip.name.contains("Dropbox"))

        val lastOp = viewModel.lastTimelineResult.value
        assertNotNull(lastOp)
        assertTrue(lastOp!!.success)
        assertEquals("IMPORT_CLOUD", lastOp.actionType)
        assertTrue(lastOp.messageAr.contains("Dropbox"))
    }
}
