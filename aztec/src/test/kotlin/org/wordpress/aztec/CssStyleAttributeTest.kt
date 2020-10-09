@file:Suppress("DEPRECATION")

package org.wordpress.aztec

import android.test.AndroidTestCase
import android.text.SpannableString
import androidx.test.core.app.ApplicationProvider
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.wordpress.aztec.source.CssStyleFormatter
import org.wordpress.aztec.spans.AztecStyleBoldSpan

@RunWith(RobolectricTestRunner::class)
@Config(sdk = intArrayOf(18, 21, 25))
class CssStyleAttributeTest : AndroidTestCase() {

    private val EMPTY_STYLE_HTML = "<b>bold</b>"
    private val HTML = "<b style=\"name:value;\">bold</b>"
    private val COMPLEX_HTML = "<b style=\"a:b; name:value;\">bold</b>"

    private lateinit var parser: AztecParser

    @Before
    fun init() {
        context = ApplicationProvider.getApplicationContext()
        parser = AztecParser(AlignmentRendering.SPAN_LEVEL)
    }

    @Test
    fun testEmptyStyleAttribute() {
        val input = EMPTY_STYLE_HTML
        val text = SpannableString(parser.fromHtml(input, RuntimeEnvironment.application.applicationContext))

        val span = text.getSpans(0, text.length, AztecStyleBoldSpan::class.java).first()

        Assert.assertTrue(span.attributes.isEmpty())

        Assert.assertFalse(CssStyleFormatter.containsStyleAttribute(span.attributes, "test"))

        Assert.assertEquals("", CssStyleFormatter.getStyleAttribute(span.attributes, "test"))
    }

    @Test
    fun testStyleAttributeAbsence() {
        val input = HTML
        val text = SpannableString(parser.fromHtml(input, RuntimeEnvironment.application.applicationContext))

        val span = text.getSpans(0, text.length, AztecStyleBoldSpan::class.java).first()

        Assert.assertFalse(span.attributes.isEmpty())

        Assert.assertFalse(CssStyleFormatter.containsStyleAttribute(span.attributes, "test"))

        Assert.assertEquals("", CssStyleFormatter.getStyleAttribute(span.attributes, "test"))
    }

    @Test
    fun testStyleAttributePresence() {
        val input = HTML
        val text = SpannableString(parser.fromHtml(input, RuntimeEnvironment.application.applicationContext))

        val span = text.getSpans(0, text.length, AztecStyleBoldSpan::class.java).first()

        Assert.assertTrue(CssStyleFormatter.containsStyleAttribute(span.attributes, "name"))

        Assert.assertEquals("value", CssStyleFormatter.getStyleAttribute(span.attributes, "name"))
    }

    @Test
    fun testStyleAttributeAdding() {
        val input = EMPTY_STYLE_HTML
        val text = SpannableString(parser.fromHtml(input, RuntimeEnvironment.application.applicationContext))

        val span = text.getSpans(0, text.length, AztecStyleBoldSpan::class.java).first()

        CssStyleFormatter.addStyleAttribute(span.attributes, "name", "value")

        Assert.assertEquals(HTML, parser.toHtml(text))

        Assert.assertFalse(CssStyleFormatter.containsStyleAttribute(span.attributes, "a"))

        CssStyleFormatter.addStyleAttribute(span.attributes, "a", "b")

        Assert.assertTrue(CssStyleFormatter.containsStyleAttribute(span.attributes, "a"))

        Assert.assertEquals("b", CssStyleFormatter.getStyleAttribute(span.attributes, "a"))
    }

    @Test
    fun testStyleAttributeRemoval() {
        val input = COMPLEX_HTML
        val text = SpannableString(parser.fromHtml(input, RuntimeEnvironment.application.applicationContext))

        val span = text.getSpans(0, text.length, AztecStyleBoldSpan::class.java).first()

        Assert.assertTrue(CssStyleFormatter.containsStyleAttribute(span.attributes, "a"))

        CssStyleFormatter.removeStyleAttribute(span.attributes, "a")

        Assert.assertFalse(CssStyleFormatter.containsStyleAttribute(span.attributes, "a"))

        Assert.assertTrue(CssStyleFormatter.containsStyleAttribute(span.attributes, "name"))

        Assert.assertEquals(HTML, parser.toHtml(text))

        CssStyleFormatter.removeStyleAttribute(span.attributes, "name")

        Assert.assertEquals(EMPTY_STYLE_HTML, parser.toHtml(text))
    }
}
