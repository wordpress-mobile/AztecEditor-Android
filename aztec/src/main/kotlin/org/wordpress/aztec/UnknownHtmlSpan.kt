package org.wordpress.aztec

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ParagraphStyle
import android.text.style.ReplacementSpan

class UnknownHtmlSpan(rawHtml: StringBuilder) : ReplacementSpan(), ParagraphStyle {
    val mRawHtml: StringBuilder = rawHtml

    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt): Int {
        return 200
    }

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        //draw the frame with custom Paint
        canvas.drawRect(x, top.toFloat(), x + 200, bottom.toFloat(), paint)
    }

    fun getRawHtml() : StringBuilder {
        return mRawHtml
    }

    companion object {
        // Following tags are ignored
        val KNOWN_TAGS = setOf<String>("html", "body")
    }
}
