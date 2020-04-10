package org.wordpress.aztec

import android.app.Activity
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.wordpress.aztec.plugins.CssUnderlinePlugin
import org.wordpress.aztec.source.CssStyleFormatter
import org.wordpress.aztec.spans.AztecUnderlineSpan

/**
 * Combined test for toolbar and inline styles.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = intArrayOf(23))
class CssUnderlinePluginTest {
    lateinit var editText: AztecText

    private val REGULAR_UNDERLINE_HTML = "<u>Underline</u>"
    private val REGULAR_UNDERLINE_WITH_STYLES_HTML = "<u style=\"color:green;\">Underline</u>"
    private val CSS_STYLE_UNDERLINE_WITH_OTHER_STYLES_HTML = "<span style=\"color:green; text-decoration:underline;\">Underline</span>"
    private val COMPLEX_HTML = "<span style=\"test:value\">$CSS_STYLE_UNDERLINE_WITH_OTHER_STYLES_HTML</span>"
    private val COMPLEX_REGULAR_DIV_HTML = "<div style=\"test:value;\">$REGULAR_UNDERLINE_WITH_STYLES_HTML</div>"
    private val COMPLEX_CSS_DIV_HTML = "<div style=\"test:value;\">$CSS_STYLE_UNDERLINE_WITH_OTHER_STYLES_HTML</div>"
    private val CSS_STYLE_UNDERLINE_WITH_EVEN_MORE_STYLES_REORDERED_HTML = "<span style=\"color:green; text-decoration:underline; test:value;\">Underline</span>"
    private val CSS_UNDERLINE_INSIDE_BOLD = "<b><span style=\"color:lime; text-decoration:underline;\">Underline</span></b>"
    private val CSS_UNDERLINE_OUTSIDE_BOLD = "<span style=\"color:lime; text-decoration:underline;\"><b>Underline</b></span>"

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
    fun testCssUnderlineWithinInlineSpan() {
        editText.fromHtml(CSS_UNDERLINE_INSIDE_BOLD)
        Assert.assertEquals(CSS_UNDERLINE_OUTSIDE_BOLD, editText.toPlainHtml())
    }

    @Test
    fun testRegularUnderlineToCssConversion() {
        editText.fromHtml(REGULAR_UNDERLINE_HTML)
        Assert.assertEquals(REGULAR_UNDERLINE_HTML, editText.toPlainHtml())
    }

    @Test
    fun testCssToCssUnderlineConversion() {
        editText.fromHtml(REGULAR_UNDERLINE_WITH_STYLES_HTML)
        Assert.assertEquals(REGULAR_UNDERLINE_WITH_STYLES_HTML, editText.toPlainHtml())
    }

    @Test
    fun testCssUnderlineToComplexCssUnderlineConversion() {
        editText.fromHtml(CSS_STYLE_UNDERLINE_WITH_OTHER_STYLES_HTML)
        Assert.assertEquals(CSS_STYLE_UNDERLINE_WITH_OTHER_STYLES_HTML, editText.toPlainHtml())
    }

    @Test
    fun testAddingStyleAttributeToUnderlineSpan() {
        editText.fromHtml(CSS_STYLE_UNDERLINE_WITH_OTHER_STYLES_HTML)

        val span = editText.text.getSpans(0, editText.length(), AztecUnderlineSpan::class.java).first()
        CssStyleFormatter.addStyleAttribute(span.attributes, "test", "value")

        Assert.assertEquals(CSS_STYLE_UNDERLINE_WITH_EVEN_MORE_STYLES_REORDERED_HTML, editText.toPlainHtml())
    }

    @Test
    fun testConversionWhenUnderlineWithExtraStyleInsideSpan() {
        editText.fromHtml(COMPLEX_HTML)
        Assert.assertEquals(COMPLEX_HTML, editText.toPlainHtml())
    }

    @Test
    fun testWhenRegularUnderlineWithExtraStyleInsideDiv() {
        editText.fromHtml(COMPLEX_REGULAR_DIV_HTML)
        Assert.assertEquals(COMPLEX_REGULAR_DIV_HTML, editText.toPlainHtml())
    }

    @Test
    fun testWhenCssUnderlineWithExtraStyleInsideDiv() {
        editText.fromHtml(COMPLEX_CSS_DIV_HTML)
        Assert.assertEquals(COMPLEX_CSS_DIV_HTML, editText.toPlainHtml())
    }
}
