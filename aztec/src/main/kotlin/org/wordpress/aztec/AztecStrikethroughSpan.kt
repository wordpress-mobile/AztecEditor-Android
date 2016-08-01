package org.wordpress.aztec

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Spanned
import android.text.TextPaint
import android.text.style.*

import org.xml.sax.Attributes

class AztecStrikethroughSpan(tag: String) : StrikethroughSpan() {

    private var mTag: String = tag

    fun getTag() : String {
        return mTag
    }
}
