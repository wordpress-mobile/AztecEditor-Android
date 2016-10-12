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
import android.os.Parcel
import android.text.Layout
import android.text.Spanned
import android.text.TextUtils
import android.text.style.LeadingMarginSpan

class AztecOrderedListSpan : LeadingMarginSpan.Standard, AztecListSpan {

    private final val TAG = "ol"

    private var textColor: Int = 0
    private var textMargin: Int = 0
    private var textPadding: Int = 0
    private var bulletWidth: Int = 0 //we are using bullet width to maintain same margin with bullet list

    override var attributes: String? = null

    //used for marking
    constructor() : super(0) {
        attributes = null
    }

    constructor(attributes: String) : super(0) {
        this.attributes = attributes
    }

    constructor(textColor: Int, textMargin: Int, bulletWidth: Int, textPadding: Int, attributes: String? = null) : super(textMargin) {
        this.textColor = textColor
        this.textMargin = textMargin
        this.bulletWidth = bulletWidth
        this.textPadding = textPadding
        this.attributes = attributes
    }

    constructor(src: Parcel) : super(src) {
        this.textColor = src.readInt()
        this.textMargin = src.readInt()
        this.textPadding = src.readInt()
        this.attributes = src.readString()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeInt(textColor)
        dest.writeInt(textMargin)
        dest.writeInt(textPadding)
        dest.writeString(attributes)
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

    fun getLineNumber(text: CharSequence, end: Int): Int {
        val textBeforeBeforeEnd = text.substring(0, end)
        val lineIndex = textBeforeBeforeEnd.length - textBeforeBeforeEnd.replace("\n", "").length
        return lineIndex + 1
    }


    override fun drawLeadingMargin(c: Canvas, p: Paint, x: Int, dir: Int,
                                   top: Int, baseline: Int, bottom: Int,
                                   text: CharSequence, start: Int, end: Int,
                                   first: Boolean, l: Layout) {

        if (!first) return

        val spanStart = (text as Spanned).getSpanStart(this)
        val spanEnd = text.getSpanEnd(this)

        if (start !in spanStart..spanEnd || end !in spanStart..spanEnd) return

        val listText = text.subSequence(spanStart, spanEnd)

        val lineNumber = getLineNumber(listText, end - spanStart)

        val style = p.style

        val oldColor = p.color

        p.color = textColor
        p.style = Paint.Style.FILL

        val textToDraw = lineNumber.toString() + "."

        val width = p.measureText(textToDraw)
        c.drawText(textToDraw, (textMargin + x + dir - width) * dir, bottom - p.descent(), p)

        p.color = oldColor
        p.style = style

    }

}