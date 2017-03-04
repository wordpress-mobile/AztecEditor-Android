package org.wordpress.aztec.spans

import org.wordpress.aztec.spans.ParagraphFlagged


interface AztecBlockSpan : AztecLineBlockSpan, AztecParagraphStyle, AztecNestable, AztecSpan, ParagraphFlagged
