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
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.plugins.shortcodes.TestUtils.safeEmpty
import org.wordpress.aztec.plugins.shortcodes.extensions.getImageCaption
import org.wordpress.aztec.plugins.shortcodes.extensions.getImageCaptionAttributes
import org.wordpress.aztec.plugins.shortcodes.extensions.hasImageCaption
import org.wordpress.aztec.plugins.shortcodes.extensions.removeImageCaption
import org.wordpress.aztec.plugins.shortcodes.extensions.setImageCaption
import org.xml.sax.Attributes

/**
 * Tests for the audio shortcode plugin
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(25))
class AudioShortcodeTest {
    lateinit var editText: AztecText

    private val GB_AUDIO_BLOCK =
            "<!-- wp:audio {\"id\":435} --><figure class=\"wp-block-audio\"><audio controls src=\"https://selfhostedmario.mystagingwebsite.com/wp-content/uploads/2018/05/ArgentinaAnthem.mp3\"></audio><figcaption>a caption</figcaption></figure><!-- /wp:audio -->"

    private val AUDIO_HTML =
            "<audio controls src=\"https://selfhostedmario.mystagingwebsite.com/wp-content/uploads/2018/05/ArgentinaAnthem.mp3\"></audio>"

    private val AUDIO_SHORTCODE_OUTPUT =
            "[audio controls=\"controls\" src=\"https://selfhostedmario.mystagingwebsite.com/wp-content/uploads/2018/05/ArgentinaAnthem.mp3\"]"
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
    fun testRetainAudioTagByDisablingAudioShortcodePlugin() {
        Assert.assertTrue(safeEmpty(editText))

        val html = AUDIO_HTML
        editText.fromHtml(html)
        Assert.assertEquals(editText.text.toString(), AUDIO_HTML)

        // now add plugin
        editText.plugins.add(AudioShortcodePlugin())
        editText.fromHtml(html)
        Assert.assertEquals(editText.text.toString(), AUDIO_SHORTCODE_OUTPUT)
    }
}
