package org.wordpress.aztec.spans

import android.text.style.ParagraphStyle

/**
 * Marks spans that are going to be parsed with {@link org.wordpress.aztec.AztecParser#withinHtml()}
 * Created in order to distinguish between spans that implement ParagraphStyle for various reasons, but have separate
 * parsing logic.
 **/
interface AztecParagraphStyle : ParagraphStyle