package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.text.BoringLayout
import android.text.Layout
import android.text.style.DynamicDrawableSpan
import android.view.View
import android.webkit.URLUtil
import org.wordpress.android.util.ImageUtils
import org.wordpress.android.util.StringUtils
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Html
import org.wordpress.aztec.R
import java.lang.ref.WeakReference

abstract class AztecDynamicImageSpan(val context: Context, var imageURI: String?, val resId : Int?) : DynamicDrawableSpan() {

    var textView: AztecText? = null
    var originalBounds : Rect
    var aspectRatio: Double = 1.0

    private var measuring = false

    protected var drawableRef: WeakReference<Drawable>? = null

    private val drawableFailed: Drawable
    private val drawableLoading: Drawable

    var imageGetter: Html.ImageGetter? = null
    var imageGetterCallbacks : Html.ImageGetter.Callbacks? = null

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

        drawableLoading = ContextCompat.getDrawable(context, R.drawable.ic_image_loading)
        drawableFailed = ContextCompat.getDrawable(context, R.drawable.ic_image_failed)

        originalBounds = Rect(drawable?.bounds ?: Rect(0, 0, 0, 0))

        computeAspectRatio(drawable)

        setInitBounds(drawable)
    }

    fun computeAspectRatio(drawable: Drawable?) {
        if ((drawable?.intrinsicWidth ?: -1) > -1 && (drawable?.intrinsicHeight ?: -1) > -1) {
            aspectRatio = 1.0 * (drawable?.intrinsicWidth ?: 1) / (drawable?.intrinsicHeight ?: 1)
        } else if (!(drawable?.bounds?.isEmpty ?: true)) {
            aspectRatio = 1.0 * (drawable?.bounds?.width() ?: 0) / (drawable?.bounds?.height() ?: 1)
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
            return Rect(drawable?.bounds ?: Rect(0, 0, 0, 0))
        }

        val layout = textView?.layout

        if (measuring || layout == null) {
            // if we're in pre-layout phase, just return a tiny rect
            return Rect(0, 0, 1, 1)
        }

        // get the TextView's target width
        calculateWantedWidth(textView?.widthMeasureSpec ?: 0)
                .minus(textView?.compoundPaddingLeft ?: 0)
                .minus(textView?.compoundPaddingRight ?: 0)

        // do a local pre-layout to measure the TextView's basic sizes and line margins
        measuring = true

        measuring = false

        val line = layout.getLineForOffset(start)

        val maxWidth = layout.getParagraphRight(line) - layout.getParagraphLeft(line)

        // use the original bounds if non-zero, otherwise try the intrinsic sizes. If those are not available then
        //  just assume maximum size.

        var width = if (originalBounds.width() > 0) originalBounds.width()
        else if ((drawable?.intrinsicWidth ?: -1) > -1) drawable?.intrinsicWidth ?: -1
        else maxWidth
        var height = if (originalBounds.height() > 0) originalBounds.height()
        else if ((drawable?.intrinsicHeight ?: -1) > -1) drawable?.intrinsicHeight ?: -1
        else (width / aspectRatio).toInt()

        if (width > maxWidth) {
            width = maxWidth
            height = (width / aspectRatio).toInt()
        }

        drawable?.bounds = Rect(0, 0, width, height)

        return Rect(drawable?.bounds ?: Rect(0, 0, 0, 0))
    }

    fun calculateWantedWidth(widthMeasureSpec: Int): Int {
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
        val wr = drawableRef
        var d: Drawable? = null

        if (wr != null)
            d = wr.get()

        if (d == null) {
            // Check if ResID was passed in the constructor use it!
            if (resId != null && imageURI == null) {
                return ContextCompat.getDrawable(context, resId)
            } else {
                val maxWidth = ImageUtils.getMaximumThumbnailWidthForEditor(context)
                val callbacks = object : Html.ImageGetter.Callbacks {
                    override fun onImageFailed() {
                        drawableRef = null
                        imageGetterCallbacks?.onImageFailed()
                    }

                    override fun onImageLoaded(drawable: Drawable?) {
                        drawableRef = WeakReference<Drawable>(drawable)
                        imageGetterCallbacks?.onImageLoaded(drawable)
                    }

                    override fun onImageLoading(drawable: Drawable?) {
                        drawableRef = null
                    }
                }

                if (URLUtil.isNetworkUrl(imageURI)) {
                    imageGetter?.loadImage(imageURI, callbacks, maxWidth)
                    d = ContextCompat.getDrawable(context, R.drawable.ic_image_loading)
                } else {
                    // Local picture: load a scaled version of the image to prevent OOM exception
                    val bitmapToShow = ImageUtils.getWPImageSpanThumbnailFromFilePath(
                            context,
                            escapeQuotes(imageURI),
                            maxWidth
                    )
                    val d = BitmapDrawable(context.resources, bitmapToShow)
                    callbacks.onImageLoaded(d)
                }
            }
        }

        return d
    }

    fun escapeQuotes(text: String?): String {
        val textNotNull = StringUtils.notNullStr(text)
        return textNotNull.replace("'", "\\'").replace("\"", "\\\"")
    }

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        canvas.save()

        if (getDrawable() != null) {
            var transY = top
            if (mVerticalAlignment == DynamicDrawableSpan.ALIGN_BASELINE) {
                transY -= paint.fontMetricsInt.descent
            }

            canvas.translate(x, transY.toFloat())
            getDrawable()!!.draw(canvas)
        }

        canvas.restore()
    }
}