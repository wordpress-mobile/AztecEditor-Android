package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.style.DynamicDrawableSpan
import android.view.Gravity
import android.view.View
import android.widget.TextView

import org.wordpress.aztec.AztecText.OnMediaTappedListener
import org.xml.sax.Attributes
import java.util.*

class AztecMediaSpan(val context: Context, private var drawable: Drawable?,
        var attributes: Attributes?, val onMediaTappedListener: OnMediaTappedListener?) : DynamicDrawableSpan() {

    private val TAG: String = "img"

    var textView: TextView? = null
    var aspectRatio: Double = 1.0

    companion object {
        @JvmStatic private fun setBounds(drawable: Drawable?) {
            drawable?.let {
                if (it.intrinsicWidth > -1 || it.intrinsicHeight > -1) {
                    it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
                }
            }
        }

        @JvmStatic private fun getWidth(drawable: Drawable?): Int {
            drawable?.let {
                if (it.intrinsicWidth < 0) {
                    // client may have set the bounds manually so, use those
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
                    // client may have set the bounds manually so, use those
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
        computeAspectRatio()

        setBounds(drawable)
    }

    fun computeAspectRatio() {
        aspectRatio = 1.0 * (drawable?.intrinsicWidth ?: 1) / (drawable?.intrinsicHeight ?: 1)
    }

    override fun getSize(paint: Paint?, text: CharSequence?, start: Int, end: Int, metrics: Paint.FontMetricsInt?): Int {
        val sizeRect = adjustBounds(start, 0)

        if (metrics != null && sizeRect.width() > 0) {
            metrics.ascent = - sizeRect.height()
            metrics.descent = 0

            metrics.top = metrics.ascent
            metrics.bottom = 0
        }

        return sizeRect.width()
    }

    fun adjustBounds(start: Int, maxHeight: Int): Rect {
        if (textView == null || textView?.layout == null) {
            return Rect(0, 0, 0, 0)
        }

        setBounds(drawable)

        val line = textView?.layout?.getLineForOffset(start) ?: 0

        val lineRect = Rect()
        textView?.layout?.getLineBounds(line, lineRect)

        val leadingMargin = textView?.layout?.getParagraphLeft(line) ?: 0
        val trailingMargin = textView?.layout?.getParagraphRight(line) ?: 0

        val maxWidth = trailingMargin - leadingMargin

        var width = drawable?.bounds?.width() ?: maxWidth
        var height = drawable?.bounds?.height() ?: lineRect.height()

        if (lineRect.width() > -1) {
            if (width > maxWidth) {
                width = maxWidth
                height = (width / aspectRatio).toInt()
            }

            if (maxHeight > 0 && height > maxHeight) {
                height = maxHeight
                width = (aspectRatio * height).toInt()
            }

            drawable?.bounds = Rect(0, 0, width, height)
        }

        return Rect(0, 0, width, height)
    }

    override fun getDrawable(): Drawable? {
        return drawable
    }

    fun setDrawable(newDrawable: Drawable?) {
        drawable = newDrawable

        computeAspectRatio()
    }

    fun setOverlay(index: Int, newDrawable: Drawable?, gravity: Int) {
        if (overlays.lastIndex >= index) {
            overlays.removeAt(index)
        }

        if (newDrawable != null) {
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
            adjustBounds(start, y - top)

            overlays.forEach {
                setBounds(it.first)
                applyOverlayGravity(it.first, it.second)
            }

            var transY = top
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
        sb.append("<")
        sb.append(TAG)

        attributes?.let {
            for (i in 0..attributes!!.length-1) {
                sb.append(' ')
                sb.append(attributes!!.getLocalName(i))
                sb.append("=\"")
                sb.append(attributes!!.getValue(i))
                sb.append("\"")
            }
        }

        sb.append("/>")
        return sb.toString()
    }

    fun onClick(view: View) {
        onMediaTappedListener?.mediaTapped(attributes, getWidth(drawable), getHeight(drawable))
    }

    fun getSource(): String {
        return attributes?.getValue("src") ?: ""
    }
}
