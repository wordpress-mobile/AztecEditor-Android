package org.wordpress.aztec.plugins.shortcodes

import org.wordpress.aztec.plugins.html2visual.IHtmlPreprocessor
import org.wordpress.aztec.plugins.shortcodes.utils.GutenbergUtils
import org.wordpress.aztec.plugins.visual2html.IHtmlPostprocessor

class AudioShortcodePlugin : IHtmlPreprocessor, IHtmlPostprocessor {

    private val TAG = "audio"

    override fun beforeHtmlProcessed(source: String): String {
        if (GutenbergUtils.contentContainsGutenbergBlocks(source)) return source
        return source.replace(Regex("(?<!\\[)\\[$TAG([^\\]]*)\\](?!\\])"), "<$TAG$1 />")
    }

    override fun onHtmlProcessed(source: String): String {
        if (GutenbergUtils.contentContainsGutenbergBlocks(source)) {
            // From https://developer.mozilla.org/en-US/docs/Web/HTML/Element/audio
            // > Tag omission	None, both the starting and ending tag are mandatory.
            return StringBuilder(source)
                    .replace(Regex("(<$TAG[^>]*?)(\\s*/>)"), "\$1></$TAG>")
        }

        return StringBuilder(source)
                .replace(Regex("<$TAG([^>]*(?<! )) */>"), "[$TAG$1]")
                .replace(Regex("<$TAG([^>]*(?<! )) *></$TAG>"), "[$TAG$1]")
    }
}