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

package org.wordpress.aztec

import android.graphics.Canvas
import android.graphics.Paint
import android.os.Parcel
import android.text.Layout
import android.text.style.QuoteSpan

class AztecQuoteSpan : QuoteSpan {

    private var quoteColor: Int = 0
    private var quoteStripeWidth: Int = 0
    private var quoteGapWidth: Int = 0
    private var extraBreaks: Int = 0

    constructor(quoteColor: Int, quoteStripeWidth: Int, quoteGapWidth: Int) {
        this.quoteColor = if (quoteColor != 0) quoteColor else DEFAULT_COLOR
        this.quoteStripeWidth = if (quoteStripeWidth != 0) quoteStripeWidth else DEFAULT_STRIPE_WIDTH
        this.quoteGapWidth = if (quoteGapWidth != 0) quoteGapWidth else DEFAULT_GAP_WIDTH
    }

    constructor(src: Parcel) : super(src) {
        this.quoteColor = src.readInt()
        this.quoteStripeWidth = src.readInt()
        this.quoteGapWidth = src.readInt()
        this.extraBreaks = src.readInt()
    }

    constructor(extra: Int) {
        extraBreaks = extra;
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeInt(quoteColor)
        dest.writeInt(quoteStripeWidth)
        dest.writeInt(quoteGapWidth)
        dest.writeInt(extraBreaks)
    }

    override fun getLeadingMargin(first: Boolean): Int {
        return quoteStripeWidth + quoteGapWidth
    }

    override fun drawLeadingMargin(c: Canvas, p: Paint, x: Int, dir: Int,
                                   top: Int, baseline: Int, bottom: Int,
                                   text: CharSequence, start: Int, end: Int,
                                   first: Boolean, layout: Layout) {
        val style = p.style
        val color = p.color

        p.style = Paint.Style.FILL
        p.color = quoteColor
        c.drawRect(x.toFloat(), top.toFloat(), (x + dir * quoteGapWidth).toFloat(), bottom.toFloat(), p)

        p.style = style
        p.color = color
    }

    fun getExtraBreaks() : Int {
        return extraBreaks
    }

    companion object {
        private val DEFAULT_STRIPE_WIDTH = 2
        private val DEFAULT_GAP_WIDTH = 2
        private val DEFAULT_COLOR = 0xff0000ff.toInt()
    }
}
