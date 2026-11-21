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
class DebugSecretResolutionTest {

    @Test
    fun testDebugSecretGeneratedAndResolved() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }

        // Initialize App Check
        GeminiStudioService.initializeAppCheck(context)

        // Resolve Debug Secret Token
        val token = GeminiStudioService.getDebugSecretToken(context)
        println("Resolved Debug Secret Token in test: $token")

        assertNotNull("Debug Secret Token should not be null", token)
        assertTrue("Token should be valid UUID length", (token?.length ?: 0) >= 32)
    }
}
