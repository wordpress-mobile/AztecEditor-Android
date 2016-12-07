package org.wordpress.aztec

import android.text.Selection
import android.text.Spannable
import android.text.method.ArrowKeyMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.widget.TextView
import org.wordpress.aztec.spans.AztecMediaClickableSpan
import org.wordpress.aztec.spans.UnknownClickableSpan

/**
 * http://stackoverflow.com/a/23566268/569430
 */
object EnhancedMovementMethod : ArrowKeyMovementMethod() {
    override fun onTouchEvent(widget: TextView, text: Spannable, event: MotionEvent): Boolean {
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

            val spans = text.getSpans(off, off, ClickableSpan::class.java)

            // Only react to AztecMediaClickableSpan and UnknownClickableSpan; not to regular links.
            if (spans.isNotEmpty() && (spans[0] is AztecMediaClickableSpan || spans[0] is UnknownClickableSpan)) {
                if (action == MotionEvent.ACTION_UP) {
                    spans[0].onClick(widget)
                } else {
                    Selection.setSelection(text, text.getSpanStart(spans[0]), text.getSpanEnd(spans[0]))
                }

                return true
            }
        }

        return super.onTouchEvent(widget, text, event)
    }
}
