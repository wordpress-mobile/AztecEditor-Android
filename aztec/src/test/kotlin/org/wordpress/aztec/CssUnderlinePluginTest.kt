package org.wordpress.aztec

import android.app.Activity
import android.widget.ToggleButton
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.wordpress.aztec.plugins.CssUnderlinePlugin
import org.wordpress.aztec.source.InlineCssStyleFormatter
import org.wordpress.aztec.source.SourceViewEditText
import org.wordpress.aztec.spans.AztecUnderlineSpan
import org.wordpress.aztec.toolbar.AztecToolbar

/**
 * Combined test for toolbar and inline styles.
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(23))
class CssUnderlinePluginTest {
    lateinit var editText: AztecText

    private val REGULAR_UNDERLINE_HTML = "<u>Underline</u>"
    private val REGULAR_UNDERLINE_WITH_STYLES_HTML = "<u style=\"color: green\">Underline</u>"
    private val CSS_STYLE_UNDERLINE_HTML = "<span style=\"text-decoration: underline\">Underline</span>"
    private val CSS_STYLE_UNDERLINE_WITH_OTHER_STYLES_HTML = "<span style=\"color: green; text-decoration: underline\">Underline</span>"
    private val COMPLEX_HTML = "<span style=\"color: green\">$CSS_STYLE_UNDERLINE_HTML</span>"
    private val VERY_COMPLEX_HTML = "<span style=\"color: green\"><span style=\"test: value; text-decoration: underline\">Underline</span></span>"

    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().visible().get()
        editText = AztecText(activity)
        editText.setCalypsoMode(false)
        editText.plugins.add(CssUnderlinePlugin())
    }

    @Test
    fun testRegularToCssUnderlineConversion() {
        editText.fromHtml(REGULAR_UNDERLINE_HTML)
        Assert.assertEquals(CSS_STYLE_UNDERLINE_HTML, editText.toPlainHtml())
    }

    @Test
    fun testRegularUnderlineToComplexCssUnderlineConversion() {
        editText.fromHtml(REGULAR_UNDERLINE_WITH_STYLES_HTML)
        Assert.assertEquals(CSS_STYLE_UNDERLINE_WITH_OTHER_STYLES_HTML, editText.toPlainHtml())
    }

    @Test
    fun testCssUnderlineToComplexCssUnderlineConversion() {
        editText.fromHtml(CSS_STYLE_UNDERLINE_WITH_OTHER_STYLES_HTML)
        Assert.assertEquals(COMPLEX_HTML, editText.toPlainHtml())
    }

    @Test
    fun testAddingStyleAttributeToUnderlineSpan() {
        editText.fromHtml(CSS_STYLE_UNDERLINE_WITH_OTHER_STYLES_HTML)

        val span = editText.text.getSpans(0, editText.length(), AztecUnderlineSpan::class.java).first()

        InlineCssStyleFormatter.addStyleAttribute(span.attributes, "test", "value")

        Assert.assertEquals(VERY_COMPLEX_HTML, editText.toPlainHtml())
    }

    @Test
    fun testConversionWhenUnderlineWithExtraStyleInsideSpan() {
        editText.fromHtml(VERY_COMPLEX_HTML)

        val MODIFIED_VERY_COMPLEX_HTML = "<span style=\"color: green\"><span style=\"test: value\">" +
                "<span style=\"text-decoration: underline\">Underline</span></span></span>"

        Assert.assertEquals(MODIFIED_VERY_COMPLEX_HTML, editText.toPlainHtml())
    }
}
