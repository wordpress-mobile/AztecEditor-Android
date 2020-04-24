package org.wordpress.aztec.spans

import android.text.Layout
import org.wordpress.aztec.AlignmentRendering
import org.wordpress.aztec.AztecAttributes

fun createHiddenHtmlBlockSpan(tag: String,
                              alignmentRendering: AlignmentRendering,
                              nestingLevel: Int,
                              attributes: AztecAttributes = AztecAttributes()) : HiddenHtmlBlockSpan =
        when (alignmentRendering) {
            AlignmentRendering.SPAN_LEVEL -> HiddenHtmlBlockSpanAligned(tag, attributes, nestingLevel)
            AlignmentRendering.VIEW_LEVEL -> HiddenHtmlBlockSpan(tag, attributes, nestingLevel)
        }

/**
 * We need to have two classes for handling alignment at either the Span-level (HiddenHtmlBlockSpanAligned)
 * or the View-level (HiddenHtmlBlockSpan). IAztecAlignment implements AlignmentSpan, which has a
 * getAlignment method that returns a non-null Layout.Alignment. The Android system checks for
 * AlignmentSpans and, if present, overrides the view's gravity with their value. Having a class
 * that does not implement AlignmentSpan allows the view's gravity to control. These classes should
 * be created using the createHeadingSpan(...) method.
 */
class HiddenHtmlBlockSpanAligned(tag: String,
                                 override var attributes: AztecAttributes,
                                 override var nestingLevel: Int
) : HiddenHtmlBlockSpan(tag, attributes, nestingLevel), IAztecAlignmentSpan {
    override var align: Layout.Alignment? = null
}

open class HiddenHtmlBlockSpan(tag: String,
                               override var attributes: AztecAttributes,
                               override var nestingLevel: Int) : IAztecBlockSpan {
    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1
    override val TAG: String = tag
}
