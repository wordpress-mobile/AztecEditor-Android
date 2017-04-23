package org.wordpress.aztec.source

import org.jsoup.Jsoup
import java.util.regex.Pattern

object Format {

    // list of block elements
    private val block = "div|br|blockquote|ul|ol|li|p|pre|h1|h2|h3|h4|h5|h6|iframe|hr|aztec_cursor"

    private val iframePlaceholder = "iframe-replacement-0x0"

    fun addFormatting(content: String): String {
        // rename iframes to prevent encoding the inner HTML
        var html = replaceAll(content, "iframe", iframePlaceholder)
        html = Jsoup.parseBodyFragment(html).body().html()
        html = replaceAll(html, iframePlaceholder, "iframe")

        //remove newline around all non block elements
        val newlineToTheLeft = replaceAll(html, "(?<!</?($block)>)\n<((?!/?($block)).*?)>", "<$2>")
        val newlineToTheRight = replaceAll(newlineToTheLeft, "<(/?(?!$block).)>\n(?!</?($block)>)", "<$1>")
        var fixBrNewlines = replaceAll(newlineToTheRight, "([\t ]*)(<br>)(?!\n)", "$1$2\n$1")
        fixBrNewlines = replaceAll(fixBrNewlines, ">([\t ]*)(<br>)", ">\n$1$2")

        return fixBrNewlines.trim()
    }

    fun clearFormatting(html: String): String {
        // remove all whitespace around block elements
        return replaceAll(html, "\\s*<(/?($block)(.*?))>\\s*", "<$1>")
    }

    private fun replaceAll(content: String, pattern: String, replacement: String): String {
        val p = Pattern.compile(pattern)
        val m = p.matcher(content)
        return m.replaceAll(replacement)
    }
}
