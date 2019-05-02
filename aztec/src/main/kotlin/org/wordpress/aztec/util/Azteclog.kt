package org.wordpress.aztec.util

import android.text.Spanned
import android.text.TextUtils
import org.wordpress.android.util.AppLog
import org.wordpress.aztec.AztecText
import java.util.Arrays
import java.util.Collections

class AztecLog {
    interface ExternalLogger {
        fun log(message : String)
        fun logException(tr : Throwable)
        fun logException(tr : Throwable, message : String)
    }

    companion object {
        private const val MARK = 1
        private const val POINT = 2
        private const val PARAGRAPH = 3

        fun logContentDetails(aztecText: AztecText) {
            AppLog.d(AppLog.T.EDITOR, "Below are the details of the content in the editor:")
            logContentDetails(aztecText.text)
        }

        fun logContentDetails(text: Spanned) {
            try {
                AppLog.d(AppLog.T.EDITOR, logSpansDetails(text))
            } catch (e: Exception) {
                AppLog.e(AppLog.T.EDITOR, "Uhh ohh! There was an error logging the spans details of the Editor. This should" +
                        "never happen.", e)
            }
        }

        @JvmStatic fun logSpansDetails(text: Spanned): String {
            val spans = text.getSpans(0, text.length, Any::class.java)
            val spansList = Arrays.asList<Any>(*spans)

            val sb = StringBuilder()
            sb.append('\n').append(text.toString().replace('\n', '¶').replace('\u200B', '¬')) // ␤↵↭
            sb.append("  length = " + text.length)

            for (span in spansList) {
                val start = text.getSpanStart(span)
                val end = text.getSpanEnd(span)

                var gap = text.length + 5

                sb.append('\n')

                if (start > 0) {
                    sb.append(spaces(start))
                    gap -= start
                }

                val spanMode = text.getSpanFlags(span) and Spanned.SPAN_POINT_MARK_MASK
                val leftMode = (spanMode and 0x30) ushr 4
                val rightMode = spanMode and 0x03

                if (end - start > 0) {
                    when (leftMode) {
                        MARK -> sb.append('>')
                        POINT -> sb.append('<')
                        PARAGRAPH -> sb.append(if (start == 0) '<' else '>')
                    }

                    gap--
                } else {
                    if (spanMode == Spanned.SPAN_INCLUSIVE_INCLUSIVE) {
                        sb.append('x')
                    } else if (spanMode == Spanned.SPAN_INCLUSIVE_EXCLUSIVE) {
                        sb.append('>')
                    } else if (spanMode == Spanned.SPAN_EXCLUSIVE_INCLUSIVE) {
                        sb.append('<')
                    } else if (spanMode == Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) {
                        sb.append('!')
                    } else if (spanMode == Spanned.SPAN_PARAGRAPH) {
                        if (start == 0) {
                            sb.append('!')
                        } else if (start == text.length) {
                            sb.append('<')
                        } else {
                            sb.append('>')
                        }
                    }
                }

                if (end - start - 1 > 0) {
                    sb.append(spaces(end - start - 1, "-"))
                    gap -= end - start - 1
                }

                if (end - start > 0) {
                    when (rightMode) {
                        MARK -> sb.append('>')
                        POINT -> sb.append('<')
                        PARAGRAPH -> sb.append(if (end == text.length) '<' else '>')
                    }
                    gap--
                }

                sb.append(spaces(gap))

                sb.append("   ")
                        .append(String.format("%03d", start))
                        .append(" -> ")
                        .append(String.format("%03d", end))
                        .append(" : ")
                        .append(span.javaClass.simpleName)
            }

            return sb.toString()
        }

        private fun spaces(count: Int, char: String = " "): String {
            return TextUtils.join("", Collections.nCopies(count, char))
        }
    }
}
