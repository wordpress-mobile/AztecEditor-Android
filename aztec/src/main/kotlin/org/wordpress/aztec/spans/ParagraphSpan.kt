package org.wordpress.aztec.spans

import android.text.Layout
import org.wordpress.aztec.AztecAttributes

fun createParagraphSpan(nestingLevel: Int,
                        attributes: AztecAttributes = AztecAttributes(),
                        align: Layout.Alignment? = null) : IAztecBlockSpan =
        if (align == null) {
            ParagraphSpan(nestingLevel, attributes)
        } else {
            ParagraphSpanAligned(align, nestingLevel, attributes)
        }

/**
 * We need to have two classes for handling alignment at either the Span-level (ParagraphSpanAligned)
 * or the View-level (ParagraphSpan). IAztecAlignment implements AlignmentSpan, which has a
 * getAlignment method that returns a non-null Layout.Alignment. The Android system checks for
 * AlignmentSpans and, if present, overrides the view's gravity with their value. Having a class
 * that does not implement AlignmentSpan allows the view's gravity to control. These classes should
 * be created using the createParagraphSpan(...) method.
 */
open class ParagraphSpan(
        override var nestingLevel: Int,
        override var attributes: AztecAttributes = AztecAttributes()
) : IAztecBlockSpan {

    override var TAG: String = "p"

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1
}

class ParagraphSpanAligned(
        override var align: Layout.Alignment?,
        nestingLevel: Int,
        attributes: AztecAttributes = AztecAttributes()
) : ParagraphSpan(nestingLevel, attributes), IAztecAlignmentSpan
