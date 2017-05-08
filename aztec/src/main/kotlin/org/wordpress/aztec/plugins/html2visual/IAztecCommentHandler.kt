package org.wordpress.aztec.plugins.html2visual

import android.content.Context
import android.text.Editable

interface IAztecCommentHandler {
    fun handleComment(text: String, output: Editable, context: Context, nestingLevel: Int)
}