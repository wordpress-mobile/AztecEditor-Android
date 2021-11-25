package org.wordpress.aztec

import android.app.Activity
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.wordpress.aztec.spans.AztecCodeSpan
import org.wordpress.aztec.spans.AztecStyleStrongSpan
import org.wordpress.aztec.spans.AztecURLSpan
import org.wordpress.aztec.spans.IAztecInlineSpan

@RunWith(RobolectricTestRunner::class)
@Config(sdk = intArrayOf(23))
class InlineElementsTest {

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
    fun preserveLinkTagOrder() {
        editText.fromHtml("<p>This is a <a href=\"http://wordpress.com\"><strong>link</strong></a></p>")
        val spans = editText.text.getSpans(0, editText.length(), IAztecInlineSpan::class.java)
        Assert.assertTrue(spans[0] is AztecURLSpan)
        Assert.assertTrue(spans[1] is AztecStyleStrongSpan)
    }

    @Test
    @Throws(Exception::class)
    fun preserveCodeTagOrder() {
        editText.fromHtml("<p>This is a <code><strong>some code</strong></code></p>")
        val spans = editText.text.getSpans(0, editText.length(), IAztecInlineSpan::class.java)
        Assert.assertTrue(spans[0] is AztecCodeSpan)
        Assert.assertTrue(spans[1] is AztecStyleStrongSpan)
    }
}
