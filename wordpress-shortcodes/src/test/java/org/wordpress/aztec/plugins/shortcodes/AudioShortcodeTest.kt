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
 * Tests for the audio shortcode plugin
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = intArrayOf(25))
class AudioShortcodeTest {
    lateinit var editText: AztecText

    private val htmlGutenbergAudioBlock =
            "<!-- wp:audio {\"id\":435} --><figure class=\"wp-block-audio\"><audio controls src=\"https://selfhostedmario.mystagingwebsite.com/wp-content/uploads/2018/05/ArgentinaAnthem.mp3\"></audio><figcaption>a caption</figcaption></figure><!-- /wp:audio -->"

    private val htmlNormalAudioTag =
            "<figure class=\"wp-block-audio\"><audio controls src=\"https://selfhostedmario.mystagingwebsite.com/wp-content/uploads/2018/05/ArgentinaAnthem.mp3\"></audio><figcaption>a caption</figcaption></figure>"

    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().visible().get()

        editText = AztecText(activity)
        editText.setCalypsoMode(false)

        activity.setContentView(editText)

        editText.plugins.add(AudioShortcodePlugin())
    }

    @Test
    @Throws(Exception::class)
    fun testGutenbergAudioBlockDoesntConvertToShortcode() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(htmlGutenbergAudioBlock)

        // expected result: the <audio> tag does not get converted to shortcode when it's found within a Gutenberg block
        // Note: the slight difference of <audio controls> being converted to <audio controls="controls"> should be equivalent
        val htmlWithoutShortcode = "<!-- wp:audio {\"id\":435} --><figure class=\"wp-block-audio\"><audio controls=\"controls\" src=\"https://selfhostedmario.mystagingwebsite.com/wp-content/uploads/2018/05/ArgentinaAnthem.mp3\"></audio><figcaption>a caption</figcaption></figure><!-- /wp:audio -->"

        Assert.assertEquals(htmlWithoutShortcode, editText.toPlainHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testAudioTagConvertsToShortcode() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(htmlNormalAudioTag)

        // expected result: the <audio> tag gets converted to shortcode when it's found in free HTML
        val htmlWithShortcode = "<figure class=\"wp-block-audio\">[audio controls=\"controls\" src=\"https://selfhostedmario.mystagingwebsite.com/wp-content/uploads/2018/05/ArgentinaAnthem.mp3\"]<figcaption>a caption</figcaption></figure>"

        Assert.assertEquals(htmlWithShortcode, editText.toPlainHtml())
    }
}
