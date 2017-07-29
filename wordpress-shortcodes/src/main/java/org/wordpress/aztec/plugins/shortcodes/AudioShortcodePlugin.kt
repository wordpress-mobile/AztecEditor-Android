package org.wordpress.aztec.plugins.shortcodes

import org.wordpress.aztec.plugins.html2visual.IHtmlPreprocessor
import org.wordpress.aztec.plugins.visual2html.IHtmlPostprocessor

class AudioShortcodePlugin : IHtmlPreprocessor, IHtmlPostprocessor {

    private val TAG = "audio"

    override fun processHtmlBeforeParsing(source: String): String {
        return source.replace(Regex("\\[$TAG([^\\]]*)\\]"), "<$TAG$1 />")
    }

    override fun processHtmlAfterSerialization(source: String): String {
        return StringBuilder(source)
                .replace(Regex("<$TAG([^>]*(?<! )) */>"), "[$TAG$1]")
                .replace(Regex("<$TAG([^>]*(?<! )) *></$TAG>"), "[$TAG$1]")
    }
}