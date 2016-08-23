package org.wordpress.aztec

import android.app.Activity
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class)
class AddUrlTest() {

    lateinit var editText: AztecText

    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().visible().get()
        editText = AztecText(activity)
        activity.setContentView(editText)
    }


    @Test
    @Throws(Exception::class)
    fun insertLinkAfterText() {
        editText.append("hello ")
        editText.setSelection(editText.length())
        editText.link("http://wordpress.com", "WordPress")
        Assert.assertEquals("hello <a href=\"http://wordpress.com\">WordPress</a>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun insertLinkIntoText() {
        editText.append("leftright")
        editText.setSelection(4)
        editText.link("http://wordpress.com", "WordPress")
        Assert.assertEquals("left<a href=\"http://wordpress.com\">WordPress</a>right", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun insertLinkIntoStyledText() {
        editText.fromHtml("<del><b>left</b><i>right</i></del>")
        editText.setSelection(4)
        editText.link("http://wordpress.com", "WordPress")
        //Still valid, but order of b and del is switched here for some reason.
        Assert.assertEquals("<b><del>left</del></b><b><del><a href=\"http://wordpress.com\">WordPress</a></del></b><del><i>right</i></del>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun insertLinkIntoEmptyEditor() {
        editText.link("http://wordpress.com", "WordPress")
        Assert.assertEquals("<a href=\"http://wordpress.com\">WordPress</a>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun setLinkToText() {
        editText.append("WordPress")
        editText.setSelection(0, editText.length())

        editText.link("http://wordpress.com", editText.getSelectedText())

        Assert.assertEquals("<a href=\"http://wordpress.com\">WordPress</a>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun setLinkToStyledText() {
        editText.fromHtml("Hello <b>WordPress</b>")

        editText.setSelection(6,editText.length())

        Assert.assertEquals("WordPress", editText.getSelectedText())

        editText.link("http://wordpress.com", editText.getSelectedText())

        Assert.assertEquals("Hello <b><a href=\"http://wordpress.com\">WordPress</a></b>", editText.toHtml())
    }

}