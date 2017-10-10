package org.wordpress.aztec

import org.xml.sax.Attributes
import org.xml.sax.helpers.AttributesImpl

class AztecAttributes(attributes: Attributes = AttributesImpl()) : AttributesImpl(attributes) {
    fun setValue(key: String, value: String) {
        val index = getIndex(key)
        if (index == -1) {
            addAttribute("", key, key, "string", value)
        } else {
            setValue(index, value)
        }
    }

    fun isEmpty() : Boolean {
        return length == 0
    }

    fun removeAttribute(key: String) {
        if (hasAttribute(key)) {
            val index = getIndex(key)
            removeAttribute(index)
        }
    }

    fun hasAttribute(key: String) : Boolean {
        return getValue(key) != null
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for (i in 0..this.length - 1) {
            sb.append(this.getLocalName(i))
            sb.append("=\"")
            sb.append(this.getValue(i))
            sb.append("\" ")
        }
        return sb.trimEnd().toString()
    }
}
