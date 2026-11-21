package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.debug.internal.StorageHelper
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class StorageHelperDirectTest {
    @Test
    fun testStorageHelper() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        val app = FirebaseApp.getInstance()
        val storageHelper = StorageHelper(context, app.persistenceKey)
        val initialSecret = storageHelper.retrieveDebugSecret()
        println("Initial secret in storageHelper: $initialSecret")

        val secretToUse = if (initialSecret != null) {
            initialSecret
        } else {
            val generated = java.util.UUID.randomUUID().toString()
            storageHelper.saveDebugSecret(generated)
            generated
        }
        println("Secret to use: $secretToUse")
        val retrievedAfter = storageHelper.retrieveDebugSecret()
        println("Retrieved after: $retrievedAfter")
        assertNotNull(retrievedAfter)
    }
}
