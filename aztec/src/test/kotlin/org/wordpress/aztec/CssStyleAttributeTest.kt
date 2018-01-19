@file:Suppress("DEPRECATION")

package org.wordpress.aztec

import android.test.AndroidTestCase
import android.test.mock.MockContext
import android.text.SpannableString
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.wordpress.aztec.source.InlineCssStyleFormatter
import org.wordpress.aztec.spans.AztecStyleBoldSpan

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(16, 21, 25))
class CssStyleAttributeTest : AndroidTestCase() {

    private val EMPTY_STYLE_HTML = "<b>bold</b>"
    private val HTML = "<b style=\"name: value\">bold</b>"
    private val COMPLEX_HTML = "<b style=\"a: b; name: value\">bold</b>"

    private lateinit var parser: AztecParser

    @Before
    fun init() {
        context = MockContext()
        parser = AztecParser()
    }

    @Test
    fun testEmptyStyleAttribute() {
        val input = EMPTY_STYLE_HTML
        val text = SpannableString(parser.fromHtml(input, RuntimeEnvironment.application.applicationContext))

        val span = text.getSpans(0, text.length, AztecStyleBoldSpan::class.java).first()

        Assert.assertTrue(span.attributes.isEmpty())

        Assert.assertFalse(InlineCssStyleFormatter.containsStyleAttribute(span.attributes, "test"))

        Assert.assertEquals(InlineCssStyleFormatter.getStyleAttribute(span.attributes, "test"), "")
    }

    @Test
    fun testStyleAttributeAbsence() {
        val input = HTML
        val text = SpannableString(parser.fromHtml(input, RuntimeEnvironment.application.applicationContext))

        val span = text.getSpans(0, text.length, AztecStyleBoldSpan::class.java).first()

        Assert.assertFalse(span.attributes.isEmpty())

        Assert.assertFalse(InlineCssStyleFormatter.containsStyleAttribute(span.attributes, "test"))

        Assert.assertEquals(InlineCssStyleFormatter.getStyleAttribute(span.attributes, "test"), "")
    }

    @Test
    fun testStyleAttributePresence() {
        val input = HTML
        val text = SpannableString(parser.fromHtml(input, RuntimeEnvironment.application.applicationContext))

        val span = text.getSpans(0, text.length, AztecStyleBoldSpan::class.java).first()

        Assert.assertTrue(InlineCssStyleFormatter.containsStyleAttribute(span.attributes, "name"))

        Assert.assertEquals(InlineCssStyleFormatter.getStyleAttribute(span.attributes, "name"), "value")
    }

    @Test
    fun testStyleAttributeAdding() {
        val input = EMPTY_STYLE_HTML
        val text = SpannableString(parser.fromHtml(input, RuntimeEnvironment.application.applicationContext))

        val span = text.getSpans(0, text.length, AztecStyleBoldSpan::class.java).first()

        InlineCssStyleFormatter.addStyleAttribute(span.attributes, "name", "value")

        Assert.assertEquals(parser.toHtml(text), HTML)

        Assert.assertFalse(InlineCssStyleFormatter.containsStyleAttribute(span.attributes, "a"))

        InlineCssStyleFormatter.addStyleAttribute(span.attributes, "a", "b")

        Assert.assertTrue(InlineCssStyleFormatter.containsStyleAttribute(span.attributes, "a"))

        Assert.assertEquals(InlineCssStyleFormatter.getStyleAttribute(span.attributes, "a"), "b")
    }

    @Test
    fun testStyleAttributeRemoval() {
        val input = COMPLEX_HTML
        val text = SpannableString(parser.fromHtml(input, RuntimeEnvironment.application.applicationContext))

        val span = text.getSpans(0, text.length, AztecStyleBoldSpan::class.java).first()

        Assert.assertTrue(InlineCssStyleFormatter.containsStyleAttribute(span.attributes, "a"))

        InlineCssStyleFormatter.removeStyleAttribute(span.attributes, "a")

        Assert.assertFalse(InlineCssStyleFormatter.containsStyleAttribute(span.attributes, "a"))

        Assert.assertTrue(InlineCssStyleFormatter.containsStyleAttribute(span.attributes, "name"))

        Assert.assertEquals(parser.toHtml(text), HTML)

        InlineCssStyleFormatter.removeStyleAttribute(span.attributes, "name")

        Assert.assertEquals(parser.toHtml(text), EMPTY_STYLE_HTML)
    }
}