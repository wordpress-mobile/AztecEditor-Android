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
 * Tests for the caption shortcode plugin
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = intArrayOf(25))
class ImageCaptionTest {
    lateinit var editText: AztecText

    private val IMG_HTML = "[caption align=\"alignright\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />Caption[/caption]test<br>test2"
    private val IMG = "${Constants.IMG_CHAR}\nCaption\ntest\ntest2"

    private val predicate = object : AztecText.AttributePredicate {
        override fun matches(attrs: Attributes): Boolean {
            return attrs.getValue("src").startsWith("https://example")
        }
    }

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

    @Test
    @Throws(Exception::class)
    fun testCaptionSplitting() {
        Assert.assertTrue(safeEmpty(editText))

        val html = IMG_HTML
        editText.fromHtml(html)

        val startPosition = editText.text.indexOf("tion")
        editText.text.insert(startPosition, "\n")

        val newText = "${Constants.IMG_CHAR}\nCap\ntion\ntest\ntest2"
        Assert.assertEquals(newText, editText.text.toString())

        val newHtml = "[caption align=\"alignright\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />" +
                "Cap[/caption]tion<br>test<br>test2"

        Assert.assertEquals(newHtml, editText.toPlainHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testCaptionDeletionWithTwoImages() {
        Assert.assertTrue(safeEmpty(editText))

        val html = "[caption align=\"alignright\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />Caption[/caption]"
        editText.fromHtml(html + html)

        val secondImagePosition = editText.text.indexOf(Constants.IMG_CHAR, 1)
        editText.text.delete(secondImagePosition - 1, secondImagePosition)

        val newText = "${Constants.IMG_CHAR}\nCaptio\n${Constants.IMG_CHAR}\nCaption"
        Assert.assertEquals(newText, editText.text.toString())

        val newHtml = "[caption align=\"alignright\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />Captio[/caption]" +
                "[caption align=\"alignright\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />Caption[/caption]"

        Assert.assertEquals(newHtml, editText.toPlainHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testCaptionPresenceAndRemoval() {
        Assert.assertTrue(safeEmpty(editText))

        val html = IMG_HTML
        editText.fromHtml(html)

        Assert.assertTrue(editText.hasImageCaption(predicate))

        editText.removeImageCaption(predicate)

        Assert.assertFalse(editText.hasImageCaption(predicate))

        val newText = "${Constants.IMG_CHAR}\ntest\ntest2"
        Assert.assertEquals(newText, editText.text.toString())

        val newHtml = "<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />test<br>test2"
        Assert.assertEquals(newHtml, editText.toPlainHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testCaptionGettingAndChanging() {
        Assert.assertTrue(safeEmpty(editText))

        val html = IMG_HTML
        editText.fromHtml(html)

        val caption = editText.getImageCaption(predicate)
        Assert.assertEquals("Caption", caption)

        val newCaption = "new caption"
        editText.setImageCaption(predicate, newCaption)

        val changedCaption = editText.getImageCaption(predicate)
        Assert.assertEquals(newCaption, changedCaption)
    }

    @Test
    @Throws(Exception::class)
    fun testGettingCaptionAttributes() {
        Assert.assertTrue(safeEmpty(editText))

        val html = IMG_HTML
        editText.fromHtml(html)

        val attrs = editText.getImageCaptionAttributes(predicate)

        Assert.assertNotNull(attrs)
        Assert.assertEquals("alignright", attrs.getValue("align"))
        Assert.assertNull(attrs.getValue("aaa"))
    }

    @Test
    @Throws(Exception::class)
    fun testSettingCaptionWithAttributes() {
        Assert.assertTrue(safeEmpty(editText))

        val html = IMG_HTML
        editText.fromHtml(html)

        val attrs = editText.getImageCaptionAttributes(predicate)
        attrs.setValue("width", "100")

        val newCaption = "test caption"
        editText.setImageCaption(predicate, newCaption, attrs)

        val changedCaption = editText.getImageCaption(predicate)
        Assert.assertEquals(newCaption, changedCaption)

        val changedHtml = "[caption width=\"100\" align=\"alignright\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />$newCaption[/caption]test<br>test2"
        Assert.assertEquals(changedHtml, editText.toPlainHtml())

        editText.removeImageCaption(predicate)
        Assert.assertFalse(editText.hasImageCaption(predicate))

        val removedAttrs = editText.getImageCaptionAttributes(predicate)
        Assert.assertTrue(removedAttrs.isEmpty())

        val differentAttrs = AztecAttributes()
        differentAttrs.setValue("width", "99")
        editText.setImageCaption(predicate, newCaption, differentAttrs)

        val newAttrs = editText.getImageCaptionAttributes(predicate)
        Assert.assertNotNull(newAttrs)
        Assert.assertEquals("99", newAttrs.getValue("width"))
        Assert.assertNull(newAttrs.getValue("align"))
        Assert.assertNull(newAttrs.getValue("aaa"))
    }

    @Test
    @Throws(Exception::class)
    fun testEmptyCaptionRemoval() {
        Assert.assertTrue(safeEmpty(editText))

        val html = IMG_HTML
        editText.fromHtml(html)

        editText.text.delete(2, 9)
        Assert.assertEquals(editText.toPlainHtml(), "<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" /><br>test<br>test2")
    }

    @Test
    @Throws(Exception::class)
    fun testEmptyHtmlCaption() {
        Assert.assertTrue(safeEmpty(editText))

        val html = IMG_HTML.replace("Caption", "")
        editText.fromHtml(html)
        Assert.assertEquals(editText.toPlainHtml(), "<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />test<br>test2")
    }

    @Test
    @Throws(Exception::class)
    fun testCaptionWithLinebreaks() {
        Assert.assertTrue(safeEmpty(editText))

        val html = "[caption align=\"alignright\" width=\"100\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" /><br>Cap<br>tion<br>[/caption]test<br>test2"
        val expectedHtml = "[caption width=\"100\" align=\"alignright\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />Cap tion[/caption]test<br>test2"
        editText.fromHtml(html)
        Assert.assertEquals(editText.toPlainHtml(), expectedHtml)
    }

    @Test
    @Throws(Exception::class)
    fun testCaptionRemovalWhenNewlineAfterImageGetsDeleted() {
        Assert.assertTrue(safeEmpty(editText))

        val html = IMG_HTML
        val expectedHtml = "<br>test<br>test2"
        editText.fromHtml(html)
        editText.text.delete(1, 2)
        Assert.assertEquals(editText.toPlainHtml(), expectedHtml)
    }

    @Test
    @Throws(Exception::class)
    fun testCaptionRemovalWhenImageGetsDeleted() {
        Assert.assertTrue(safeEmpty(editText))

        val html = IMG_HTML
        val expectedHtml = "<br>test<br>test2"
        editText.fromHtml(html)
        editText.text.delete(0, 1)
        Assert.assertEquals(editText.toPlainHtml(), expectedHtml)
    }

    @Test
    @Throws(Exception::class)
    fun testCaptionRemovalWhenImageWithCaptionGetDeleted() {
        Assert.assertTrue(safeEmpty(editText))

        val html = IMG_HTML
        val expectedHtml = "<br>test<br>test2"
        editText.fromHtml(html)
        editText.text.delete(0, 4)
        Assert.assertEquals(editText.toPlainHtml(), expectedHtml)
    }
}
