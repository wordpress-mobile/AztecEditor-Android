package org.wordpress.aztec.plugins

import android.view.View

interface IMediaToolbarButton : IToolbarButton {

    fun setMediaToolbarButtonClickListener(clickListener: IMediaToolbarClickListener)

    interface IMediaToolbarClickListener {
        fun onClick(view: View)
    }

}