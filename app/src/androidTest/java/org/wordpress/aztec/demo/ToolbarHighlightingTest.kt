package org.wordpress.aztec.demo

import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.closeSoftKeyboard
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.view.KeyEvent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.wordpress.aztec.demo.TestUtils.*

@RunWith(AndroidJUnit4::class)
class ToolbarHighlightingTest {

    @Rule @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    //test behavior of highlighted style at 0 index of editor with 1 line of text (EOB marker at the 1 line)
    @Test
    fun testLeadingStyleHighlightInEmptyEditor() {
        boldButton.perform(betterScrollTo(), betterClick())
        aztecText.perform(typeText(formattedText))

        italicButton.perform(betterScrollTo(), betterClick())

        boldButton.check(matches(isChecked()))
        italicButton.check(matches(isChecked()))

        formattedText.forEach {
            aztecText.perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))
        }

        boldButton.check(matches(isChecked()))
        italicButton.check(matches(isNotChecked()))

        aztecText.perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))

        boldButton.check(matches(isNotChecked()))
        italicButton.check(matches(isNotChecked()))

    }

    //test behavior of highlighted style at 0 index of editor with > 1 lines of text (no EOB marker at the 1 line)
    @Test
    fun testLeadingStyleHighlightInNotEmptyEditor() {
        boldButton.perform(betterScrollTo(), betterClick())
        aztecText.perform(typeText(formattedText))
        italicButton.perform(betterScrollTo(), betterClick())

        aztecText.perform(ViewActions.pressKey(KeyEvent.KEYCODE_ENTER))

        boldButton.check(matches(isNotChecked()))
        italicButton.check(matches(isNotChecked()))

        aztecText.perform(typeText(formattedText))


        formattedText.forEach {
            aztecText.perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))
        }

        formattedText.forEach {
            aztecText.perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))
        }

        aztecText.perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))

        boldButton.check(matches(isChecked()))
        italicButton.check(matches(isNotChecked()))

        aztecText.perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))

        boldButton.check(matches(isNotChecked()))
        italicButton.check(matches(isNotChecked()))
    }

    //make sure that inline style is not sticking to end of buffer marker
    @Test
    fun testInlineIsDeselectedNearEndOfBufferMarker() {
        boldButton.perform(betterScrollTo(), betterClick())
        aztecText.perform(typeText(formattedText))

        boldButton.check(matches(isChecked()))
        boldButton.perform(betterScrollTo(), betterClick())
        boldButton.check(matches(isNotChecked()))

        aztecText.perform(typeText(unformattedText))

        boldButton.check(matches(isNotChecked()))

        // Check that HTML formatting tags were correctly added
        toggleHTMLView()
        sourceText.check(matches(withText("<b>$formattedText</b>$unformattedText")))
    }


    //make sure that selected toolbar style in empty editor remains when soft keyboard is displayed
    @Test
    fun testStyleHighlightPersistenceInEmptyEditorOnWindowFocusChange() {
        aztecText.perform(closeSoftKeyboard()) //make sure keyboard is closed
        boldButton.perform(betterScrollTo(), betterClick())
        aztecText.perform(betterClick()) //click in editor so the soft keyboard is up

        boldButton.check(matches(isChecked()))

        aztecText.perform(closeSoftKeyboard())

        boldButton.check(matches(isChecked()))

        aztecText.perform(typeText(formattedText))
        toggleHTMLView()
        sourceText.check(matches(withText("<b>$formattedText</b>")))
    }

}
