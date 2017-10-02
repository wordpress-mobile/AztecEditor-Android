package org.wordpress.aztec.demo

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.*
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import org.wordpress.android.util.AppLog
import org.wordpress.android.util.ImageUtils
import org.wordpress.android.util.PermissionUtils
import org.wordpress.android.util.ToastUtils
import org.wordpress.aztec.*
import org.wordpress.aztec.glideloader.GlideImageLoader
import org.wordpress.aztec.glideloader.GlideVideoThumbnailLoader
import org.wordpress.aztec.plugins.shortcodes.AudioShortcodePlugin
import org.wordpress.aztec.plugins.shortcodes.CaptionShortcodePlugin
import org.wordpress.aztec.plugins.shortcodes.VideoShortcodePlugin
import org.wordpress.aztec.plugins.shortcodes.handlers.CaptionHandler
import org.wordpress.aztec.plugins.wpcomments.WordPressCommentsPlugin
import org.wordpress.aztec.plugins.wpcomments.toolbar.MoreToolbarButton
import org.wordpress.aztec.plugins.wpcomments.toolbar.PageToolbarButton
import org.wordpress.aztec.source.SourceViewEditText
import org.wordpress.aztec.spans.AztecDynamicImageSpan
import org.wordpress.aztec.toolbar.AztecToolbar
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener
import org.wordpress.aztec.watchers.BlockElementWatcher
import org.xml.sax.Attributes
import java.io.File

class MainActivity : AppCompatActivity(),
        AztecText.OnImeBackListener,
        AztecText.OnImageTappedListener,
        AztecText.OnVideoTappedListener,
        AztecText.OnAudioTappedListener,
        AztecText.OnMediaDeletedListener,
        IAztecToolbarClickListener,
        IHistoryListener,
        OnRequestPermissionsResultCallback,
        PopupMenu.OnMenuItemClickListener,
        View.OnTouchListener {

    companion object {
        private val HEADING =
                "<h1>Heading 1</h1>" +
                        "<h2>Heading 2</h2>" +
                        "<h3>Heading 3</h3>" +
                        "<h4>Heading 4</h4>" +
                        "<h5>Heading 5</h5>" +
                        "<h6>Heading 6</h6>"
        private val BOLD = "<b>Bold</b><br>"
        private val ITALIC = "<i>Italic</i><br>"
        private val UNDERLINE = "<u>Underline</u><br>"
        private val STRIKETHROUGH = "<s class=\"test\">Strikethrough</s><br>" // <s> or <strike> or <del>
        private val ORDERED = "<ol><li>Ordered</li><li></li></ol>"
        private val LINE = "<hr>"
        private val UNORDERED = "<ul><li>Unordered</li><li></li></ul>"
        private val QUOTE = "<blockquote>Quote</blockquote>"
        private val LINK = "<a href=\"https://github.com/wordpress-mobile/WordPress-Aztec-Android\">Link</a><br>"
        private val UNKNOWN = "<iframe class=\"classic\">Menu</iframe><br>"
        private val COMMENT = "<!--Comment--><br>"
        private val COMMENT_MORE = "<!--more--><br>"
        private val COMMENT_PAGE = "<!--nextpage--><br>"
        private val HIDDEN =
                "<span></span>" +
                        "<div class=\"first\">" +
                        "    <div class=\"second\">" +
                        "        <div class=\"third\">" +
                        "            Div<br><span><b>Span</b></span><br>Hidden" +
                        "        </div>" +
                        "        <div class=\"fourth\"></div>" +
                        "        <div class=\"fifth\"></div>" +
                        "    </div>" +
                        "    <span class=\"second last\"></span>" +
                        "</div>" +
                        "<br>"
        private val PREFORMAT =
                "<pre>" +
                        "when (person) {<br>" +
                        "    MOCTEZUMA -> {<br>" +
                        "        print (\"friend\")<br>" +
                        "    }<br>" +
                        "    CORTES -> {<br>" +
                        "        print (\"foe\")<br>" +
                        "    }<br>" +
                        "}" +
                        "</pre>"
        private val CODE = "<code>if (value == 5) printf(value)</code><br>"
        private val IMG = "[caption align=\"alignright\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />Caption[/caption]"
        private val EMOJI = "&#x1F44D;"
        private val NON_LATIN_TEXT = "测试一个"
        private val LONG_TEXT = "<br><br>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. "
        private val VIDEO = "[video src=\"https://examplebloge.files.wordpress.com/2017/06/d7d88643-88e6-d9b5-11e6-92e03def4804.mp4\"]"
        private val AUDIO = "[audio src=\"https://upload.wikimedia.org/wikipedia/commons/9/94/H-Moll.ogg\"]"

        private val EXAMPLE =
                IMG +
                        HEADING +
                        BOLD +
                        ITALIC +
                        UNDERLINE +
                        STRIKETHROUGH +
                        ORDERED +
                        LINE +
                        UNORDERED +
                        QUOTE +
                        PREFORMAT +
                        LINK +
                        HIDDEN +
                        COMMENT +
                        COMMENT_MORE +
                        COMMENT_PAGE +
                        CODE +
                        UNKNOWN +
                        EMOJI +
                        NON_LATIN_TEXT +
                        LONG_TEXT +
                        VIDEO +
                        AUDIO

        private val isRunningTest: Boolean by lazy {
            try {
                Class.forName("android.support.test.espresso.Espresso")
                true
            } catch (e: ClassNotFoundException) {
                false
            }
        }
    }

    private val MEDIA_CAMERA_PHOTO_PERMISSION_REQUEST_CODE: Int = 1001
    private val MEDIA_CAMERA_VIDEO_PERMISSION_REQUEST_CODE: Int = 1002
    private val MEDIA_PHOTOS_PERMISSION_REQUEST_CODE: Int = 1003
    private val MEDIA_VIDEOS_PERMISSION_REQUEST_CODE: Int = 1004
    private val REQUEST_MEDIA_CAMERA_PHOTO: Int = 2001
    private val REQUEST_MEDIA_CAMERA_VIDEO: Int = 2002
    private val REQUEST_MEDIA_PHOTO: Int = 2003
    private val REQUEST_MEDIA_VIDEO: Int = 2004

    private lateinit var aztec: Aztec
    private lateinit var mediaFile: String
    private lateinit var mediaPath: String

    private lateinit var invalidateOptionsHandler: Handler
    private lateinit var invalidateOptionsRunnable: Runnable

    private var addPhotoMediaDialog: AlertDialog? = null
    private var addVideoMediaDialog: AlertDialog? = null
    private var mediaUploadDialog: AlertDialog? = null
    private var mediaMenu: PopupMenu? = null

    private var mIsKeyboardOpen = false
    private var mHideActionBarOnSoftKeyboardUp = false

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            var bitmap: Bitmap? = null

            when (requestCode) {
                REQUEST_MEDIA_CAMERA_PHOTO -> {
                    // By default, BitmapFactory.decodeFile sets the bitmap's density to the device default so, we need
                    //  to correctly set the input density to 160 ourselves.
                    val options = BitmapFactory.Options()
                    options.inDensity = DisplayMetrics.DENSITY_DEFAULT
                    bitmap = BitmapFactory.decodeFile(mediaPath, options)
                    insertImageAndSimulateUpload(bitmap, mediaPath)
                }
                REQUEST_MEDIA_PHOTO -> {
                    mediaPath = data?.data.toString()
                    val stream = contentResolver.openInputStream(Uri.parse(mediaPath))
                    // By default, BitmapFactory.decodeFile sets the bitmap's density to the device default so, we need
                    //  to correctly set the input density to 160 ourselves.
                    val options = BitmapFactory.Options()
                    options.inDensity = DisplayMetrics.DENSITY_DEFAULT
                    bitmap = BitmapFactory.decodeStream(stream, null, options)

                    insertImageAndSimulateUpload(bitmap, mediaPath)
                }
                REQUEST_MEDIA_CAMERA_VIDEO -> {
                    mediaPath = data?.data.toString()
                }
                REQUEST_MEDIA_VIDEO -> {
                    mediaPath = data?.data.toString()

                    aztec.visualEditor.videoThumbnailGetter?.loadVideoThumbnail(mediaPath, object : Html.VideoThumbnailGetter.Callbacks {
                        override fun onThumbnailFailed() {
                        }

                        override fun onThumbnailLoaded(drawable: Drawable?) {
                            val conf = Bitmap.Config.ARGB_8888 // see other conf types
                            bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight, conf)
                            val canvas = Canvas(bitmap)
                            drawable.setBounds(0, 0, canvas.width, canvas.height)
                            drawable.draw(canvas)

                            insertVideoAndSimulateUpload(bitmap, mediaPath)
                        }

                        override fun onThumbnailLoading(drawable: Drawable?) {
                        }

                    }, this.resources.displayMetrics.widthPixels)
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun insertImageAndSimulateUpload(bitmap: Bitmap?, mediaPath: String) {
        val bitmapResized = ImageUtils.getScaledBitmapAtLongestSide(bitmap, aztec.visualEditor.maxImagesWidth)
        val (id, attrs) = generateAttributesForMedia(mediaPath, isVideo = false)
        //aztec.visualEditor.insertImage(BitmapDrawable(resources, bitmap), attrs)
        aztec.visualEditor.insertImage(object : AztecDynamicImageSpan.IImageProvider {
            override fun requestImage(span: AztecDynamicImageSpan) {
                span.drawable = BitmapDrawable(resources, bitmapResized)
            }
        }, attrs)
        insertMediaAndSimulateUpload(id, attrs)
    }

    fun insertVideoAndSimulateUpload(bitmap: Bitmap?, mediaPath: String) {
        val bitmapResized = ImageUtils.getScaledBitmapAtLongestSide(bitmap, aztec.visualEditor.maxImagesWidth)
        val (id, attrs) = generateAttributesForMedia(mediaPath, isVideo = true)
        aztec.visualEditor.insertVideo(object : AztecDynamicImageSpan.IImageProvider {
            override fun requestImage(span: AztecDynamicImageSpan) {
                span.drawable = BitmapDrawable(resources, bitmapResized)
            }
        }, attrs)
        insertMediaAndSimulateUpload(id, attrs)
    }

    private fun generateAttributesForMedia(mediaPath: String, isVideo: Boolean): Pair<String, AztecAttributes> {
        val id = (Math.random() * Int.MAX_VALUE).toString()

        val attrs = AztecAttributes()
        attrs.setValue("src", mediaPath) // Temporary source value.  Replace with URL after uploaded.
        attrs.setValue("id", id)
        attrs.setValue("uploading", "true")

        if (isVideo) {
            attrs.setValue("video", "true")
        }

        return Pair(id, attrs)
    }

    private fun insertMediaAndSimulateUpload(id: String, attrs: AztecAttributes) {
        val predicate = object : AztecText.AttributePredicate {
            override fun matches(attrs: Attributes): Boolean {
                return attrs.getValue("id") == id
            }
        }

        aztec.visualEditor.setOverlay(predicate, 0, ColorDrawable(0x80000000.toInt()), Gravity.FILL)
        aztec.visualEditor.updateElementAttributes(predicate, attrs)

        val progressDrawable = ContextCompat.getDrawable(this, android.R.drawable.progress_horizontal)
        // set the height of the progress bar to 2 (it's in dp since the drawable will be adjusted by the span)
        progressDrawable.setBounds(0, 0, 0, 4)

        aztec.visualEditor.setOverlay(predicate, 1, progressDrawable, Gravity.FILL_HORIZONTAL or Gravity.TOP)
        aztec.visualEditor.updateElementAttributes(predicate, attrs)

        var progress = 0

        // simulate an upload delay
        val runnable: Runnable = Runnable {
            aztec.visualEditor.setOverlayLevel(predicate, 1, progress)
            aztec.visualEditor.updateElementAttributes(predicate, attrs)
            aztec.visualEditor.resetAttributedMediaSpan(predicate)
            progress += 2000

            if (progress >= 10000) {
                attrs.removeAttribute(attrs.getIndex("uploading"))
                aztec.visualEditor.clearOverlays(predicate)

                if (attrs.hasAttribute("video")) {
                    attrs.removeAttribute(attrs.getIndex("video"))
                    aztec.visualEditor.setOverlay(predicate, 0, ContextCompat.getDrawable(this, android.R.drawable.ic_media_play), Gravity.CENTER)
                }

                aztec.visualEditor.updateElementAttributes(predicate, attrs)
            }
        }

        Handler().post(runnable)
        Handler().postDelayed(runnable, 2000)
        Handler().postDelayed(runnable, 4000)
        Handler().postDelayed(runnable, 6000)
        Handler().postDelayed(runnable, 8000)

        aztec.visualEditor.refreshText()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup hiding the action bar when the soft keyboard is displayed for narrow viewports
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                && !resources.getBoolean(R.bool.is_large_tablet_landscape)) {
            mHideActionBarOnSoftKeyboardUp = true
        }

        val visualEditor = findViewById<AztecText>(R.id.aztec)
        val sourceEditor = findViewById<SourceViewEditText>(R.id.source)
        val toolbar = findViewById<AztecToolbar>(R.id.formatting_toolbar)


        aztec = Aztec.with(visualEditor, sourceEditor, toolbar, this)
                .setImageGetter(GlideImageLoader(this))
                .setVideoThumbnailGetter(GlideVideoThumbnailLoader(this))
                .setOnImeBackListener(this)
                .setOnTouchListener(this)
                .setHistoryListener(this)
                .setOnImageTappedListener(this)
                .setOnVideoTappedListener(this)
                .setOnAudioTappedListener(this)
                .setOnMediaDeletedListener(this)
                .addPlugin(WordPressCommentsPlugin(visualEditor))
                .addPlugin(MoreToolbarButton(visualEditor))
                .addPlugin(PageToolbarButton(visualEditor))
                .addPlugin(CaptionShortcodePlugin())
                .addPlugin(VideoShortcodePlugin())
                .addPlugin(AudioShortcodePlugin())

        BlockElementWatcher(visualEditor)
                .add(CaptionHandler())
                .install(visualEditor)

        // initialize the text & HTML
        if (!isRunningTest) {
            aztec.sourceEditor?.displayStyledAndFormattedHtml(EXAMPLE)
        }

        if (savedInstanceState == null) {
            aztec.visualEditor.fromHtml(aztec.sourceEditor?.getPureHtml()!!)
            aztec.initSourceEditorHistory()
        }

        invalidateOptionsHandler = Handler()
        invalidateOptionsRunnable = Runnable { invalidateOptionsMenu() }
    }

    override fun onPause() {
        super.onPause()
        mIsKeyboardOpen = false
    }

    override fun onResume() {
        super.onResume()

        showActionBarIfNeeded()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Toggle action bar auto-hiding for the new orientation
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
                && !resources.getBoolean(R.bool.is_large_tablet_landscape)) {
            mHideActionBarOnSoftKeyboardUp = true
            hideActionBarIfNeeded()
        } else {
            mHideActionBarOnSoftKeyboardUp = false
            showActionBarIfNeeded()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        aztec.initSourceEditorHistory()

        savedInstanceState?.let {
            if (savedInstanceState.getBoolean("isPhotoMediaDialogVisible")) {
                showPhotoMediaDialog()
            }

            if (savedInstanceState.getBoolean("isVideoMediaDialogVisible")) {
                showVideoMediaDialog()
            }

            if (savedInstanceState.getBoolean("isMediaUploadDialogVisible")) {
                showMediaUploadDialog()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        if (addPhotoMediaDialog != null && addPhotoMediaDialog!!.isShowing) {
            outState?.putBoolean("isPhotoMediaDialogVisible", true)
        }

        if (addVideoMediaDialog != null && addVideoMediaDialog!!.isShowing) {
            outState?.putBoolean("isVideoMediaDialogVisible", true)
        }

        if (mediaUploadDialog != null && mediaUploadDialog!!.isShowing) {
            outState?.putBoolean("isMediaUploadDialogVisible", true)
        }
    }

    /**
     * Returns true if a hardware keyboard is detected, otherwise false.
     */
    private fun isHardwareKeyboardPresent(): Boolean {
        val config = resources.configuration
        var returnValue = false
        if (config.keyboard != Configuration.KEYBOARD_NOKEYS) {
            returnValue = true
        }
        return returnValue
    }

    private fun hideActionBarIfNeeded() {

        val actionBar = supportActionBar
        if (actionBar != null
                && !isHardwareKeyboardPresent()
                && mHideActionBarOnSoftKeyboardUp
                && mIsKeyboardOpen
                && actionBar.isShowing) {
            actionBar.hide()
        }
    }

    /**
     * Show the action bar if needed.
     */
    private fun showActionBarIfNeeded() {

        val actionBar = supportActionBar
        if (actionBar != null && !actionBar.isShowing) {
            actionBar.show()
        }
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            // If the WebView or EditText has received a touch event, the keyboard will be displayed and the action bar
            // should hide
            mIsKeyboardOpen = true
            hideActionBarIfNeeded()
        }
        return false
    }

    override fun onBackPressed() {
        mIsKeyboardOpen = false
        showActionBarIfNeeded()

        return super.onBackPressed()
    }

    /**
     * Intercept back button press while soft keyboard is visible.
     */
    override fun onImeBack() {
        mIsKeyboardOpen = false
        showActionBarIfNeeded()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.undo ->
                if (aztec.visualEditor.visibility == View.VISIBLE) {
                    aztec.visualEditor.undo()
                } else {
                    aztec.sourceEditor?.undo()
                }
            R.id.redo ->
                if (aztec.visualEditor.visibility == View.VISIBLE) {
                    aztec.visualEditor.redo()
                } else {
                    aztec.sourceEditor?.redo()
                }
            else -> {
            }
        }

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.redo)?.isEnabled = aztec.visualEditor.history.redoValid()
        menu?.findItem(R.id.undo)?.isEnabled = aztec.visualEditor.history.undoValid()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onRedoEnabled() {
        invalidateOptionsHandler.removeCallbacks(invalidateOptionsRunnable)
        invalidateOptionsHandler.postDelayed(invalidateOptionsRunnable, resources.getInteger(android.R.integer.config_mediumAnimTime).toLong())
    }

    override fun onUndoEnabled() {
        invalidateOptionsHandler.removeCallbacks(invalidateOptionsRunnable)
        invalidateOptionsHandler.postDelayed(invalidateOptionsRunnable, resources.getInteger(android.R.integer.config_mediumAnimTime).toLong())
    }

    fun onCameraPhotoMediaOptionSelected() {
        if (PermissionUtils.checkAndRequestCameraAndStoragePermissions(this, MEDIA_CAMERA_PHOTO_PERMISSION_REQUEST_CODE)) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            mediaFile = "wp-" + System.currentTimeMillis() + ".jpg"
            mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() +
                    File.separator + "Camera" + File.separator + mediaFile
            intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this,
                    BuildConfig.APPLICATION_ID + ".provider", File(mediaPath)))

            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, REQUEST_MEDIA_CAMERA_PHOTO)
            }
        }
    }

    fun onCameraVideoMediaOptionSelected() {
        if (PermissionUtils.checkAndRequestCameraAndStoragePermissions(this, MEDIA_CAMERA_PHOTO_PERMISSION_REQUEST_CODE)) {
            val intent = Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA)

            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, REQUEST_MEDIA_CAMERA_VIDEO)
            }
        }
    }

    fun onGalleryMediaOptionSelected() {
        Toast.makeText(this, "Launch gallery", Toast.LENGTH_SHORT).show()
    }

    fun onPhotoLibraryMediaOptionSelected() {
        Toast.makeText(this, "Open library", Toast.LENGTH_SHORT).show()
    }

    fun onPhotosMediaOptionSelected() {
        if (PermissionUtils.checkAndRequestStoragePermission(this, MEDIA_PHOTOS_PERMISSION_REQUEST_CODE)) {
            val intent: Intent

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            } else {
                intent = Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT), getString(R.string.title_select_photo))
            }

            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"

            try {
                startActivityForResult(intent, REQUEST_MEDIA_PHOTO)
            } catch (exception: ActivityNotFoundException) {
                AppLog.e(AppLog.T.EDITOR, exception.message)
                ToastUtils.showToast(this, getString(R.string.error_chooser_photo), ToastUtils.Duration.LONG)
            }
        }
    }

    fun onVideoLibraryMediaOptionSelected() {
        Toast.makeText(this, "Open library", Toast.LENGTH_SHORT).show()
    }

    fun onVideosMediaOptionSelected() {
        if (PermissionUtils.checkAndRequestStoragePermission(this, MEDIA_PHOTOS_PERMISSION_REQUEST_CODE)) {
            val intent: Intent

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            } else {
                intent = Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT), getString(R.string.title_select_video))
            }

            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "video/*"

            try {
                startActivityForResult(intent, REQUEST_MEDIA_VIDEO)
            } catch (exception: ActivityNotFoundException) {
                AppLog.e(AppLog.T.EDITOR, exception.message)
                ToastUtils.showToast(this, getString(R.string.error_chooser_video), ToastUtils.Duration.LONG)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            MEDIA_CAMERA_PHOTO_PERMISSION_REQUEST_CODE,
            MEDIA_CAMERA_VIDEO_PERMISSION_REQUEST_CODE -> {
                var isPermissionDenied = false

                for (i in grantResults.indices) {
                    when (permissions[i]) {
                        Manifest.permission.CAMERA -> {
                            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                                isPermissionDenied = true
                            }
                        }
                        Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                                isPermissionDenied = true
                            }
                        }
                    }
                }

                if (isPermissionDenied) {
                    ToastUtils.showToast(this, getString(R.string.permission_required_media_camera))
                } else {
                    when (requestCode) {
                        MEDIA_CAMERA_PHOTO_PERMISSION_REQUEST_CODE -> {
                            onCameraPhotoMediaOptionSelected()
                        }
                        MEDIA_CAMERA_VIDEO_PERMISSION_REQUEST_CODE -> {
                            onCameraVideoMediaOptionSelected()
                        }
                    }
                }
            }
            MEDIA_PHOTOS_PERMISSION_REQUEST_CODE,
            MEDIA_VIDEOS_PERMISSION_REQUEST_CODE -> {
                var isPermissionDenied = false

                for (i in grantResults.indices) {
                    when (permissions[i]) {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                                isPermissionDenied = true
                            }
                        }
                    }
                }

                when (requestCode) {
                    MEDIA_PHOTOS_PERMISSION_REQUEST_CODE -> {
                        if (isPermissionDenied) {
                            ToastUtils.showToast(this, getString(R.string.permission_required_media_photos))
                        } else {
                            onPhotosMediaOptionSelected()
                        }
                    }
                    MEDIA_VIDEOS_PERMISSION_REQUEST_CODE -> {
                        if (isPermissionDenied) {
                            ToastUtils.showToast(this, getString(R.string.permission_required_media_videos))
                        } else {
                            onVideosMediaOptionSelected()
                        }
                    }
                }
            }
            else -> {
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onToolbarCollapseButtonClicked() {
    }

    override fun onToolbarExpandButtonClicked() {
    }

    override fun onToolbarFormatButtonClicked(format: ITextFormat, isKeyboardShortcut: Boolean) {
    }

    override fun onToolbarHeadingButtonClicked() {
    }

    override fun onToolbarHtmlButtonClicked() {
        val uploadingPredicate = object : AztecText.AttributePredicate {
            override fun matches(attrs: Attributes): Boolean {
                return attrs.getIndex("uploading") > -1
            }
        }

        val mediaPending = aztec.visualEditor.getAllElementAttributes(uploadingPredicate).isNotEmpty()

        if (mediaPending) {
            ToastUtils.showToast(this, R.string.media_upload_dialog_message)
        } else {
            aztec.toolbar?.toggleEditorMode()
        }
    }

    override fun onToolbarListButtonClicked() {
    }

    override fun onToolbarMediaButtonClicked() {
        mediaMenu = PopupMenu(this, aztec.toolbar)
        mediaMenu?.setOnMenuItemClickListener(this)
        mediaMenu?.inflate(R.menu.media)
        mediaMenu?.show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        item?.isChecked = (item?.isChecked == false)

        when (item?.itemId) {
            R.id.gallery -> {
                onGalleryMediaOptionSelected()
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
            else -> return false
        }
    }

    private fun showMediaUploadDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(org.wordpress.aztec.R.string.media_upload_dialog_message))
        builder.setPositiveButton(getString(org.wordpress.aztec.R.string.media_upload_dialog_positive), null)
        mediaUploadDialog = builder.create()
        mediaUploadDialog!!.show()
    }

    private fun showPhotoMediaDialog() {
        val dialog = layoutInflater.inflate(R.layout.dialog_photo_media, null)

        val camera = dialog.findViewById<TextView>(org.wordpress.aztec.R.id.media_camera)
        camera.setOnClickListener({
            onCameraPhotoMediaOptionSelected()
            addPhotoMediaDialog?.dismiss()
        })

        val photos = dialog.findViewById<TextView>(org.wordpress.aztec.R.id.media_photos)
        photos.setOnClickListener({
            onPhotosMediaOptionSelected()
            addPhotoMediaDialog?.dismiss()
        })

        val library = dialog.findViewById<TextView>(org.wordpress.aztec.R.id.media_library)
        library.setOnClickListener({
            onPhotoLibraryMediaOptionSelected()
            addPhotoMediaDialog?.dismiss()
        })

        val builder = AlertDialog.Builder(this)
        builder.setView(dialog)
        addPhotoMediaDialog = builder.create()
        addPhotoMediaDialog!!.show()
    }

    private fun showVideoMediaDialog() {
        val dialog = layoutInflater.inflate(org.wordpress.aztec.R.layout.dialog_video_media, null)

        val camera = dialog.findViewById<TextView>(org.wordpress.aztec.R.id.media_camera)
        camera.setOnClickListener({
            onCameraVideoMediaOptionSelected()
            addVideoMediaDialog?.dismiss()
        })

        val videos = dialog.findViewById<TextView>(org.wordpress.aztec.R.id.media_videos)
        videos.setOnClickListener({
            onVideosMediaOptionSelected()
            addVideoMediaDialog?.dismiss()
        })

        val library = dialog.findViewById<TextView>(org.wordpress.aztec.R.id.media_library)
        library.setOnClickListener({
            onVideoLibraryMediaOptionSelected()
            addVideoMediaDialog?.dismiss()
        })

        val builder = AlertDialog.Builder(this)
        builder.setView(dialog)
        addVideoMediaDialog = builder.create()
        addVideoMediaDialog!!.show()
    }

    override fun onImageTapped(attrs: AztecAttributes, naturalWidth: Int, naturalHeight: Int) {
        ToastUtils.showToast(this, "Image tapped!")
    }

    override fun onVideoTapped(attrs: AztecAttributes) {
        val url = attrs.getValue("src")
        url?.let {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.setDataAndType(Uri.parse(url), "video/*")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(browserIntent)
                } catch (e: ActivityNotFoundException) {
                    ToastUtils.showToast(this, "Video tapped!")
                }
            }
        }
    }

    override fun onAudioTapped(attrs: AztecAttributes) {
        val url = attrs.getValue("src")
        url?.let {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.setDataAndType(Uri.parse(url), "audio/*")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(browserIntent)
                } catch (e: ActivityNotFoundException) {
                    ToastUtils.showToast(this, "Audio tapped!")
                }
            }
        }
    }

    override fun onMediaDeleted(attrs: AztecAttributes) {
        val url = attrs.getValue("src")
        ToastUtils.showToast(this, "Media Deleted! " + url)
    }
}
