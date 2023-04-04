package org.wordpress.aztec

import android.app.Activity
import android.view.View
import androidx.annotation.IdRes
import org.wordpress.aztec.plugins.IAztecPlugin
import org.wordpress.aztec.plugins.IToolbarButton
import org.wordpress.aztec.source.SourceViewEditText
import org.wordpress.aztec.toolbar.AztecToolbar
import org.wordpress.aztec.toolbar.IAztecToolbar
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener
import java.util.ArrayList

open class Aztec private constructor(
        val visualEditor: AztecText,
        val sourceEditor: SourceViewEditText? = null,
        val toolbar: IAztecToolbar,
        toolbarClickListener: IAztecToolbarClickListener) {
    private var imageGetter: Html.ImageGetter? = null
    private var videoThumbnailGetter: Html.VideoThumbnailGetter? = null
    private var mediaCallback: Html.MediaCallback? = null
    private var imeBackListener: AztecText.OnImeBackListener? = null
    private var onAztecKeyListener: AztecText.OnAztecKeyListener? = null
    private var onTouchListener: View.OnTouchListener? = null
    private var historyListener: IHistoryListener? = null
    private var onImageTappedListener: AztecText.OnImageTappedListener? = null
    private var onVideoTappedListener: AztecText.OnVideoTappedListener? = null
    private var onAudioTappedListener: AztecText.OnAudioTappedListener? = null
    private val onMediaDeletedListeners: MutableList<AztecText.OnMediaDeletedListener> = mutableListOf()
    private val onMediaDeletedListener = object : AztecText.OnMediaDeletedListener {
        override fun onMediaDeleted(attrs: AztecAttributes) {
            onMediaDeletedListeners.forEach { it.onMediaDeleted(attrs) }
        }

        override fun beforeMediaDeleted(attrs: AztecAttributes) {
            onMediaDeletedListeners.forEach { it.beforeMediaDeleted(attrs) }
        }
    }
    private var beforeBackSpaceListeners: MutableList<AztecText.BeforeBackSpaceListener> = mutableListOf()
    private val beforeBackSpaceListener = object : AztecText.BeforeBackSpaceListener {
        override fun shouldOverrideBackSpace(position: Int): Boolean {
            return beforeBackSpaceListeners.any { it.shouldOverrideBackSpace(position) }
        }
    }
    private var onVideoInfoRequestedListener: AztecText.OnVideoInfoRequestedListener? = null
    private var onLinkTappedListener: AztecText.OnLinkTappedListener? = null
    private var isLinkTapEnabled: Boolean = false
    private var plugins: ArrayList<IAztecPlugin> = visualEditor.plugins

    init {
        initToolbar(toolbarClickListener)
        sourceEditor?.let {
            initSourceEditorHistory()
        }
    }

    private constructor(activity: Activity, @IdRes aztecTextId: Int,
                        @IdRes sourceTextId: Int, @IdRes toolbarId: Int,
                        toolbarClickListener: IAztecToolbarClickListener) : this(activity.findViewById<AztecText>(aztecTextId),
            activity.findViewById<SourceViewEditText>(sourceTextId), activity.findViewById<AztecToolbar>(toolbarId), toolbarClickListener)

    private constructor(activity: Activity, @IdRes aztecTextId: Int,
                        @IdRes toolbarId: Int,
                        toolbarClickListener: IAztecToolbarClickListener) : this(activity.findViewById<AztecText>(aztecTextId), null,
            activity.findViewById<AztecToolbar>(toolbarId), toolbarClickListener)

    companion object Factory {
        @JvmStatic
        fun with(activity: Activity, @IdRes aztecTextId: Int, @IdRes sourceTextId: Int,
                 @IdRes toolbarId: Int, toolbarClickListener: IAztecToolbarClickListener): Aztec {
            return Aztec(activity, aztecTextId, sourceTextId, toolbarId, toolbarClickListener)
        }

        @JvmStatic
        fun with(visualEditor: AztecText, sourceEditor: SourceViewEditText,
                 toolbar: IAztecToolbar, toolbarClickListener: IAztecToolbarClickListener): Aztec {
            return Aztec(visualEditor, sourceEditor, toolbar, toolbarClickListener)
        }

        @JvmStatic
        fun with(visualEditor: AztecText, toolbar: AztecToolbar, toolbarClickListener: IAztecToolbarClickListener): Aztec {
            return Aztec(visualEditor, null, toolbar, toolbarClickListener)
        }
    }

    fun setImageGetter(imageGetter: Html.ImageGetter): Aztec {
        this.imageGetter = imageGetter
        initImageGetter()
        return this
    }

    fun setVideoThumbnailGetter(videoThumbnailGetter: Html.VideoThumbnailGetter): Aztec {
        this.videoThumbnailGetter = videoThumbnailGetter
        initVideoGetter()
        return this
    }

    fun setMediaCallback(mediaCallback: Html.MediaCallback): Aztec {
        this.mediaCallback = mediaCallback
        initMediaCallback()
        return this
    }

    fun setOnImeBackListener(imeBackListener: AztecText.OnImeBackListener): Aztec {
        this.imeBackListener = imeBackListener
        initImeBackListener()
        return this
    }

    fun setAztecKeyListener(aztecKeyListener: AztecText.OnAztecKeyListener): Aztec {
        this.onAztecKeyListener = aztecKeyListener
        initAztecKeyListener()
        return this
    }

    fun setOnTouchListener(onTouchListener: View.OnTouchListener): Aztec {
        this.onTouchListener = onTouchListener
        initTouchListener()
        return this
    }

    fun setOnImageTappedListener(onImageTappedListener: AztecText.OnImageTappedListener): Aztec {
        this.onImageTappedListener = onImageTappedListener
        initImageTappedListener()
        return this
    }

    fun setOnVideoTappedListener(onVideoTappedListener: AztecText.OnVideoTappedListener): Aztec {
        this.onVideoTappedListener = onVideoTappedListener
        initVideoTappedListener()
        return this
    }

    fun setOnAudioTappedListener(onAudioTappedListener: AztecText.OnAudioTappedListener): Aztec {
        this.onAudioTappedListener = onAudioTappedListener
        initAudioTappedListener()
        return this
    }

    @Deprecated("Use the method to add a media deleted listener instead", ReplaceWith("addOnMediaDeletedListener"))
    fun setOnMediaDeletedListener(onMediaDeletedListener: AztecText.OnMediaDeletedListener): Aztec {
        this.onMediaDeletedListeners.clear()
        this.onMediaDeletedListeners.add(onMediaDeletedListener)
        initMediaDeletedListener()
        return this
    }

    fun addOnMediaDeletedListener(onMediaDeletedListener: AztecText.OnMediaDeletedListener): Aztec {
        this.onMediaDeletedListeners.add(onMediaDeletedListener)
        initMediaDeletedListener()
        return this
    }

    fun addBeforeBackSpaceListener(beforeBackSpaceListener: AztecText.BeforeBackSpaceListener): Aztec {
        this.beforeBackSpaceListeners.add(beforeBackSpaceListener)
        initBeforeBackSpaceListener()
        return this
    }

    fun setOnVideoInfoRequestedListener(onVideoInfoRequestedListener: AztecText.OnVideoInfoRequestedListener): Aztec {
        this.onVideoInfoRequestedListener = onVideoInfoRequestedListener
        initVideoInfoRequestedListener()
        return this
    }

    fun setHistoryListener(historyListener: IHistoryListener): Aztec {
        this.historyListener = historyListener
        initHistoryListener()
        return this
    }

    fun setOnLinkTappedListener(onLinkTappedListener: AztecText.OnLinkTappedListener): Aztec {
        this.onLinkTappedListener = onLinkTappedListener
        initLinkTappedListener()
        return this
    }

    fun setLinkTapEnabled(isLinkTapEnabled: Boolean): Aztec {
        this.isLinkTapEnabled = isLinkTapEnabled
        initLinkTapEnabled()
        return this
    }

    fun addPlugin(plugin: IAztecPlugin): Aztec {
        plugins.add(plugin)

        if (plugin is IToolbarButton) {
            toolbar.addButton(plugin)
        }

        return this
    }

    fun initSourceEditorHistory() {
        sourceEditor?.history = visualEditor.history
    }

    private fun initToolbar(toolbarClickListener: IAztecToolbarClickListener) {
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

    private fun initMediaCallback() {
        if (mediaCallback != null) {
            visualEditor.mediaCallback = mediaCallback
        }
    }

    private fun initImeBackListener() {
        if (imeBackListener != null) {
            visualEditor.setOnImeBackListener(imeBackListener!!)
        }
    }

    private fun initAztecKeyListener() {
        if (onAztecKeyListener != null) {
            visualEditor.setAztecKeyListener(onAztecKeyListener!!)
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

    private fun initAudioTappedListener() {
        if (onAudioTappedListener != null) {
            visualEditor.setOnAudioTappedListener(onAudioTappedListener!!)
        }
    }

    private fun initMediaDeletedListener() {
        visualEditor.setOnMediaDeletedListener(onMediaDeletedListener)
    }

    private fun initBeforeBackSpaceListener() {
        visualEditor.setBeforeBackSpaceListener(beforeBackSpaceListener)
    }

    private fun initVideoInfoRequestedListener() {
        if (onVideoInfoRequestedListener != null) {
            visualEditor.setOnVideoInfoRequestedListener(onVideoInfoRequestedListener!!)
        }
    }

    private fun initLinkTappedListener() {
        if (onLinkTappedListener != null) {
            visualEditor.setOnLinkTappedListener(onLinkTappedListener!!)
        }
    }

    private fun initLinkTapEnabled() {
        visualEditor.setLinkTapEnabled(isLinkTapEnabled)
    }
}
