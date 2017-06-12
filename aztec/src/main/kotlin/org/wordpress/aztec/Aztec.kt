package org.wordpress.aztec

import android.app.Activity
import android.support.annotation.IdRes
import android.view.View
import org.wordpress.aztec.plugins.IAztecPlugin
import org.wordpress.aztec.source.SourceViewEditText
import org.wordpress.aztec.toolbar.AztecToolbar
import org.wordpress.aztec.toolbar.AztecToolbarClickListener
import java.util.*

class Aztec private constructor(var activity: Activity) {

    var visualEditor: AztecText? = null
    var sourceEditor: SourceViewEditText? = null
    var formattingToolbar: AztecToolbar? = null

    var plugins: List<IAztecPlugin> = ArrayList()

    companion object Factory {
        fun with(activity: Activity) : Aztec {
            return Aztec(activity)
        }
    }

    fun initVisualEditor(@IdRes aztecTextId: Int) : Aztec {
        visualEditor = activity.findViewById(aztecTextId) as AztecText
        initHistory()
        return this
    }

    fun setVisualEditor(visualEditor: AztecText) : Aztec {
        this.visualEditor = visualEditor
        initHistory()
        return this
    }

    fun initSourceEditor(@IdRes sourceTextId: Int) : Aztec {
        sourceEditor = activity.findViewById(sourceTextId) as SourceViewEditText
        initHistory()
        return this
    }

    fun setSourceEditor(sourceEditor: SourceViewEditText) : Aztec {
        this.sourceEditor = sourceEditor
        initHistory()
        return this
    }

    private fun initHistory() {
        if (sourceEditor != null && visualEditor != null) {
            sourceEditor!!.history = visualEditor!!.history
        }
    }

    fun initToolbar(@IdRes toolbarId: Int, toolbarClickListener: AztecToolbarClickListener) : Aztec {
        formattingToolbar = activity.findViewById(toolbarId) as AztecToolbar
        if (formattingToolbar != null && visualEditor != null && sourceEditor != null) {
            formattingToolbar!!.setEditor(visualEditor!!, sourceEditor!!)
            formattingToolbar!!.setToolbarListener(toolbarClickListener)
            visualEditor!!.setToolbar(formattingToolbar!!)
        }
        return this
    }

    fun setImageGetter(imageGetter: Html.ImageGetter) : Aztec {
        return this
    }

    fun setVideoThumbnailGetter(videoThumbnailGetter: Html.VideoThumbnailGetter) : Aztec {
        return this
    }

    fun setOnImeBackListener(imeBackListener: AztecText.OnImeBackListener) : Aztec {
        return this
    }

    fun setOnTouchListener(onTouchListener: View.OnTouchListener) : Aztec {
        return this
    }

    fun setHistoryListener(historyListener: HistoryListener) : Aztec {
        return this
    }

    fun setOnImageTappedListener(onImageTappedListener: AztecText.OnImageTappedListener) : Aztec {
        return this
    }

    fun setOnVideoTappedListener(onVideoTappedListener: AztecText.OnVideoTappedListener) : Aztec {
        return this
    }

    fun addPlugin(plugin: IAztecPlugin) : Aztec {
        plugins += plugin
        return this
    }
}