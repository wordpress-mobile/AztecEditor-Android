package org.wordpress.aztec.plugins.shortcodes

import org.wordpress.aztec.plugins.html2visual.IHtmlPreprocessor
import org.wordpress.aztec.plugins.visual2html.IHtmlPostprocessor

class VideoShortcodePlugin : IHtmlPreprocessor, IHtmlPostprocessor {

    private val TAG = "video"
    private val TAG_VIDEOPRESS_SHORTCODE = "wpvideo"
    private val TAG_VIDEOPRESS_INNER_ID = "videopress_inner_id"

    private fun fromVidePressShortCodeToHTML(source: MatchResult): String {
        val match = source.groupValues.get(1)
        val splittedMatch = match.split(" ")
        var attributesBuilder = StringBuilder()
        attributesBuilder.append("<$TAG ")


        splittedMatch.forEach {
            if (!it.isBlank()) {
                if (it.contains('=')) {
                    attributesBuilder.append(it)
                } else {
                    // This is the videopress ID
                    attributesBuilder.append("$TAG_VIDEOPRESS_INNER_ID=" + it)
                }
                attributesBuilder.append(' ')
            }
        }

        attributesBuilder.append(" />")
        return attributesBuilder.toString()
    }

    override fun beforeHtmlProcessed(source: String): String {
        var newSource = source.replace(Regex("(?<!\\[)\\[$TAG([^\\]]*)\\](?!\\])"), "<$TAG$1 />")
        newSource = newSource.replace(Regex("(?<!\\[)\\[$TAG_VIDEOPRESS_SHORTCODE([^\\]]*)\\](?!\\])"), { it -> fromVidePressShortCodeToHTML(it) })
        return newSource
    }

    private fun fromHTMLToShortcode(source: MatchResult): String {
        val match = source.groupValues.get(1)
        val splittedMatch = match.split(" ")
        var attributesBuilder = StringBuilder()

        var isVideoPress = false
        splittedMatch.forEach {
            if (!it.isBlank()) {
                if (it.contains(TAG_VIDEOPRESS_INNER_ID)) {
                    // This is the videopress ID attribute
                    val splitted = it.split("=")
                    if (splitted.size == 2) {
                        // just make sure there is a correct ID
                        attributesBuilder.append(splitted[1].replace("\"", ""))
                        isVideoPress = true
                    }
                } else {
                    attributesBuilder.append(it)
                }
                attributesBuilder.append(' ')
            }
        }

        val shotcodeTag = if (isVideoPress) TAG_VIDEOPRESS_SHORTCODE else TAG
        return "[$shotcodeTag " + attributesBuilder.toString().trim() + "]"
    }

    override fun onHtmlProcessed(source: String): String {
        var newSource = StringBuilder(source)
                .replace(Regex("<$TAG([^>]*(?<! )) */>"), { it -> fromHTMLToShortcode(it) })
                .replace(Regex("<$TAG([^>]*(?<! )) */>"), { it -> fromHTMLToShortcode(it) })
        return newSource
    }
}
