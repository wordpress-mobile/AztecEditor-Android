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
import org.wordpress.aztec.Constants
import org.wordpress.aztec.plugins.shortcodes.TestUtils.safeEmpty

/**
 * Tests for the caption shortcode plugin
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(25))
class ImageCaptionTest {
    lateinit var editText: AztecText

    private val IMG_HTML = "[caption align=\"alignright\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />Caption[/caption]test<br>test2"
    private val IMG = "${Constants.IMG_CHAR}\nCaption\ntest\ntest2"

    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().visible().get()

        editText = AztecText(activity)
        editText.setCalypsoMode(false)

        activity.setContentView(editText)

        editText.plugins.add(CaptionShortcodePlugin(editText))
    }

    @Test
    @Throws(Exception::class)
    fun testTextInsertionBeforeImage() {
        Assert.assertTrue(safeEmpty(editText))

        val html = IMG_HTML
        editText.fromHtml(html)

        Assert.assertEquals(editText.text.toString(), IMG)

        editText.setSelection(0)
        editText.text.insert(0, "word")

        Assert.assertEquals("word\n" + IMG, editText.text.toString())
        Assert.assertEquals("word" + IMG_HTML, editText.toPlainHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testTextInsertionAfterImage() {
        Assert.assertTrue(safeEmpty(editText))

        val html = IMG_HTML
        editText.fromHtml(html)

        val imagePosition = editText.text.indexOf(Constants.IMG_CHAR)
        editText.text.insert( imagePosition + 1, "word")

        val newText = "${Constants.IMG_CHAR}\nCaption\nword\ntest\ntest2"
        Assert.assertEquals(newText, editText.text.toString())

        val newHtml = "[caption align=\"alignright\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />" +
                "Caption[/caption]word<br>test<br>test2"

        Assert.assertEquals(newHtml, editText.toPlainHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testDeletingCharactersAboveImage() {
        Assert.assertTrue(safeEmpty(editText))

        val html = "start" + IMG_HTML
        editText.fromHtml(html)

        val imagePosition = editText.text.indexOf(Constants.IMG_CHAR)

        val text = "start\n${Constants.IMG_CHAR}\nCaption\ntest\ntest2"
        Assert.assertEquals(text, editText.text.toString())

        editText.text.delete(imagePosition - 1, imagePosition)

        val newText = "star\n${Constants.IMG_CHAR}\nCaption\ntest\ntest2"
        Assert.assertEquals(newText, editText.text.toString())

        Assert.assertEquals(editText.selectionStart, 4)

        val newHtml = "star[caption align=\"alignright\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />" +
                "Caption[/caption]test<br>test2"

        Assert.assertEquals(newHtml, editText.toPlainHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testDeletingEmptyLineAboveImage() {
        Assert.assertTrue(safeEmpty(editText))

        val html = "<br>" + IMG_HTML
        editText.fromHtml(html)

        val imagePosition = editText.text.indexOf(Constants.IMG_CHAR)

        val text = "\n${Constants.IMG_CHAR}\nCaption\ntest\ntest2"
        Assert.assertEquals(text, editText.text.toString())

        editText.text.delete(imagePosition - 1, imagePosition)

        val newText = "${Constants.IMG_CHAR}\nCaption\ntest\ntest2"
        Assert.assertEquals(newText, editText.text.toString())

        Assert.assertEquals(editText.selectionStart, 0)

        val newHtml = "[caption align=\"alignright\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />" +
                "Caption[/caption]test<br>test2"

        Assert.assertEquals(newHtml, editText.toPlainHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testDeletingLastCharacterAboveImage() {
        Assert.assertTrue(safeEmpty(editText))

        val html = "a" + IMG_HTML
        editText.fromHtml(html)

        val imagePosition = editText.text.indexOf(Constants.IMG_CHAR)

        val text = "a\n${Constants.IMG_CHAR}\nCaption\ntest\ntest2"
        Assert.assertEquals(text, editText.text.toString())

        editText.text.delete(imagePosition - 1, imagePosition)

        val newText = "${Constants.IMG_CHAR}\nCaption\ntest\ntest2"
        Assert.assertEquals(newText, editText.text.toString())

        Assert.assertEquals(editText.selectionStart, 0)

        val newHtml = "[caption align=\"alignright\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />" +
                "Caption[/caption]test<br>test2"

        Assert.assertEquals(newHtml, editText.toPlainHtml())
    }
}
