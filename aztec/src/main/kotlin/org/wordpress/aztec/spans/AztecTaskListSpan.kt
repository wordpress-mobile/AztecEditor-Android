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

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Layout
import android.text.Spanned
import org.wordpress.aztec.AlignmentRendering
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.R
import org.wordpress.aztec.formatting.BlockFormatter
import org.wordpress.aztec.setTaskList

fun createTaskListSpan(
        nestingLevel: Int,
        alignmentRendering: AlignmentRendering,
        attributes: AztecAttributes = AztecAttributes(),
        context: Context,
        listStyle: BlockFormatter.ListStyle = BlockFormatter.ListStyle(0, 0, 0, 0, 0)
) = when (alignmentRendering) {
    AlignmentRendering.SPAN_LEVEL -> AztecTaskListSpanAligned(nestingLevel, attributes, context, listStyle, null)
    AlignmentRendering.VIEW_LEVEL -> AztecTaskListSpan(nestingLevel, attributes, context, listStyle)
}

/**
 * We need to have two classes for handling alignment at either the Span-level (AztecTaskListSpanAligned)
 * or the View-level (AztecTaskListSpan). IAztecAlignment implements AlignmentSpan, which has a
 * getAlignment method that returns a non-null Layout.Alignment. The Android system checks for
 * AlignmentSpans and, if present, overrides the view's gravity with their value. Having a class
 * that does not implement AlignmentSpan allows the view's gravity to control. These classes should
 * be created using the createTaskListSpan(...) methods.
 */
class AztecTaskListSpanAligned(
        nestingLevel: Int,
        attributes: AztecAttributes = AztecAttributes(),
        context: Context,
        listStyle: BlockFormatter.ListStyle = BlockFormatter.ListStyle(0, 0, 0, 0, 0),
        override var align: Layout.Alignment?
) : AztecTaskListSpan(nestingLevel, attributes, context, listStyle), IAztecAlignmentSpan

open class AztecTaskListSpan(
        override var nestingLevel: Int,
        override var attributes: AztecAttributes = AztecAttributes(),
        val context: Context,
        var listStyle: BlockFormatter.ListStyle = BlockFormatter.ListStyle(0, 0, 0, 0, 0),
        var onRefresh: ((AztecTaskListSpan) -> Unit)? = null
) : AztecListSpan(nestingLevel, listStyle.verticalPadding) {
    private var toggled: Boolean = false
    override val TAG = "ul"

    override val startTag: String
        get() {
            attributes.setTaskList()
            return "$TAG $attributes"
        }

    override fun getLeadingMargin(first: Boolean): Int {
        return listStyle.indicatorMargin + 2 * listStyle.indicatorWidth + listStyle.indicatorPadding
    }

    override fun drawLeadingMargin(c: Canvas, p: Paint, x: Int, dir: Int,
                                   top: Int, baseline: Int, bottom: Int,
                                   text: CharSequence, start: Int, end: Int,
                                   first: Boolean, l: Layout) {
        if (!first) return

        val spanStart = (text as Spanned).getSpanStart(this)
        val spanEnd = text.getSpanEnd(this)

        if (start !in spanStart..spanEnd || end !in spanStart..spanEnd) return

        val lineIndex = getIndexOfProcessedLine(text, end) ?: return

        val style = p.style
        val oldColor = p.color

        p.color = listStyle.indicatorColor
        p.style = Paint.Style.FILL

        // Make sure the marker is correctly aligned on RTL languages
        val markerStartPosition: Float = x + (listStyle.indicatorMargin * dir) * 1f
        val d: Drawable = context.resources.getDrawable(R.drawable.ic_checkbox, null)
        val leftBound = markerStartPosition.toInt()
        if (isChecked(text, lineIndex)) {
            d.state = intArrayOf(android.R.attr.state_checked)
        } else {
            d.state = intArrayOf()
        }
        d.setBounds(leftBound - 40, baseline - 40, leftBound + 8, baseline + 8)
        d.draw(c)

        p.color = oldColor
        p.style = style
    }

    fun canToggle(): Boolean {
        val canToggle = !toggled
        toggled = canToggle
        return canToggle
    }

    fun refresh() {
        onRefresh?.invoke(this)
    }

    private fun isChecked(text: CharSequence, lineIndex: Int): Boolean {
        val spanStart = (text as Spanned).getSpanStart(this)
        val spanEnd = text.getSpanEnd(this)
        return text.getSpans(spanStart, spanEnd, AztecListItemSpan::class.java).getOrNull(lineIndex - 1)?.attributes?.getValue("checked") == "true"
    }
}
