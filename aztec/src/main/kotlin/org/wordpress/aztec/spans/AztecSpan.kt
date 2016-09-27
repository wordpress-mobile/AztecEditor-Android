package org.wordpress.aztec.spans

interface AztecSpan {

    var attributes: String?

    fun getStartTag(): String
    fun getEndTag(): String

}
