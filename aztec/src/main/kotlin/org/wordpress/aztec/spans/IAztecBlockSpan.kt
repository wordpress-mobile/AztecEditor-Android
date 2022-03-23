package org.wordpress.aztec.spans

import org.wordpress.aztec.ITextFormat

interface IAztecBlockSpan : IAztecParagraphStyle, IAztecSurroundedWithNewlines, IParagraphFlagged {
    /**
     * Marks the text format associated with the block span. This field is not mandatory as some block styles don't
     * have text formats.
     */
    val textFormat: ITextFormat?
}
