package org.wordpress.aztec.plugins.html2visual

import org.wordpress.aztec.plugins.IAztecPlugin

interface IHtmlPreprocessor : IAztecPlugin {
    fun beforeHtmlProcessed(source: String): String
}