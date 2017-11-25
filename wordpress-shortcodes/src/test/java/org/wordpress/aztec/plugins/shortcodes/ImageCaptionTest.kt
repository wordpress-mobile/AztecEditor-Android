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
import org.wordpress.aztec.plugins.shortcodes.TestUtils.backspaceAt
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

        val text = "start\n${Constants.IMG_CHAR}\nCaption\ntest\ntest2"
        Assert.assertEquals(text, editText.text.toString())

        val imagePosition = editText.text.indexOf(Constants.IMG_CHAR)
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

        val text = "\n${Constants.IMG_CHAR}\nCaption\ntest\ntest2"
        Assert.assertEquals(text, editText.text.toString())

        val imagePosition = editText.text.indexOf(Constants.IMG_CHAR)
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

        val text = "a\n${Constants.IMG_CHAR}\nCaption\ntest\ntest2"
        Assert.assertEquals(text, editText.text.toString())

        val imagePosition = editText.text.indexOf(Constants.IMG_CHAR)
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
    fun testNewlineRightAfterImageFollowedByText() {
        Assert.assertTrue(safeEmpty(editText))

        val html = IMG_HTML
        editText.fromHtml(html)

        val imagePosition = editText.text.indexOf(Constants.IMG_CHAR)
        editText.text.insert(imagePosition + 1, "\n")

        val newText = "${Constants.IMG_CHAR}\nCaption\n\ntest\ntest2"
        Assert.assertEquals(newText, editText.text.toString())

        Assert.assertEquals(editText.selectionStart, 10)

        val newHtml = "[caption align=\"alignright\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />" +
                "Caption[/caption]<br>test<br>test2"

        Assert.assertEquals(newHtml, editText.toPlainHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testNewlineRightAfterImage() {
        Assert.assertTrue(safeEmpty(editText))

        val html = "[caption align=\"alignright\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />" +
                "Caption[/caption]"
        editText.fromHtml(html)

        val imagePosition = editText.text.indexOf(Constants.IMG_CHAR)
        editText.text.insert(imagePosition + 1, "\n")

        val newText = "${Constants.IMG_CHAR}\nCaption\n" + Constants.END_OF_BUFFER_MARKER
        Assert.assertEquals(newText, editText.text.toString())

        Assert.assertEquals(editText.selectionStart, 10)

        val newHtml = "[caption align=\"alignright\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />" +
                "Caption[/caption]"

        Assert.assertEquals(newHtml, editText.toPlainHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testNewlineRightBeforeCaptionFollowedByText() {
        Assert.assertTrue(safeEmpty(editText))

        val html = IMG_HTML
        editText.fromHtml(html)

        val startPosition = editText.text.indexOf(Constants.IMG_CHAR)
        editText.text.insert(startPosition + 2, "\n")

        val newText = "${Constants.IMG_CHAR}\nCaption\n\ntest\ntest2"
        Assert.assertEquals(newText, editText.text.toString())

        Assert.assertEquals(editText.selectionStart, 10)

        val newHtml = "[caption align=\"alignright\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />" +
                "Caption[/caption]<br>test<br>test2"

        Assert.assertEquals(newHtml, editText.toPlainHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testNewlineRightBeforeCaption() {
        Assert.assertTrue(safeEmpty(editText))

        val html = "[caption align=\"alignright\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />" +
                "Caption[/caption]"
        editText.fromHtml(html)

        val startPosition = editText.text.indexOf(Constants.IMG_CHAR)
        editText.text.insert(startPosition + 2, "\n")

        val newText = "${Constants.IMG_CHAR}\nCaption\n" + Constants.END_OF_BUFFER_MARKER
        Assert.assertEquals(newText, editText.text.toString())

        Assert.assertEquals(editText.selectionStart, 10)

        val newHtml = "[caption align=\"alignright\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />" +
                "Caption[/caption]"

        Assert.assertEquals(newHtml, editText.toPlainHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testNewlineRightBeforeImageFollowedByText() {
        Assert.assertTrue(safeEmpty(editText))

        val html = IMG_HTML
        editText.fromHtml(html)

        val imagePosition = editText.text.indexOf(Constants.IMG_CHAR)
        editText.text.insert(imagePosition, "\n")

        val newText = "\n${Constants.IMG_CHAR}\nCaption\ntest\ntest2"
        Assert.assertEquals(newText, editText.text.toString())

        Assert.assertEquals(editText.selectionStart, 1)

        val newHtml = "<br>[caption align=\"alignright\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />" +
                "Caption[/caption]test<br>test2"

        Assert.assertEquals(newHtml, editText.toPlainHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testNewlineRightAfterCaptionFollowedByText() {
        Assert.assertTrue(safeEmpty(editText))

        val html = IMG_HTML
        editText.fromHtml(html)

        val startPosition = editText.text.indexOf("test") - 1
        editText.text.insert(startPosition, "\n")

        val newText = "${Constants.IMG_CHAR}\nCaption\n\ntest\ntest2"
        Assert.assertEquals(newText, editText.text.toString())

        val newHtml = "[caption align=\"alignright\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />" +
                "Caption[/caption]<br>test<br>test2"

        Assert.assertEquals(newHtml, editText.toPlainHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testNewlineRightAfterCaption() {
        Assert.assertTrue(safeEmpty(editText))

        val html = "[caption align=\"alignright\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />" +
                "Caption[/caption]"
        editText.fromHtml(html)

        val startPosition = editText.text.length
        editText.text.insert(startPosition, "\n")

        val newText = "${Constants.IMG_CHAR}\nCaption\n" + Constants.END_OF_BUFFER_MARKER
        Assert.assertEquals(newText, editText.text.toString())

        val newHtml = "[caption align=\"alignright\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />" +
                "Caption[/caption]"

        Assert.assertEquals(newHtml, editText.toPlainHtml())
    }


    @Test
    @Throws(Exception::class)
    fun testMergeOfLineBelowCaption() {
        Assert.assertTrue(safeEmpty(editText))

        val html = IMG_HTML
        editText.fromHtml(html)

        val startPosition = editText.text.indexOf("test")
        editText.text.delete(startPosition - 1, startPosition)

        val newText = "${Constants.IMG_CHAR}\nCaptiontest\ntest2"
        Assert.assertEquals(newText, editText.text.toString())

        val newHtml = "[caption align=\"alignright\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />" +
                "Captiontest[/caption]test2"

        Assert.assertEquals(newHtml, editText.toPlainHtml())
    }
}
