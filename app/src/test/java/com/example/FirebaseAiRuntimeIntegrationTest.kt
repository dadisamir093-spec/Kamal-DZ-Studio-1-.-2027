package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.remote.GeminiStudioService
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class FirebaseAiRuntimeIntegrationTest {

    @Test
    fun testGeminiRuntimeConnection() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            println("=== STARTING FIREBASE AI LOGIC RUNTIME TEST ===")
            
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            
            // Initialize App Check
            GeminiStudioService.initializeAppCheck(context)
            
            val prompt = "Reply with exactly: KAMAL DZ FIREBASE CONNECTION OK"
            println("Sending Prompt: '$prompt' to gemini-2.5-flash via Firebase.ai...")

            val result = GeminiStudioService.optimizeCinematicPrompt(
                rawPrompt = prompt,
                cameraStyle = "Static",
                mode = "Test"
            )

            result.onSuccess { text ->
                println("=== FIREBASE AI RESPONSE RECEIVED ===")
                println("Response Content: $text")
            }.onFailure { err ->
                println("=== FIREBASE AI CONNECTION FAILED ===")
                println("Error Type: ${err.javaClass.name}")
                println("Error Message: ${err.message}")
                err.cause?.let { println("Cause: ${it.message}") }
            }
        }
    }
}
