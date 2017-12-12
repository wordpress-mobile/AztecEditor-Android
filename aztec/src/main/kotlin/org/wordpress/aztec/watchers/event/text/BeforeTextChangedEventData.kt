package org.wordpress.aztec.watchers.event.text

data class BeforeTextChangedEventData(val textBefore: CharSequence = "", val start: Int = 0, val count: Int = 0, val after: Int = 0) {

}

