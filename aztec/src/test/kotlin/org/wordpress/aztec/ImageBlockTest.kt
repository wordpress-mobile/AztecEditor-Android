package org.wordpress.aztec

import android.app.Activity
import android.view.MenuItem
import android.widget.PopupMenu
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.wordpress.aztec.source.SourceViewEditText
import org.wordpress.aztec.toolbar.AztecToolbar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = intArrayOf(23))
class ImageBlockTest {
    lateinit var editText: AztecText
    lateinit var menuList: PopupMenu
    lateinit var menuListOrdered: MenuItem
    lateinit var menuListUnordered: MenuItem
    lateinit var sourceText: SourceViewEditText
    lateinit var toolbar: AztecToolbar


    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().visible().get()
        editText = AztecText(activity)
        editText.setCalypsoMode(false)
        editText.addMediaAfterBlocks()
        sourceText = SourceViewEditText(activity)
        sourceText.setCalypsoMode(false)
        toolbar = AztecToolbar(activity)
        toolbar.setEditor(editText, sourceText)
        menuList = toolbar.getListMenu() as PopupMenu
        menuListOrdered = menuList.menu.getItem(1)
        menuListUnordered = menuList.menu.getItem(0)
        activity.setContentView(editText)
    }

    @Test
    @Throws(Exception::class)
    fun addImageAfterAListAtTheEnd() {
        editText.fromHtml("<ul><li>item 1</li><li>item 2</li></ul>")

        editText.setSelection(editText.editableText.indexOf("2"))
        val attributes = AztecAttributes()
        attributes.setValue("id", "1234")
        editText.insertImage(null, attributes)

        Assert.assertEquals("<ul><li>item 1</li><li>item 2</li></ul><img id=\"1234\" />", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addHRAfterAListAtTheEnd() {
        editText.fromHtml("<ul><li>item 1</li><li>item 2</li></ul>")

        editText.setSelection(editText.editableText.indexOf("2"))
        editText.lineBlockFormatter.applyHorizontalRule(false)

        Assert.assertEquals("<ul><li>item 1</li><li>item 2</li></ul><hr />", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addImageAfterAListInTheMiddle() {
        editText.fromHtml("<ul><li>item 1</li><li>item 2</li></ul>\n<p>test</p>")

        editText.setSelection(editText.editableText.indexOf("2"))
        val attributes = AztecAttributes()
        attributes.setValue("id", "1234")
        editText.insertImage(null, attributes)

        Assert.assertEquals("<ul><li>item 1</li><li>item 2</li></ul><img id=\"1234\" /><p>test</p>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addHRAfterAListInTheMiddle() {
        editText.fromHtml("<ul><li>item 1</li><li>item 2</li></ul>\n<p>test</p>")

        editText.setSelection(editText.editableText.indexOf("2"))
        editText.lineBlockFormatter.applyHorizontalRule(false)

        Assert.assertEquals("<ul><li>item 1</li><li>item 2</li></ul><hr /><p>test</p>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addImageAfterHeadline() {
        editText.fromHtml("<h1>Headline 1</h1>")

        editText.setSelection(editText.editableText.indexOf("1"))
        val attributes = AztecAttributes()
        attributes.setValue("id", "1234")
        editText.insertImage(null, attributes)

        Assert.assertEquals("<h1>Headline 1</h1><img id=\"1234\" />", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addHRAfterHeadline() {
        editText.fromHtml("<h1>Headline 1</h1>")

        editText.setSelection(editText.editableText.indexOf("1"))
        editText.lineBlockFormatter.applyHorizontalRule(false)

        Assert.assertEquals("<h1>Headline 1</h1><hr />", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addImageBetweenHeadlines() {
        editText.fromHtml("<h1>Headline 1</h1><h2>Headline 2</h2>")

        editText.setSelection(editText.editableText.indexOf("1"))
        val attributes = AztecAttributes()
        attributes.setValue("id", "1234")
        editText.insertImage(null, attributes)

        Assert.assertEquals("<h1>Headline 1</h1><img id=\"1234\" /><h2>Headline 2</h2>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addHRBetweenHeadlines() {
        editText.fromHtml("<h1>Headline 1</h1><h2>Headline 2</h2>")

        editText.setSelection(editText.editableText.indexOf("1"))
        editText.lineBlockFormatter.applyHorizontalRule(false)

        Assert.assertEquals("<h1>Headline 1</h1><hr /><h2>Headline 2</h2>", editText.toHtml())
    }
}

