package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan

class AztecCommentSpan(val context: Context, drawable: Drawable) : ImageSpan(drawable) {
    companion object {
        private val rect: Rect = Rect()
        private val HTML_MORE: String = "more"
        private val HTML_PAGE: String = "nextpage"
    }

    enum class Comment constructor(val html: String) {
        MORE(HTML_MORE),
        PAGE(HTML_PAGE)
    }

    override fun getSize(paint: Paint?, text: CharSequence?, start: Int, end: Int, metrics: Paint.FontMetricsInt?): Int {
        val drawable = drawable
        val bounds = getBounds(drawable)

        if (metrics != null) {
            metrics.ascent = -bounds.bottom
            metrics.descent = 0

            metrics.top = metrics.ascent
            metrics.bottom = 0
        }

        return bounds.right
    }

    private fun getBounds(drawable: Drawable): Rect {
        if (drawable.intrinsicWidth === 0) {
            rect.set(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            return rect
        }

        val width = context.resources.displayMetrics.widthPixels
        val height = drawable.intrinsicHeight * width / drawable.intrinsicWidth
        drawable.setBounds(0, 0, width, height)

        return drawable.bounds
    }
}
