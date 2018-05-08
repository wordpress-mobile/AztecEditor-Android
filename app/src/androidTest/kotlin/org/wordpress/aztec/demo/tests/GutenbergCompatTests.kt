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
    fun testRetainGutenbergComments() {
        val html = "<!-- wp:paragraph -->" + "<p>This is a paragraph</p>" + "<!-- /wp:paragraph -->"

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
    fun testAddPhotoAtEndOfGutenberParagraph() {
        val htmlStart = "<!-- wp:paragraph --><p>Blue is not</p><!-- /wp:paragraph -->"
        val htmlEndRegExp = "$htmlStart.*<img src=.+>"
        val regex = Regex(htmlEndRegExp, RegexOption.DOT_MATCHES_ALL)

        val editorPage = EditorPage()

        // insert starter text
        editorPage
                .toggleHtml()
                .insertHTML(htmlStart)
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
