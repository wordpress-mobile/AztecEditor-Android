package org.wordpress.aztec

import android.graphics.Rect
import android.text.Spannable
import android.text.method.ArrowKeyMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.widget.TextView
import org.wordpress.aztec.spans.AztecMediaClickableSpan
import org.wordpress.aztec.spans.AztecURLSpan
import org.wordpress.aztec.spans.UnknownClickableSpan

/**
 * http://stackoverflow.com/a/23566268/569430
 */
object EnhancedMovementMethod : ArrowKeyMovementMethod() {
    var isLinkTapEnabled = false
    var linkTappedListener: AztecText.OnLinkTappedListener? = null

    override fun onTouchEvent(widget: TextView, text: Spannable, event: MotionEvent): Boolean {
        val action = event.action

        if (action == MotionEvent.ACTION_UP) {
            var x = event.x.toInt()
            var y = event.y.toInt()

            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop

            x += widget.scrollX
            y += widget.scrollY

            if (x < 0) return true

            val layout = widget.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())

            // get the character's position. This may be the left or the right edge of the character so, find the
            //  other edge by inspecting nearby characters (if they exist)
            val charX = layout.getPrimaryHorizontal(off)
            val charPrevX = if (off > 0) layout.getPrimaryHorizontal(off - 1) else charX
            val charNextX = if (off < text.length) layout.getPrimaryHorizontal(off + 1) else charX

            val lineRect = Rect()
            layout.getLineBounds(line, lineRect)

            val clickedWithinLineHeight = y >= lineRect.top && y <= lineRect.bottom
            val clickedOnSpanToTheLeftOfCursor = x in charPrevX..charX
            val clickedOnSpanToTheRightOfCursor = x in charX..charNextX

            val clickedOnSpan = clickedWithinLineHeight &&
                    (clickedOnSpanToTheLeftOfCursor || clickedOnSpanToTheRightOfCursor)

            val clickedSpanBordersAnotherOne = (text.getSpans(off, off, ClickableSpan::class.java).size == 1 &&
                    text.getSpans(off + 1, off + 1, ClickableSpan::class.java).isNotEmpty())

            val isClickedSpanAmbiguous = text.getSpans(off, off, ClickableSpan::class.java).size > 1

            val failedToPinpointClickedSpan = (isClickedSpanAmbiguous || clickedSpanBordersAnotherOne)
                    && !clickedOnSpanToTheLeftOfCursor && !clickedOnSpanToTheRightOfCursor

            var link: ClickableSpan? = null

            if (clickedOnSpan) {
                if (isClickedSpanAmbiguous) {
                    if (clickedOnSpanToTheLeftOfCursor) {
                        link = text.getSpans(off, off, ClickableSpan::class.java)[0]
                    } else if (clickedOnSpanToTheRightOfCursor) {
                        link = text.getSpans(off, off, ClickableSpan::class.java)[1]
                    }
                } else {
                    link = text.getSpans(off, off, ClickableSpan::class.java).firstOrNull()
                }
            } else if (failedToPinpointClickedSpan) {
                link = text.getSpans(off, off, ClickableSpan::class.java).firstOrNull { text.getSpanStart(it) == off }
            }

            if (link != null) {
                if (link is AztecMediaClickableSpan || link is UnknownClickableSpan) {
                    link.onClick(widget)
                    return true
                } else if (link is AztecURLSpan && isLinkTapEnabled) {
                    linkTappedListener?.onLinkTapped(widget, link.url) ?: link.onClick(widget)
                    return true
                }
            }
        }

        return super.onTouchEvent(widget, text, event)
    }
}
