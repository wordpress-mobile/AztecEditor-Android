package org.wordpress.aztec

import android.text.Selection
import android.text.Spannable
import android.text.method.ArrowKeyMovementMethod
import android.view.MotionEvent
import android.widget.TextView
import org.wordpress.aztec.spans.UnknownClickableSpan

/**
 * http://stackoverflow.com/a/23566268/569430
 */
object EnhancedMovementMethod : ArrowKeyMovementMethod() {

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        val action = event.action

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            var x = event.x.toInt()
            var y = event.y.toInt()

            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop

            x += widget.scrollX
            y += widget.scrollY

            val layout = widget.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())

            //made to only react to UnknownClickableSpan, and not to regular links
            val link = buffer.getSpans(off, off, UnknownClickableSpan::class.java).firstOrNull()
            if (link != null) {
                if (action == MotionEvent.ACTION_UP) {
                    link.onClick(widget)
                } else {
                    Selection.setSelection(buffer, buffer.getSpanStart(link), buffer.getSpanEnd(link))
                }

                return true
            }
        }

        return super.onTouchEvent(widget, buffer, event)
    }

}
