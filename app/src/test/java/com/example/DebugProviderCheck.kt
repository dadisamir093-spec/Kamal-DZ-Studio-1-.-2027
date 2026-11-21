package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DebugProviderCheck {
    @Test
    fun checkPrefs() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        val app = FirebaseApp.getInstance()
        val factory = DebugAppCheckProviderFactory.getInstance()
        val provider = factory.create(app)
        println("Provider created: $provider (${provider.javaClass.name})")

        // List all shared preferences files
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
