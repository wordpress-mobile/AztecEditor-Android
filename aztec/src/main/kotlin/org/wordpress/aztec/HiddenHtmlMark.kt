package org.wordpress.aztec

import android.text.TextPaint
import android.text.style.CharacterStyle

import org.xml.sax.Attributes

class HiddenHtmlMark(tag: String, attributes: StringBuilder) : CharacterStyle() {

    val startTag: StringBuilder
    val endTag: StringBuilder
    var isClosed: Boolean = false
        private set
    var isParsed: Boolean = false
        private set
    var endOrder: Int = 0
        private set

    init {
        this.startTag = StringBuilder()
        this.startTag.append("<").append(tag).append(attributes).append(">")

        this.endTag = StringBuilder()
        this.endTag.append("</").append(tag).append(">")

        isClosed = false
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

    private class Hidden(internal var attributes: Attributes)
}
