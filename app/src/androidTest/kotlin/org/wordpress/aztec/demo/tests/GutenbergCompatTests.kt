package org.wordpress.aztec.demo.tests

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.test.espresso.intent.Intents
import android.support.test.espresso.intent.matcher.IntentMatchers
import android.support.test.espresso.intent.rule.IntentsTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.demo.BaseTest
import org.wordpress.aztec.demo.MainActivity
import org.wordpress.aztec.demo.R
import org.wordpress.aztec.demo.pages.EditorPage
import org.wordpress.aztec.source.SourceViewEditText
import java.io.File
import java.io.FileOutputStream

class GutenbergCompatTests : BaseTest() {

    @Rule
    @JvmField
    val mActivityIntentsTestRule = IntentsTestRule<MainActivity>(MainActivity::class.java)

    @Before
    fun init() {
        val aztecText = mActivityIntentsTestRule.activity.findViewById<AztecText>(R.id.aztec)
        val sourceText = mActivityIntentsTestRule.activity.findViewById<SourceViewEditText>(R.id.source)

        aztecText.setCalypsoMode(false)
        sourceText.setCalypsoMode(false)
    }

    @Test
    fun testRetainGutenbergPostContent() {
        val html = "<!-- wp:paragraph --><p>Blue is not a color</p><!-- /wp:paragraph -->" +
                "<!-- wp:list --><ul><li>item 1</li><li>item2</li></ul><!-- /wp:list -->" +
                "<!-- wp:heading --><h2>H2</h2><!-- /wp:heading -->"

        EditorPage().toggleHtml()
                .insertHTML(html)
                .toggleHtml()
                .toggleHtml()
                .verifyHTML(html)
    }

    @Test
    fun testRetainGutenbergCommentsOnAddingText() {
        val htmlStart = "<!-- wp:paragraph --><p>Blue is not</p><!-- /wp:paragraph -->"
        val appendText = " a beautiful color"
        val htmlVerify = "<!-- wp:paragraph --><p>Blue is not$appendText</p><!-- /wp:paragraph -->"

        val editorPage = EditorPage()

        // insert starter text
        editorPage
                .toggleHtml()
                .insertHTML(htmlStart)
                .toggleHtml()

        // append text
        editorPage
                .setCursorPositionAtEnd()
                .insertText(appendText)

        // verify html
        editorPage
                .toggleHtml()
                .verifyHTML(htmlVerify)
    }

    @Test
    fun testAddPhotoAtBeginningOfGutenberParagraph() {
        val htmlStart = "<!-- wp:paragraph --><p>"
        val htmlEnd = "Blue is not a color</p><!-- /wp:paragraph -->"
        val imagePlaceholder = "<img src=.+>"

        val contentBeforeImageInsertion = htmlStart + htmlEnd
        val contentAfterImageInsertion = htmlStart + imagePlaceholder + htmlEnd
        val regex = Regex(contentAfterImageInsertion, RegexOption.DOT_MATCHES_ALL)
        val editorPage = EditorPage()

        // insert starter text
        editorPage
                .toggleHtml()
                .insertHTML(contentBeforeImageInsertion)
                .toggleHtml()

        // insert image
        editorPage
                .moveCursorLeftAsManyTimes(contentBeforeImageInsertion.length)
        createImageIntentFilter()
        EditorPage()
                .insertMedia()

        // Must wait for simulated upload
        Thread.sleep(10000)

        EditorPage()
                .toggleHtml()
                .verifyHTML(regex)
    }

    @Test
    fun testAddPhotoAtEndOfGutenberParagraph() {
        val htmlStart = "<!-- wp:paragraph --><p>Blue is not"
        val htmlEnd = "</p><!-- /wp:paragraph -->"
        val imagePlaceholder = "<img src=.+>"

        val contentBeforeImageInsertion = htmlStart + htmlEnd
        val contentAfterImageInsertion = htmlStart + imagePlaceholder + htmlEnd
        val regex = Regex(contentAfterImageInsertion, RegexOption.DOT_MATCHES_ALL)
        val editorPage = EditorPage()

        // insert starter text
        editorPage
                .toggleHtml()
                .insertHTML(contentBeforeImageInsertion)
                .toggleHtml()

        // insert image
        editorPage
                .setCursorPositionAtEnd()
        createImageIntentFilter()
        EditorPage()
                .insertMedia()

        // Must wait for simulated upload
        Thread.sleep(10000)

        EditorPage()
                .toggleHtml()
                .verifyHTML(regex)
    }

    @Test
    fun testAddPhotoAtEndOfGutenberFirstParagraph() {
        val htmlStart = "<!-- wp:paragraph --><p>Blue is not a color"
        val htmlEnd = "</p><!-- /wp:paragraph -->"
        val imagePlaceholder = "<img src=.+>"
        val secondParagraphContent = "Red is a color"
        val htmlSecondParagraph = "<!-- wp:paragraph --><p>$secondParagraphContent</p><!-- /wp:paragraph -->"

        val contentBeforeImageInsertion = htmlStart + htmlEnd + htmlSecondParagraph
        val contentAfterImageInsertion = htmlStart + imagePlaceholder + htmlEnd + htmlSecondParagraph
        val regex = Regex(contentAfterImageInsertion, RegexOption.DOT_MATCHES_ALL)
        val editorPage = EditorPage()

        // insert starter text
        editorPage
                .toggleHtml()
                .insertHTML(contentBeforeImageInsertion)
                .toggleHtml()

        // insert image
        editorPage
                .setCursorPositionAtEnd()
                .moveCursorLeftAsManyTimes(secondParagraphContent.length + 1)
        createImageIntentFilter()
        EditorPage()
                .insertMedia()

        // Must wait for simulated upload
        Thread.sleep(10000)

        EditorPage()
                .toggleHtml()
                .verifyHTML(regex)
    }

    @Test
    fun testAddPhotoAtStartOfGutenberSecondParagraph() {
        val firstParagraphHTML = "<!-- wp:paragraph --><p>Blue is not a color</p><!-- /wp:paragraph -->"
        val secondParagraphContent = "Red is a color"
        val htmlStart = "<!-- wp:paragraph --><p>"
        val htmlEnd = "$secondParagraphContent</p><!-- /wp:paragraph -->"
        val imagePlaceholder = "<img src=.+>"

        val contentBeforeImageInsertion = firstParagraphHTML + htmlStart + htmlEnd
        val contentAfterImageInsertion = firstParagraphHTML + htmlStart + imagePlaceholder + htmlEnd
        val regex = Regex(contentAfterImageInsertion, RegexOption.DOT_MATCHES_ALL)
        val editorPage = EditorPage()

        // insert starter text
        editorPage
                .toggleHtml()
                .insertHTML(contentBeforeImageInsertion)
                .toggleHtml()

        // insert image
        editorPage
                .setCursorPositionAtEnd()
                .moveCursorLeftAsManyTimes(secondParagraphContent.length)
        createImageIntentFilter()
        EditorPage()
                .insertMedia()

        // Must wait for simulated upload
        Thread.sleep(10000)

        EditorPage()
                .toggleHtml()
                .verifyHTML(regex)
    }

    @Test
    fun testSimpleAddItemToOrderedList() {
        val htmlStart = "<!-- wp:list --><ul><li>item 1</li><li>item2</li></ul><!-- /wp:list -->"
        val newItem = "item3"
        val html = "<!-- wp:list --><ul><li>item 1</li><li>item2</li><li>$newItem</li></ul><!-- /wp:list -->"

        val editorPage = EditorPage()

        // insert starter text
        editorPage
                .toggleHtml()
                .insertHTML(htmlStart)
                .toggleHtml()

        // insert new item
        editorPage
                .setCursorPositionAtEnd()
                .insertText("\n" + newItem)

        EditorPage()
                .toggleHtml()
                .verifyHTML(html)
    }

    private fun createImageIntentFilter() {
        val file = File(mActivityIntentsTestRule.activity.filesDir, "aztec.png")
        val outputStream = FileOutputStream(file)
        val bitmap = BitmapFactory.decodeResource(mActivityIntentsTestRule.activity.resources, R.drawable.aztec)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.close()

        val intent = Intent()
        intent.data = Uri.fromFile(file)
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_OPEN_DOCUMENT))
                .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, intent))
    }

}
