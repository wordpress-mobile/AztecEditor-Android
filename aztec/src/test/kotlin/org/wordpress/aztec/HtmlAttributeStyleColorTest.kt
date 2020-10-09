@file:Suppress("DEPRECATION")

package org.wordpress.aztec

import android.test.AndroidTestCase
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.test.core.app.ApplicationProvider
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.wordpress.aztec.spans.IAztecAttributedSpan

/**
 * Tests covering the Html style attribute - *color* property.
 * Example:
 * <pre><code>
 *     <b style="color:red">Color me red!</b>
 * </code></pre>
 * Tests the [ForegroundColorSpan] is properly added to various [IAztecAttributedSpan]s that
 * cover the two places where these events happen:
 * 1. [Html] during the [Html.fromHtml] processing.
 * 2. [AztecTagHandler] while processing more complex tags.
 *
 * Also tests invalid html style attribute color properties.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = intArrayOf(18, 21, 25))
class HtmlAttributeStyleColorTest : AndroidTestCase() {

    private val HTML_BOLD_STYLE_COLOR = "<b style=\"color:blue;\">Blue</b>"
    private val HTML_BOLD_STYLE_INVALID = "<b style=\"color:@java;\">Blue</b>"

    private val HTML_STRIKE_STYLE_COLOR = "<s style=\"color:red;\">Red</s>"
    private val HTML_STRIKE_STYLE_INVALID = "<s style=\"color:\">Red</s>"

    private val HTML_UL_ITEM_ONLY_COLOR = "<ul><li>No Color</li><li style=\"color:@black;\">Black</li></ul>"
    private val HTML_OL_WHOLE_LIST_COLOR = "<ol style=\"color:#FF00FF00;\"><li>Green</li><li>Still green</li></ol>"

    private lateinit var parser: AztecParser

    @Before
    fun init() {
        context = ApplicationProvider.getApplicationContext()
        parser = AztecParser(AlignmentRendering.SPAN_LEVEL)
    }

    /**
     * Nested tag test.
     * Test only the second list item has a [ForegroundColorSpan].
     */
    @Test
    fun formatNestedListItemColor() {
        val input = HTML_UL_ITEM_ONLY_COLOR
        val span = SpannableString(parser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        //
        // Verify only a single [ForegroundColorSpan] exists
        val colorSpan = span.getSpans(6, span.length, ForegroundColorSpan::class.java)
        Assert.assertEquals(1, colorSpan.count())
        //
        // Verify the [ForegroundColorSpan] only covers the text of the single
        // list item.
        val startTextIndex = span.indexOf("Black", 0, true)
        val colorSpanStart = span.getSpanStart(colorSpan[0])
        val colorSpanEnd = span.getSpanEnd(colorSpan[0])
        Assert.assertEquals(startTextIndex, colorSpanStart)
        Assert.assertEquals(startTextIndex + "Black".length, colorSpanEnd)
    }

    /**
     * Nested tag test.
     * Test the whole list has a [ForegroundColorSpan].
     */
    @Test
    fun formatNestedListColor() {
        val input = HTML_OL_WHOLE_LIST_COLOR
        val span = SpannableString(parser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        //
        // Verify only a single [ForegroundColorSpan] exists
        val colorSpan = span.getSpans(0, span.length, ForegroundColorSpan::class.java)
        Assert.assertEquals(1, colorSpan.count())
        //
        // Verify the [ForegroundColorSpan] covers the entire length of the list span
        val colorSpanStart = span.getSpanStart(colorSpan[0])
        val colorSpanEnd = span.getSpanEnd(colorSpan[0])
        Assert.assertEquals(0, colorSpanStart)
        Assert.assertEquals(colorSpanEnd, span.length)
    }

    /**
     * Test color styles applied by the [Html] class. A [ForegroundColorSpan] should
     * be created for the valid color style property.
     */
    @Test
    fun formatBoldStyleColorAttributeValid() {
        val input = HTML_BOLD_STYLE_COLOR
        val span = SpannableString(parser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val colorSpanCount = span.getSpans(0, span.length, ForegroundColorSpan::class.java).count()
        Assert.assertEquals(1, colorSpanCount)
    }

    /**
     * Test color styles applied by the [Html] class. Since the color property is invalid,
     * no [ForegroundColorSpan] should exist inside the span.
     */
    @Test
    fun formatBoldStyleColorAttributeInvalid() {
        val input = HTML_BOLD_STYLE_INVALID
        val span = SpannableString(parser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val colorSpanCount = span.getSpans(0, span.length, ForegroundColorSpan::class.java).count()
        Assert.assertEquals(0, colorSpanCount)
    }

    /**
     * Test color styles applied by the [AztecTagHandler] class. A [ForegroundColorSpan] should
     * be created for the valid color style property.
     */
    @Test
    fun formatStrikeStyleColorAttributeValid() {
        val input = HTML_STRIKE_STYLE_COLOR
        val span = SpannableString(parser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val colorSpanCount = span.getSpans(0, span.length, ForegroundColorSpan::class.java).count()
        Assert.assertEquals(1, colorSpanCount)
    }

    /**
     * Test color styles applied by the [AztecTagHandler] class. Since the color property is invalid,
     * no [ForegroundColorSpan] should exist inside the span.
     */
    @Test
    fun formatStrikeStyleColorAttributeInvalid() {
        val input = HTML_STRIKE_STYLE_INVALID
        val span = SpannableString(parser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val colorSpanCount = span.getSpans(0, span.length, ForegroundColorSpan::class.java).count()
        Assert.assertEquals(0, colorSpanCount)
    }
}
