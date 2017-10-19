package org.wordpress.aztec.spans

import android.text.TextPaint
import android.text.style.CharacterStyle
import org.wordpress.aztec.AztecAttributes

class HiddenHtmlSpan(tag: String, attributes: AztecAttributes = AztecAttributes(), openOrder: Int) : CharacterStyle() {
    val startTag: StringBuilder = StringBuilder()
    val endTag: StringBuilder = StringBuilder()

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
        this.startTag.append("<").append(tag)
        if (!attributes.isEmpty()) {
            this.startTag.append(" ").append(attributes)
        }
        this.startTag.append(">")

        this.endTag.append("</").append(tag).append(">")

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
