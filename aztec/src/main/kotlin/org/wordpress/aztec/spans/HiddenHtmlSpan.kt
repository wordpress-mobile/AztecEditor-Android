package org.wordpress.aztec.spans

import android.text.TextPaint
import android.text.style.CharacterStyle
import org.wordpress.aztec.AztecAttributes

class HiddenHtmlSpan(tag: String, override var attributes: AztecAttributes = AztecAttributes(), val openOrder: Int) : CharacterStyle(), IAztecSpan, IAztecAttributedSpan {

    override val TAG: String = tag

    var isClosed: Boolean = false
        private set
    var isOpened: Boolean = false
        private set
    var isParsed: Boolean = false
        private set
    var endOrder: Int = 0
        private set
    var startOrder = openOrder
        private set

    init {
        reset()
    }

    fun reset() {
        isClosed = false
        isOpened = false
        isParsed = false
    }

    override fun updateDrawState(textPaint: TextPaint) {
    }

    fun close(order: Int) {
        isClosed = true
        endOrder = order
    }

    fun parse() {
        isParsed = true
    }

    fun open() {
        isOpened = true
    }
}
