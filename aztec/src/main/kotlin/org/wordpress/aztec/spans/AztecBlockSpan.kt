package org.wordpress.aztec.spans

import org.wordpress.aztec.ParagraphFlagged


interface AztecBlockSpan : AztecLineBlockSpan, AztecParagraphStyle, AztecNestable, AztecSpan, ParagraphFlagged
