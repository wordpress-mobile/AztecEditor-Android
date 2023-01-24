package org.wordpress.aztec.plugins

import android.util.Patterns

/**
 * This plugin overrides the paste logic of URLs in the AztecText. The purpose is to make sure inserted links are
 * treated as HTML links.
 */
class UrlPastePlugin : IClipboardPastePlugin.ITextPastePlugin {
    /**
     * If the pasted text is a link, make sure it's wrapped with the `a` tag so that it's rendered as a link.
     */
    override fun toHtml(pastedItem: IClipboardPastePlugin.PastedItem.HtmlText, selectedText: String?): String {
        return if (Patterns.WEB_URL.matcher(pastedItem.text).matches()) {
            if (selectedText != null) {
                "<a href=\"${pastedItem.text}\">$selectedText</a>"
            } else {
                "<a href=\"${pastedItem.text}\">${pastedItem.text}</a>"
            }
        } else {
            pastedItem.text
        }
    }
}

