package org.wordpress.aztec.watchers.event.text

import android.text.SpannableStringBuilder

data class BeforeTextChangedEventData(val textBefore: SpannableStringBuilder?, val start: Int = 0, val count: Int = 0, val after: Int = 0)

