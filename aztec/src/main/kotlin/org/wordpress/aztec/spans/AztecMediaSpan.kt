package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.DynamicDrawableSpan
import android.view.View
import android.widget.Toast
import org.wordpress.android.util.DisplayUtils

class AztecMediaSpan @JvmOverloads constructor(val context: Context?, private var image: Drawable?, val source: String, attributes: String = "") : DynamicDrawableSpan()  {
    var attributes: String = ""
    private val TAG: String = "img"

    init {
        if (attributes.isEmpty()) {
            this.attributes = " src=\"$source\""
        } else {
            this.attributes = attributes
        }

        setBounds(image)
    }

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        val drawable = image
        canvas.save()

        if (drawable != null) {
            var transY = bottom - drawable.bounds.bottom
            if (mVerticalAlignment == ALIGN_BASELINE) {
                transY -= paint.fontMetricsInt.descent
            }

            canvas.translate(x, transY.toFloat())
            drawable.draw(canvas)
            canvas.restore()
        }
    }

    override fun getDrawable(): Drawable? {
        return image
    }

    override fun getSize(paint: Paint?, text: CharSequence?, start: Int, end: Int, metrics: Paint.FontMetricsInt?): Int {
        val drawable = image
        val bounds = drawable?.bounds

        if (metrics != null && bounds != null) {
            metrics.ascent = -bounds.bottom
            metrics.descent = 0

            metrics.top = metrics.ascent
            metrics.bottom = 0
        }

        return bounds?.right ?: 0
    }

    private fun setBounds(drawable: Drawable?) {
        if (drawable != null && context != null) {
            /*
            * Following Android guidelines for keylines and spacing, screen edge margins should be
            * 16dp.  Therefore, the width of images should be the width of the screen minus 16dp on
            * both sides (i.e. 16 * 2 = 32).
            *
            * https://material.io/guidelines/layout/metrics-keylines.html#metrics-keylines-baseline-grids
            */
            val width = Math.min(drawable.intrinsicWidth, DisplayUtils.getDisplayPixelWidth(context) - DisplayUtils.dpToPx(context, 32))
            val height = drawable.intrinsicHeight * width / drawable.intrinsicWidth
            drawable.setBounds(0, 0, width, height)
        }
    }

    fun getHtml(): String {
        return "<$TAG$attributes />"
    }

    fun onClick(view: View) {
        Toast.makeText(view.context, source, Toast.LENGTH_SHORT).show()
    }

    fun setDrawable(newDrawable: Drawable?) {
        image = newDrawable
        setBounds(image)
    }
}
