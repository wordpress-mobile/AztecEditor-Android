package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.DynamicDrawableSpan
import android.view.View
import android.widget.Toast

import org.wordpress.android.util.DisplayUtils
import org.xml.sax.Attributes

class AztecMediaSpan(val context: Context, private var drawable: Drawable?, initAdjustBounds: Boolean, var attributes: Attributes) :
        DynamicDrawableSpan() {

    init {
        if (initAdjustBounds) {
            setBoundsToDp(context, drawable)
        }
    }

    companion object {
        fun setBoundsToDp(context: Context, drawable: Drawable?) {
            drawable?.setBounds(0, 0, DisplayUtils.dpToPx(context, (drawable.intrinsicWidth)),
                    DisplayUtils.dpToPx(context, (drawable.intrinsicHeight)))
        }
    }

    override fun getSize(paint: Paint?, text: CharSequence?, start: Int, end: Int, metrics: Paint.FontMetricsInt?): Int {
        drawable?.let {
            val bounds = drawable!!.bounds

            if (metrics != null) {
                metrics.ascent = -bounds.bottom
                metrics.descent = 0

                metrics.top = metrics.ascent
                metrics.bottom = 0
            }

            return bounds.right
        }

        return 0
    }

    override fun getDrawable(): Drawable? {
        return drawable
    }

    fun setDrawableAndAdjustBounds(newDrawable: Drawable?) {
        drawable = newDrawable
        setBoundsToDp(context, drawable)
    }

    fun setDrawableWithoutAdjustingBounds(newDrawable: Drawable?) {
        drawable = newDrawable
    }

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        canvas.save()

        if (drawable != null) {
            var transY = bottom - drawable!!.bounds.bottom
            if (mVerticalAlignment == ALIGN_BASELINE) {
                transY -= paint.fontMetricsInt.descent
            }

            canvas.translate(x, transY.toFloat())
            drawable!!.draw(canvas)
            canvas.restore()
        }
    }

    fun getHtml(): String {
        var sb = StringBuilder()
        sb.append("<img")

        for (i in 0..attributes.length-1) {
            sb.append(' ')
            sb.append(attributes.getLocalName(i))
            sb.append("=\"")
            sb.append(attributes.getValue(i))
            sb.append("\"")
        }
        sb.append("/>")
        return sb.toString()
    }

    fun onClick(view: View) {
        Toast.makeText(view.context, getHtml(), Toast.LENGTH_SHORT).show()
    }

    fun getSource(): String {
        return attributes.getValue("src")
    }
}
