package org.wordpress.aztec

import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


class AztecTestRunner(testClass: Class<*>?) : RobolectricTestRunner(testClass) {

    override fun buildGlobalConfig(): Config {
        return Config.Builder()
                .setMinSdk(23)
                .setMaxSdk(25)
                .setPackageName("org.wordpress.aztec")
                .setConstants(BuildConfig::class.java)
                .build()
    }

}