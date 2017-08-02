package org.wordpress.aztec.plugins.shortcodes.handlers

import org.wordpress.aztec.handlers.GenericBlockHandler
import org.wordpress.aztec.plugins.shortcodes.spans.CaptionShortcodeSpan

class CaptionHandler : GenericBlockHandler<CaptionShortcodeSpan>(CaptionShortcodeSpan::class.java) {
    override fun handleNewlineInBody() {
        block.end = newlineIndex + 1
    }

    override fun handleNewlineAtTextEnd() {
        block.end = newlineIndex + 1
    }
}