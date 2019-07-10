package org.wordpress.aztec.demo.tests

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.rule.IntentsTestRule
import org.junit.Rule
import org.junit.Test
import org.wordpress.aztec.demo.BaseTest
import org.wordpress.aztec.demo.MainActivity
import org.wordpress.aztec.demo.R
import org.wordpress.aztec.demo.pages.EditorPage
import java.io.File
import java.io.FileOutputStream

class ImageHistoryTests : BaseTest() {

    @Rule
    @JvmField
    val mActivityIntentsTestRule = IntentsTestRule<MainActivity>(MainActivity::class.java)

    val throttleTime = 1000L

    @Test
    fun testUndoRedoImageUpload() {
        val regex = Regex("<img src=.+ id=.+>|<img id=.+ src=.+>")
        val editorPage = EditorPage()

        // Upload image and verify
        uploadImageFromDeviceAndVerify(regex, editorPage)

        // Undo upload image and verify
        editorPage
                .undoChange()
                .toggleHtml()
                .verifyHTML("")
                .toggleHtml()
                .threadSleep(throttleTime) // wait for history button to enable

        // Redo upload image and verify
        editorPage
                .redoChange()
                .toggleHtml()
                .verifyHTML(regex)
    }

    @Test
    fun testAddImageDeleteUndoRedo() {
        val regex = Regex("<img src=.+ id=.+>|<img id=.+ src=.+>")
        val editorPage = EditorPage()

        // Upload image and verify
        uploadImageFromDeviceAndVerify(regex, editorPage)

        // Delete image and verify
        editorPage
                .threadSleep(throttleTime)
                .tapTop()
                .selectAllAndDelete()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML("")
                .toggleHtml()

        // Undo delete and verify
        editorPage
                .undoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(regex)
                .toggleHtml()

        // Redo delete and verify
        editorPage
                .redoChange()
                .toggleHtml()
                .verifyHTML("")
    }

    @Test
    fun testCopyPasteImageUndoRedo() {
        val regex1Image = Regex("<img src=.+ id=.+>|<img id=.+ src=.+>")
        val regex2Images = Regex("(<img src=.+ id=.+>|<img id=.+ src=.+>).*(<img src=.+ id=.+>|<img id=.+ src=.+>)")
        val editorPage = EditorPage()

        // Upload image and verify
        uploadImageFromDeviceAndVerify(regex1Image, editorPage)

        // Copy image to clipboard
        editorPage
                .threadSleep(throttleTime)
                .selectAllText()
                .copyToClipboard()
                .threadSleep(throttleTime)
                .setCursorPositionAtEnd()

        // Paste from clipboard and verify
        editorPage
                .pasteFromClipboard()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(regex2Images)
                .toggleHtml()

        // Undo paste and verify
        editorPage
                .undoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(regex1Image)
                .toggleHtml()

        // Redo paste and verify
        editorPage
                .redoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(regex2Images)
    }

    @Test
    fun testAddTwoImagesUndoRedo() {
        val regex1Image = Regex("<img src=.+ id=.+>|<img id=.+ src=.+>")
        val regex2Images = Regex("(<img src=.+ id=.+>|<img id=.+ src=.+>).*(<img src=.+ id=.+>|<img id=.+ src=.+>)")
        val editorPage = EditorPage()

        // Upload first image and verify
        uploadImageFromDeviceAndVerify(regex1Image, editorPage)

        // Upload second image and verify
        uploadImageFromDeviceAndVerify(regex2Images, editorPage)

        // Undo adding second image
        editorPage
                .undoChange()
                .toggleHtml()
                .verifyHTML(regex1Image)
                .toggleHtml()

        // Undo adding first image
        editorPage
                .undoChange()
                .toggleHtml()
                .verifyHTML("")
                .toggleHtml()
                .threadSleep(throttleTime)

        // Redo adding both images
        editorPage
                .redoChange()
                .redoChange()
                .toggleHtml()
                .verifyHTML(regex2Images)
    }

    @Test
    fun testAddPhotoWithHtmlUndoRedo() {
        val regex = Regex("<img src=.+>")
        val editorPage = EditorPage()

        // Add image with html and verify
        createImageIntentFilter()
        addPhotoWithHTML()

        editorPage
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(regex)
                .toggleHtml()

        // Undo adding html image and verify
        editorPage
                .undoChange()
                .toggleHtml()
                .verifyHTML("")
                .toggleHtml()
                .threadSleep(throttleTime)

        // Redo adding html image and verify
        editorPage
                .redoChange()
                .toggleHtml()
                .verifyHTML(regex)
    }

    private fun uploadImageFromDeviceAndVerify(regex: Regex, editorPage: EditorPage = EditorPage()) {
        // Upload image
        createImageIntentFilter()
        editorPage
                .tapTop()
                .insertMedia()

        // Must wait for the simulated upload to start
        Thread.sleep(10000)

        // Verify image uploaded
        editorPage
                .toggleHtml()
                .verifyHTML(regex)
                .toggleHtml()

        Thread.sleep(1000)
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
