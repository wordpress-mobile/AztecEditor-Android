package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.BoringLayout
import android.text.Layout
import android.text.StaticLayout
import android.text.style.DynamicDrawableSpan
import android.view.View
import org.wordpress.aztec.AztecText

abstract class AztecDynamicImageSpan(val context: Context, protected var imageDrawable: Drawable?) : DynamicDrawableSpan() {

    var textView: AztecText? = null
    var originalBounds = Rect(imageDrawable?.bounds ?: Rect(0, 0, 0, 0))
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

        if (metrics != null && sizeRect.width() > 0) {
            metrics.ascent = - sizeRect.height()
            metrics.descent = 0

            metrics.top = metrics.ascent
            metrics.bottom = 0
        }

        return sizeRect.width()
    }

    fun adjustBounds(start: Int): Rect {
        if (textView == null || textView?.widthMeasureSpec == 0) {
            return Rect(imageDrawable?.bounds ?: Rect(0, 0, 0, 0))
        }

        if (measuring) {
            // if we're in pre-layout phase, just return a tiny rect
            return Rect(0, 0, 1, 1)
        }

        // get the TextView's target width
        val want = calculateWantedWidth(textView?.widthMeasureSpec ?: 0)
                .minus(textView?.compoundPaddingLeft ?: 0)
                .minus(textView?.compoundPaddingRight ?: 0)

        // do a local pre-layout to measure the TextView's basic sizes and line margins
        measuring = true

        val layout = textView?.layout ?: StaticLayout(textView?.text ?: "", 0, textView?.text?.length ?: 0,
                textView?.paint, want, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, true)

        measuring = false

        val line = layout.getLineForOffset(start)

        val maxWidth = layout.getParagraphRight(line) - layout.getParagraphLeft(line)

        // use the original bounds if non-zero, otherwise try the intrinsic sizes. If those are not available then
        //  just assume maximum size.

        var width = if (originalBounds.width() > 0) originalBounds.width()
        else if ((imageDrawable?.intrinsicWidth ?: -1) > -1) imageDrawable?.intrinsicWidth ?: -1
        else maxWidth
        var height = if (originalBounds.height() > 0) originalBounds.height()
        else if ((imageDrawable?.intrinsicHeight ?: -1) > -1) imageDrawable?.intrinsicHeight ?: -1
        else (width / aspectRatio).toInt()

        if (width > maxWidth) {
            width = maxWidth
            height = (width / aspectRatio).toInt()
        }

        imageDrawable?.bounds = Rect(0, 0, width, height)

        return Rect(imageDrawable?.bounds ?: Rect(0, 0, 0, 0))
    }

    fun calculateWantedWidth(widthMeasureSpec: Int): Int
    {
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)

        var width: Int

        val UNKNOWN_BORING = BoringLayout.Metrics()

        var boring: BoringLayout.Metrics? = UNKNOWN_BORING

        var des = -1

        if (widthMode == View.MeasureSpec.EXACTLY) {
            // Parent has told us how big to be. So be it.
            width = widthSize
        } else {
            if (des < 0) {
                boring = BoringLayout.isBoring("", textView?.paint)
            }

            if (boring == null || boring === UNKNOWN_BORING) {
                if (des < 0) {
                    des = Math.ceil(Layout.getDesiredWidth("", textView?.paint).toDouble()).toInt()
                }
                width = des
            } else {
                width = boring.width
            }

            if (widthMode == View.MeasureSpec.AT_MOST) {
                width = Math.min(widthSize, width)
            }
        }

        return width
    }

    override fun getDrawable(): Drawable? {
        return imageDrawable
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