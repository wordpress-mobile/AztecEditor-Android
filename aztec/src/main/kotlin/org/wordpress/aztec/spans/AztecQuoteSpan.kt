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
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.support.v4.text.TextDirectionHeuristicCompat
import android.support.v4.text.TextDirectionHeuristicsCompat
import android.support.v4.text.TextUtilsCompat
import android.support.v4.util.ArrayMap
import android.support.v4.view.ViewCompat
import android.text.Editable
import android.text.Layout
import android.text.Spanned
import android.text.style.LineBackgroundSpan
import android.text.style.LineHeightSpan
import android.text.style.QuoteSpan
import android.text.style.UpdateLayout
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.formatting.BlockFormatter
import java.util.Locale

class AztecQuoteSpan(
        override var nestingLevel: Int,
        override var attributes: AztecAttributes = AztecAttributes(),
        var quoteStyle: BlockFormatter.QuoteStyle = BlockFormatter.QuoteStyle(0, 0, 0f, 0, 0, 0, 0),
        override var align: Layout.Alignment? = null)
    : QuoteSpan(), LineBackgroundSpan, IAztecBlockSpan, LineHeightSpan, UpdateLayout {

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1

    private val rect = Rect()
    private var offset: Int = 0
    private val quoteStart = ArrayMap<Int, Float>()

    override val TAG: String = "blockquote"

    override fun chooseHeight(text: CharSequence, start: Int, end: Int, spanstartv: Int, v: Int, fm: Paint.FontMetricsInt) {
        val spanned = text as Spanned
        val spanStart = spanned.getSpanStart(this)
        val spanEnd = spanned.getSpanEnd(this)

        if (start == spanStart || start < spanStart) {
            fm.ascent -= quoteStyle.verticalPadding
            fm.top -= quoteStyle.verticalPadding
        }
        if (end == spanEnd || spanEnd < end) {
            fm.descent += quoteStyle.verticalPadding
            fm.bottom += quoteStyle.verticalPadding
        }
    }

    override fun getLeadingMargin(first: Boolean): Int {
        return quoteStyle.quoteMargin + quoteStyle.quoteWidth + quoteStyle.quotePadding - offset
    }

    override fun drawLeadingMargin(c: Canvas, p: Paint, x: Int, dir: Int,
                                   top: Int, baseline: Int, bottom: Int,
                                   text: CharSequence, start: Int, end: Int,
                                   first: Boolean, layout: Layout) {
        val style = p.style
        val color = p.color

        p.style = Paint.Style.FILL
        p.color = quoteStyle.quoteColor

        val editable = text as Editable
        val isWithinListItem = isWithinListItem(editable, start, end)
        val isRtl = isRtlQuote(text, start, end)

        val margin: Int
        if (isWithinListItem) {
            margin = x
            offset = quoteStyle.quoteMargin
        } else {
            margin = if (isRtl) {
                x - quoteStyle.quoteMargin
            } else {
                x + quoteStyle.quoteMargin
            }
            offset = 0
        }

        val marginStart: Float
        val marginEnd: Float

        if (isRtl) {
            marginStart = (margin + dir * quoteStyle.quoteWidth).toFloat()
            marginEnd = margin.toFloat()

            quoteStart[start] = marginStart
        } else {
            marginStart = margin.toFloat()
            marginEnd = (margin + dir * quoteStyle.quoteWidth).toFloat()

            quoteStart[start] = marginEnd
        }

        c.drawRect(marginStart, top.toFloat(), marginEnd, bottom.toFloat(), p)

        p.style = style
        p.color = color
    }

    private fun isWithinListItem(editable: Editable, start: Int, end: Int): Boolean {
        return editable.getSpans(start, end, AztecListItemSpan::class.java)
                .any { it.nestingLevel == nestingLevel - 1 }
    }

    override fun drawBackground(c: Canvas, p: Paint, left: Int, right: Int,
                                top: Int, baseline: Int, bottom: Int,
                                text: CharSequence, start: Int, end: Int,
                                lnum: Int) {
        val alpha: Int = (quoteStyle.quoteBackgroundAlpha * 255).toInt()

        val paintColor = p.color
        p.color = Color.argb(
                alpha,
                Color.red(quoteStyle.quoteBackground),
                Color.green(quoteStyle.quoteBackground),
                Color.blue(quoteStyle.quoteBackground))

        val quoteBackgroundStart: Int
        val quoteBackgroundEnd: Int

        val isRtl = isRtlQuote(text, start, end)

        if (isRtl) {
            quoteBackgroundStart = left
            quoteBackgroundEnd = quoteStart[start]?.toInt() ?: 0
        } else {
            quoteBackgroundStart = quoteStart[start]?.toInt() ?: 0
            quoteBackgroundEnd = right
        }

        rect.set(quoteBackgroundStart, top, quoteBackgroundEnd, bottom)

        c.drawRect(rect, p)
        p.color = paintColor
    }

    private fun isRtlQuote(text: CharSequence, start: Int, end: Int): Boolean {
        val textDirectionHeuristic: TextDirectionHeuristicCompat =
                if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                    TextDirectionHeuristicsCompat.FIRSTSTRONG_RTL
                } else {
                    TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR
                }
        return textDirectionHeuristic.isRtl(text, start, end - start)
    }

}