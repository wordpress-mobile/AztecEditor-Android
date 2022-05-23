package org.wordpress.aztec.demo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import org.wordpress.android.util.ToastUtils
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.demo.PlaceholderManager.PlaceholderDrawer.PlaceholderHeight

class LayoutDrawer(override val placeholderHeight: PlaceholderHeight = PlaceholderHeight.Ratio(0.5f)) : PlaceholderManager.PlaceholderDrawer {
    override fun onCreateView(context: Context, id: String, attrs: AztecAttributes): View {
        return LayoutInflater.from(context).inflate(R.layout.test_layout, null).apply {
            findViewById<TextView>(R.id.content_text).text = id
            setOnClickListener {
                ToastUtils.showToast(context, "Testing click")
            }
        }
    }
}

