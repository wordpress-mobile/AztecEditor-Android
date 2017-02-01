package org.wordpress.aztec.spans

import android.os.Parcel
import android.text.style.LeadingMarginSpan
import org.wordpress.aztec.formatting.BlockFormatter


abstract class AztecListSpan : LeadingMarginSpan.Standard, AztecBlockSpan {
    abstract var lastItem: AztecListItemSpan

    constructor(listStyle: BlockFormatter.ListStyle, attributes: String, last: AztecListItemSpan) : super(listStyle.indicatorMargin)

    constructor(src: Parcel) : super(src)

    constructor(attributes: String) : super(0)

    constructor() : super(0)

    fun getLineNumber(text: CharSequence, end: Int): Int {
        val textBeforeBeforeEnd = text.substring(0, end)
        val lineIndex = textBeforeBeforeEnd.length - textBeforeBeforeEnd.replace("\n", "").length
        return lineIndex + 1
    }
}
