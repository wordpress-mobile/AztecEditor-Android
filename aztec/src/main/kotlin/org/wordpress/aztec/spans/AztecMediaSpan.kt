package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.Bitmap
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


abstract class AztecMediaSpan(context: Context, drawable: Drawable?, override var attributes: AztecAttributes = AztecAttributes(),
                              var onMediaDeletedListener: AztecText.OnMediaDeletedListener? = null,
                              editor: AztecText? = null) : AztecDynamicImageSpan(context, drawable), IAztecAttributedSpan {

    abstract val TAG: String

    private val overlays: ArrayList<Pair<Drawable?, Int>> = ArrayList()

    private var innerPlaceholder: Bitmap? = null
    private var isDirty = false

    init {
        textView = editor
    }

    private fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        }
        return Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ALPHA_8)
    }

    fun setDrawable(newDrawable: Drawable?) {
        innerPlaceholder = drawableToBitmap(newDrawable)

        imageDrawable = newDrawable

        originalBounds = Rect(imageDrawable?.bounds ?: Rect(0, 0, 0, 0))

        setInitBounds(newDrawable)

        computeAspectRatio()
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

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        if (textView == null) {
            return
        }
        val scrollBounds = Rect()
        textView?.getLocalVisibleRect(scrollBounds)

        if (scrollBounds.top > bottom || top > scrollBounds.bottom) {
            // the picture is outside the current viewable area. Use a placeholder instead, otherwise text jumps
            if (innerPlaceholder != null) {
                imageDrawable = BitmapDrawable(context.resources, innerPlaceholder)
            }
            isDirty = true
        } else {
            // The picture is visible on the screen. Check if it's a placeholder
            if (isDirty) {
                //require real picture here
                isDirty = false
                
            }
        }

        canvas.save()

        if (imageDrawable != null) {
            var transY = top
            if (mVerticalAlignment == ALIGN_BASELINE) {
                transY -= paint.fontMetricsInt.descent
            }

            canvas.translate(x, transY.toFloat())
            imageDrawable!!.draw(canvas)
        }

        overlays.forEach {
            applyOverlayGravity(it.first, it.second)
        }

        overlays.forEach {
            it.first?.draw(canvas)
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
