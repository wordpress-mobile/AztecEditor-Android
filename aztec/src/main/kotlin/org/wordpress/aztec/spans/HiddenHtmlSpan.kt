package org.wordpress.aztec.spans

import android.text.TextPaint
import android.text.style.CharacterStyle

class HiddenHtmlSpan(tag: String, attributes: StringBuilder, openOrder : Int) : CharacterStyle() {

    val startTag: StringBuilder
    val endTag: StringBuilder
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
        this.startTag = StringBuilder()
        this.startTag.append("<").append(tag).append(attributes).append(">")

        this.endTag = StringBuilder()
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
