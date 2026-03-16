@file:Suppress("DEPRECATION")

package org.wordpress.aztec.demo

import android.Manifest.permission.CAMERA
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
abstract class BaseTest {

    @Before
    fun grantPermissions() {
        val (uiAutomation, packageName) = with(InstrumentationRegistry.getInstrumentation()) {
            Pair(uiAutomation, targetContext.packageName)
        }
        uiAutomation.grantRuntimePermission(packageName, CAMERA)
    }

    companion object {
        fun label(label: String) {
            Log.d("BaseTest", label)
        }
    }
}
