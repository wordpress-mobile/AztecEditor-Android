package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.BoringLayout
import android.text.Layout
import android.text.StaticLayout
import android.text.TextUtils
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.view.View
import org.wordpress.android.util.DisplayUtils
import org.wordpress.aztec.AztecText

class AztecHorizontalLineSpan(context: Context, drawable: Drawable, override var nestingLevel: Int) :
        AztecDynamicImageSpan(context, drawable), AztecFullWidthImageSpan {

    private val TAG: String = "hr"

    override var attributes: String = ""

    override fun getStartTag(): String {
        if (TextUtils.isEmpty(attributes)) {
            return TAG
        }
        return TAG + attributes
    }

    override fun getEndTag(): String {
        return TAG
    }
}