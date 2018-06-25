package org.wordpress.aztec.util

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser

object CleaningUtils {

    @JvmStatic
    fun cleanNestedBoldTags(doc: Document) {
        // clean all nested <b> tags that don't contain anything
        doc.select("b > b")
        .filter { !it.hasText() }
        .forEach { it.remove() }

        // unwrap text in between nested <b> tags that have some text in them
        doc.select("b > b")
        .filter { it.hasText() }
        .forEach { it.unwrap() }
    }

    @JvmStatic
    fun cleanNestedBoldTags(html: String) : String {
        val doc = Jsoup.parse(html, "", Parser.xmlParser()).outputSettings(Document.OutputSettings().prettyPrint(false))
        cleanNestedBoldTags(doc)
        return doc.html()
    }
}