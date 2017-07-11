package org.wordpress.aztec.plugins

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.Spanned
import android.text.style.CharacterStyle
import org.wordpress.aztec.plugins.IAztecPlugin

@SuppressLint("NewApi")
interface IAztecCommentHandler : IAztecPlugin {

    fun canHandle(span: CharacterStyle): Boolean {
        return true
    }

    fun shouldParseContent(): Boolean {
        return true
    }

    fun handleCommentHtml(text: String, output: Editable, context: Context, nestingLevel: Int) : Boolean {
        return true
    }

    fun handleCommentSpanStart(out: StringBuilder, span: CharacterStyle)
    fun handleCommentSpanEnd(out: StringBuilder, span: CharacterStyle)
}