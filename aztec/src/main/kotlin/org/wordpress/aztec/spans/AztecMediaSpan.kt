package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.style.DynamicDrawableSpan
import android.view.Gravity
import android.view.View

import org.wordpress.android.util.DisplayUtils
import org.wordpress.aztec.AztecText.OnMediaTappedListener
import org.xml.sax.Attributes
import java.util.*

class AztecMediaSpan(val context: Context, private var drawable: Drawable?,
        var attributes: Attributes, val onMediaTappedListener: OnMediaTappedListener?) : DynamicDrawableSpan() {

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

        @JvmStatic private fun getWidth(drawable: Drawable?): Int {
            drawable?.let {
                if (it.intrinsicWidth < 0) {
                    // client may have set the bounds manually so, use those to adjust to px
                    return it.bounds.width()
                } else {
                    return it.intrinsicWidth
                }
            }

            return 0
        }

        @JvmStatic private fun getHeight(drawable: Drawable?): Int {
            drawable?.let {
                if (it.intrinsicHeight < 0) {
                    // client may have set the bounds manually so, use those to adjust to px
                    return it.bounds.height()
                } else {
                    return it.intrinsicHeight
                }
            }

            return 0
        }
    }

    private val overlays: ArrayList<Pair<Drawable?, Int>> = ArrayList()

    init {
        setBoundsToPx(context, drawable)
    }

    override fun getSize(paint: Paint?, text: CharSequence?, start: Int, end: Int, metrics: Paint.FontMetricsInt?): Int {
        val width1 = drawable?.bounds?.width() ?: 0
        var width: Int = 0

        overlays.forEach {
            val width2 = it.first?.bounds?.width() ?: 0
            width = if (width1 > width2) width1 else width2
        }

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

    fun setOverlay(index: Int, newDrawable: Drawable?, gravity: Int) {
        if (overlays.lastIndex >= index) {
            overlays.removeAt(index)
        }

        if (newDrawable != null) {
            setBoundsToPx(context, newDrawable)
            applyOverlayGravity(newDrawable, gravity)

            overlays.ensureCapacity(index + 1)
            overlays.add(index, Pair(newDrawable, gravity))
        }
    }

    fun clearOverlays() {
        overlays.clear()
    }

    fun setOverayLevel(index: Int, level: Int): Boolean {
        return overlays[index].first?.setLevel(level) ?: false
    }

    private fun applyOverlayGravity(overlay: Drawable?, gravity: Int) {
        if (drawable != null && overlay != null) {
            val rect = Rect(0, 0, drawable!!.bounds.width(), drawable!!.bounds.height())
            val outRect = Rect()

            Gravity.apply(gravity, overlay.bounds.width(), overlay.bounds.height(), rect, outRect)

            overlay.setBounds(outRect.left, outRect.top, outRect.right, outRect.bottom)
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

        overlays.forEach {
            it.first?.draw(canvas)
        }

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
        onMediaTappedListener?.mediaTapped(attributes, getWidth(drawable), getHeight(drawable))
    }

    fun getSource(): String {
        return attributes.getValue("src")
    }
}
