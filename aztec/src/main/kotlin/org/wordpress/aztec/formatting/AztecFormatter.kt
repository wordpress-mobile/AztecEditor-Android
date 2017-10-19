package org.wordpress.aztec.formatting

import android.text.Editable
import org.wordpress.aztec.AztecText

abstract class AztecFormatter(val editor: AztecText) {
    val selectionStart: Int
        get() = this.editor.selectionStart

    val selectionEnd: Int
        get() = this.editor.selectionEnd

    val editableText: Editable
        get() = this.editor.editableText
}
