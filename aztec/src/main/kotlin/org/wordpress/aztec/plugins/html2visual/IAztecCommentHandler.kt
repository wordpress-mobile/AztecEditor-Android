package org.wordpress.aztec.plugins.html2visual

import android.content.Context
import android.text.Editable
import org.wordpress.aztec.plugins.IAztecPlugin

interface IAztecCommentHandler : IAztecPlugin {
    fun handleComment(text: String, output: Editable, context: Context, nestingLevel: Int) : Boolean
}