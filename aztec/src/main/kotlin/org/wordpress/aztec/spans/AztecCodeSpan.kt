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

import android.graphics.Color
import android.graphics.Typeface
import android.os.Parcel
import android.text.ParcelableSpan
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.MetricAffectingSpan
import org.wordpress.aztec.formatting.InlineFormatter

class AztecCodeSpan : MetricAffectingSpan, ParcelableSpan, AztecContentSpan, AztecInlineSpan {

    private val TAG: String = "code"

    private var codeBackground: Int = 0
    private var codeBackgroundAlpha: Float = 0.0f
    private var codeColor: Int = 0

    override var attributes: String = ""

    constructor(attributes: String = "") : super() {
        this.attributes = attributes
    }

    constructor(codeStyle: InlineFormatter.CodeStyle, attributes: String = "") : this(attributes) {
        this.codeBackground = codeStyle.codeBackground
        this.codeBackgroundAlpha = codeStyle.codeBackgroundAlpha
        this.codeColor = codeStyle.codeColor
    }

    constructor(src: Parcel) {
        this.codeBackground = src.readInt()
        this.codeBackgroundAlpha = src.readFloat()
        this.codeColor = src.readInt()
    }

    override fun getSpanTypeId(): Int {
        return getSpanTypeIdInternal()
    }

    fun getSpanTypeIdInternal(): Int {
        return AztecSpanIds.CODE_SPAN
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(codeBackground)
        dest.writeInt(codeColor)
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

    override fun updateDrawState(tp: TextPaint?) {
        configureTextPaint(tp)
    }

    override fun updateMeasureState(tp: TextPaint?) {
        configureTextPaint(tp)
    }

    private fun configureTextPaint(tp: TextPaint?) {
        val alpha: Int = (codeBackgroundAlpha * 255).toInt()
        tp?.typeface = Typeface.MONOSPACE
        tp?.bgColor = Color.argb(alpha, Color.red(codeBackground), Color.green(codeBackground), Color.blue(codeBackground))
        tp?.color = codeColor
    }
}
