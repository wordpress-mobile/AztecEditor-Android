package org.wordpress.aztec.demo

import android.content.Context
import android.graphics.Rect
import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.support.test.espresso.action.CoordinatesProvider
import android.support.test.espresso.action.GeneralClickAction
import android.support.test.espresso.action.Press
import android.support.test.espresso.action.Tap
import android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom
import android.support.test.espresso.matcher.ViewMatchers.isClickable
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.view.View
import android.widget.EditText
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.watchers.EndOfBufferMarkerAdder

object Actions {
    fun invokeClick(): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View>? {
                return allOf(isClickable(), isDisplayed())
            }

            override fun getDescription(): String? {
                return "Calls 'callOnClick' on view"
            }

            override fun perform(uiController: UiController, view: View) {
                view.performClick()
            }
        }
    }

    fun relativeClick(xPercent: Float = 0.5f, yPercent: Float = 0.5f): ViewAction {
        return GeneralClickAction(
                Tap.SINGLE,
                CoordinatesProvider { view ->
                    val rect = Rect()
                    view.getGlobalVisibleRect(rect)

                    val x = rect.left + (rect.width() - rect.left) * xPercent
                    val y = rect.top + (rect.height() - rect.top) * yPercent

                    floatArrayOf(x, y)
                },
                Press.FINGER, 0, 0)
    }

    fun selectAll(): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View>? {
                return allOf(isDisplayed(), isAssignableFrom(EditText::class.java))
            }

            override fun getDescription(): String? {
                return "Select all"
            }

            override fun perform(uiController: UiController, view: View) {
                if (view is EditText) {
                    view.selectAll()
                }
            }
        }
    }

    fun copyToClipboard(source: String): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return allOf(isDisplayed(), isAssignableFrom(EditText::class.java))
            }

            override fun getDescription(): String {
                return "Copy text to clipboard"
            }

            override fun perform(uiController: UiController?, view: View?) {
                if (view is AztecText) {
                    val clipboard = view.context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Aztec", source)
                    clipboard.primaryClip = clip
                }
            }
        }
    }

    fun copyToClipboardAztec(): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return allOf(isDisplayed(), isAssignableFrom(EditText::class.java))
            }

            override fun getDescription(): String {
                return "Copy to Aztec clipboard"
            }

            override fun perform(uiController: UiController?, view: View?) {
                if (view is AztecText) {
                    view.copy(view.text, view.selectionStart, view.selectionEnd)
                }
            }
        }
    }

    fun copyRangeToClipboardAztec(start: Int, end: Int): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return allOf(isDisplayed(), isAssignableFrom(EditText::class.java))
            }

            override fun getDescription(): String {
                return "Copy to Aztec clipboard"
            }

            override fun perform(uiController: UiController?, view: View?) {
                if (view is AztecText) {
                    view.copy(view.text, start, end)
                }
            }
        }
    }

    fun pasteFromClipboardAztec(): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return allOf(isDisplayed(), isAssignableFrom(EditText::class.java))
            }

            override fun getDescription(): String {
                return "Paste from Aztec clipboard"
            }

            override fun perform(uiController: UiController?, view: View?) {
                if (view is AztecText) {
                    view.paste(view.text, view.selectionStart, view.selectionEnd)
                }
            }
        }
    }

    fun pasteRangeFromClipboardAztec(start: Int, end: Int): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return allOf(isDisplayed(), isAssignableFrom(EditText::class.java))
            }

            override fun getDescription(): String {
                return "Paste from Aztec clipboard from range [$start] to [$end]"
            }

            override fun perform(uiController: UiController?, view: View?) {
                if (view is AztecText) {
                    view.paste(view.text, start, end)
                }
            }
        }
    }

    fun setAztecCursorPositionEnd(): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return allOf(isDisplayed(), isAssignableFrom(EditText::class.java))
            }

            override fun getDescription(): String {
                return "Set Aztec cursor at the end of current text buffer"
            }

            override fun perform(uiController: UiController?, view: View?) {
                if (view is AztecText) {
                    view.setSelection(EndOfBufferMarkerAdder.safeLength(view))
                }
            }
        }
    }

    fun setSelection(start: Int, end: Int): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return allOf(isDisplayed(), isAssignableFrom(EditText::class.java))
            }

            override fun getDescription(): String {
                return "Set Aztec text selection to the specified range"
            }

            override fun perform(uiController: UiController?, view: View?) {
                if (view is AztecText) {
                    view.setSelection(start, end)
                }
            }
        }
    }
}
