package org.wordpress.aztec

import android.app.Activity
import android.support.annotation.IdRes
import android.view.View
import org.wordpress.aztec.plugins.IAztecPlugin
import org.wordpress.aztec.plugins.IToolbarButton
import org.wordpress.aztec.source.SourceViewEditText
import org.wordpress.aztec.toolbar.AztecToolbar
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener
import java.util.*

open class Aztec private constructor(val visualEditor: AztecText, val sourceEditor: SourceViewEditText,
                                     val toolbar: AztecToolbar, val toolbarClickListener: IAztecToolbarClickListener) {
        
    private var imageGetter: Html.ImageGetter? = null
    private var videoThumbnailGetter: Html.VideoThumbnailGetter? = null
    private var imeBackListener: AztecText.OnImeBackListener? = null
    private var onTouchListener: View.OnTouchListener? = null
    private var historyListener: IHistoryListener? = null
    private var onImageTappedListener: AztecText.OnImageTappedListener? = null
    private var onVideoTappedListener: AztecText.OnVideoTappedListener? = null
    private var plugins: ArrayList<IAztecPlugin> = visualEditor.plugins

    init {
        initHistory()
        initToolbar()
    }
    
    constructor(activity: Activity, @IdRes aztecTextId: Int,
                @IdRes sourceTextId: Int, @IdRes toolbarId: Int,
                toolbarClickListener: IAztecToolbarClickListener) : this(activity.findViewById(aztecTextId) as AztecText,
            activity.findViewById(sourceTextId) as SourceViewEditText, activity.findViewById(toolbarId) as AztecToolbar, toolbarClickListener)

    companion object Factory {
        fun with(activity: Activity, @IdRes aztecTextId: Int, @IdRes sourceTextId: Int, 
                 @IdRes toolbarId: Int, toolbarClickListener: IAztecToolbarClickListener) : Aztec {
            return Aztec(activity, aztecTextId, sourceTextId, toolbarId, toolbarClickListener)
        }

        fun with(visualEditor: AztecText, sourceEditor: SourceViewEditText,
                 toolbar: AztecToolbar, toolbarClickListener: IAztecToolbarClickListener) : Aztec {
            return Aztec(visualEditor, sourceEditor, toolbar, toolbarClickListener)
        }
    }

    fun setImageGetter(imageGetter: Html.ImageGetter) : Aztec {
        this.imageGetter = imageGetter
        initImageGetter()
        return this
    }

    fun setVideoThumbnailGetter(videoThumbnailGetter: Html.VideoThumbnailGetter) : Aztec {
        this.videoThumbnailGetter = videoThumbnailGetter
        initVideoGetter()
        return this
    }

    fun setOnImeBackListener(imeBackListener: AztecText.OnImeBackListener) : Aztec {
        this.imeBackListener = imeBackListener
        initImeBackListener()
        return this
    }

    fun setOnTouchListener(onTouchListener: View.OnTouchListener) : Aztec {
        this.onTouchListener = onTouchListener
        initTouchListener()
        return this
    }

    fun setOnImageTappedListener(onImageTappedListener: AztecText.OnImageTappedListener) : Aztec {
        this.onImageTappedListener = onImageTappedListener
        initImageTappedListener()
        return this
    }

    fun setOnVideoTappedListener(onVideoTappedListener: AztecText.OnVideoTappedListener) : Aztec {
        this.onVideoTappedListener = onVideoTappedListener
        initVideoTappedListener()
        return this
    }

    fun setHistoryListener(historyListener: IHistoryListener) : Aztec {
        this.historyListener = historyListener
        initHistoryListener()
        return this
    }

    fun addPlugin(plugin: IAztecPlugin) : Aztec {
        plugins.add(plugin)

        if (plugin is IToolbarButton) {
            toolbar.addButton(plugin)
        }

        return this
    }

    fun initHistory() {
        sourceEditor.history = visualEditor.history
    }

    private fun initToolbar() {
        toolbar.setEditor(visualEditor, sourceEditor)
        toolbar.setToolbarListener(toolbarClickListener)
        visualEditor.setToolbar(toolbar)
    }

    private fun initHistoryListener() {
        if (historyListener != null) {
            visualEditor.history.setHistoryListener(historyListener!!)
        }
    }

    private fun initImageGetter() {
        if (imageGetter != null) {
            visualEditor.imageGetter = imageGetter
        }
    }

    private fun initVideoGetter() {
        if (videoThumbnailGetter != null) {
            visualEditor.videoThumbnailGetter = videoThumbnailGetter
        }
    }

    private fun initImeBackListener() {
        if (imeBackListener != null) {
            visualEditor.setOnImeBackListener(imeBackListener!!)
        }
    }

    private fun initTouchListener() {
        if (onTouchListener != null) {
            visualEditor.setOnTouchListener(onTouchListener!!)
        }
    }

    private fun initImageTappedListener() {
        if (onImageTappedListener != null) {
            visualEditor.setOnImageTappedListener(onImageTappedListener!!)
        }
    }

    private fun initVideoTappedListener() {
        if (onVideoTappedListener != null) {
            visualEditor.setOnVideoTappedListener(onVideoTappedListener!!)
        }
    }
}