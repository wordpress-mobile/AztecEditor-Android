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

    private val uuid1: String = "uuid1"
    private val uuid2: String = "uuid2"

    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().visible().get()
        container = FrameLayout(activity)
        editText = AztecText(activity)
        container.addView(editText, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
        var counter = 0
        placeholderManager = PlaceholderManager(editText, container, generateUuid = {
            listOf(uuid1, uuid2)[counter++]
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

            Assert.assertEquals("<placeholder uuid=\"$uuid1\" type=\"image_with_caption\" src=\"image.jpg\" caption=\"Caption 123\" /><p>Line 1</p>", editText.toHtml())

            placeholderManager.removeItem {
                it.getValue("uuid") == uuid1
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

            Assert.assertEquals("<p>Line 123<placeholder uuid=\"uuid1\" type=\"image_with_caption\" src=\"image.jpg\" caption=\"Caption 123\" /></p><p>Line 2</p>", editText.toHtml())

            placeholderManager.removeItem {
                it.getValue("uuid") == uuid1
            }

            Assert.assertEquals(initialHtml, editText.toHtml())
        }
    }

    @Test
    @Throws(Exception::class)
    fun updatePlaceholderAtTheBeginning() {
        runBlocking {
            val initialHtml = "<p>Line 1</p>"
            editText.fromHtml(initialHtml)

            editText.setSelection(0)
            ImageWithCaptionAdapter.insertImageWithCaption(placeholderManager, "image.jpg", "Caption 1")
            ImageWithCaptionAdapter.insertImageWithCaption(placeholderManager, "image.jpg", "Caption 2")

            Assert.assertEquals("${placeholderWithCaption("Caption 1 - Caption 2")}<p>Line 1</p>", editText.toHtml())

            placeholderManager.removeItem {
                it.getValue("uuid") == uuid1
            }

            Assert.assertEquals(initialHtml, editText.toHtml())
        }
    }

    @Test
    @Throws(Exception::class)
    fun doNotUpdatePlaceholderAtTheBeginningWhenMergeDisabled() {
        runBlocking {
            val initialHtml = "<p>Line 1</p>"
            editText.fromHtml(initialHtml)

            editText.setSelection(0)
            ImageWithCaptionAdapter.insertImageWithCaption(placeholderManager, "image.jpg", "Caption 1")
            ImageWithCaptionAdapter.insertImageWithCaption(placeholderManager, "image.jpg", "Caption 2", shouldMergePlaceholders = false)

            Assert.assertEquals("<placeholder uuid=\"uuid1\" type=\"image_with_caption\" src=\"image.jpg\" caption=\"Caption 1\" /><br><placeholder uuid=\"uuid2\" type=\"image_with_caption\" src=\"image.jpg\" caption=\"Caption 2\" /><p>Line 1</p>", editText.toHtml())

            placeholderManager.removeItem {
                it.getValue("uuid") == uuid1
            }

            Assert.assertEquals("<br><placeholder uuid=\"uuid2\" type=\"image_with_caption\" src=\"image.jpg\" caption=\"Caption 2\" /><p>Line 1</p>", editText.toHtml())
        }
    }

    @Test
    @Throws(Exception::class)
    fun updatePlaceholderWhenInsertingBeforeNewLine() {
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
    fun doNotUpdatePlaceholderWhenInsertingBeforeNewLineAndMergeDisabled() {
        runBlocking {
            val initialHtml = "<p>Line 1</p>${placeholderWithCaption("First")}<p>Line 2</p>"
            editText.fromHtml(initialHtml)

            editText.setSelection(editText.editableText.indexOf("1"))
            ImageWithCaptionAdapter.insertImageWithCaption(placeholderManager, "image.jpg", "Second", shouldMergePlaceholders = false)

            Assert.assertEquals("<p>Line 1</p><placeholder uuid=\"uuid2\" type=\"image_with_caption\" src=\"image.jpg\" caption=\"Second\" /><br><placeholder src=\"image.jpg\" caption=\"First\" uuid=\"uuid1\" type=\"image_with_caption\" /><p>Line 2</p>", editText.toHtml())
        }
    }

    @Test
    @Throws(Exception::class)
    fun updatePlaceholderWhenInsertingRightBefore() {
        runBlocking {
            val initialHtml = "<p>Line 1</p>${placeholderWithCaption("First")}<p>Line 2</p>"
            editText.fromHtml(initialHtml)

            editText.setSelection(editText.editableText.indexOf("1") + 1)
            ImageWithCaptionAdapter.insertImageWithCaption(placeholderManager, "image.jpg", "Second")

            Assert.assertEquals("<p>Line 1</p>${placeholderWithCaption("Second - First")}<p>Line 2</p>", editText.toHtml())
        }
    }

    @Test
    @Throws(Exception::class)
    fun doNotUpdatePlaceholderWhenInsertingRightBeforeAndMergeDisabled() {
        runBlocking {
            val initialHtml = "<p>Line 1</p>${placeholderWithCaption("First")}<p>Line 2</p>"
            editText.fromHtml(initialHtml)

            editText.setSelection(editText.editableText.indexOf("1") + 1)
            ImageWithCaptionAdapter.insertImageWithCaption(placeholderManager, "image.jpg", "Second", shouldMergePlaceholders = false)

            Assert.assertEquals("<p>Line 1</p><placeholder uuid=\"uuid2\" type=\"image_with_caption\" src=\"image.jpg\" caption=\"Second\" /><br>${placeholderWithCaption("First")}<p>Line 2</p>", editText.toHtml())
        }
    }

    private fun placeholderWithCaption(caption: String): String {
        return "<placeholder src=\"image.jpg\" caption=\"$caption\" uuid=\"uuid1\" type=\"image_with_caption\" />"
    }

    @Test
    @Throws(Exception::class)
    fun updatePlaceholderWhenItShouldBe() {
        runBlocking {
            val initialHtml = "<placeholder uuid=\"uuid1\" type=\"image_with_caption\" src=\"image.jpg;image2.jpg\" caption=\"Caption - 1, 2\" /><p>Line</p>"
            editText.fromHtml(initialHtml)

            placeholderManager.removeOrUpdate("uuid1", shouldUpdateItem = {
                true
            }) { currentAttributes ->
                val result = mutableMapOf<String, String>()
                result["src"] = currentAttributes["src"]?.split(";")?.firstOrNull() ?: ""
                result["caption"] = "Updated caption"
                result
            }

            Assert.assertEquals("<placeholder src=\"image.jpg\" caption=\"Updated caption\" uuid=\"uuid1\" type=\"image_with_caption\" /><p>Line</p>", editText.toHtml())
        }
    }

    @Test
    @Throws(Exception::class)
    fun updatePlaceholderAtTheEnd() {
        runBlocking {
            val initialHtml = "<p>First Line</p><placeholder uuid=\"uuid1\" type=\"image_with_caption\" src=\"image.jpg;image2.jpg\" caption=\"Caption - 1, 2\" /><p>Second Line</p>"
            editText.fromHtml(initialHtml)
            editText.setSelection(editText.editableText.indexOf("First") + 1)
            val initialSelectionStart = editText.selectionStart
            val initialSelectionEnd = editText.selectionEnd

            placeholderManager.removeOrUpdate("uuid1", shouldUpdateItem = {
                true
            }) { currentAttributes ->
                val result = mutableMapOf<String, String>()
                result["src"] = currentAttributes["src"]?.split(";")?.firstOrNull() ?: ""
                result["caption"] = "Updated caption"
                result
            }

            Assert.assertEquals("<p>First Line</p><placeholder src=\"image.jpg\" caption=\"Updated caption\" uuid=\"uuid1\" type=\"image_with_caption\" /><p>Second Line</p>", editText.toHtml())
            Assert.assertEquals(initialSelectionStart, editText.selectionStart)
            Assert.assertEquals(initialSelectionEnd, editText.selectionEnd)
        }
    }

    @Test
    @Throws(Exception::class)
    fun removePlaceholderWhenItShouldNotBeUpdated() {
        runBlocking {
            val initialHtml = "<placeholder uuid=\"uuid1\" type=\"image_with_caption\" src=\"image.jpg;image2.jpg\" caption=\"Caption - 1, 2\" /><p>Line</p>"
            editText.fromHtml(initialHtml)

            placeholderManager.removeOrUpdate("uuid1", shouldUpdateItem = {
                false
            }) { currentAttributes ->
                val result = mutableMapOf<String, String>()
                result["src"] = currentAttributes["src"]?.split(";")?.firstOrNull() ?: ""
                result["caption"] = "Updated caption"
                result
            }

            Assert.assertEquals("<p>Line</p>", editText.toHtml())
        }
    }
}
