package org.wordpress.aztec.toolbar

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.PopupMenu.OnMenuItemClickListener
import android.widget.Toast
import android.widget.ToggleButton
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.R
import org.wordpress.aztec.TextFormat
import org.wordpress.aztec.source.SourceViewEditText
import java.util.*

class AztecToolbar : FrameLayout, OnMenuItemClickListener {
    private var addLinkDialog: AlertDialog? = null
    private var addPhotoMediaDialog: AlertDialog? = null
    private var addVideoMediaDialog: AlertDialog? = null
    private var editor: AztecText? = null
    private var headingMenu: PopupMenu? = null
    private var mediaMenu: PopupMenu? = null
    private var mediaOptionSelectedListener: OnMediaOptionSelectedListener? = null
    private var sourceEditor: SourceViewEditText? = null

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }

    interface OnMediaOptionSelectedListener {
        fun onCameraPhotoMediaOptionSelected()
        fun onCameraVideoMediaOptionSelected()
        fun onPhotosMediaOptionSelected()
        fun onVideosMediaOptionSelected()
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())

        if (addPhotoMediaDialog != null && addPhotoMediaDialog!!.isShowing) {
            bundle.putBoolean("isPhotoMediaDialogVisible", true)
        }

        if (addVideoMediaDialog != null && addVideoMediaDialog!!.isShowing) {
            bundle.putBoolean("isVideoMediaDialogVisible", true)
        }

        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var superState = state

        if (state is Bundle) {
            superState = state.getParcelable("superState")

            if (state.getBoolean("isPhotoMediaDialogVisible")) {
                showPhotoMediaDialog()
            }

            if (state.getBoolean("isVideoMediaDialogVisible")) {
                showVideoMediaDialog()
            }
        }

        super.onRestoreInstanceState(superState)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (addLinkDialog != null && addLinkDialog!!.isShowing) {
            addLinkDialog!!.dismiss()
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        item?.isChecked = (item?.isChecked == false)

        when (item?.itemId) {
            // Media popup menu options
            R.id.gallery -> {
                Toast.makeText(context, "Launch gallery", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.photo -> {
                showPhotoMediaDialog()
                return true
            }
            R.id.video -> {
                showVideoMediaDialog()
                return true
            }
            // Heading popup menu options
            R.id.paragraph -> {
                editor?.toggleFormatting(TextFormat.FORMAT_PARAGRAPH)
                return true
            }
            R.id.heading_1 -> {
                editor?.toggleFormatting(TextFormat.FORMAT_HEADING_1)
                return true
            }
            R.id.heading_2 -> {
                editor?.toggleFormatting(TextFormat.FORMAT_HEADING_2)
                return true
            }
            R.id.heading_3 -> {
                editor?.toggleFormatting(TextFormat.FORMAT_HEADING_3)
                return true
            }
            R.id.heading_4 -> {
                editor?.toggleFormatting(TextFormat.FORMAT_HEADING_4)
                return true
            }
            R.id.heading_5 -> {
                editor?.toggleFormatting(TextFormat.FORMAT_HEADING_5)
                return true
            }
            R.id.heading_6 -> {
                editor?.toggleFormatting(TextFormat.FORMAT_HEADING_6)
                return true
            }
            else -> return false
        }
    }

    private fun isEditorAttached(): Boolean {
        return editor != null && editor is AztecText
    }

    fun setEditor(editor: AztecText, sourceEditor: SourceViewEditText) {
        this.sourceEditor = sourceEditor
        this.editor = editor
        //highlight toolbar buttons based on what styles are applied to the text beneath cursor
        this.editor!!.setOnSelectionChangedListener(object : AztecText.OnSelectionChangedListener {
            override fun onSelectionChanged(selStart: Int, selEnd: Int) {
                highlightAppliedStyles(selStart, selEnd)
            }
        })
    }

    fun setMediaOptionSelectedListener(listener: OnMediaOptionSelectedListener) {
        mediaOptionSelectedListener = listener
    }

    private fun initView() {
        View.inflate(context, R.layout.format_bar, this)

        for (toolbarAction in ToolbarAction.values()) {
            val button = findViewById(toolbarAction.buttonId)
            button?.setOnClickListener { onToolbarAction(toolbarAction) }

            if (toolbarAction == ToolbarAction.ADD_MEDIA) {
                setMediaMenu(findViewById(toolbarAction.buttonId))
            }

            if (toolbarAction == ToolbarAction.HEADING) {
                setHeaderMenu(findViewById(toolbarAction.buttonId))
            }
        }
    }

    fun highlightActionButtons(toolbarActions: ArrayList<ToolbarAction>) {
        ToolbarAction.values().forEach { action ->
            if (toolbarActions.contains(action)) {
                toggleButton(findViewById(action.buttonId), true)
            } else {
                toggleButton(findViewById(action.buttonId), false)
            }
        }
    }

    private fun getSelectedActions(): ArrayList<ToolbarAction> {
        val actions = ArrayList<ToolbarAction>()

        for (action in ToolbarAction.values()) {
            val view = findViewById(action.buttonId) as ToggleButton
            if (view.isChecked) actions.add(action)
        }

        return actions
    }

    private fun toggleButton(button: View?, checked: Boolean) {
        if (button != null && button is ToggleButton) {
            button.isChecked = checked
        }
    }

    private fun toggleButtonState(button: View?, enabled: Boolean) {
        if (button != null) {
            button.isEnabled = enabled
        }
    }

    private fun highlightAppliedStyles(selStart: Int, selEnd: Int) {
        if (!isEditorAttached()) return

        val newSelStart = if (selStart > 0 && !editor!!.isTextSelected()) selStart - 1 else selStart

        val appliedStyles = editor!!.getAppliedStyles(newSelStart, selEnd)

        highlightActionButtons(ToolbarAction.getToolbarActionsForStyles(appliedStyles))
        selectHeaderMenu(editor!!.getAppliedHeading(newSelStart, selEnd))
    }

    private fun onToolbarAction(action: ToolbarAction) {
        if (!isEditorAttached()) return

        //if nothing is selected just mark the style as active
        if (!editor!!.isTextSelected() && action.actionType == ToolbarActionType.INLINE_STYLE) {
            val actions = getSelectedActions()
            val textFormats = ArrayList<TextFormat>()

            actions.forEach { if (it.isStylingAction()) textFormats.add(it.textFormat!!) }
            return editor!!.setSelectedStyles(textFormats)
        }

        //if text is selected and action is styling - toggle the style
        if (action.isStylingAction()) {
            return editor!!.toggleFormatting(action.textFormat!!)
        }

        //other toolbar action
        when (action) {
            ToolbarAction.ADD_MEDIA -> mediaMenu?.show()
            ToolbarAction.HEADING -> headingMenu?.show()
            ToolbarAction.LINK -> editor!!.showLinkDialog()
            ToolbarAction.HTML -> toggleEditorMode()
            else -> {
                Toast.makeText(context, "Unsupported action", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun toggleEditorMode(){
        if (editor!!.visibility == View.VISIBLE) {
            if (!editor!!.isMediaAdded) {
                sourceEditor!!.displayStyledAndFormattedHtml(editor!!.toHtml(true))
                editor!!.visibility = View.GONE
                sourceEditor!!.visibility = View.VISIBLE

                toggleHtmlMode(true)
            } else {
                toggleButton(findViewById(ToolbarAction.HTML.buttonId), false)
                showMediaUploadDialog()
            }
        } else {
            editor!!.fromHtml(sourceEditor!!.getPureHtml(true))
            editor!!.visibility = View.VISIBLE
            sourceEditor!!.visibility = View.GONE

            toggleHtmlMode(false)
        }
    }

    private fun selectHeaderMenu(textFormat: TextFormat?) {
        when (textFormat) {
            TextFormat.FORMAT_HEADING_1 -> headingMenu?.menu?.getItem(1)?.isChecked = true
            TextFormat.FORMAT_HEADING_2 -> headingMenu?.menu?.getItem(2)?.isChecked = true
            TextFormat.FORMAT_HEADING_3 -> headingMenu?.menu?.getItem(3)?.isChecked = true
            TextFormat.FORMAT_HEADING_4 -> headingMenu?.menu?.getItem(4)?.isChecked = true
            TextFormat.FORMAT_HEADING_5 -> headingMenu?.menu?.getItem(5)?.isChecked = true
            TextFormat.FORMAT_HEADING_6 -> headingMenu?.menu?.getItem(6)?.isChecked = true
            else -> headingMenu?.menu?.getItem(0)?.isChecked = true
        }
    }

    private fun setHeaderMenu(view: View) {
        headingMenu = PopupMenu(context, view)
        headingMenu?.setOnMenuItemClickListener(this)
        headingMenu?.inflate(R.menu.heading)
    }

    private fun setMediaMenu(view: View) {
        mediaMenu = PopupMenu(context, view)
        mediaMenu?.setOnMenuItemClickListener(this)
        mediaMenu?.inflate(R.menu.media)
    }

    private fun toggleHtmlMode(isHtmlMode: Boolean) {
        ToolbarAction.values().forEach { action ->
            if (action == ToolbarAction.HTML) {
                toggleButton(findViewById(action.buttonId), isHtmlMode)
            } else {
                toggleButtonState(findViewById(action.buttonId), !isHtmlMode)
            }
        }
	}

    private fun showMediaUploadDialog() {
        if (!isEditorAttached()) return

        val builder = AlertDialog.Builder(context)
        builder.setMessage(context.getString(R.string.media_upload_dialog_message))
        builder.setPositiveButton(context.getString(R.string.media_upload_dialog_positive), null)
        addVideoMediaDialog = builder.create()
        addVideoMediaDialog!!.show()
    }

    private fun showPhotoMediaDialog() {
        if (!isEditorAttached()) return

        val dialog = LayoutInflater.from(context).inflate(R.layout.dialog_photo_media, null)

        val camera = dialog.findViewById(R.id.media_camera)
        camera.setOnClickListener({
            mediaOptionSelectedListener?.onCameraPhotoMediaOptionSelected()
            addPhotoMediaDialog?.dismiss()
        })

        val photos = dialog.findViewById(R.id.media_photos)
        photos.setOnClickListener({
            mediaOptionSelectedListener?.onPhotosMediaOptionSelected()
            addPhotoMediaDialog?.dismiss()
        })

        val library = dialog.findViewById(R.id.media_library)
        library.setOnClickListener({
            Toast.makeText(context, "Open library", Toast.LENGTH_SHORT).show()
            addPhotoMediaDialog?.dismiss()
        })

        val builder = AlertDialog.Builder(context)
        builder.setView(dialog)
        addPhotoMediaDialog = builder.create()
        addPhotoMediaDialog!!.show()
    }

    private fun showVideoMediaDialog() {
        if (!isEditorAttached()) return

        val dialog = LayoutInflater.from(context).inflate(R.layout.dialog_video_media, null)

        val camera = dialog.findViewById(R.id.media_camera)
        camera.setOnClickListener({
            mediaOptionSelectedListener?.onCameraVideoMediaOptionSelected()
            addVideoMediaDialog?.dismiss()
        })

        val videos = dialog.findViewById(R.id.media_videos)
        videos.setOnClickListener({
            mediaOptionSelectedListener?.onVideosMediaOptionSelected()
            addVideoMediaDialog?.dismiss()
        })

        val library = dialog.findViewById(R.id.media_library)
        library.setOnClickListener({
            Toast.makeText(context, "Open library", Toast.LENGTH_SHORT).show()
            addVideoMediaDialog?.dismiss()
        })

        val builder = AlertDialog.Builder(context)
        builder.setView(dialog)
        addVideoMediaDialog = builder.create()
        addVideoMediaDialog!!.show()
    }
}
