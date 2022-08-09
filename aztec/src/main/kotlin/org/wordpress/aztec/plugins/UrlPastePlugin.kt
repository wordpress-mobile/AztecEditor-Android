package org.wordpress.aztec.plugins

import android.util.Patterns

/**
 * This plugin overrides the paste logic of URLs in the AztecText. The purpose is to make sure inserted links are
 * treated as HTML links.
 */
class UrlPastePlugin : ITextPastePlugin {
    /**
     * If the pasted text is a link, make sure it's wrapped with the `a` tag so that it's rendered as a link.
     */
    override fun toHtml(pastedText: String): String {
        return if (Patterns.WEB_URL.matcher(pastedText).matches()) {
            "<a href=\"$pastedText\">$pastedText</a>"
        } else {
            pastedText
        }
    }

    /**
     * If the pasted text is a link, make sure the selected text is wrapped with `a` tag and not removed.
     */
    override fun toHtml(selectedText: String, pastedText: String): String {
        return if (Patterns.WEB_URL.matcher(pastedText).matches()) {
            "<a href=\"$pastedText\">$selectedText</a>"
        } else {
            pastedText
        }
    }
}

