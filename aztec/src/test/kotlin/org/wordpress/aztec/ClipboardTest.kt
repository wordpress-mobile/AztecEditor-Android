package org.wordpress.aztec

import android.app.Activity
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(23))
class ClipboardTest {

    lateinit var editText: AztecText

    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().visible().get()
        editText = AztecText(activity)
        editText.setCalypsoMode(false)
        activity.setContentView(editText)
    }

    @Test
    @Throws(Exception::class)
    fun copyAndPasteSameInlineStyle() {
        editText.fromHtml("<b>Bold</b>")

        editText.setSelection(0, editText.length())
        TestUtils.copyToClipboard(editText)

        editText.setSelection(editText.length())
        TestUtils.pasteFromClipboard(editText)

        Assert.assertEquals("<b>Bold</b><b>Bold</b>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun copyAndPasteDifferentInlineStyle() {
        editText.fromHtml("<b>Bold</b><i>Italic</i>")

        editText.setSelection(0, editText.length())
        TestUtils.copyToClipboard(editText)

        editText.setSelection(editText.length())
        TestUtils.pasteFromClipboard(editText)

        Assert.assertEquals("<b>Bold</b><i>Italic</i><b>Bold</b><i>Italic</i>", editText.toHtml())
    }
}