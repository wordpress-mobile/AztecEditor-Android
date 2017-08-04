package org.wordpress.aztec.plugins.html2visual

import android.text.Editable
import org.wordpress.aztec.plugins.IAztecPlugin
import org.xml.sax.Attributes

interface IHtmlTagHandler : IAztecPlugin {
    fun canHandleTag(tag: String): Boolean
    fun handleTag(opening: Boolean, tag: String, output: Editable, attributes: Attributes, nestingLevel: Int): Boolean
}