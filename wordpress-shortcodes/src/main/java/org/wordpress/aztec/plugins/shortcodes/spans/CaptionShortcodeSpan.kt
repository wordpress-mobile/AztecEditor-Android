package org.wordpress.aztec.plugins.shortcodes.spans

import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.ParagraphStyle
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.spans.*
import org.xml.sax.Attributes

class CaptionShortcodeSpan(override var attributes: AztecAttributes,
                           override val TAG: String, override var nestingLevel: Int)
    : IAztecInlineBlockSpan