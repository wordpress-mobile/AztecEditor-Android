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

class AztecUnorderedListSpan(
        override var nestingLevel: Int,
        override var attributes: AztecAttributes = AztecAttributes(),
        var listStyle: BlockFormatter.ListStyle = BlockFormatter.ListStyle(0, 0, 0, 0, 0)
    ) : AztecListSpan(nestingLevel, listStyle.verticalPadding) {
    override val TAG = "ul"

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

        val style = p.style
        val oldColor = p.color

        p.color = listStyle.indicatorColor
        p.style = Paint.Style.FILL

        val lineIndex = getIndexOfProcessedLine(text, end)
        val textToDraw = if (lineIndex > -1) "\u2022" else ""

        val width = p.measureText(textToDraw)

        // Make sure the marker is correctly aligned on RTL languages
        var markerStartPosition: Float = x + (listStyle.indicatorMargin * dir) * 1f
        if (dir == 1)
            markerStartPosition -= width

        c.drawText(textToDraw, markerStartPosition , (baseline + (width - p.descent())), p)

        p.color = oldColor
        p.style = style
    }
}
