package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.android.gms.tasks.Tasks
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DebugTokenTriggerTest {
    @Test
    fun testGetToken() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        val app = FirebaseApp.getInstance()
        val appCheck = FirebaseAppCheck.getInstance()
        appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())

        // Call getAppCheckToken
        try {
            val task = appCheck.getAppCheckToken(false)
            // Wait for task or check if determineDebugSecret ran
            Tasks.await(task)
        } catch (e: Exception) {
            println("Token task completed with exception (expected since no network in test): ${e.message}")
        }

        val sharedPrefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
        println("sharedPrefsDir exists: ${sharedPrefsDir.exists()}")
        if (sharedPrefsDir.exists()) {
            sharedPrefsDir.listFiles()?.forEach { file ->
                println("Pref file: ${file.name}")
                val prefName = file.name.removeSuffix(".xml")
                val sp = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                sp.all.forEach { (k, v) ->
                    println("  $k -> $v")
                }
            }
        }
    }
}
