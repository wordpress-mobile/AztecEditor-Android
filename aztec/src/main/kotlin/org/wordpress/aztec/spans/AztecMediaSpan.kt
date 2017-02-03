package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.style.DynamicDrawableSpan
import android.view.Gravity
import android.view.View
import android.widget.Toast

import org.wordpress.android.util.DisplayUtils
import org.xml.sax.Attributes

class AztecMediaSpan(val context: Context, private var drawable: Drawable?, var overlay: Drawable?, overlayGravity: Int,
        var attributes: Attributes) : DynamicDrawableSpan() {

    companion object {
        @JvmStatic private fun setBoundsToPx(context: Context, drawable: Drawable?) {
            drawable?.let {
                if (it.intrinsicWidth < 0 && it.intrinsicHeight < 0) {
                    // client may have set the bounds manually so, use those to adjust to px
                    it.setBounds(0, 0, DisplayUtils.dpToPx(context, (it.bounds.width())),
                            DisplayUtils.dpToPx(context, (it.bounds.height())))
                } else {
                    it.setBounds(0, 0, DisplayUtils.dpToPx(context, (it.intrinsicWidth)),
                            DisplayUtils.dpToPx(context, (it.intrinsicHeight)))
                }

                val maxWidth = DisplayUtils.getDisplayPixelWidth(context) - DisplayUtils.dpToPx(context, 32)
                if (drawable.bounds.width() > maxWidth) {
                    drawable.setBounds(0, 0, maxWidth, maxWidth * drawable.bounds.height() / drawable.bounds.width())
                }
            }
        }
    }

    init {
        setBoundsToPx(context, drawable)
        setBoundsToPx(context, overlay)
        applyOverlayGravity(overlayGravity)
    }

    override fun getSize(paint: Paint?, text: CharSequence?, start: Int, end: Int, metrics: Paint.FontMetricsInt?): Int {
        val width1 = drawable?.bounds?.width() ?: 0
        val width2 = overlay?.bounds?.width() ?: 0
        val width: Int = if (width1 > width2) width1 else width2

        drawable?.let {
            if (metrics != null) {
                metrics.ascent = -drawable!!.bounds!!.height()
                metrics.descent = 0

                metrics.top = metrics.ascent
                metrics.bottom = 0
            }
        }

        return width
    }

    override fun getDrawable(): Drawable? {
        return drawable
    }

    fun setDrawable(newDrawable: Drawable?) {
        setBoundsToPx(context, newDrawable)
        drawable = newDrawable
    }

    fun setOverlay(newDrawable: Drawable?, gravity: Int) {
        setBoundsToPx(context, newDrawable)
        overlay = newDrawable

        applyOverlayGravity(gravity)
    }

    fun setOverayLevel(level: Int): Boolean {
        return overlay?.setLevel(level) ?: false
    }

    private fun applyOverlayGravity(gravity: Int) {
        if (drawable != null && overlay != null) {
            val rect = Rect(0, 0, drawable!!.bounds.width(), drawable!!.bounds.height())
            val outRect = Rect()

            Gravity.apply(gravity, overlay!!.bounds.width(), overlay!!.bounds.height(), rect, outRect)

            overlay!!.setBounds(outRect.left, outRect.top, outRect.right, outRect.bottom)
        }
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
        }

        overlay?.draw(canvas)

        canvas.restore()
    }

    fun getHtml(): String {
        val sb = StringBuilder()
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
