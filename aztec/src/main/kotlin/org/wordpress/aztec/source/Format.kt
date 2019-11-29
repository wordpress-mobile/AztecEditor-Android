package org.wordpress.aztec.source

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.wordpress.aztec.spans.AztecQuoteSpan
import org.wordpress.aztec.spans.AztecVisualLinebreak
import org.wordpress.aztec.spans.EndOfParagraphMarker
import org.wordpress.aztec.spans.IAztecAlignmentSpan
import org.wordpress.aztec.spans.IAztecParagraphStyle
import org.wordpress.aztec.spans.ParagraphSpan
import org.wordpress.aztec.util.CleaningUtils
import java.util.regex.Matcher
import java.util.regex.Pattern

object Format {
    // list of block elements
    private val block = "div|br|blockquote|ul|ol|li|p|pre|h1|h2|h3|h4|h5|h6|iframe|hr"

    private val iframePlaceholder = "iframe-replacement-0x0"

    @JvmStatic
    fun addSourceEditorFormatting(content: String, isCalypsoFormat: Boolean = false): String {
        var html = replaceAll(content, "iframe", iframePlaceholder)
        html = html.replace("<aztec_cursor>", "")

        val doc = Jsoup.parseBodyFragment(html).outputSettings(Document.OutputSettings().prettyPrint(!isCalypsoFormat))
        CleaningUtils.cleanNestedBoldTags(doc)
        if (isCalypsoFormat) {
            // remove empty span tags
            doc.select("*")
                    .filter { !it.hasText() && it.tagName() == "span" && it.childNodes().size == 0 }
                    .forEach { it.remove() }

            html = replaceAll(doc.body().html(), iframePlaceholder, "iframe")

            html = replaceAll(html, "<p>(?:<br ?/?>|\u00a0|\uFEFF| )*</p>", "<p>&nbsp;</p>")
            html = toCalypsoSourceEditorFormat(html)
        } else {
            html = replaceAll(doc.body().html(), iframePlaceholder, "iframe")

            val newlineToTheLeft = replaceAll(html, "(?<!</?($block)>)\n<((?!/?($block)).*?)>", "<$2>")
            val newlineToTheRight = replaceAll(newlineToTheLeft, "<(/?(?!$block).)>\n(?!</?($block)>)", "<$1>")
            val fixBrNewlines = replaceAll(newlineToTheRight, "([\t ]*)(<br>)(?!\n)", "$1$2\n$1")
            html = replaceAll(fixBrNewlines, ">([\t ]*)(<br>)", ">\n$1$2")
        }

        return html.trim()
    }

    @JvmStatic
    fun removeSourceEditorFormatting(html: String, isCalypsoFormat: Boolean = false, isGutenbergMode: Boolean = false): String {
        if (isCalypsoFormat) {
            val htmlWithoutSourceFormatting = toCalypsoHtml(html)
            val doc = Jsoup.parseBodyFragment(htmlWithoutSourceFormatting.replace("\n", "")).outputSettings(Document.OutputSettings().prettyPrint(false))
            return doc.body().html()
        } else {
            return if (isGutenbergMode) { html } else { replaceAll(html, "\\s*<(/?($block)(.*?))>\\s*", "<$1>") }
        }
    }

    private fun replaceAll(content: String, pattern: String, replacement: String): String {
        val p = Pattern.compile(pattern)
        val m = p.matcher(content)
        return m.replaceAll(replacement)
    }

    // Takes HTML and formats it for Source editor and calypso back-end
    // based on removep() from https://github.com/Automattic/wp-calypso/blob/master/client/lib/formatting/index.js
    fun toCalypsoSourceEditorFormat(htmlContent: String): String {
        var content = htmlContent
        if (TextUtils.isEmpty(content.trim { it <= ' ' })) {
            // Just whitespace, null, or undefined
            return ""
        }

        var preserve_linebreaks = false
        var preserve_br = false

        var p: Pattern
        var m: Matcher
        var sb: StringBuffer

        // Protect pre|script tags
        if (content.contains("<pre") || content.contains("<script")) {
            preserve_linebreaks = true

            content = content.replace(Regex("<(pre|script)[^>]*>[\\s\\S]+?</\\1>"), { matchResult: MatchResult ->
                var value = replaceAll(matchResult.groupValues[0], "<br ?/?>(\\r\\n|\\n)?", "<wp-line-break>")
                value = replaceAll(value, "</?p( [^>]*)?>(\\r\\n|\\n)?", "<wp-line-break>")
                replaceAll(value, "\\r?\\n", "<wp-line-break>")
            })
        }

        // keep <br> tags inside captions and remove line breaks
        if (content.contains("[caption")) {
            preserve_br = true
            p = Pattern.compile("\\[caption[\\s\\S]+?\\[/caption]")
            m = p.matcher(content)
            sb = StringBuffer()
            if (m.find()) {
                val result = replaceAll(content.substring(m.start(), m.end()), "<br([^>]*)>", "<wp-temp-br$1>")
                m.appendReplacement(sb, Matcher.quoteReplacement(replace(result, "[\\r\\n\\t]+", "")))
            }
            m.appendTail(sb)
            content = sb.toString()
        }
        if (content.contains("<hr")) {
            content = replaceAll(content, "<hr ?/?>", "<hr>")
        }

        // Pretty it up for the source editor
        val blocklist = "blockquote|ul|ol|li|table|thead|tbody|tfoot|tr|th|td|h[1-6]|fieldset|div|p"

        content = replaceAll(content, "\\s*</($blocklist)>\\s*", "</$1>\n")
        content = replaceAll(content, "\\s*<((?:$blocklist)(?: [^>]*)?)>", "\n<$1>")

        content = replaceAll(content, "\\s*<(!--.*?--|hr)>\\s*", "\n\n<$1>\n\n")

        // Mark </p> if it has any attributes.
        content = replaceAll(content, "(<p [^>]+>.*?)</p>", "$1</p#>")

        // Separate <div> containing <p>
        content = replaceAll(content, "(?i)<div( [^>]*)?>\\s*<p>", "<div$1>\n\n")

        // Remove <p> and <br />
        content = replaceAll(content, "(?i)\\s*<p>", "")
        content = replaceAll(content, "(?i)\\s*</p>\\s*", "\n\n")
        content = replaceAll(content, "\\n[\\s\\u00a0]+\\n", "\n\n")
        content = replaceAll(content, "(?i)\\s*<br ?/?>\\s*", "\n")

        // Fix some block element newline issues
        content = replaceAll(content, "\n\n<div", "\n<div")
        content = replaceAll(content, "</div>\n\n", "</div>\n")
        content = replaceAll(content, "(?i)\\s*\\[caption([^\\[]+)\\[/caption\\]\\s*", "\n\n[caption$1[/caption]\n\n")
        content = replaceAll(content, "caption\\]\\n\\n+\\[caption", "caption]\n\n[caption")

        content = replaceAll(content, "<li([^>]*)>", "\t<li$1>")

        if (content.contains("<option")) {
            content = replaceAll(content, "\\s*<option", "\n<option")
            content = replaceAll(content, "\\s*</select>", "\n</select>")
        }

        if (content.contains("<object")) {
            p = Pattern.compile("<object[\\s\\S]+?</object>")
            m = p.matcher(content)
            sb = StringBuffer()
            if (m.find()) {
                m.appendReplacement(sb, Matcher.quoteReplacement(replace(content.substring(m.start(), m.end()), "[\\r\\n]+", "")))
            }
            m.appendTail(sb)
            content = sb.toString()
        }

        // Unmark special paragraph closing tags
        content = replaceAll(content, "</p#>", "</p>")
        content = replaceAll(content, "\\s*(<p [^>]+>[\\s\\S]*?</p>)", "\n$1")

        // Trim whitespace
        content = replaceAll(content, "^\\s+", "")
        content = replaceAll(content, "[\\s\\u00a0]+$", "")

        content = replaceAll(content, "&nbsp;", " ")

        // put back the line breaks in pre|script
        if (preserve_linebreaks) {
            content = replaceAll(content, "<wp-line-break>", "\n")
        }

        // and the <br> tags in captions
        if (preserve_br) {
            content = replaceAll(content, "<wp-temp-br([^>]*)>", "<br$1>")
        }

        return content
    }

    // Converts visual newlines to <p> and <br> tags. This method produces html used for html2span parser
    // based on wpautop() from https://github.com/Automattic/wp-calypso/blob/master/client/lib/formatting/index.js
    fun toCalypsoHtml(formattedHtml: String): String {
        // remove references to cursor when in calypso mode
        var html = formattedHtml.replace("<aztec_cursor></aztec_cursor>", "")
        if (TextUtils.isEmpty(html.trim { it <= ' ' })) {
            // Just whitespace, null, or undefined
            return ""
        }

        var preserve_linebreaks = false
        var preserve_br = false

        var p: Pattern
        var m: Matcher
        var sb: StringBuffer

        val blocklist = "table|thead|tfoot|caption|col|colgroup|tbody|tr|td|th|div|dl|dd|dt|ul|ol|li|pre" +
                "|form|map|area|blockquote|address|math|style|p|h[1-6]|hr|fieldset|legend|section" +
                "|article|aside|hgroup|header|footer|nav|figure|details|menu|summary"

        if (html.contains("<object")) {
            p = Pattern.compile("<object[\\s\\S]+?</object>")
            m = p.matcher(html)
            sb = StringBuffer()
            while (m.find()) {
                m.appendReplacement(sb, Matcher.quoteReplacement(replaceAll(html.substring(m.start(), m.end()), "[\\r\\n]+", "")))
            }
            m.appendTail(sb)
            html = sb.toString()
        }

        p = Pattern.compile("<[^<>]+>")
        m = p.matcher(html)
        sb = StringBuffer()
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(replaceAll(html.substring(m.start(), m.end()), "[\\r\\n]+", "")))
        }
        m.appendTail(sb)
        html = sb.toString()

        // Protect pre|script tags
        if (html.contains("<pre") || html.contains("<script")) {
            preserve_linebreaks = true

            html = html.replace(Regex("<(pre|script)[^>]*>[\\s\\S]+?</\\1>"), { matchResult: MatchResult ->
                replaceAll(matchResult.groupValues[0], "(\\r\\n|\\n)", "<wp-line-break>")
            })
        }

        // keep <br> tags inside captions and convert line breaks
        if (html.contains("[caption' )")) {
            preserve_br = true

            p = Pattern.compile("\\[caption[\\s\\S]+?\\[/caption]")
            m = p.matcher(html)
            sb = StringBuffer()
            while (m.find()) {
                // keep existing <br>
                m.appendReplacement(sb, Matcher.quoteReplacement(replaceAll(html.substring(m.start(), m.end()), "<br([^>]*)>", "<wp-temp-br$1>")))

                // no line breaks inside HTML tags
                val p2 = Pattern.compile("<[a-zA-Z0-9]+( [^<>]+)?>")
                val content = html.substring(m.start(), m.end())
                val m2 = p2.matcher(content)
                val sb2 = StringBuffer()
                while (m2.find()) {
                    m2.appendReplacement(sb2, Matcher.quoteReplacement(replace(content.substring(m2.start(), m2.end()), "[\\r\\n\\t]+", " ")))
                }
                m2.appendTail(sb2)
                m.appendReplacement(sb, Matcher.quoteReplacement(sb2.toString()))

                // convert remaining line breaks to <br>
                m.appendReplacement(sb, Matcher.quoteReplacement(replaceAll(html.substring(m.start(), m.end()), "\\s*\\n\\s*", "<wp-temp-br />")))
            }
            m.appendTail(sb)
            html = sb.toString()
        }

        html = replaceAll(html, "(?i)<br ?/?>\\s*<br ?/?>", "\n\n")
        html = replaceAll(html, "(?i)(<(?:$blocklist)(?: [^>]*)?>)", "\n$1")
        html = replaceAll(html, "(?i)(</(?:$blocklist)>)", "$1\n\n")

        html = replaceAll(html, "(?i)(<!--(.*?)-->)", "\n$1\n\n")
        html = replaceAll(html, "(?i)<hr ?/?>", "<hr>\n\n") // hr is self closing block element

        html = replaceAll(html, "(?i)\\s*<option", "<option") // No <p> or <br> around <option>
        html = replaceAll(html, "(?i)</option>\\s*", "</option>")
        html = replaceAll(html, "\\r\\n|\\r", "\n")
        html = replaceAll(html, "\\n\\s*\\n+", "\n\n")
        html = replaceAll(html, "([\\s\\S]+?)\\n\\n", "<p>$1</p>\n")
        html = replaceAll(html, "(?i)<p>\\s*?</p>", "")
        html = replaceAll(html, "(?i)<p>\\s*(</?(?:$blocklist)(?: [^>]*)?>)\\s*</p>", "$1")
        html = replaceAll(html, "(?i)<p>(<li.+?)</p>", "$1")
        html = replaceAll(html, "(?i)<p>\\s*<blockquote([^>]*)>", "<blockquote$1><p>")
        html = replaceAll(html, "(?i)</blockquote>\\s*</p>", "</p></blockquote>")

        html = replaceAll(html, "(?i)<p>\\s*(</?(?:div)(?: [^>]*)?>)", "$1<p>")
        html = replaceAll(html, "(?i)(</?(?:div)(?: [^>]*)?>)\\s*</p>", "</p>$1")

        html = replaceAll(html, "(?i)<p>\\s*(</?(?:$blocklist)(?: [^>]*)?>)", "$1")
        html = replaceAll(html, "(?i)(</?(?:$blocklist)(?: [^>]*)?>)\\s*</p>", "$1")
        html = replaceAll(html, "(?i)\\s*\\n", "<br>\n")
        html = replaceAll(html, "(?i)(</?(?:$blocklist)[^>]*>)\\s*<br ?/?>", "$1")

        html = replaceAll(html, "(?i)<br ?/?>(\\s*</?(?:p|li|div|dl|dd|dt|th|pre|td|ul|ol)>)", "$1")
        html = replaceAll(html, "(?i)(?:<p>|<br ?/?>)*\\s*\\[caption([^\\[]+)\\[/caption\\]\\s*(?:</p>|<br ?/?>)*", "[caption$1[/caption]")

        // put back the line breaks in pre|script
        if (preserve_linebreaks) {
            html = replaceAll(html, "<wp-line-break>", "<br>")
        }

        if (preserve_br) {
            html = replaceAll(html, "<wp-temp-br([^>]*)>", "<br$1>")
        }

        return html.replace("\n", "").trim()
    }

    @JvmStatic
    fun preProcessSpannedText(text: Spannable, isCalypsoFormat: Boolean) {
        if (isCalypsoFormat) {
            text.getSpans(0, text.length, AztecVisualLinebreak::class.java).forEach {
                val spanStart = text.getSpanStart(it)
                val spanEnd = text.getSpanEnd(it)

                if (text.getSpans(spanStart, spanEnd, ParagraphSpan::class.java).isNotEmpty()) {
                    text.setSpan(EndOfParagraphMarker(), spanEnd, spanEnd + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            // we don't need paragraph spans in calypso at this point
            text.getSpans(0, text.length, ParagraphSpan::class.java)
                    .filter { it.attributes.isEmpty() }
                    .forEach {
                        text.removeSpan(it)
                    }
        }
    }

    @JvmStatic
    fun postProcessSpannedText(text: SpannableStringBuilder, isCalypsoFormat: Boolean) {
        if (isCalypsoFormat) {
            val spans = text.getSpans(0, text.length, EndOfParagraphMarker::class.java)
            spans.sortByDescending { text.getSpanStart(it) }

            // add additional newline to the end of every paragraph
            spans.forEach {
                val spanStart = text.getSpanStart(it)
                val spanEnd = text.getSpanEnd(it)

                if (text[spanStart] == '\n' && text.getSpans(spanEnd, spanEnd + 1, IAztecParagraphStyle::class.java)
                        .filter { (it !is ParagraphSpan || !it.attributes.isEmpty()) && text.getSpanStart(it) == spanEnd }.isEmpty()) {
                    text.insert(spanEnd, "\n")
                }

                if (text.getSpans(spanStart, spanEnd, AztecQuoteSpan::class.java)
                        .filter { text.getSpanEnd(it) == spanEnd }.isEmpty() &&
                    text.getSpans(spanStart, spanEnd, ParagraphSpan::class.java)
                            .filter { !it.attributes.isEmpty() }.isEmpty()) {
                    text.getSpans(spanStart, spanEnd, AztecVisualLinebreak::class.java).forEach { text.removeSpan(it) }
                }
            }

            // split up paragraphs that contain double newlines
            text.getSpans(0, text.length, ParagraphSpan::class.java)
                    .forEach {
                        val start = text.getSpanStart(it)
                        val end = text.getSpanEnd(it)
                        val double = text.indexOf("\n\n", start)
                        if (double != -1 && double < end) {
                            text.setSpan(it, start, double + 1, text.getSpanFlags(it))
                            text.setSpan(AztecVisualLinebreak(), double + 1, double + 2, text.getSpanFlags(it))
                        }
                    }

            // we don't care about actual ParagraphSpan in calypso that don't have attributes or are empty (paragraphs are made from double newline)
            text.getSpans(0, text.length, ParagraphSpan::class.java)
                    .filter {
                        val hasNoAttributes = it.attributes.isEmpty()
                        val isAligned = it is IAztecAlignmentSpan && it.align != null
                        val isEmpty = text.getSpanStart(it) == text.getSpanEnd(it) - 1
                        (hasNoAttributes && !isAligned) || isEmpty
                    }.forEach {
                        text.removeSpan(it)
                    }
        }
    }

    private fun replace(content: String, pattern: String, replacement: String): String {
        val p = Pattern.compile(pattern)
        val m = p.matcher(content)
        return m.replaceFirst(replacement)
    }
}
