package org.wordpress.aztec.plugins.shortcodes

import org.wordpress.aztec.plugins.shortcodes.extensions.ATTRIBUTE_VIDEOPRESS_HIDDEN_ID
import org.wordpress.aztec.plugins.shortcodes.extensions.ATTRIBUTE_VIDEOPRESS_HIDDEN_SRC
import org.wordpress.aztec.plugins.html2visual.IHtmlPreprocessor
import org.wordpress.aztec.plugins.visual2html.IHtmlPostprocessor

class VideoShortcodePlugin : IHtmlPreprocessor, IHtmlPostprocessor {

    private val TAG = "video"
    private val TAG_VIDEOPRESS_SHORTCODE = "wpvideo"

    override fun beforeHtmlProcessed(source: String): String {
        var newSource = source.replace(Regex("(?<!\\[)\\[$TAG([^\\]]*)\\](?!\\])"), "<$TAG$1 />")
        newSource = newSource.replace(Regex("(?<!\\[)\\[$TAG_VIDEOPRESS_SHORTCODE([^\\]]*)\\](?!\\])"), { it -> fromVideoPressShortCodeToHTML(it) })
        return newSource
    }

    override fun onHtmlProcessed(source: String): String {
        return StringBuilder(source)
                .replace(Regex("<$TAG([^>]*(?<! )) */>"), { it -> fromHTMLToShortcode(it) })
                .replace(Regex("<$TAG([^>]*(?<! )) */>"), { it -> fromHTMLToShortcode(it) })
    }

    /**
     * This function is used to convert VideoPress shortcode to HTML video.
     * Called by `beforeHtmlProcessed`.
     *
     * For example, a shortcode like the following
     * `[wpvideo OcobLTqC w=640 h=400 autoplay=true html5only=true3]` will be converted to
     * `<video videopress_hidden_id=OcobLTqC w=640 h=400 autoplay=true html5only=true3>
     */
    private fun fromVideoPressShortCodeToHTML(source: MatchResult): String {
        val match = source.groupValues.get(1)
        val splittedMatch = match.split(" ")
        val attributesBuilder = StringBuilder()
        attributesBuilder.append("<$TAG ")

        splittedMatch.forEach {
            if (!it.isBlank()) {
                if (it.contains('=')) {
                    attributesBuilder.append(it)
                } else {
                    // This is the videopress ID
                    attributesBuilder.append("$ATTRIBUTE_VIDEOPRESS_HIDDEN_ID=" + it)
                }
                attributesBuilder.append(' ')
            }
        }

        attributesBuilder.append(" />")
        return attributesBuilder.toString()
    }

    /**
     * This function is used to convert HTML video tag to the correct video shortcode.
     * At the moment standard WordPress `video` shortcodes and VideoPress `wpvideo` shortcodes are supported.
     */
    private fun fromHTMLToShortcode(source: MatchResult): String {
        val match = source.groupValues.get(1)
        val splittedMatch = match.split(" ")
        val attributesBuilder = StringBuilder()

        var isVideoPress = false
        splittedMatch.forEach {
            if (!it.isBlank()) {
                if (it.contains(ATTRIBUTE_VIDEOPRESS_HIDDEN_ID)) {
                    // This is the videopress ID attribute
                    val splitted = it.split("=")
                    if (splitted.size == 2) {
                        // just make sure there is a correct ID
                        attributesBuilder.append(splitted[1].replace("\"", ""))
                        isVideoPress = true
                    }
                } else if (it.contains(ATTRIBUTE_VIDEOPRESS_HIDDEN_SRC)) {
                    // nope - do nothing. It's just used to keep a reference to the real src of the video,
                    // and use it to play the video in the apps
                } else {
                    attributesBuilder.append(it)
                }
                attributesBuilder.append(' ')
            }
        }

        val shotcodeTag = if (isVideoPress) TAG_VIDEOPRESS_SHORTCODE else TAG
        return "[$shotcodeTag " + attributesBuilder.toString().trim() + "]"
    }
}
