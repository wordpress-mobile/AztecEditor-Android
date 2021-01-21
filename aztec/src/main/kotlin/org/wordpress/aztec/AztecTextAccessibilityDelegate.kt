package org.wordpress.aztec

import android.content.Context
import android.os.Build
import android.text.Selection
import android.view.MotionEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.EditText
import org.apache.commons.lang3.StringUtils

/**
 * Delegate which adds support for ExploreByTouch to an EditText. TalkBack reads each line as the user hovers over them.
 */
class AztecTextAccessibilityDelegate(private val aztecText: EditText) {
    private val ACCESSIBILITY_INVALID_LINE_ID = -1

    private val mediaItemContentDescription = aztecText.getContext().getString(R.string.media_item_content_description)
    private val cursorMovedText = aztecText.getContext().getString(R.string.cursor_moved)
    private val accessibilityManager = aztecText.context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

    /**
     * Offset of most recently announced line.
     */
    private var lastLineAnnouncedForAccessibilityOffset = ACCESSIBILITY_INVALID_LINE_ID

    fun onHoverEvent(event: MotionEvent): Boolean {
        if (!accessibilityManager.isEnabled() || !accessibilityManager.isTouchExplorationEnabled) {
            return false
        }
        if (event.action == MotionEvent.ACTION_HOVER_ENTER) {
            resetLastLineAnnouncedForAccessibilityOffset()
        }
        if (event.action == MotionEvent.ACTION_HOVER_EXIT) {
            moveCursor(event.x, event.y)
        }
        return announceForAccessibility(event)
    }

    private fun resetLastLineAnnouncedForAccessibilityOffset() {
        lastLineAnnouncedForAccessibilityOffset = ACCESSIBILITY_INVALID_LINE_ID
    }

    private fun moveCursor(x: Float, y: Float) {
        // we need to remove the selection first, otherwise the TalkBack reads the text between old and new cursor position
        Selection.removeSelection(aztecText.text)
        aztecText.announceForAccessibility(cursorMovedText)
        Selection.setSelection(aztecText.text, aztecText.getOffsetForPosition(x, y))
    }

    private fun announceForAccessibility(event: MotionEvent): Boolean {
        val lineOffset = getLineOffset(event.x, event.y)
        if (lineOffset != ACCESSIBILITY_INVALID_LINE_ID && lastLineAnnouncedForAccessibilityOffset != lineOffset) {
            announceLine(lineOffset)
        }
        if (lineOffset != ACCESSIBILITY_INVALID_LINE_ID) {
            return true
        }
        return false
    }

    private fun announceLine(lineOffset: Int) {
        if (!aztecText.isFocused || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !aztecText.isAccessibilityFocused)) {
            aztecText.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
        } else {
            val announcement = getTextAtLine(lineOffset).replace(Constants.IMG_STRING, mediaItemContentDescription)
            accessibilityManager.interrupt()
            aztecText.announceForAccessibility(announcement)
        }
        lastLineAnnouncedForAccessibilityOffset = lineOffset
    }

    private fun getLineOffset(x: Float, y: Float): Int {
        val charPos = aztecText.getOffsetForPosition(x, y)
        var lineOffset = ACCESSIBILITY_INVALID_LINE_ID
        if (charPos != -1) {
            lineOffset = aztecText.layout.getLineForOffset(charPos)
            // skip empty lines
            if (isLineBlank(lineOffset)) {
                lineOffset = ACCESSIBILITY_INVALID_LINE_ID
            }
        }
        return lineOffset
    }

    private fun getTextAtLine(lineOffset: Int): String {
        val lineStartOffset = aztecText.layout.getLineStart(lineOffset)
        val lineEndOffset = aztecText.layout.getLineEnd(lineOffset)
        return aztecText.text.substring(lineStartOffset, lineEndOffset)
    }

    private fun isLineBlank(lineOffset: Int): Boolean {
        return StringUtils.isBlank(getTextAtLine(lineOffset).replace(Constants.MAGIC_STRING, ""))
    }
}