package org.wordpress.aztec.placeholders

import android.app.Activity
import android.view.MenuItem
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.PopupMenu
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.source.SourceViewEditText
import org.wordpress.aztec.toolbar.AztecToolbar

@RunWith(RobolectricTestRunner::class)
class PlaceholderTest {
    lateinit var container: FrameLayout
    lateinit var editText: AztecText
    lateinit var menuList: PopupMenu
    lateinit var menuListOrdered: MenuItem
    lateinit var menuListUnordered: MenuItem
    lateinit var sourceText: SourceViewEditText
    lateinit var toolbar: AztecToolbar
    lateinit var placeholderManager: PlaceholderManager

    private val uuid: String = "uuid123"

    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().visible().get()
        container = FrameLayout(activity)
        editText = AztecText(activity)
        container.addView(editText, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
        placeholderManager = PlaceholderManager(editText, container, generateUuid = {
            uuid
        })
        placeholderManager.registerAdapter(ImageWithCaptionAdapter())
        editText.setCalypsoMode(false)
        editText.addMediaAfterBlocks()
        editText.plugins.add(placeholderManager)
        sourceText = SourceViewEditText(activity)
        sourceText.setCalypsoMode(false)
        toolbar = AztecToolbar(activity)
        toolbar.setEditor(editText, sourceText)
        menuList = toolbar.getListMenu() as PopupMenu
        menuListOrdered = menuList.menu.getItem(1)
        menuListUnordered = menuList.menu.getItem(0)
        activity.setContentView(container)
    }

    @Test
    @Throws(Exception::class)
    fun addAndRemovePlaceholderAtTheBeginning() {
        runBlocking {
            val initialHtml = "<p>Line 1</p>"
            editText.fromHtml(initialHtml)

            editText.setSelection(0)
            ImageWithCaptionAdapter.insertImageWithCaption(placeholderManager, "image.jpg", "Caption 123")

            Assert.assertEquals("<placeholder uuid=\"$uuid\" type=\"image_with_caption\" src=\"image.jpg\" caption=\"Caption 123\" /><p>Line 1</p>", editText.toHtml())

            placeholderManager.removeItem {
                it.getValue("uuid") == uuid
            }

            Assert.assertEquals(initialHtml, editText.toHtml())
        }
    }

    @Test
    @Throws(Exception::class)
    fun addAndRemovePlaceholderAtTheEndOfLine() {
        runBlocking {
            val initialHtml = "<p>Line 123</p><p>Line 2</p>"
            editText.fromHtml(initialHtml)

            editText.setSelection(editText.editableText.indexOf("1"))
            ImageWithCaptionAdapter.insertImageWithCaption(placeholderManager, "image.jpg", "Caption 123")

            Assert.assertEquals("<p>Line 123<placeholder uuid=\"uuid123\" type=\"image_with_caption\" src=\"image.jpg\" caption=\"Caption 123\" /></p><p>Line 2</p>", editText.toHtml())

            placeholderManager.removeItem {
                it.getValue("uuid") == uuid
            }

            Assert.assertEquals(initialHtml, editText.toHtml())
        }
    }

    @Test
    @Throws(Exception::class)
    fun insertOrUpdateAPlaceholderAtTheBeginning() {
        runBlocking {
            val initialHtml = "<p>Line 1</p>"
            editText.fromHtml(initialHtml)

            editText.setSelection(0)
            ImageWithCaptionAdapter.insertImageWithCaption(placeholderManager, "image.jpg", "Caption 1")
            ImageWithCaptionAdapter.insertImageWithCaption(placeholderManager, "image.jpg", "Caption 2")

            Assert.assertEquals("${placeholderWithCaption("Caption 2 - Caption 1")}<p>Line 1</p>", editText.toHtml())

            placeholderManager.removeItem {
                it.getValue("uuid") == uuid
            }

            Assert.assertEquals(initialHtml, editText.toHtml())
        }
    }

    @Test
    @Throws(Exception::class)
    fun insertOrUpdateAPlaceholderWhenInsertingBeforeNewLine() {
        runBlocking {
            val initialHtml = "<p>Line 1</p>${placeholderWithCaption("First")}<p>Line 2</p>"
            editText.fromHtml(initialHtml)

            editText.setSelection(editText.editableText.indexOf("1"))
            ImageWithCaptionAdapter.insertImageWithCaption(placeholderManager, "image.jpg", "Second")

            Assert.assertEquals("<p>Line 1</p>${placeholderWithCaption("Second - First")}<p>Line 2</p>", editText.toHtml())
        }
    }

    @Test
    @Throws(Exception::class)
    fun insertOrUpdateAPlaceholderWhenInsertingRightBefore() {
        runBlocking {
            val initialHtml = "<p>Line 1</p>${placeholderWithCaption("First")}<p>Line 2</p>"
            editText.fromHtml(initialHtml)

            editText.setSelection(editText.editableText.indexOf("1") + 1)
            ImageWithCaptionAdapter.insertImageWithCaption(placeholderManager, "image.jpg", "Second")

            Assert.assertEquals("<p>Line 1</p>${placeholderWithCaption("Second - First")}<p>Line 2</p>", editText.toHtml())
        }
    }

    private fun placeholderWithCaption(caption: String): String {
        return "<placeholder src=\"image.jpg\" caption=\"$caption\" uuid=\"uuid123\" type=\"image_with_caption\" />"
    }
}
