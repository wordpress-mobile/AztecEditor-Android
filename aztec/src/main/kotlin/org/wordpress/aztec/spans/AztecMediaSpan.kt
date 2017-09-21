package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.Gravity
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText
import java.util.*


abstract class AztecMediaSpan(context: Context, imageProvider: IImageProvider, override var attributes: AztecAttributes = AztecAttributes(),
                              var onMediaDeletedListener: AztecText.OnMediaDeletedListener? = null,
                              editor: AztecText? = null) : AztecDynamicImageSpan(context, imageProvider), IAztecAttributedSpan {

    abstract val TAG: String

    private val overlays: ArrayList<Pair<Drawable?, Int>> = ArrayList()
    private val EXTRA_LOADING_SIZE = 500
    private var drawableHeight = 0
    private var drawableWidth = 0

    init {
        textView = editor
    }

    fun setDrawable(newDrawable: Drawable?, isPlaceholder: Boolean = false) {
        super.setDrawable(newDrawable)

        Log.d("Danilo", "Called setDrawable")

        // Store the picture size to be used later when drawing the white rectangle placeholder when picture
        // is out of the viewable area
        if (newDrawable != null) {
            if (newDrawable is BitmapDrawable && newDrawable.bitmap != null) {
                drawableHeight = newDrawable.bitmap.height
                drawableWidth = newDrawable.bitmap.width
            } else {
                drawableHeight = getHeight(newDrawable)
                drawableWidth = getWidth(newDrawable)
            }
            Log.d("Danilo", "bitmap was NOT null ")

        } else {
            Log.d("Danilo", "bitmap NULLLLLL")
        }
        Log.d("Danilo", "Dims are "  + drawableWidth + " " + drawableHeight)
        Log.d("Danilo", "--------------------")
    }

    fun setOverlay(index: Int, newDrawable: Drawable?, gravity: Int) {
        if (overlays.lastIndex >= index) {
            overlays.removeAt(index)
        }

        if (newDrawable != null) {
            overlays.ensureCapacity(index + 1)
            overlays.add(index, Pair(newDrawable, gravity))

            setInitBounds(newDrawable)
        }
    }

    fun clearOverlays() {
        overlays.clear()
    }

    fun setOverlayLevel(index: Int, level: Int): Boolean {
        return overlays.getOrNull(index)?.first?.setLevel(level) ?: false
    }

    private fun applyOverlayGravity(overlay: Drawable?, gravity: Int) {
        if (imageDrawable != null && overlay != null) {
            val rect = Rect(0, 0, imageDrawable!!.bounds.width(), imageDrawable!!.bounds.height())
            val outRect = Rect()

            Gravity.apply(gravity, overlay.bounds.width(), overlay.bounds.height(), rect, outRect)

            overlay.setBounds(outRect.left, outRect.top, outRect.right, outRect.bottom)
        }
    }

    override fun getSize(paint: Paint?, text: CharSequence?, start: Int, end: Int, metrics: Paint.FontMetricsInt?): Int {
        Log.d("Danilo", "Called getSize")
        val size = super.getSize(paint, text, start, end, metrics)
        Log.d("Danilo", "Size is " + size)
        Log.d("Danilo", "--------------------")
        return size
    }

    override fun computeAspectRatio() {
        Log.d("Danilo", "Called computeAspectRatio")
        if (drawableWidth > 0 && drawableHeight > 0) {
            aspectRatio = 1.0 * ( drawableWidth / drawableHeight)
        } else if (!(imageDrawable?.bounds?.isEmpty ?: true)) {
            aspectRatio = 1.0 * (imageDrawable?.bounds?.width() ?: 0) / (imageDrawable?.bounds?.height() ?: 1)
        } else {
            aspectRatio = 1.0
        }
        Log.d("Danilo", "aspectRatio is " + aspectRatio)
        Log.d("Danilo", "--------------------")
    }

    override fun adjustBounds(start: Int): Rect {

        computeAspectRatio()

        if (textView?.layout == null || textView?.widthMeasureSpec == 0) {
            return Rect(imageDrawable?.bounds ?: Rect(0, 0, 0, 0))
        }

        val layout = textView?.layout!!
        val line = layout.getLineForOffset(start)
        val maxWidth = layout.getParagraphRight(line) - layout.getParagraphLeft(line)

        // use the original bounds if non-zero, otherwise try the intrinsic sizes. If those are not available then
        //  just assume maximum size.

        var width = if (drawableWidth > 0) drawableWidth  //if ((imageDrawable?.intrinsicWidth ?: -1) > -1) imageDrawable?.intrinsicWidth ?: -1
        else maxWidth
        var height = if (drawableHeight > 0) drawableHeight //((imageDrawable?.intrinsicHeight ?: -1) > -1) imageDrawable?.intrinsicHeight ?: -1
        else (width / aspectRatio).toInt()

        if (width > maxWidth) {
            width = maxWidth
            height = (width / aspectRatio).toInt()
        }

        imageDrawable?.bounds = Rect(0, 0, width, height)

        return Rect(imageDrawable?.bounds ?: Rect(0, 0, width, height))
    }

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        if (textView == null) {
            return
        }
        val scrollBounds = Rect()
        textView?.getLocalVisibleRect(scrollBounds)

        if (scrollBounds.top > bottom + EXTRA_LOADING_SIZE || top - EXTRA_LOADING_SIZE > scrollBounds.bottom) {
            // the picture is outside the current viewable area. We draw a blank rect, otherwise text jumps
            setDrawable(null, false)
        } else {
            // The picture is on the visible area of the screen. Check if we had set it to null
            if (this.drawable == null) {
                imageProvider.requestImage(this)
            }
        }

        canvas.save()
        if (imageDrawable?.bounds?.width() ?: 0 != 0) {
            var transY = top
            if (mVerticalAlignment == ALIGN_BASELINE) {
                transY -= paint.fontMetricsInt.descent
            }

            canvas.translate(x, transY.toFloat())
            imageDrawable!!.draw(canvas)

            overlays.forEach {
                applyOverlayGravity(it.first, it.second)
            }

            overlays.forEach {
                it.first?.draw(canvas)
            }
        } else {
            // draw an empty rectangle in this case
            if (this.drawable == null && drawableHeight > 0 && drawableWidth > 0) {
                var transY = top
                if (mVerticalAlignment == ALIGN_BASELINE) {
                    transY -= paint.fontMetricsInt.descent
                }

                canvas.translate(x, transY.toFloat())

                val myRect =  Rect(imageDrawable?.bounds ?: Rect(0, 0, drawableWidth, drawableHeight))

                canvas.drawRect(myRect, paint)

                overlays.forEach {
                    applyOverlayGravity(it.first, it.second)
                }

                overlays.forEach {
                    it.first?.draw(canvas)
                }
            } else {
                Log.d("Danilo", "Should not be here!")
            }
        }

        canvas.restore()
    }

    open fun getHtml(): String {
        val sb = StringBuilder("<$TAG ")

        attributes.removeAttribute("aztec_id")

        sb.append(attributes)
        sb.trim()
        sb.append(" />")

        return sb.toString()
    }

    fun getSource(): String {
        return attributes.getValue("src") ?: ""
    }

    abstract fun onClick()

    fun onMediaDeleted() {
        onMediaDeletedListener?.onMediaDeleted(attributes)
    }

}
