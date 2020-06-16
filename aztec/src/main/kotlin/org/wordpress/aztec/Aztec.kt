package org.wordpress.aztec

import android.app.Activity
import android.view.View
import androidx.annotation.IdRes
import org.wordpress.aztec.plugins.IAztecPlugin
import org.wordpress.aztec.source.SourceViewEditText
import java.util.ArrayList

open class Aztec private constructor(val visualEditor: AztecText) {
    private var imeBackListener: AztecText.OnImeBackListener? = null
    private var onAztecKeyListener: AztecText.OnAztecKeyListener? = null
    private var onTouchListener: View.OnTouchListener? = null
    private var historyListener: IHistoryListener? = null
    private var onLinkTappedListener: AztecText.OnLinkTappedListener? = null
    private var isLinkTapEnabled: Boolean = false
    private var plugins: ArrayList<IAztecPlugin> = visualEditor.plugins
    var sourceEditor: SourceViewEditText? = null

    private constructor(activity: Activity, @IdRes aztecTextId: Int,
                        @IdRes sourceTextId: Int)
            : this(activity.findViewById<AztecText>(aztecTextId),
            activity.findViewById<SourceViewEditText>(sourceTextId))

    private constructor(activity: Activity, @IdRes aztecTextId: Int)
            : this(activity.findViewById<AztecText>(aztecTextId))

    private constructor(visualEditor: AztecText, sourceEditor: SourceViewEditText)
            : this(visualEditor) {
        this.sourceEditor = sourceEditor

        initSourceEditorHistory()
    }

    companion object Factory {
        @JvmStatic
        fun with(activity: Activity, @IdRes aztecTextId: Int, @IdRes sourceTextId: Int): Aztec {
            return Aztec(activity, aztecTextId, sourceTextId)
        }

        @JvmStatic
        fun with(visualEditor: AztecText, sourceEditor: SourceViewEditText): Aztec {
            return Aztec(visualEditor, sourceEditor)
        }

        @JvmStatic
        fun with(visualEditor: AztecText): Aztec {
            return Aztec(visualEditor)
        }
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
        return this
    }

    fun initSourceEditorHistory() {
        sourceEditor?.history = visualEditor.history
    }

    private fun initHistoryListener() {
        if (historyListener != null) {
            visualEditor.history.setHistoryListener(historyListener!!)
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

    private fun initLinkTappedListener() {
        if (onLinkTappedListener != null) {
            visualEditor.setOnLinkTappedListener(onLinkTappedListener!!)
        }
    }

    private fun initLinkTapEnabled() {
        visualEditor.setLinkTapEnabled(isLinkTapEnabled)
    }
}
