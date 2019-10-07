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
 * This class is the same as the {@link ParagraphSpanAligned except it does not implement
 * AlignmentSpan (via IAztecAlignmentSpan). This is necessary because IAztecParagraphSpan implements
 * AlignmentSpan which has a getAlignment method that returns a non-null Layout.Alignment. Since this
 * cannot be null it will always override the view's gravity. By having a class that does not implement
 * AlignmentSpan the view's gravity can control.
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
