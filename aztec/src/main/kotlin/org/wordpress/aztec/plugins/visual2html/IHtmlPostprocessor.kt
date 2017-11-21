package org.wordpress.aztec.plugins.visual2html

import org.wordpress.aztec.plugins.IAztecPlugin

interface IHtmlPostprocessor : IAztecPlugin {
    fun onHtmlProcessed(source: String): String
}