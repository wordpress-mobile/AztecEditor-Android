package org.wordpress.aztec.demo

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.MediaController
import android.widget.VideoView
import org.wordpress.aztec.demo.PlaceholderManager.PlaceholderDrawer.PlaceholderHeight

class VideoDrawer(private val context: Context, override val placeholderHeight: PlaceholderHeight = PlaceholderHeight.Ratio(0.5f)) : PlaceholderManager.PlaceholderDrawer {
    private val media = mutableMapOf<String, MediaObject>()
    fun addMedia(id: String, src: String) {
        media[id] = MediaObject(id, src, View.generateViewId())
    }

    override fun onCreateView(context: Context, id: String): View {
        return VideoView(context).apply {
            this.id = media[id]!!.layoutId
        }
    }

    override fun onViewCreated(view: View, id: String) {
        val mediaController = MediaController(context)
        val videoView = view as VideoView
        mediaController.setAnchorView(videoView)
        val uri: Uri = Uri.parse(media[id]!!.src)

        videoView.setMediaController(mediaController)
        videoView.setVideoURI(uri)
        videoView.requestFocus()
        videoView.start()
    }

    override fun onPlaceholderDeleted(id: String) {
        media.remove(id)
    }

    data class MediaObject(val id: String, val src: String, val layoutId: Int)
}

