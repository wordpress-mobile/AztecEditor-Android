package org.wordpress.aztec

import android.graphics.Rect
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
            var off = layout.getOffsetForHorizontal(line, x.toFloat())

            if (text.length > off) {
                off++
            }

            // get the character's position. This may be the left or the right edge of the character so, find the
            //  other edge by inspecting nearby characters (if they exist)
            val charX = layout.getPrimaryHorizontal(off)
            val charPrevX = if (off > 0) layout.getPrimaryHorizontal(off - 1) else charX
            val charNextX = if (off < text.length) layout.getPrimaryHorizontal(off + 1) else charX

            val lineRect = Rect()
            layout.getLineBounds(line, lineRect)

            if (((x in charPrevX..charX) || (x in charX..charNextX))
                    && y >= lineRect.top && y <= lineRect.bottom) {
                val link = text.getSpans(off, off, ClickableSpan::class.java).firstOrNull()

                // Only react to AztecMediaClickableSpan and UnknownClickableSpan; not to regular links.
                if (link != null && (link is AztecMediaClickableSpan || link is UnknownClickableSpan)) {
                    if (action == MotionEvent.ACTION_UP) {
                        link.onClick(widget)
                    }
                    return true
                }
            }
        }

        return super.onTouchEvent(widget, text, event)
    }
}
