/*
 * Copyright (C) 2016 Automattic
 * Copyright (C) 2015 Matthew Lee
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wordpress.aztec.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.Spanned
import org.wordpress.aztec.AlignmentRendering
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.formatting.BlockFormatter

fun createOrderedListSpan(
        nestingLevel: Int,
        alignmentRendering: AlignmentRendering,
        attributes: AztecAttributes = AztecAttributes(),
        listStyle: BlockFormatter.ListStyle = BlockFormatter.ListStyle(0, 0, 0, 0, 0)
) = when (alignmentRendering) {
    AlignmentRendering.SPAN_LEVEL -> AztecOrderedListSpanAligned(nestingLevel, attributes, listStyle, null)
    AlignmentRendering.VIEW_LEVEL -> AztecOrderedListSpan(nestingLevel, attributes, listStyle)
}

/**
 * We need to have two classes for handling alignment at either the Span-level (AztecOrderedListSpanAligned)
 * or the View-level (AztecOrderedListSpan). IAztecAlignment implements AlignmentSpan, which has a
 * getAlignment method that returns a non-null Layout.Alignment. The Android system checks for
 * AlignmentSpans and, if present, overrides the view's gravity with their value. Having a class
 * that does not implement AlignmentSpan allows the view's gravity to control. These classes should
 * be created using the createOrderedListSpan(...) methods.
 */
class AztecOrderedListSpanAligned(
        nestingLevel: Int,
        attributes: AztecAttributes = AztecAttributes(),
        listStyle: BlockFormatter.ListStyle = BlockFormatter.ListStyle(0, 0, 0, 0, 0),
        override var align: Layout.Alignment?
) : AztecOrderedListSpan(nestingLevel, attributes, listStyle), IAztecAlignmentSpan

open class AztecOrderedListSpan(
        override var nestingLevel: Int,
        override var attributes: AztecAttributes = AztecAttributes(),
        var listStyle: BlockFormatter.ListStyle = BlockFormatter.ListStyle(0, 0, 0, 0, 0)
) : AztecListSpan(nestingLevel, listStyle.verticalPadding) {
    override val TAG = "ol"

    private var horizontalShift = 0
    private var maxWidth = 0f

    override fun getLeadingMargin(first: Boolean): Int {
        return listStyle.indicatorMargin + 2 * listStyle.indicatorWidth + listStyle.indicatorPadding + horizontalShift
    }

    override fun drawLeadingMargin(c: Canvas, p: Paint, x: Int, dir: Int,
                                   top: Int, baseline: Int, bottom: Int,
                                   text: CharSequence, start: Int, end: Int,
                                   first: Boolean, l: Layout) {
        if (!first) return

        val spanStart = (text as Spanned).getSpanStart(this)
        val spanEnd = text.getSpanEnd(this)

        if (start !in spanStart..spanEnd || end !in spanStart..spanEnd) return

        val style = p.style
        val oldColor = p.color

        p.color = listStyle.indicatorColor
        p.style = Paint.Style.FILL

        val start = if (attributes.hasAttribute("start") == true) {
            attributes.getValue("start").toInt()
        } else {
            0
        }

        var textToDraw = ""
        getIndexOfProcessedLine(text, end)?.let {
            val isReversed = attributes.hasAttribute("reversed")
            val lineIndex = if (start > 0) {
                if (isReversed) start - (it - 1)
                else start + (it - 1)
            } else {
                val number = getNumberOfItemsInProcessedLine(text)
                if (isReversed) number - (it - 1)
                else it
            }

            textToDraw = if (dir >= 0) lineIndex.toString() + "."
            else "." + lineIndex.toString()
        }

        val width = p.measureText(textToDraw)
        maxWidth = Math.max(maxWidth, width)

        // Make sure the item number is correctly aligned on RTL languages
        var textStart: Float = x + (listStyle.indicatorMargin * dir) * 1f
        if (dir == 1)
            textStart -= width

        if (textStart < 0) {
            horizontalShift = -textStart.toInt()
            textStart = 0f
        }

        if (horizontalShift > 0 && width < maxWidth) {
            textStart += horizontalShift
        }

        c.drawText(textToDraw, textStart, baseline.toFloat(), p)

        p.color = oldColor
        p.style = style
    }
}
