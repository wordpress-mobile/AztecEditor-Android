package org.wordpress.aztec.demo.tests

import androidx.test.espresso.intent.rule.IntentsTestRule
import org.junit.Rule
import org.junit.Test
import org.wordpress.aztec.demo.BaseTest
import org.wordpress.aztec.demo.MainActivity
import org.wordpress.aztec.demo.pages.EditLinkPage
import org.wordpress.aztec.demo.pages.EditorPage

class LinkTests : BaseTest() {

    @Rule
    @JvmField
    val mActivityIntentsTestRule = IntentsTestRule<MainActivity>(MainActivity::class.java)

    @Test
    fun testAddLink() {
        val text = "sample link"
        val url = "https://github.com/wordpress-mobile/AztecEditor-Android"
        val html = "<a href=\"$url\">$text</a>"

        EditorPage()
                .makeLink()

        EditLinkPage()
                .updateURL(url)
                .updateName(text)
                .ok()

        EditorPage()
                .toggleHtml()
                .verifyHTML(html)
    }

    @Test
    fun testAddLinkWithOpenExternal() {
        val text = "sample link"
        val url = "https://github.com/wordpress-mobile/AztecEditor-Android"
        val html = "<a target=\"_blank\" rel=\"noopener\" href=\"$url\">$text</a>"

        EditorPage()
                .makeLink()

        EditLinkPage()
                .updateURL(url)
                .updateName(text)
                .toggleOpenInNewWindow()
                .ok()

        EditorPage()
                .toggleHtml()
                .verifyHTML(html)
    }

    @Test
    fun testMixedLinkFormatting() {
        val text1 = "sample "
        val text2 = "link"
        val url = "https://github.com/wordpress-mobile/AztecEditor-Android"
        val html = "$text1<a href=\"$url\">$text2</a>"

        EditorPage()
                .insertText(text1)
                .makeLink()

        EditLinkPage()
                .updateURL(url)
                .updateName(text2)
                .ok()

        EditorPage()
                .toggleHtml()
                .verifyHTML(html)
    }

    @Test
    fun testEditLinkURL() {
        val text = "sample link"
        val url1 = "https://github.com/wordpress-mobile"
        val url2 = "https://github.com/wordpress-mobile/AztecEditor-Android"
        val link = "<a href=\"$url1\">$text</a>"
        val html = "<a href=\"$url2\">$text</a>"

        EditorPage()
                .toggleHtml()
                .insertHTML(link)
                .toggleHtml()
                .makeLink()

        EditLinkPage()
                .updateURL(url2)
                .ok()

        EditorPage()
                .toggleHtml()
                .verifyHTML(html)
    }

    @Test
    fun testEditLinkName() {
        val text1 = "sample link"
        val text2 = "updated link"
        val url = "https://github.com/wordpress-mobile/AztecEditor-Android"
        val link = "<a href=\"$url\">$text1</a>"
        val html = "<a href=\"$url\">$text2</a>"

        EditorPage()
                .toggleHtml()
                .insertHTML(link)
                .toggleHtml()
                .makeLink()

        EditLinkPage()
                .updateName(text2)
                .ok()

        EditorPage()
                .toggleHtml()
                .verifyHTML(html)
    }

    @Test
    fun testToggleOpenInNewWindowLink() {
        val text = "sample link"
        val url1 = "https://github.com/wordpress-mobile/AztecEditor-Android"
        val link = "<a href=\"$url1\" rel=\"noopener\" target=\"_blank\">$text</a>"
        val html = "<a href=\"$url1\">$text</a>"

        EditorPage()
                .toggleHtml()
                .insertHTML(link)
                .toggleHtml()
                .makeLink()

        EditLinkPage()
                .toggleOpenInNewWindow()
                .ok()

        EditorPage()
                .toggleHtml()
                .verifyHTML(html)
    }

    @Test
    fun testRemoveLink() {
        val text = "sample link"
        val url = "https://github.com/wordpress-mobile/AztecEditor-Android"
        val link = "<a href=\"$url\">$text</a>"

        EditorPage()
                .toggleHtml()
                .insertHTML(link)
                .toggleHtml()
                .makeLink()

        EditLinkPage()
                .remove()

        EditorPage()
                .toggleHtml()
                .verifyHTML(text)
    }

    @Test
    fun testToggleLinkUnderLine() {
        val text = "sample link"
        val url = "https://github.com/wordpress-mobile/AztecEditor-Android"
        val link = "<a href=\"$url\">$text</a>"
        val html = "<a href=\"$url\"><u>$text</u></a>"

        EditorPage()
                .toggleHtml()
                .insertHTML(link)
                .toggleHtml()
                .selectAllText()
                .toggleUnderline()
                .toggleHtml()
                .verifyHTML(html)
    }
}
