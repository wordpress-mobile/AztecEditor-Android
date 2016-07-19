/*
 * Copyright (C) 2016 Automattic
 * Copyright (C) 2015 Matthew Lee
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wordpress.aztec

import android.content.Context
import android.graphics.Typeface
import android.text.Spanned
import android.text.TextUtils
import android.text.style.*

object AztecParser {
    fun fromHtml(source: String, context: Context): Spanned {
        return Html.fromHtml(source, null, AztecTagHandler(), context)
    }

    fun toHtml(text: Spanned): String {
        val out = StringBuilder()
        withinHtml(out, text)
        return tidy(out.toString())
    }

    private fun withinHtml(out: StringBuilder, text: Spanned) {
        var next: Int

        var i = 0
        while (i < text.length) {
            next = text.nextSpanTransition(i, text.length, ParagraphStyle::class.java)

            val styles = text.getSpans(i, next, ParagraphStyle::class.java)
            if (styles.size == 2) {
                if (styles[0] is BulletSpan && styles[1] is QuoteSpan) {
                    // Let a <br> follow the BulletSpan or QuoteSpan end, so next++
                    withinBulletThenQuote(out, text, i, next++)
                } else if (styles[0] is QuoteSpan && styles[1] is BulletSpan) {
                    withinQuoteThenBullet(out, text, i, next++)
                } else {
                    withinContent(out, text, i, next)
                }
            } else if (styles.size == 1) {
                if (styles[0] is BulletSpan) {
                    withinBullet(out, text, i, next++)
                } else if (styles[0] is AztecQuoteSpan) {
                    withinQuote(out, text, i, next++)
                } else if (styles[0] is UnknownHtmlSpan) {
                    withinUnknown(styles[0] as UnknownHtmlSpan, out)
                } else {
                    withinContent(out, text, i, next)
                }
            } else {
                withinContent(out, text, i, next)
            }
            i = next
        }
    }

    private fun withinUnknown(unknownHtmlSpan: UnknownHtmlSpan, out: StringBuilder) {
        out.append(unknownHtmlSpan.getRawHtml())
    }

    private fun withinBulletThenQuote(out: StringBuilder, text: Spanned, start: Int, end: Int) {
        out.append("<ul><li>")
        withinQuote(out, text, start, end)
        out.append("</li></ul>")
    }

    private fun withinQuoteThenBullet(out: StringBuilder, text: Spanned, start: Int, end: Int) {
        out.append("<blockquote>")
        withinBullet(out, text, start, end)
        out.append("</blockquote>")
    }

    private fun withinBullet(out: StringBuilder, text: Spanned, start: Int, end: Int) {
        out.append("<ul>")

        var next: Int

        var i = start
        while (i < end) {
            next = text.nextSpanTransition(i, end, BulletSpan::class.java)

            val spans = text.getSpans(i, next, BulletSpan::class.java)
            for (span in spans) {
                out.append("<li>")
            }

            withinContent(out, text, i, next)
            for (span in spans) {
                out.append("</li>")
            }
            i = next
        }

        out.append("</ul>")
    }

    private fun withinQuote(out: StringBuilder, text: Spanned, start: Int, end: Int) {
        var next: Int

        var i = start
        while (i < end) {
            next = text.nextSpanTransition(i, end, QuoteSpan::class.java)

            val quotes = text.getSpans(i, next, QuoteSpan::class.java)
            for (quote in quotes) {
                out.append("<blockquote>")
            }

            withinContent(out, text, i, next)
            for (quote in quotes) {
                out.append("</blockquote>")
            }
            i = next
        }
    }

    private fun withinContent(out: StringBuilder, text: Spanned, start: Int, end: Int) {
        var next: Int

        var i = start
        while (i < end) {
            next = TextUtils.indexOf(text, '\n', i, end)
            if (next < 0) {
                next = end
            }

            var nl = 0
            while (next < end && text[next] == '\n') {
                next++
                nl++
            }

            withinParagraph(out, text, i, next - nl, nl)
            i = next
        }
    }

    // Copy from https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/text/Html.java,
    // remove some tag because we don't need them in Aztec.
    private fun withinParagraph(out: StringBuilder, text: Spanned, start: Int, end: Int, nl: Int) {
        var next: Int

        run {
            var i = start
            while (i < end) {
                next = text.nextSpanTransition(i, end, CharacterStyle::class.java)

                val spans = text.getSpans(i, next, CharacterStyle::class.java)
                for (j in spans.indices) {
                    if (spans[j] is StyleSpan) {
                        val style = (spans[j] as StyleSpan).style

                        if (style and Typeface.BOLD != 0) {
                            out.append("<b>")
                        }

                        if (style and Typeface.ITALIC != 0) {
                            out.append("<i>")
                        }
                    }

                    if (spans[j] is UnderlineSpan) {
                        out.append("<u>")
                    }

                    // Use standard strikethrough tag <del> rather than <s> or <strike>
                    if (spans[j] is StrikethroughSpan) {
                        out.append("<del>")
                    }

                    if (spans[j] is URLSpan) {
                        out.append("<a href=\"")
                        out.append((spans[j] as URLSpan).url)
                        out.append("\">")
                    }

                    if (spans[j] is ImageSpan) {
                        out.append("<img src=\"")
                        out.append((spans[j] as ImageSpan).source)
                        out.append("\">")

                        // Don't output the dummy character underlying the image.
                        i = next
                    }

                    if (spans[j] is CommentSpan) {
                        out.append("<!--")
                    }
                }

                withinStyle(out, text, i, next)

                for (j in spans.indices.reversed()) {
                    if (spans[j] is URLSpan) {
                        out.append("</a>")
                    }

                    if (spans[j] is StrikethroughSpan) {
                        out.append("</del>")
                    }

                    if (spans[j] is UnderlineSpan) {
                        out.append("</u>")
                    }

                    if (spans[j] is StyleSpan) {
                        val style = (spans[j] as StyleSpan).style

                        if (style and Typeface.BOLD != 0) {
                            out.append("</b>")
                        }

                        if (style and Typeface.ITALIC != 0) {
                            out.append("</i>")
                        }
                    }

                    if (spans[j] is CommentSpan) {
                        out.append("-->")
                    }
                }
                i = next
            }
        }

        for (i in 0..nl - 1) {
            out.append("<br>")
        }
    }

    private fun withinStyle(out: StringBuilder, text: CharSequence, start: Int, end: Int) {
        var i = start
        while (i < end) {
            val c = text[i]

            if (c == '<') {
                out.append("&lt;")
            } else if (c == '>') {
                out.append("&gt;")
            } else if (c == '&') {
                out.append("&amp;")
            } else if (c.toInt() >= 0xD800 && c.toInt() <= 0xDFFF) {
                if (c.toInt() < 0xDC00 && i + 1 < end) {
                    val d = text[i + 1]
                    if (d.toInt() >= 0xDC00 && d.toInt() <= 0xDFFF) {
                        i++
                        val codepoint = 0x010000 or c.toInt() - 0xD800 shl 10 or d.toInt() - 0xDC00
                        out.append("&#").append(codepoint).append(";")
                    }
                }
            } else if (c.toInt() > 0x7E || c < ' ') {
                out.append("&#").append(c.toInt()).append(";")
            } else if (c == ' ') {
                while (i + 1 < end && text[i + 1] == ' ') {
                    out.append("&nbsp;")
                    i++
                }

                out.append(' ')
            } else {
                out.append(c)
            }
            i++
        }
    }

    private fun tidy(html: String): String {
        return html.replace("</ul>(<br>)?".toRegex(), "</ul>").replace("</blockquote>(<br>)?".toRegex(), "</blockquote>")
    }
}
