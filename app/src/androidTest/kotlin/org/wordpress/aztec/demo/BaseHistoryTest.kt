package org.wordpress.aztec.demo

import androidx.test.rule.ActivityTestRule
import org.junit.Before
import org.junit.Rule
import org.wordpress.aztec.AztecText

/**
 * Base class for History testing.
 */
abstract class BaseHistoryTest : BaseTest() {

    protected val throttleTime: Long = 1000L

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    /**
     * Increases the history time to cover test device variability.
     */
    @Before
    fun init() {
        val aztecText = mActivityTestRule.activity.findViewById<AztecText>(R.id.aztec)
        aztecText.history.historyThrottleTime = throttleTime
    }
}
