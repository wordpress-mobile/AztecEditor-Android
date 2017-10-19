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
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.formatting.BlockFormatter

class AztecOrderedListSpan(
        override var nestingLevel: Int,
        override var attributes: AztecAttributes = AztecAttributes(),
        var listStyle: BlockFormatter.ListStyle = BlockFormatter.ListStyle(0, 0, 0, 0, 0)
) : AztecListSpan(nestingLevel, listStyle.verticalPadding) {
    override val TAG = "ol"

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

        val oldStyle = p.style
        val oldColor = p.color
        val oldTextSize = p.textSize

        p.color = listStyle.indicatorColor
        p.style = Paint.Style.FILL

        val lineIndex = getIndexOfProcessedLine(text, end)
        val textToDraw = if (lineIndex > -1) lineIndex.toString() + "." else ""

        var width = p.measureText(textToDraw)
        var xStartDraw = (listStyle.indicatorMargin + x + dir - width) * dir

        // If we can't draw the item number in the available space, try with smaller text size until it fits the available space
        while (xStartDraw < 0) {
            p.textSize = p.textSize - 1
            width = p.measureText(textToDraw)
            xStartDraw = (listStyle.indicatorMargin + x + dir - width) * dir
        }

        c.drawText(textToDraw, xStartDraw, baseline.toFloat(), p)

        p.color = oldColor
        p.style = oldStyle
        p.textSize = oldTextSize
    }
}
