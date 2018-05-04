package org.wordpress.aztec.plugins.shortcodes

import android.util.Base64
import org.wordpress.aztec.plugins.html2visual.IHtmlPreprocessor
import org.wordpress.aztec.plugins.visual2html.IHtmlPostprocessor

class GutenFreePlugin : IHtmlPreprocessor, IHtmlPostprocessor {

    private val HIDDEN_GB_ATTRIBUTE_USED_IN_TAGS = "gb_hidden_comment"
    private val REGEX_GB_ENDING_COMMENT = "<!-- /wp:.*? -->"
    private val REGEX_GB_START_COMMENT = "<!-- wp:(.*?) -->.*?\\<([^\\s]+)"
    private val HTML_TAG_MATCH = "<([\\w]+)[^>]*>(.*?)<\\/\\1>" // Need to recheck this!

    override fun beforeHtmlProcessed(source: String): String {
        var newSource = source.replace(Regex(REGEX_GB_ENDING_COMMENT), "")
        newSource = newSource.replace(Regex(REGEX_GB_START_COMMENT), { it -> injectGutenbergStartDataIntoTag(it) })
        return newSource
    }

    private fun injectGutenbergStartDataIntoTag(source: MatchResult): String {
        val gutenbergCommentMatch = String(Base64.encode(source.groupValues.get(1).toByteArray(), Base64.DEFAULT))
        val nextTagMatch = source.groupValues.get(2)
        return "<$nextTagMatch $HIDDEN_GB_ATTRIBUTE_USED_IN_TAGS=\"$gutenbergCommentMatch\""
    }

    // Called when Visual to HTML conversion happens, and re-set the proper GB HTML comment where needed
    override fun onHtmlProcessed(source: String): String {
        var newSource = source.replace(Regex(HTML_TAG_MATCH), { it -> ejectGutenbergCommentsIntoHTML(it) })
        return newSource
    }

    private fun ejectGutenbergCommentsIntoHTML(source: MatchResult): String {
        val tag = source.groupValues.get(0)

        val gbCommentStartIndex = tag.indexOf(HIDDEN_GB_ATTRIBUTE_USED_IN_TAGS)
        if (gbCommentStartIndex == -1) return source.value
        val gbCommentEndIndex = tag.indexOf( "\"", gbCommentStartIndex + HIDDEN_GB_ATTRIBUTE_USED_IN_TAGS.length + 2 , false)

        if (gbCommentEndIndex == -1 || gbCommentEndIndex == gbCommentStartIndex
                || gbCommentEndIndex == gbCommentStartIndex
                || gbCommentEndIndex < gbCommentStartIndex)
            return source.value

        val encodedValue = tag.substring(gbCommentStartIndex + HIDDEN_GB_ATTRIBUTE_USED_IN_TAGS.length + 2, gbCommentEndIndex)
        val decodedValue = String(Base64.decode(encodedValue, Base64.DEFAULT))
        return "<!-- wp:" + decodedValue + " -->" +
                tag.replaceRange(gbCommentStartIndex, gbCommentEndIndex + 1, "") + "<!-- /wp:" + decodedValue + " -->"
    }

    /*

     private val HTML_OPEN_TAG_MATCH = "(\\<[^(/|!)]([^\\>]+)[\\>])" // Matches HTML open tags that are not comments
    private val HTML_CLOSE_TAG_MATCH = "(\\</.*?[\\>])" // Matches HTML close tags
    private val REGEX_GB_ENDING_COMMENT = "(\\<([^\\>]+)[\\>])<!-- /wp:(.*?) -->"

    private fun injectGutenbergEndDataIntoTag(source: MatchResult): String {
        val gutenbergCommentMatch = String(Base64.encode(source.groupValues.get(3).toByteArray(), Base64.DEFAULT))
        val prevTagMatch = source.groupValues.get(2)
        return "<$prevTagMatch $HIDDEN_GB_ATTRIBUTE_USED_IN_TAGS=\"$gutenbergCommentMatch\">"
    }

    override fun onHtmlProcessed(source: String): String {
        var newSource = source.replace(Regex(HTML_OPEN_TAG_MATCH), { it -> ejectGutenbergStartDataIntoHTML(it) })
        newSource = newSource.replace(Regex(HTML_CLOSE_TAG_MATCH), { it -> ejectGutenbergEndDataIntoHTML(it) })
        return newSource
    }

    private fun ejectGutenbergStartDataIntoHTML(source: MatchResult): String {
        val tag = source.groupValues.get(0)
        val gbCommentStartIndex = tag.indexOf(HIDDEN_GB_ATTRIBUTE_USED_IN_TAGS)
        if (gbCommentStartIndex == -1) return source.value
        val gbCommentEndIndex = tag.indexOf( "\"", gbCommentStartIndex + HIDDEN_GB_ATTRIBUTE_USED_IN_TAGS.length + 2 , false)

        if (gbCommentEndIndex == -1 || gbCommentEndIndex == gbCommentStartIndex
                || gbCommentEndIndex == gbCommentStartIndex
                || gbCommentEndIndex < gbCommentStartIndex)
            return source.value

        val encodedValue = tag.substring(gbCommentStartIndex + HIDDEN_GB_ATTRIBUTE_USED_IN_TAGS.length + 2, gbCommentEndIndex)
        val decodedValue = String(Base64.decode(encodedValue, Base64.DEFAULT))
        return "<!-- wp:" +  decodedValue + " -->" +
                tag.replaceRange(gbCommentStartIndex, gbCommentEndIndex + 1, "")
    }

    private fun ejectGutenbergEndDataIntoHTML(source: MatchResult): String {
        val tag = source.groupValues.get(0)
        val gbCommentStartIndex = tag.indexOf(HIDDEN_GB_ATTRIBUTE_USED_IN_TAGS)
        if (gbCommentStartIndex == -1) return source.value
        val gbCommentEndIndex = tag.indexOf( "\"", gbCommentStartIndex + HIDDEN_GB_ATTRIBUTE_USED_IN_TAGS.length + 2 , false)

        if (gbCommentEndIndex == -1 || gbCommentEndIndex == gbCommentStartIndex
                || gbCommentEndIndex == gbCommentStartIndex
                || gbCommentEndIndex < gbCommentStartIndex)
            return source.value

        val encodedValue = tag.substring(gbCommentStartIndex + HIDDEN_GB_ATTRIBUTE_USED_IN_TAGS.length + 2, gbCommentEndIndex)
        val decodedValue = String(Base64.decode(encodedValue, Base64.DEFAULT))
        return tag.replaceRange(gbCommentStartIndex, gbCommentEndIndex + 1, "") + "<!-- /wp:" +  decodedValue + " -->"
    }*/
}
