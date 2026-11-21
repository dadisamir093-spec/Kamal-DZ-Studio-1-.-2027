package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.remote.GeminiStudioService
import com.google.firebase.FirebaseApp
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class VerifyDebugSecretTest {

    @Test
    fun testTokenResolutionDirectly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }

        GeminiStudioService.initializeAppCheck(context)

        val token = GeminiStudioService.getDebugSecretToken(context)
        println("Resolved token in test: $token")
        assertNotNull("Token must not be null", token)
        assertTrue("Token must be a valid UUID", (token?.length ?: 0) >= 32)
    }
}
