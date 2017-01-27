package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.text.style.ParagraphStyle
import android.view.View
import android.widget.Toast
import org.wordpress.android.util.DisplayUtils

class AztecMediaSpan @JvmOverloads constructor(val context: Context?, private var image: Drawable?, val source: String, attributes: String = "") : DynamicDrawableSpan(), AztecSpan  {

    private val TAG: String = "img"

    override var attributes: String = ""

    companion object {
        private val rect: Rect = Rect()
    }

    init {
        if (attributes.isEmpty()) {
            this.attributes = " src=\"$source\""
        } else {
            this.attributes = attributes
        }

        setBounds(image)
    }

    override fun getDrawable(): Drawable? {
        return image
    }

    fun setDrawable(newDrawable: Drawable?) {
        image = newDrawable
        setBounds(image)
    }

    override fun getStartTag(): String {
        if (TextUtils.isEmpty(attributes)) {
            return TAG
        }
        return TAG + attributes
    }

    override fun getEndTag(): String {
        return TAG
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

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float,
                      top: Int, y: Int, bottom: Int, paint: Paint) {
        val b = image
        canvas.save()

        if (b != null) {
            var transY = bottom - b.bounds.bottom
            if (mVerticalAlignment == ALIGN_BASELINE) {
                transY -= paint.fontMetricsInt.descent
            }

            canvas.translate(x, transY.toFloat())
            b.draw(canvas)
            canvas.restore()
        }
    }

    private fun setBounds(drawable: Drawable?) {
        if (drawable != null && context != null) {
            val width = Math.min(DisplayUtils.dpToPx(context, drawable.intrinsicWidth), DisplayUtils.getDisplayPixelWidth(context) - DisplayUtils.dpToPx(context, 32))
            val height = drawable.intrinsicHeight * width / drawable.intrinsicWidth
            drawable.setBounds(0, 0, width, height)
        }
    }

    fun getHtml() : String {
        return "<$TAG $attributes />"
    }

    fun onClick(view: View) {
        Toast.makeText(view.context, source, Toast.LENGTH_SHORT).show()
    }
}