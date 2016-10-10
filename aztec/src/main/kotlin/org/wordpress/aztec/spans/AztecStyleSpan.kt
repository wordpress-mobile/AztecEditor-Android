package org.wordpress.aztec.spans

import android.text.style.StyleSpan

class AztecStyleSpan(var tag: String, override var attributes: String?, style: Int) : StyleSpan(style), AztecSpan {

    override fun getStartTag(): String {
        return tag + attributes
    }

    override fun getEndTag(): String {
        return tag
    }
}