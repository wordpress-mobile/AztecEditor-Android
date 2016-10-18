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
import android.graphics.Rect
import android.os.Parcel
import android.text.Layout
import android.text.TextUtils
import android.text.style.LineBackgroundSpan
import android.text.style.QuoteSpan


class AztecQuoteSpan : QuoteSpan, LineBackgroundSpan, AztecBlockSpan {

    val rect = Rect()

    private val TAG: String = "blockquote"

    private var quoteBackground: Int = 0
    private var quoteColor: Int = 0
    private var quoteMargin: Int = 0
    private var quotePadding: Int = 0
    private var quoteWidth: Int = 0

    override var attributes: String? = null


    constructor(attributes: String? = null) : super() {
        this.attributes = attributes
    }

    constructor(quoteBackground: Int, quoteColor: Int, quoteMargin: Int, quoteWidth: Int, quotePadding: Int, attributes: String? = null) : this(attributes) {
        this.quoteBackground = quoteBackground
        this.quoteColor = quoteColor
        this.quoteMargin = quoteMargin
        this.quoteWidth = quoteWidth
        this.quotePadding = quotePadding
    }

    constructor(src: Parcel) : super(src) {
        this.quoteBackground = src.readInt()
        this.quoteColor = src.readInt()
        this.quoteMargin = src.readInt()
        this.quoteWidth = src.readInt()
        this.quotePadding = src.readInt()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeInt(quoteBackground)
        dest.writeInt(quoteColor)
        dest.writeInt(quoteMargin)
        dest.writeInt(quoteWidth)
        dest.writeInt(quotePadding)
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

        rect.set(left + quoteMargin, top, right, bottom)

        c.drawRect(rect, p)
        p.color = paintColor
    }
}
