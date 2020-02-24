@file:Suppress("DEPRECATION")

package org.wordpress.aztec.plugins.shortcodes

import android.app.Activity
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.plugins.shortcodes.TestUtils.safeEmpty

/**
 * Tests for the video shortcode plugin
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = intArrayOf(25))
class VideoShortcodeTest {
    lateinit var editText: AztecText

    private val htmlGutenbergVideoBlock =
            "<!-- wp:video {\"id\":25} --><figure class=\"wp-block-video\"><video controls src=\"https://somedomain.com/wp-content/uploads/2017/05/VID-20170508-WA0011-1.mp4\"></video></figure><!-- /wp:video -->"

    private val htmlNormalVideoTag =
            "<figure class=\"wp-block-video\"><video controls src=\"https://somedomain.com/wp-content/uploads/2017/05/VID-20170508-WA0011-1.mp4\"></video></figure>"

    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().visible().get()

        editText = AztecText(activity)
        editText.setCalypsoMode(false)

        activity.setContentView(editText)

        editText.plugins.add(VideoShortcodePlugin())
    }

    @Test
    @Throws(Exception::class)
    fun testGutenbergVideoBlockDoesntConvertToShortcode() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(htmlGutenbergVideoBlock)

        // expected result: the <video> tag does not get converted to shortcode when it's found within a Gutenberg block
        // Note: the slight difference of <video controls> being converted to <video controls="controls"> should be equivalent
        val htmlWithoutShortcode = "<!-- wp:video {\"id\":25} --><figure class=\"wp-block-video\"><video controls=\"controls\" src=\"https://somedomain.com/wp-content/uploads/2017/05/VID-20170508-WA0011-1.mp4\"></video></figure><!-- /wp:video -->"

        Assert.assertEquals(htmlWithoutShortcode, editText.toPlainHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testVideoTagConvertsToShortcode() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(htmlNormalVideoTag)

        // expected result: the <video> tag gets converted to shortcode when it's found in free HTML
        val htmlWithShortcode = "<figure class=\"wp-block-video\">[video controls=\"controls\" src=\"https://somedomain.com/wp-content/uploads/2017/05/VID-20170508-WA0011-1.mp4\"]</figure>"

        Assert.assertEquals(htmlWithShortcode, editText.toPlainHtml())
    }
}
