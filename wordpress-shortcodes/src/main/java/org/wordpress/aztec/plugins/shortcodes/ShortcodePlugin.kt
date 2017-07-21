package org.wordpress.aztec.plugins.shortcodes

import android.text.Editable
import org.wordpress.aztec.plugins.shortcodes.spans.CaptionShortcodeSpan

abstract class ShortcodePlugin(protected val tagName: String) {

    protected fun getLastSpan(output: Editable): CaptionShortcodeSpan? {
        var span: CaptionShortcodeSpan? = null
        val spans = output.getSpans(0, output.length, CaptionShortcodeSpan::class.java)
        if (spans.isNotEmpty()) {
            span = spans.last()
        }
        return span
    }

    protected fun isStart(text: String): Boolean {
        return text.startsWith("[$tagName")
    }

    protected fun parseAttributes(text: String): Map<String, String> {
        val map = HashMap<String, String>()

        if (isStart(text)) {
            val attrString = text.substring("[$tagName ".length..text.length-2).trim()
            val pairs = attrString.split(" ")

            pairs.forEach {
                val splitPair = it.split("=")
                if (splitPair.size == 2) {
                    map.put(splitPair[0], splitPair[1])
                }
            }
        }
        return map
    }

    protected fun joinAttributes(attrs: Map<String, String>): String {
        return attrs.map { it.key + "=" + it.value }.joinToString(" ")
    }
}