package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.style.DynamicDrawableSpan
import android.view.View
import android.widget.Toast

import org.wordpress.android.util.DisplayUtils
import org.xml.sax.Attributes

class AztecMediaSpan(val context: Context, private var drawable: Drawable?, var attributes: Attributes) :
        DynamicDrawableSpan() {

    companion object {
        private val rect: Rect = Rect()
    }

    override fun getSize(paint: Paint?, text: CharSequence?, start: Int, end: Int, metrics: Paint.FontMetricsInt?): Int {
        drawable?.let {
            val bounds = adjustBounds(it)

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

    fun setDrawable(newDrawable: Drawable?) {
        drawable = newDrawable
        drawable?.let { adjustBounds(it) }
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

    private fun adjustBounds(drawable: Drawable): Rect {
        if (drawable.intrinsicWidth === 0) {
            rect.set(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            return rect
        }

        /*
         * Following Android guidelines for keylines and spacing, screen edge margins should
         * be 16dp.  Therefore, the width of images should be the width of the screen minus
         * 16dp on both sides (i.e. 16 * 2 = 32).
         *
         * https://material.io/guidelines/layout/metrics-keylines.html#metrics-keylines-baseline-grids
         */
        val width = context.resources.displayMetrics.widthPixels - DisplayUtils.dpToPx(context, 32)
        val height = drawable.intrinsicHeight * width / drawable.intrinsicWidth
        drawable.setBounds(0, 0, width, height)

        return drawable.bounds
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
