package org.wordpress.aztec.spans

import android.text.Layout
import org.wordpress.aztec.AlignmentRendering
import org.wordpress.aztec.AztecAttributes

fun createParagraphSpan(nestingLevel: Int,
                        alignmentRendering: AlignmentRendering,
                        attributes: AztecAttributes = AztecAttributes()) : IAztecBlockSpan =
        when (alignmentRendering) {
            AlignmentRendering.SPAN_LEVEL -> ParagraphSpanAligned(nestingLevel, attributes, null)
            AlignmentRendering.VIEW_LEVEL -> ParagraphSpan(nestingLevel, attributes)
        }

fun createParagraphSpan(nestingLevel: Int,
                        align: Layout.Alignment?,
                        attributes: AztecAttributes = AztecAttributes()) : IAztecBlockSpan =
        ParagraphSpanAligned(nestingLevel, attributes, align)

/**
 * We need to have two classes for handling alignment at either the Span-level (ParagraphSpanAligned)
 * or the View-level (ParagraphSpan). IAztecAlignment implements AlignmentSpan, which has a
 * getAlignment method that returns a non-null Layout.Alignment. The Android system checks for
 * AlignmentSpans and, if present, overrides the view's gravity with their value. Having a class
 * that does not implement AlignmentSpan allows the view's gravity to control. These classes should
 * be created using the createParagraphSpan(...) methods.
 */
open class ParagraphSpan(
        override var nestingLevel: Int,
        override var attributes: AztecAttributes) : IAztecBlockSpan {

    override var TAG: String = "p"

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1
}

class ParagraphSpanAligned(
        nestingLevel: Int,
        attributes: AztecAttributes,
        override var align: Layout.Alignment?) : ParagraphSpan(nestingLevel, attributes), IAztecAlignmentSpan
