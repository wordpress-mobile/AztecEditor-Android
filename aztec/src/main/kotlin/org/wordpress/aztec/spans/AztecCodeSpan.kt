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

import android.graphics.Typeface
import android.os.Parcel
import android.text.ParcelableSpan
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.CharacterStyle
import android.text.style.UpdateAppearance
import org.wordpress.aztec.formatting.InlineFormatter

class AztecCodeSpan : CharacterStyle, UpdateAppearance, ParcelableSpan, AztecContentSpan, AztecInlineSpan {

    private val TAG: String = "code"

    private var codeBackground: Int = 0
    private var codeColor: Int = 0

    override var attributes: String? = null

    constructor(attributes: String? = null) : super() {
        this.attributes = attributes
    }

    constructor(codeStyle: InlineFormatter.CodeStyle, attributes: String? = null) : this(attributes) {
        this.codeBackground = codeStyle.codeBackground
        this.codeColor = codeStyle.codeColor
    }

    constructor(src: Parcel) {
        this.codeBackground = src.readInt()
        this.codeColor = src.readInt()
    }

    override fun getSpanTypeId(): Int {
        return getSpanTypeIdInternal()
    }

    fun getSpanTypeIdInternal(): Int {
        return AztecSpanIds.CODE_SPAN
    }

    override fun describeContents(): Int {
        return 0;
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
        tp?.typeface = Typeface.MONOSPACE
        tp?.bgColor = codeBackground
        tp?.color = codeColor
    }
}
