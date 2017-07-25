package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.Gravity
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText
import java.lang.ref.WeakReference
import java.util.*

abstract class AztecMediaSpan(context: Context, imageURI: String?, resId : Int?, override var attributes: AztecAttributes = AztecAttributes(),
                              editor: AztecText? = null) : AztecDynamicImageSpan(context, imageURI, resId), IAztecAttributedSpan {

    abstract val TAG: String

    private val overlays: ArrayList<Pair<Drawable?, Int>> = ArrayList()

    init {
        textView = editor
    }

    fun setDrawablePath(newDrawable: Drawable?) {
        drawableRef = WeakReference<Drawable>(newDrawable)

        originalBounds = Rect(getDrawable()?.bounds ?: Rect(0, 0, 0, 0))

        setInitBounds(newDrawable)

        computeAspectRatio(newDrawable)
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
        if (getDrawable() != null && overlay != null) {
            val rect = Rect(0, 0, getDrawable()!!.bounds.width(), getDrawable()!!.bounds.height())
            val outRect = Rect()

            Gravity.apply(gravity, overlay.bounds.width(), overlay.bounds.height(), rect, outRect)

            overlay.setBounds(outRect.left, outRect.top, outRect.right, outRect.bottom)
        }
    }

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        canvas.save()

        if (getDrawable() != null) {
            var transY = top
            if (mVerticalAlignment == ALIGN_BASELINE) {
                transY -= paint.fontMetricsInt.descent
            }

            canvas.translate(x, transY.toFloat())
            getDrawable()!!.draw(canvas)
        }

        overlays.forEach {
            applyOverlayGravity(it.first, it.second)
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
        sb.append(' ')

        attributes.removeAttribute("aztec_id")

        sb.append(attributes)
        sb.append("/>")

        return sb.toString()
    }

    fun getSource(): String {
        return attributes.getValue("src") ?: ""
    }

    abstract fun onClick()
}
