package org.wordpress.aztec.plugins

import android.graphics.Canvas

/**
 * Use this plugin in order to get access to canvas during drawing cycle of AztecText
 */
interface IOnDrawPlugin : IAztecPlugin {
    /**
     * This method is called when onDraw method of AztecText is called.
     * @param canvas canvas of AztecText
     * @return html of the result
     */
    fun onDraw(canvas: Canvas)
}
