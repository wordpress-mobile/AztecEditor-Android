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
import org.junit.Rule
import org.junit.Test
import org.wordpress.aztec.demo.BaseTest
import org.wordpress.aztec.demo.MainActivity
import org.wordpress.aztec.demo.R
import org.wordpress.aztec.demo.pages.EditorPage
import java.io.File
import java.io.FileOutputStream

class ImageTests : BaseTest() {

    @Rule
    @JvmField
    val mActivityIntentsTestRule = IntentsTestRule<MainActivity>(MainActivity::class.java)

    // Simple photo test, also tests the issue described in
    // https://github.com/wordpress-mobile/AztecEditor-Android/issues/306
    @Test
    fun testAddPhoto() {
        val regex = Regex("<img src=.+>")

        createImageIntentFilter()

        EditorPage()
                .tapTop()
                .insertMedia()

        // Must wait for simulated upload
        Thread.sleep(10000)

        EditorPage()
                .toggleHtml()
                .verifyHTML(regex)
    }

    @Test
    fun testAddPhotoAndText() {
        var sampleText = "sample text "
        val regex = Regex(".+<img src=.+>.+")

        for (i in 1..5) {
            sampleText += sampleText
        }

        createImageIntentFilter()

        EditorPage()
                .replaceText(sampleText)
                .tapTop()
                .insertMedia()

        // Must wait for simulated upload
        Thread.sleep(10000)

        EditorPage()
                .toggleHtml()
                .verifyHTML(regex)
    }

    @Test
    fun testAddTwoPhotos() {
        val regex = Regex(".*<img src=.+>.*<img src=.+>.*", RegexOption.DOT_MATCHES_ALL)

        createImageIntentFilter()
        addPhotoWithHTML()

        EditorPage()
                .tapTop()
                .insertMedia()

        // Must wait for simulated upload
        Thread.sleep(10000)

        EditorPage()
                .toggleHtml()
                .verifyHTML(regex)
    }

    // Tests the issue described in
    // https://github.com/wordpress-mobile/AztecEditor-Android/issues/299
    @Test
    fun testUndoImageUpload() {
        createImageIntentFilter()

        EditorPage()
                .tapTop()
                .insertMedia()

        // Must wait for simulated upload to start
        Thread.sleep(1000)

        EditorPage()
                .undoChange()
                .toggleHtml()
                .verifyHTML("")
    }

    private fun addPhotoWithHTML() {
        val imageHtml = "<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\">"

        EditorPage()
                .tapTop()
                .toggleHtml()
                .replaceHTML(imageHtml)
                .toggleHtml()
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
