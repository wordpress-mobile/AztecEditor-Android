package org.wordpress.aztec

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ParagraphStyle
import android.text.style.ReplacementSpan
import android.view.View
import android.widget.Toast

class UnknownHtmlSpan(private val rawHtml: StringBuilder) : ReplacementSpan(), ParagraphStyle {
    val mRawHtml: StringBuilder = rawHtml

    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt): Int {
        //return text with relative to the Paint
        return 200
    }

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        //draw the frame with custom Paint
        canvas.drawRect(x, top.toFloat(), x + 200, bottom.toFloat(), paint)
    }

    fun getRawHtml() : StringBuilder {
        return mRawHtml
    }

    fun onClick(view: View) {
        Toast.makeText(view.getContext(), "<tag> clicked", Toast.LENGTH_SHORT).show()
    }

    companion object {
        val TEXT = "Unknown"
    }
}
