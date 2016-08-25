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
class LinkTest() {

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
        editText.setSelection(6, editText.length())

        editText.link("http://wordpress.com", editText.getSelectedText())
        Assert.assertEquals("Hello <b><a href=\"http://wordpress.com\">WordPress</a></b>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun setLinkAndReplaceText() {
        editText.fromHtml("Hello <b>WordPress</b>")
        editText.setSelection(6, editText.length())

        editText.link("http://wordpress.com", "World")
        Assert.assertEquals("Hello <b><a href=\"http://wordpress.com\">World</a></b>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun removeLink() {
        editText.fromHtml("Hello <b><a href=\"http://wordpress.com\">WordPress</a></b>")
        editText.setSelection(6, editText.length())

        editText.link("", "WordPress")
        Assert.assertEquals("Hello <b>WordPress</b>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun removeLinkAndChangeAnchor() {
        editText.fromHtml("Hello <b><a href=\"http://wordpress.com\">WordPress</a></b>")
        editText.setSelection(6, editText.length())

        editText.link("", "World") //removing url wont cause anchor to change
        Assert.assertEquals("Hello <b>WordPress</b>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun changeAnchorAndUrl() {
        editText.fromHtml("Hello <b><a href=\"http://wordpress.com\">WordPress</a></b>")
        editText.setSelection(7)

        editText.link("http://automattic.com", "World")
        Assert.assertEquals("Hello <b><a href=\"http://automattic.com\">World</a></b>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun changeAnchorAndUrlWithPartialSelection() {
        editText.fromHtml("Hello <b><a href=\"http://wordpress.com\">WordPress</a></b>")
        editText.setSelection(6, editText.length() - 1)

        editText.link("http://automattic.com", "World")
        Assert.assertEquals("Hello <b><a href=\"http://automattic.com\">World</a></b>", editText.toHtml())
    }


    //TODO: Modify parser to produce cleaner html
    //Currently the way tags are closed (at every span transition) makes toHtml produce this:
    //<a href="http://automattic.com">FirstUrl Hello </a><a href="http://automattic.com"><b>SecondUrl</b></a>
    @Test
    @Throws(Exception::class)
    fun changeUrlOfMultipleSelectedLinks() {
        editText.fromHtml("<a href=\"http://first\">FirstUrl</a> Hello <b><a href=\"http://second\">SecondUrl</a></b>")
        editText.setSelection(0, editText.length())

        editText.link("http://automattic.com", editText.getSelectedText())
        Assert.assertEquals("<a href=\"http://automattic.com\">FirstUrl Hello </a>" +
                "<a href=\"http://automattic.com\"><b>SecondUrl</b></a>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun removeUrl() {
        editText.fromHtml("Hello <b><a href=\"http://wordpress.com\">WordPress</a></b>")
        editText.setSelection(7)

        editText.removeLink()
        Assert.assertEquals("Hello <b>WordPress</b>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun removeUrlWithOtherTextSelected() {
        editText.fromHtml("Hello <b><a href=\"http://wordpress.com\">WordPress</a></b>")
        editText.setSelection(0, editText.length())

        editText.removeLink()
        Assert.assertEquals("Hello <b>WordPress</b>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun removeUrlFromMultipleSelectedUrls() {
        editText.fromHtml("<a href=\"http://first\">FirstUrl</a> Hello <b><a href=\"http://second\">SecondUrl</a></b>")
        editText.setSelection(0, editText.length())

        editText.removeLink()
        Assert.assertEquals("FirstUrl Hello <b>SecondUrl</b>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun insertLinkIntoHiddenHtmlSpan() {
        editText.fromHtml("<div class=\"third\">Div<br><span>Span</span><br>Hidden</div>")
        editText.setSelection(3)

        editText.link("http://first_link","First Link")
        Assert.assertEquals("<div class=\"third\">Div<a href=\"http://first_link\">First Link</a>" +
                "<br><span>Span</span><br>Hidden</div>", editText.toHtml())
        editText.setSelection(14)
        editText.link("http://second_link","Second Link")

        Assert.assertEquals("<div class=\"third\">Div<a href=\"http://first_link\">First Link</a>" +
                "<br><a href=\"http://second_link\">Second Link</a><span>Span</span><br>Hidden</div>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun setLinkToHiddenHtmlSpanItems() {
        editText.fromHtml("<div class=\"third\">Div<br><span>Span</span><br>Hidden</div>")
        editText.setSelection(4,8)

        editText.link("http://span",editText.getSelectedText())
        Assert.assertEquals("<div class=\"third\">Div<br><span><a href=\"http://span\">Span</a></span><br>Hidden</div>", editText.toHtml())


        editText.setSelection(0,3)

        editText.link("http://div",editText.getSelectedText())
        Assert.assertEquals("<div class=\"third\"><a href=\"http://div\">Div</a><br><span><a href=\"http://span\">Span</a></span><br>Hidden</div>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun setLinkToWholeHiddenHtmlSpan() {
        editText.fromHtml("<div class=\"third\">Div<br><span>Span</span><br>Hidden</div>")
        editText.selectAll()
        editText.link("http://link",editText.getSelectedText())

        Assert.assertEquals("<div class=\"third\"><a href=\"http://link\">Div</a><br><a href=\"http://link\"><span>Span</span></a><br><a href=\"http://link\">Hidden</a></div>", editText.toHtml())
    }

}