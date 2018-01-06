package org.wordpress.aztec.watchers.event.text

import android.text.SpannableStringBuilder

data class OnTextChangedEventData(val textOn: SpannableStringBuilder?, val start: Int = 0, val before: Int = 0, val count: Int = 0)

