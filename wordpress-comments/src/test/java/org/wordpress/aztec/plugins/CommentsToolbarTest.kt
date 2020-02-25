package org.wordpress.aztec.plugins

import android.app.Activity
import android.widget.ToggleButton
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.wordpress.aztec.Aztec
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.plugins.wpcomments.R
import org.wordpress.aztec.plugins.wpcomments.WordPressCommentsPlugin
import org.wordpress.aztec.plugins.wpcomments.toolbar.MoreToolbarButton
import org.wordpress.aztec.plugins.wpcomments.toolbar.PageToolbarButton
import org.wordpress.aztec.source.SourceViewEditText
import org.wordpress.aztec.toolbar.AztecToolbar
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener

/**
 * Combined test for toolbar and inline styles.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = intArrayOf(23))
class CommentsToolbarTest {

    lateinit var editText: AztecText
    lateinit var sourceText: SourceViewEditText
    lateinit var toolbar: AztecToolbar

    lateinit var moreButton: ToggleButton
    lateinit var pageButton: ToggleButton

    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().visible().get()
        editText = AztecText(activity)
        editText.setCalypsoMode(false)
        sourceText = SourceViewEditText(activity)

        activity.setContentView(editText)
        toolbar = AztecToolbar(activity)

        Aztec.with(editText, sourceText, toolbar, object : IAztecToolbarClickListener {
                    override fun onToolbarCollapseButtonClicked() {}
                    override fun onToolbarExpandButtonClicked() {}
                    override fun onToolbarFormatButtonClicked(format: ITextFormat, isKeyboardShortcut: Boolean) {}
                    override fun onToolbarHeadingButtonClicked() {}
                    override fun onToolbarHtmlButtonClicked() {}
                    override fun onToolbarListButtonClicked() {}
                    override fun onToolbarMediaButtonClicked() : Boolean { return false }
                })
                .addPlugin(WordPressCommentsPlugin(editText))
                .addPlugin(MoreToolbarButton(editText))
                .addPlugin(PageToolbarButton(editText))

        moreButton = toolbar.findViewById(R.id.format_bar_button_more)
        pageButton = toolbar.findViewById(R.id.format_bar_button_more)
    }

    /**
     * Testing initial state of the editor and a toolbar.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun initialState() {
        Assert.assertFalse(moreButton.isChecked)
        Assert.assertFalse(pageButton.isChecked)

        Assert.assertTrue(TestUtils.safeEmpty(editText))
    }

    /**
     * Insert comment at selection when More format toolbar button is tapped.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreWithButton() {
        editText.fromHtml("")
        moreButton.performClick()
        TestUtils.equalsIgnoreWhitespace("<!--more-->", sourceText.text.toString())

        // Select location.
        editText.fromHtml("<b>Bold</b>")
        editText.setSelection(3)
        moreButton.performClick()
        TestUtils.equalsIgnoreWhitespace("<b>Bol</b><br><!--more--><br><b>d</b><br>", sourceText.text.toString())

        // Select characters.
        editText.fromHtml("<b>Bold</b>")
        editText.setSelection(2, 3)
        moreButton.performClick()
        TestUtils.equalsIgnoreWhitespace("<b>B</b><br><!--more--><br><b>d</b><br>", sourceText.text.toString())

        // Select characters across lines.
        editText.fromHtml("<b>Bold</b><br><i>Italic</i><br>")
        editText.setSelection(3, 5)
        moreButton.performClick()
        TestUtils.equalsIgnoreWhitespace("<b>Bol</b><br><!--more--><br><i>talic</i><br>", sourceText.text.toString())
    }

    /**
     * Insert comment when <!--more--> is input.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreWithCode() {
        editText.fromHtml("")
        sourceText.append("<!--more-->")
        TestUtils.equalsIgnoreWhitespace("more", editText.text.toString())
    }

    /**
     * Insert comment at selection when Page Break format toolbar button is tapped.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageWithButton() {
        editText.fromHtml("")
        pageButton.performClick()
        TestUtils.equalsIgnoreWhitespace("<!--nextpage-->", sourceText.text.toString())

        // Select location.
        editText.fromHtml("<b>Bold</b>")
        editText.setSelection(3)
        pageButton.performClick()
        TestUtils.equalsIgnoreWhitespace("<b>Bol</b><br><!--nextpage--><br><b>d</b><br>", sourceText.text.toString())

        // Select characters.
        editText.fromHtml("<b>Bold</b>")
        editText.setSelection(2, 3)
        pageButton.performClick()
        TestUtils.equalsIgnoreWhitespace("<b>B</b><br><!--nextpage--><br><b>d</b><br>", sourceText.text.toString())

        // Select characters across lines.
        editText.fromHtml("<b>Bold</b><br><i>Italic</i><br>")
        editText.setSelection(3, 5)
        pageButton.performClick()
        TestUtils.equalsIgnoreWhitespace("<b>Bol</b><br><!--nextpage--><br><i>talic</i><br>", sourceText.text.toString())
    }

    /**
     * Insert comment when <!--nextpage--> is input.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageWithCode() {
        editText.fromHtml("")
        sourceText.append("<!--nextpage-->")
        TestUtils.equalsIgnoreWhitespace("nextpage", editText.text.toString())
    }
}
