package org.wordpress.aztec.plugins

/**
 * Use this plugin in order to override the default text paste behaviour. An example is overriding the paste so that
 * you can handle pasted URLs as links over the selected text.
 */
interface ITextPastePlugin : IAztecPlugin {
    /**
     * This method is called when the cursor is placed into the editor, no text is selected and the user pastes text
     * into the editor. This method should return HTML (plain text is OK if you don't apply any changes to the pasted
     * text).
     * @param pastedText pasted text
     * @return html of the result
     */
    fun toHtml(pastedText: String): String {
        return pastedText
    }

    /**
     * This method is called when some text is selected in the editor and the user pastes text. The default behaviour
     * is to replace the selected text with the pasted text but it can be changed in this method. This method should
     * return HTML (plain text is OK if you don't apply any changes to the pasted text).
     * @param selectedText currently selected text
     * @param pastedText text pasted over selected text
     * @return html of the result
     */
    fun toHtml(selectedText: String, pastedText: String): String {
        return pastedText
    }
}

