package org.wordpress.aztec

import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


class AztecTestRunner(testClass: Class<*>?) : RobolectricTestRunner(testClass) {

    override fun buildGlobalConfig(): Config {
        return Config.Builder().setMinSdk(14).setMaxSdk(25).setConstants(BuildConfig::class.java).build()
    }

}