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
import android.text.style.LineBackgroundSpan
import android.text.style.QuoteSpan

class AztecQuoteSpan : QuoteSpan, LineBackgroundSpan {

    private var quoteBackground: Int = 0
    private var quoteColor: Int = 0
    private var quoteMargin: Int = 0
    private var quotePadding: Int = 0
    private var quoteTextColor: Int = 0
    private var quoteWidth: Int = 0

    constructor(quoteTextColor: Int, quoteBackground: Int, quoteColor: Int, quoteMargin: Int, quoteWidth: Int, quotePadding: Int) {
        this.quoteTextColor = quoteTextColor
        this.quoteBackground = quoteBackground
        this.quoteColor = quoteColor
        this.quoteMargin = quoteMargin
        this.quoteWidth = quoteWidth
        this.quotePadding = quotePadding
    }

    constructor(src: Parcel) : super(src) {
        this.quoteTextColor = src.readInt()
        this.quoteBackground = src.readInt()
        this.quoteColor = src.readInt()
        this.quoteMargin = src.readInt()
        this.quoteWidth = src.readInt()
        this.quotePadding = src.readInt()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeInt(quoteTextColor)
        dest.writeInt(quoteBackground)
        dest.writeInt(quoteColor)
        dest.writeInt(quoteMargin)
        dest.writeInt(quoteWidth)
        dest.writeInt(quotePadding)
    }

    override fun getLeadingMargin(first: Boolean): Int {
        return quoteMargin + quoteWidth + quotePadding
    }

    override fun drawLeadingMargin(c: Canvas, p: Paint, x: Int, dir: Int,
                                   top: Int, baseline: Int, bottom: Int,
                                   text: CharSequence, start: Int, end: Int,
                                   first: Boolean, layout: Layout) {
        val style = p.style
        val color = p.color

        p.style = Paint.Style.FILL
        p.color = quoteColor
        c.drawRect(x.toFloat() + quoteMargin, top.toFloat(), (x + quoteMargin + dir * quoteWidth).toFloat(), bottom.toFloat(), p)

        p.style = style
        p.color = color
    }

    override fun drawBackground(c: Canvas, p: Paint, left: Int, right: Int,
                                top: Int, baseline: Int, bottom: Int,
                                text: CharSequence?, start: Int, end: Int,
                                lnum: Int) {
        val paintColor = p.color
        p.color = quoteBackground
        c.drawRect(left.toFloat() + quoteMargin, top.toFloat(), right.toFloat(), bottom.toFloat(), p)
        p.color = paintColor
    }
}
