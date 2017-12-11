package org.wordpress.aztec.plugins.html2visual

import android.text.Editable
import org.wordpress.aztec.plugins.IAztecPlugin

interface IHtmlTextHandler : IAztecPlugin {
    val pattern: String

    fun onHtmlTextMatch(text: String, output: Editable, nestingLevel: Int): Boolean
}