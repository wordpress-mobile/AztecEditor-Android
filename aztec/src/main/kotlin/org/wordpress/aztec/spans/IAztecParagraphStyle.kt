package org.wordpress.aztec.spans

import android.text.Layout
import android.text.style.AlignmentSpan

/**
 * Marks spans that are going to be parsed with {@link org.wordpress.aztec.AztecParser#withinHtml()}
 * Created in order to distinguish between spans that implement ParagraphStyle for various reasons, but have separate
 * parsing logic, like  {@link org.wordpress.aztec.spans.AztecHeadingSpan}
 **/
interface IAztecParagraphStyle : AlignmentSpan, IAztecSpan, IAztecNestable {

    var align: Layout.Alignment?

    override fun getAlignment(): Layout.Alignment {
        return align ?: Layout.Alignment.ALIGN_NORMAL
    }
}