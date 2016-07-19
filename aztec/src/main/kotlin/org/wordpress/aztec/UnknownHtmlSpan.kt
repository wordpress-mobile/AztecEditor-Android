package org.wordpress.aztec

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ParagraphStyle
import android.text.style.ReplacementSpan
import android.view.View
import android.widget.Toast

class UnknownHtmlSpan(rawHtml: StringBuilder) : ReplacementSpan(), ParagraphStyle {
    val mRawHtml: StringBuilder = rawHtml

    override fun getSize(p0: Paint?, p1: CharSequence?, p2: Int, p3: Int, p4: Paint.FontMetricsInt?): Int {
        return 200;
    }

    override fun draw(canvas: Canvas?, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int,
                      bottom: Int, paint: Paint?) {
        canvas?.drawRect(x, top.toFloat(), x + 200, bottom.toFloat(), paint)
    }

    fun getRawHtml() : StringBuilder {
        return mRawHtml
    }

    fun onClick(view: View) {
        Toast.makeText(view.context, mRawHtml.toString(), Toast.LENGTH_SHORT).show()
    }

    companion object {
        // Following tags are ignored
        val KNOWN_TAGS = setOf("html", "body")
    }
}
