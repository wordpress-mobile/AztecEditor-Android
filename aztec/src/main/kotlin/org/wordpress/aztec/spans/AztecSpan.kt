package org.wordpress.aztec.spans

interface AztecSpan : AztecAttributedSpan {

    fun getStartTag(): String
    fun getEndTag(): String

}
