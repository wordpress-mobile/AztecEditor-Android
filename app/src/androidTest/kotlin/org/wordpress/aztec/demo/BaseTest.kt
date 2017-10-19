package org.wordpress.aztec.demo

import android.support.test.rule.GrantPermissionRule
import android.support.test.runner.AndroidJUnit4
import com.xamarin.testcloud.espresso.Factory
import com.xamarin.testcloud.espresso.ReportHelper
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
abstract class BaseTest {

    @Rule
    @JvmField
    val mReportHelper: ReportHelper = Factory.getReportHelper()

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    companion object {
        private lateinit var reportHelper: ReportHelper

        fun label(label: String) {
            reportHelper.label(label)
        }
    }

    init {
        BaseTest.reportHelper = mReportHelper
    }

    @Before
    fun SetUp() {
        reportHelper.label("Starting App")
    }
}
