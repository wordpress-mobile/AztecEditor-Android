package org.wordpress.aztec.plugins

import android.content.Intent
import android.net.Uri

/**
 * Use this plugin in order to override the default item paste behaviour. An example is overriding the paste so that
 * you can handle pasted image URLs as images over the selected text.
 */
interface IClipboardPastePlugin<T : IClipboardPastePlugin.PastedItem> : IAztecPlugin {
    fun toHtml(pastedItem: T, selectedText: String? = null): String

    /**
     * This method is called when text is pasted into the editor. If text is selected, the default behaviour
     * is to replace the selected text with the pasted item but it can be changed by overriding this method.
     * If text is not selected, this returned object of this method is inserted into the text.
     * This method should return HTML (plain text is OK if you don't apply any changes to the pasted text).
     * @param pastedItem clipboard item pasted over selected text
     * @param selectedText currently selected text
     * @return html of the result
     */
    fun itemToHtml(pastedItem: PastedItem, selectedText: String? = null): String? {
        return when {
            pastedItem is PastedItem.HtmlText && this is ITextPastePlugin -> this.toHtml(pastedItem, selectedText)
            pastedItem is PastedItem.Url && this is IUriPastePlugin -> this.toHtml(pastedItem, selectedText)
            pastedItem is PastedItem.PastedIntent && this is IIntentPastePlugin -> this.toHtml(pastedItem, selectedText)
            else -> null
        }
    }

    interface ITextPastePlugin : IClipboardPastePlugin<PastedItem.HtmlText> {
        /**
         * Override this method if you only need to handle the pasted text and not other types. If returned value is
         * null, it will be ignored and the default behaviour will take over.
         * @param pastedItem pasted text
         * @return value of the pasted HTML
         */
        override fun toHtml(pastedItem: PastedItem.HtmlText, selectedText: String?): String
    }

    interface IUriPastePlugin : IClipboardPastePlugin<PastedItem.Url> {
        /**
         * Override this method to handle pasted URIs. If returned value is null, it will be ignored and the default
         * behaviour will take over.
         * @param pastedItem pasted URI
         * @return HTML representation of an URI
         */
        override fun toHtml(pastedItem: PastedItem.Url, selectedText: String?): String
    }

    interface IIntentPastePlugin : IClipboardPastePlugin<PastedItem.PastedIntent> {
        /**
         * Override this method to handle pasted intents. If returned value is null, it will be ignored and the default
         * behaviour will take over.
         * @param pastedItem Pasted intent
         * @return HTML representation of an intent
         */
        override fun toHtml(pastedItem: PastedItem.PastedIntent, selectedText: String?): String
    }

    /**
     * Pasted items supported by Clipboard
     */
    sealed class PastedItem {
        data class HtmlText(val text: String) : PastedItem()
        data class Url(val uri: Uri) : PastedItem()
        data class PastedIntent(val intent: Intent) : PastedItem()
    }
}

