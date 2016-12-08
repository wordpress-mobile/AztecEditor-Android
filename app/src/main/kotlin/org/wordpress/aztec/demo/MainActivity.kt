package org.wordpress.aztec.demo

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import org.wordpress.android.util.AppLog
import org.wordpress.android.util.PermissionUtils
import org.wordpress.android.util.ToastUtils
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.source.SourceViewEditText
import org.wordpress.aztec.toolbar.AztecToolbar
import org.wordpress.aztec.toolbar.AztecToolbar.OnMediaOptionSelectedListener
import java.io.File

class MainActivity : AppCompatActivity(), OnMediaOptionSelectedListener, OnRequestPermissionsResultCallback {
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
        private val ORDERED = "<ol><li>Ordered</li></ol>"
        private val UNORDERED = "<ul><li>Unordered</li></ul>"
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
        private val EXAMPLE = HEADING + BOLD + ITALIC + UNDERLINE + STRIKETHROUGH + ORDERED + UNORDERED + QUOTE + LINK + HIDDEN + COMMENT + COMMENT_MORE + COMMENT_PAGE + UNKNOWN
    }

    private val MEDIA_CAMERA_PHOTO_PERMISSION_REQUEST_CODE: Int = 1001
    private val MEDIA_CAMERA_VIDEO_PERMISSION_REQUEST_CODE: Int = 1002
    private val MEDIA_PHOTOS_PERMISSION_REQUEST_CODE: Int = 1003
    private val MEDIA_VIDEOS_PERMISSION_REQUEST_CODE: Int = 1004
    private val REQUEST_MEDIA_CAMERA_PHOTO: Int = 2001
    private val REQUEST_MEDIA_CAMERA_VIDEO: Int = 2002
    private val REQUEST_MEDIA_PHOTO: Int = 2003
    private val REQUEST_MEDIA_VIDEO: Int = 2004

    private lateinit var aztec: AztecText
    private lateinit var mediaFile: String
    private lateinit var mediaPath: String
    private lateinit var source: SourceViewEditText
    private lateinit var formattingToolbar: AztecToolbar

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            var bitmap: Bitmap? = null

            when (requestCode) {
                REQUEST_MEDIA_CAMERA_PHOTO -> {
                    bitmap = BitmapFactory.decodeFile(mediaPath)
                }
                REQUEST_MEDIA_CAMERA_VIDEO -> {
                }
                REQUEST_MEDIA_PHOTO -> {
                    mediaPath = data?.data.toString()
                    val stream = contentResolver.openInputStream(Uri.parse(mediaPath))
                    bitmap = BitmapFactory.decodeStream(stream)
                }
                REQUEST_MEDIA_VIDEO -> {
                }
            }

            val source = "<img src=\"$mediaPath\">"  // Temporary source value.  Replace with URL after uploaded.
            aztec.insertMedia(this, BitmapDrawable(resources, bitmap), source)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        aztec = findViewById(R.id.aztec) as AztecText
        source = findViewById(R.id.source) as SourceViewEditText

        formattingToolbar = findViewById(R.id.formatting_toolbar) as AztecToolbar
        formattingToolbar.setEditor(aztec, source)
        formattingToolbar.setMediaOptionSelectedListener(this)

        // initialize the text & HTML
        aztec.fromHtml(EXAMPLE)
        source.displayStyledAndFormattedHtml(aztec.toHtml())
        aztec.fromHtml(source.getPureHtml())

        source.history = aztec.history
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.undo ->
                if (aztec.visibility == View.VISIBLE) {
                    aztec.undo()
                } else {
                    source.undo()
                }
            R.id.redo ->
                if (aztec.visibility == View.VISIBLE) {
                    aztec.redo()
                } else {
                    source.redo()
                }
            else -> {
            }
        }

        return true
    }

    override fun onCameraPhotoMediaOptionSelected() {
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

    override fun onCameraVideoMediaOptionSelected() {
        if (PermissionUtils.checkAndRequestCameraAndStoragePermissions(this, MEDIA_CAMERA_PHOTO_PERMISSION_REQUEST_CODE)) {
            val intent = Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA)

            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, REQUEST_MEDIA_CAMERA_VIDEO)
            }
        }
    }

    override fun onPhotosMediaOptionSelected() {
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

    override fun onVideosMediaOptionSelected() {
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
                        Manifest.permission.READ_EXTERNAL_STORAGE -> {
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
}
