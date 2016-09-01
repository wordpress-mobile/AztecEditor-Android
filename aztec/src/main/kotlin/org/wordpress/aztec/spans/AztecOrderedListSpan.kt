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
import android.graphics.Path
import android.os.Parcel
import android.text.Layout
import android.text.Spanned
import android.text.TextUtils
import android.text.style.LeadingMarginSpan

class AztecOrderedListSpan : LeadingMarginSpan.Standard, AztecListSpan {

    override fun getTag(): String {
        return "ol"
    }

    private var bulletColor: Int = 0
    private var bulletMargin: Int = 0
    private var bulletPadding: Int = 0
    private var bulletWidth: Int = 0


    //used for marking
    constructor() : super(0) {
    }

    constructor(bulletColor: Int, bulletMargin: Int, bulletWidth: Int, bulletPadding: Int) : super(bulletMargin) {
        this.bulletColor = bulletColor
        this.bulletMargin = bulletMargin
        this.bulletWidth = bulletWidth
        this.bulletPadding = bulletPadding
    }

    constructor(src: Parcel) : super(src) {
        this.bulletColor = src.readInt()
        this.bulletMargin = src.readInt()
        this.bulletWidth = src.readInt()
        this.bulletPadding = src.readInt()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeInt(bulletColor)
        dest.writeInt(bulletMargin)
        dest.writeInt(bulletWidth)
        dest.writeInt(bulletPadding)
    }

    override fun getLeadingMargin(first: Boolean): Int {
        return bulletMargin + 2 * bulletWidth + bulletPadding
    }

    fun getLineNumber(text: CharSequence, start: Int, end: Int): Int {

        var stringBeforeStart = text.substring(0, end)
        stringBeforeStart = stringBeforeStart.removePrefix("\n")

        val numberOfNewlines = TextUtils.split(stringBeforeStart.toString(), "\n").size

        return numberOfNewlines

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

        val lineNumber = getLineNumber(listText, start - spanStart, end - spanStart)

        val style = p.style

        val oldColor = p.color

        p.color = bulletColor
        p.style = Paint.Style.FILL

        val textToDraw = lineNumber.toString() + "."

        val width = p.measureText(textToDraw)
        c.drawText(textToDraw, (bulletMargin + x - width) * dir, bottom - p.descent(), p)

        p.color = oldColor
        p.style = style

    }

    companion object {
        private var bulletPath: Path? = null
    }
}