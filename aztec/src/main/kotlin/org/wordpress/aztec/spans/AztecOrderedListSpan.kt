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
import android.text.TextUtils
import org.wordpress.aztec.formatting.BlockFormatter

class AztecOrderedListSpan : AztecListSpan {

    private val TAG = "ol"

    private var textColor: Int = 0
    private var textMargin: Int = 0
    private var textPadding: Int = 0
    private var bulletWidth: Int = 0 //we are using bullet width to maintain same margin with bullet list

    override var attributes: String = ""
    override var lastItem: AztecListItemSpan = AztecListItemSpan()

    constructor(attributes: String) : super(0) {
        this.attributes = attributes
    }

    constructor(listStyle: BlockFormatter.ListStyle, attributes: String, last: AztecListItemSpan) : super(listStyle.verticalPadding) {
        this.textColor = listStyle.indicatorColor
        this.textMargin = listStyle.indicatorMargin
        this.bulletWidth = listStyle.indicatorWidth
        this.textPadding = listStyle.indicatorPadding
        this.attributes = attributes
        this.lastItem = last
    }


    override fun getStartTag(): String {
        if (TextUtils.isEmpty(attributes)) {
            return TAG
        }
        return TAG + attributes
    }

    override fun getEndTag(): String {
        return TAG
    }

    override fun getLeadingMargin(first: Boolean): Int {
        return textMargin + 2 * bulletWidth + textPadding
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

        p.color = textColor
        p.style = Paint.Style.FILL

        val textToDraw = getIndexOfProcessedLine(text, end).toString() + "."

        val width = p.measureText(textToDraw)
        c.drawText(textToDraw, (textMargin + x + dir - width) * dir, (bottom - p.descent()) + getIndicatorAdjustment(text, end), p)

        p.color = oldColor
        p.style = style

    }

}