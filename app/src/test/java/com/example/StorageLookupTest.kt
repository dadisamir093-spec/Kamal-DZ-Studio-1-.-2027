package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class StorageLookupTest {
    @Test
    fun testLookup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        val app = FirebaseApp.getInstance()
        println("FirebaseApp name: ${app.name}")
        println("FirebaseApp persistenceKey: ${app.persistenceKey}")
        val expectedPrefName = "com.google.firebase.appcheck.debug.store.${app.persistenceKey}"
        println("Expected pref name: $expectedPrefName")
    }
}
