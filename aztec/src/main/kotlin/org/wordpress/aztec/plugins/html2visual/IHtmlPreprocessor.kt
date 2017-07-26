package org.wordpress.aztec.plugins.html2visual

import org.wordpress.aztec.plugins.IAztecPlugin

interface IHtmlPreprocessor : IAztecPlugin {
    fun processHtmlBeforeParsing(source: String): String
}