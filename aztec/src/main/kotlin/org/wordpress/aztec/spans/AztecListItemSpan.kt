package org.wordpress.aztec.spans

import android.text.Layout
import org.wordpress.aztec.AlignmentRendering
import org.wordpress.aztec.AztecAttributes

fun createListItemSpan(nestingLevel: Int,
                       alignmentRendering: AlignmentRendering,
                       attributes: AztecAttributes = AztecAttributes()) : IAztecBlockSpan =
        when (alignmentRendering) {
            AlignmentRendering.SPAN_LEVEL -> AztecListItemSpanAligned(nestingLevel, attributes, null)
            AlignmentRendering.VIEW_LEVEL -> AztecListItemSpan(nestingLevel, attributes)
        }

/**
 * We need to have two classes for handling alignment at either the Span-level (ListItemSpanAligned)
 * or the View-level (ListItemSpan). IAztecAlignment implements AlignmentSpan, which has a
 * getAlignment method that returns a non-null Layout.Alignment. The Android system checks for
 * AlignmentSpans and, if present, overrides the view's gravity with their value. Having a class
 * that does not implement AlignmentSpan allows the view's gravity to control. These classes should
 * be created using the createListItemSpan(...) methods.
 */
open class AztecListItemSpan(
        override var nestingLevel: Int,
        override var attributes: AztecAttributes) : IAztecCompositeBlockSpan {
    override val TAG = "li"

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1
}

class AztecListItemSpanAligned(
        nestingLevel: Int,
        attributes: AztecAttributes,
        override var align: Layout.Alignment?
) : AztecListItemSpan(nestingLevel, attributes), IAztecAlignmentSpan
