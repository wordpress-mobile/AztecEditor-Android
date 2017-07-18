package org.wordpress.aztec.plugins.html2visual

import android.content.Context
import android.text.Editable
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.plugins.IAztecPlugin
import org.xml.sax.Attributes

interface ITagHandler : IAztecPlugin {
    fun handleTag(opening: Boolean, tag: String, output: Editable, context: Context,
                  attributes: Attributes, nestingLevel: Int): Boolean
}