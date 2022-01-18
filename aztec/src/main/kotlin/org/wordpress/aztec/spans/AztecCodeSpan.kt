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
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.formatting.InlineFormatter

class AztecCodeSpan(override var attributes: AztecAttributes = AztecAttributes()) : MetricAffectingSpan(), IAztecInlineSpan {
    override val TAG = "code"

    var codeStyle = InlineFormatter.CodeStyle(0, 0.0f, 0)

    constructor(codeStyle: InlineFormatter.CodeStyle, attributes: AztecAttributes = AztecAttributes()) : this(attributes) {
        this.codeStyle = codeStyle
    }

    override fun updateDrawState(tp: TextPaint?) {
        configureTextPaint(tp)
    }

    override fun updateMeasureState(tp: TextPaint?) {
        configureTextPaint(tp)
    }

    private fun configureTextPaint(tp: TextPaint?) {
        val alpha: Int = (codeStyle.codeBackgroundAlpha * 255).toInt()
        tp?.typeface = Typeface.MONOSPACE
        tp?.bgColor = Color.argb(
                alpha,
                Color.red(codeStyle.codeBackground),
                Color.green(codeStyle.codeBackground),
                Color.blue(codeStyle.codeBackground))
        tp?.color = codeStyle.codeColor
    }
}
