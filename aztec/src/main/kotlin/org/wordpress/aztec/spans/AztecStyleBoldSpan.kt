package org.wordpress.aztec.spans

import android.graphics.Typeface

class AztecStyleBoldSpan(override var nestingLevel: Int = 0, attributes: String = "") : AztecStyleSpan(0,Typeface.BOLD, attributes) {
}