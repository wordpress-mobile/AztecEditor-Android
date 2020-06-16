package org.wordpress.aztec.demo

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import org.wordpress.aztec.Aztec
import org.wordpress.aztec.AztecExceptionHandler
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.IHistoryListener
import org.wordpress.aztec.plugins.CssUnderlinePlugin
import org.wordpress.aztec.source.SourceViewEditText
import org.wordpress.aztec.util.AztecLog

open class MainActivity : AppCompatActivity(),
        AztecText.OnImeBackListener,
        IHistoryListener,
        OnRequestPermissionsResultCallback,
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
        private val ITALIC = "<i style=\"color:darkred\">Italic</i><br>"
        private val UNDERLINE = "<u style=\"color:lime\">Underline</u><br>"
        private val STRIKETHROUGH = "<s style=\"color:#ff666666\" class=\"test\">Strikethrough</s><br>" // <s> or <strike> or <del>
        private val ORDERED = "<ol style=\"color:green\"><li>Ordered</li><li>should have color</li></ol>"
        private val ORDERED_WITH_START = "<h4>Start in 10 List:</h4>" +
                "<ol start=\"10\">\n" +
                "    <li>Ten</li>\n" +
                "    <li>Eleven</li>\n" +
                "    <li>Twelve</li>\n" +
                "</ol>"
        private val ORDERED_REVERSED = "<h4>Reversed List:</h4>" +
                "<ol reversed>\n" +
                "    <li>Three</li>\n" +
                "    <li>Two</li>\n" +
                "    <li>One</li>\n" +
                "</ol>"
        private val ORDERED_REVERSED_WITH_START = "<h4>Reversed Start in 10 List:</h4>" +
                "<ol reversed start=\"10\">\n" +
                "    <li>Ten</li>\n" +
                "    <li>Nine</li>\n" +
                "    <li>Eight</li>\n" +
                "</ol>"
        private val ORDERED_REVERSED_NEGATIVE_WITH_START = "<h4>Reversed Start in 1 List:</h4>" +
                "<ol reversed start=\"1\">\n" +
                "    <li>One</li>\n" +
                "    <li>Zero</li>\n" +
                "    <li>Minus One</li>\n" +
                "</ol>"
        private val ORDERED_REVERSED_WITH_START_IDENT = "<h4>Reversed Start in 6 List:</h4>" +
                "<ol reversed>" +
                "   <li>Six</li>" +
                "   <li>Five</li>" +
                "   <li>Four</li>" +
                "   <li>Three</li>" +
                "   <li>Two</li>" +
                "   <li>One<ol>" +
                "   <li>One</li>" +
                "   <li>Two</li>" +
                "   <li>Three</li>" +
                "   <li>Four</li>" +
                "   <li>Five</li>" +
                "   <li>Six</li>" +
                "   <li>Seven</li> " +
                "   </ol></li></ol>"
        private val LINE = "<hr />"
        private val UNORDERED = "<ul><li style=\"color:darkred\">Unordered</li><li>Should not have color</li></ul>"
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
        private val EMOJI = "&#x1F44D;"
        private val NON_LATIN_TEXT = "测试一个"
        private val LONG_TEXT = "<br><br>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. "
        private val QUOTE_RTL = "<blockquote>לְצַטֵט<br>same quote but LTR</blockquote>"

        private val EXAMPLE =
                        HEADING +
                        BOLD +
                        ITALIC +
                        UNDERLINE +
                        STRIKETHROUGH +
                        ORDERED +
                        ORDERED_WITH_START +
                        ORDERED_REVERSED +
                        ORDERED_REVERSED_WITH_START +
                        ORDERED_REVERSED_NEGATIVE_WITH_START +
                        ORDERED_REVERSED_WITH_START_IDENT +
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
                        QUOTE_RTL

        private val isRunningTest: Boolean by lazy {
            try {
                Class.forName("androidx.test.espresso.Espresso")
                true
            } catch (e: ClassNotFoundException) {
                false
            }
        }
    }

    protected lateinit var aztec: Aztec

    private lateinit var invalidateOptionsHandler: Handler
    private lateinit var invalidateOptionsRunnable: Runnable

    private var mIsKeyboardOpen = false
    private var mHideActionBarOnSoftKeyboardUp = false

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

        visualEditor.externalLogger = object : AztecLog.ExternalLogger {
            override fun log(message: String) {
            }

            override fun logException(tr: Throwable) {
            }

            override fun logException(tr: Throwable, message: String) {
            }
        }

        aztec = Aztec.with(visualEditor, sourceEditor)
                .setOnImeBackListener(this)
                .setOnTouchListener(this)
                .setHistoryListener(this)

        // initialize the plugins, text & HTML
        if (!isRunningTest) {
            aztec.visualEditor.enableCrashLogging(object : AztecExceptionHandler.ExceptionHandlerHelper {
                override fun shouldLog(ex: Throwable): Boolean {
                    return true
                }
            })
            aztec.visualEditor.setCalypsoMode(false)
            aztec.sourceEditor?.setCalypsoMode(false)

            aztec.sourceEditor?.displayStyledAndFormattedHtml(EXAMPLE)

            aztec.addPlugin(CssUnderlinePlugin())
        }

        if (savedInstanceState == null) {
            if (!isRunningTest) {
                aztec.visualEditor.fromHtml(EXAMPLE)
            }
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

    override fun onDestroy() {
        super.onDestroy()
        aztec.visualEditor.disableCrashLogging()
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

    @SuppressLint("ClickableViewAccessibility")
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

    override fun onUndo() {}

    override fun onRedo() {}
}
