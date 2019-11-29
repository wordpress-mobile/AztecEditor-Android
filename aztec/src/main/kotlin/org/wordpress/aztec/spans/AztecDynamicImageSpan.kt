package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.style.DynamicDrawableSpan
import org.wordpress.aztec.AztecText

abstract class AztecDynamicImageSpan(val context: Context, protected var imageDrawable: Drawable?) : DynamicDrawableSpan() {
    var textView: AztecText? = null
    var aspectRatio: Double = 1.0

    private var measuring = false

    companion object {
        @JvmStatic protected fun setInitBounds(drawable: Drawable?) {
            drawable?.let {
                if (it.bounds.isEmpty && (it.intrinsicWidth > -1 || it.intrinsicHeight > -1)) {
                    it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
                }
            }
        }

        @JvmStatic protected fun getWidth(drawable: Drawable?): Int {
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

        @JvmStatic protected fun getHeight(drawable: Drawable?): Int {
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

    init {
        computeAspectRatio()

        setInitBounds(imageDrawable)
    }

    fun computeAspectRatio() {
        if ((imageDrawable?.intrinsicWidth ?: -1) > -1 && (imageDrawable?.intrinsicHeight ?: -1) > -1) {
            aspectRatio = 1.0 * (imageDrawable?.intrinsicWidth ?: 1) / (imageDrawable?.intrinsicHeight ?: 1)
        } else if (!(imageDrawable?.bounds?.isEmpty ?: true)) {
            aspectRatio = 1.0 * (imageDrawable?.bounds?.width() ?: 0) / (imageDrawable?.bounds?.height() ?: 1)
        } else {
            aspectRatio = 1.0
        }
    }

    override fun getSize(paint: Paint?, text: CharSequence?, start: Int, end: Int, metrics: Paint.FontMetricsInt?): Int {
        val sizeRect = adjustBounds(start)
        if (metrics != null && sizeRect.height() > 0) {

            metrics.ascent = - sizeRect.height()
            metrics.descent = 0

            metrics.top = metrics.ascent
            metrics.bottom = 0
        }

        if (sizeRect.width() > 0) {
            return sizeRect.width()
        } else {
            // This block of code was added in order to resolve
            // span overlap issue on Chromebook devices
            // -> https://github.com/wordpress-mobile/AztecEditor-Android/issues/836
            return super.getSize(paint, text, start, end, metrics)
        }
    }

    fun adjustBounds(start: Int): Rect {
        if (textView == null || textView?.widthMeasureSpec == 0) {
            return Rect(imageDrawable?.bounds ?: Rect(0, 0, 0, 0))
        }

        val layout = textView?.layout

        if (measuring || layout == null) {
            // if we're in pre-layout phase, just return an empty rect
            // Update: Previous version of this code was: return Rect(0, 0, 1, 1)
            // but we needed to change it as it caused span overlap issue on Chromebook
            // devices -> https://github.com/wordpress-mobile/AztecEditor-Android/issues/836
            return Rect(0, 0, 0, 0)
        }

        val line = layout.getLineForOffset(start)

        val maxWidth = layout.getParagraphRight(line) - layout.getParagraphLeft(line)

        // use the original bounds if non-zero, otherwise try the intrinsic sizes. If those are not available then
        //  just assume maximum size.
        var width = if ((imageDrawable?.intrinsicWidth ?: -1) > -1) imageDrawable?.intrinsicWidth ?: -1
        else maxWidth
        var height = if ((imageDrawable?.intrinsicHeight ?: -1) > -1) imageDrawable?.intrinsicHeight ?: -1
        else (width / aspectRatio).toInt()

        if (width > maxWidth) {
            width = maxWidth
            height = (width / aspectRatio).toInt()
        }

        imageDrawable?.bounds = Rect(0, 0, width, height)

        return Rect(imageDrawable?.bounds ?: Rect(0, 0, 0, 0))
    }

    override fun getDrawable(): Drawable? {
        return imageDrawable
    }

    open fun setDrawable(newDrawable: Drawable?) {
        imageDrawable = newDrawable

        setInitBounds(newDrawable)

        computeAspectRatio()
    }

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        canvas.save()

        if (imageDrawable != null) {
            var transY = top
            if (mVerticalAlignment == DynamicDrawableSpan.ALIGN_BASELINE) {
                transY -= paint.fontMetricsInt.descent
            }

            canvas.translate(x, transY.toFloat())
            imageDrawable!!.draw(canvas)
        }

        canvas.restore()
    }
}
