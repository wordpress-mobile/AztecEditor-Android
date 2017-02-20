package org.wordpress.aztec

import android.text.Spanned
import android.text.TextUtils
import java.util.*

/**
 * Created by hypest on 11/01/17.
 */
object SpanLogger {
    private fun spaces(count: Int, char: String = " "): String {
        return TextUtils.join("", Collections.nCopies(count, char))
    }

    @JvmStatic fun logSpans(text: Spanned): String {
        val spans = text.getSpans(0, 9999999, Any::class.java)
        val spansList = Arrays.asList<Any>(*spans)

//        // sort the spans list by start position and size
//        Collections.sort(spansList) { o1, o2 ->
//            var diff = text.getSpanStart(o1) - text.getSpanStart(o2)
//            if (diff == 0) {
//                diff = text.getSpanEnd(o1) - text.getSpanEnd(o2)
//            }
//
//            diff / Math.abs(if (diff == 0) 1 else diff)
//        }

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

            if (end - start > 0) {
                sb.append(if (spanMode == Spanned.SPAN_EXCLUSIVE_INCLUSIVE || spanMode == Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) '<' else '>')
//                sb.append(if (spanMode == Spanned.SPAN_EXCLUSIVE_INCLUSIVE || spanMode == Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) '⇤' else '⇠')
//                sb.append('|')
                gap--
            } else {
                if (spanMode == Spanned.SPAN_INCLUSIVE_INCLUSIVE) {
                    sb.append('x')
                } else if (spanMode == Spanned.SPAN_INCLUSIVE_EXCLUSIVE) {
                    sb.append('>')
                } else if (spanMode == Spanned.SPAN_EXCLUSIVE_INCLUSIVE) {
                    sb.append('<')
                } else if (spanMode == Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) {
                    sb.append('o')
                }
            }

            if (end - start - 1 > 0) {
                sb.append(spaces(end - start - 1, "-"))
                gap -= end - start - 1
            }

            if (end - start > 0) {
                sb.append(if (spanMode == Spanned.SPAN_INCLUSIVE_EXCLUSIVE || spanMode == Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) '>' else '<')
//                sb.append('|')
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

//    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
//        Log.v("edit", "beforeTextChanged size=" + s.length + ", s=" + s + ", start=" + start + ", count=" +
//                count + ", after=" + after + "\n\nspans:" + logSpans(s) + "\n\n ")
//    }
//
//    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//        Log.v("edit", "onTextChanged size=" + s.length + ", s=" + s + ", start=" + start + ", before=" + before
//                + ", count=" + count + "\n\nspans: " + logSpans(s) + "\n\n ")
//    }
//
//    override fun afterTextChanged(s: Editable) {
//        Log.v("edit", "afterTextChanged size=" + s.length + ", s=" + s + "\n\nspans:" + logSpans(s) + "\n\n ")
//    }
}