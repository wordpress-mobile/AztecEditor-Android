package org.wordpress.aztec.placeholders

import org.wordpress.aztec.AztecText

/**
 * This class overrides the backspace event and stops backspace if the previous item is a placeholder span. This is
 * useful to show some kind of a dialog that will let the user decide if they want to really remove the placeholder.
 */
class PlaceholderBackspaceListener(private val visualEditor: AztecText, private val predicate: (span: AztecPlaceholderSpan) -> Boolean) : AztecText.BeforeBackSpaceListener {
    override fun shouldOverrideBackSpace(position: Int): Boolean {
        val editableText = visualEditor.editableText

        return editableText.getSpans(position, position + 1, AztecPlaceholderSpan::class.java).any {
            predicate(it)
        }
    }
}

