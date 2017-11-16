package org.wordpress.aztec.demo

import android.os.Bundle

class NonCalypsoActivity : MainActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        aztec.visualEditor.setCalypsoMode(false)
        aztec.sourceEditor?.setCalypsoMode(false)
    }
}