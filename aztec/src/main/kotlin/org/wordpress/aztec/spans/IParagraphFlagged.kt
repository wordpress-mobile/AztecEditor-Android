package org.wordpress.aztec.spans

import android.annotation.SuppressLint

@SuppressLint("NewApi")
interface IParagraphFlagged {
    var startBeforeCollapse: Int
    fun clearStartBeforeCollapse() { startBeforeCollapse = -1 }
    fun hasCollapsed(): Boolean { return startBeforeCollapse != -1 }

    var endBeforeBleed: Int
    fun clearEndBeforeBleed() { endBeforeBleed = -1 }
    fun hasBled(): Boolean { return endBeforeBleed != -1 }
}
