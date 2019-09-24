package org.wordpress.aztec.spans

import android.text.Layout
import android.text.style.AlignmentSpan

interface IAztecAlignmentSpan : AlignmentSpan {

    var align: Layout.Alignment?

    override fun getAlignment(): Layout.Alignment = align ?: Layout.Alignment.ALIGN_NORMAL

    fun shouldParseAlignmentToHtml(): Boolean = true
}