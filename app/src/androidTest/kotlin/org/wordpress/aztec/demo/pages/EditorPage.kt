package org.wordpress.aztec.demo.pages

import androidx.test.espresso.DataInteraction
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressKey
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import android.view.KeyEvent
import android.view.View
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.hasToString
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.demo.Actions
import org.wordpress.aztec.demo.BasePage
import org.wordpress.aztec.demo.Matchers
import org.wordpress.aztec.demo.R

class EditorPage : BasePage() {
    private var editor: ViewInteraction
    private var htmlEditor: ViewInteraction

    private var undoButton: ViewInteraction
    private var redoButton: ViewInteraction

    private var openMediaToolbarButton: ViewInteraction
    private var closeMediaToolbarButton: ViewInteraction
    private var headingButton: ViewInteraction
    private var listButton: ViewInteraction
    private var quoteButton: ViewInteraction
    private var boldButton: ViewInteraction
    private var italicsButton: ViewInteraction
    private var linkButton: ViewInteraction
    private var underlineButton: ViewInteraction
    private var strikethroughButton: ViewInteraction
    private var horizontalRuleButton: ViewInteraction
    private var moreRuleButton: ViewInteraction
    private var pageButton: ViewInteraction
    private var htmlButton: ViewInteraction

    private var photoButton: ViewInteraction
    private var galleryButton: ViewInteraction

    override val trait: ViewInteraction
        get() = onView(withId(R.id.aztec))

    init {
        editor = onView(withId(R.id.aztec))
        htmlEditor = onView(withId(R.id.source))

        undoButton = onView(withId(R.id.undo))
        redoButton = onView(withId(R.id.redo))

        openMediaToolbarButton = onView(withId(R.id.format_bar_button_media_collapsed))
        closeMediaToolbarButton = onView(withId(R.id.format_bar_button_media_expanded))
        headingButton = onView(withId(R.id.format_bar_button_heading))
        listButton = onView(withId(R.id.format_bar_button_list))
        quoteButton = onView(withId(R.id.format_bar_button_quote))
        boldButton = onView(withId(R.id.format_bar_button_bold))
        italicsButton = onView(withId(R.id.format_bar_button_italic))
        linkButton = onView(withId(R.id.format_bar_button_link))
        underlineButton = onView(withId(R.id.format_bar_button_underline))
        strikethroughButton = onView(withId(R.id.format_bar_button_strikethrough))
        horizontalRuleButton = onView(withId(R.id.format_bar_button_horizontal_rule))
        moreRuleButton = onView(withId(R.id.format_bar_button_more))
        pageButton = onView(withId(R.id.format_bar_button_page))
        htmlButton = onView(withId(R.id.format_bar_button_html))

        photoButton = onView(allOf(withId(android.R.id.title), withText("Photo from device")))
        galleryButton = onView(withId(R.id.media_bar_button_gallery))
    }

    fun tapTop(): EditorPage {
        editor.perform(Actions.relativeClick(0.5f, 0.1f))
        label("Tapped editor")

        return this
    }

    fun closeKeyboard(): EditorPage {
        editor.perform(ViewActions.closeSoftKeyboard())
        label("Closed keyboard")

        return this
    }

    fun selectAllText(): EditorPage {
        editor.perform(Actions.selectAll())
        label("Selected all text")

        return this
    }

    fun selectText(start: Int, end: Int): EditorPage {
        editor.perform(Actions.setSelection(start, end))
        label("Selected text")

        return this
    }

    /**
     * Using selectAllText() + delete() do not work as intended. This method
     * will select all the text in the editor and then delete that text.
     */
    fun selectAllAndDelete(): EditorPage {
        selectAllText()
        editor.perform(pressKey(KeyEvent.KEYCODE_FORWARD_DEL))
        label("Select all text and delete")

        return this
    }

    fun delete(characters: Int): EditorPage {
        for (i in 1..characters) {
            editor.perform(pressKey(KeyEvent.KEYCODE_DEL))
        }

        return this
    }

    fun insertNewLine(): EditorPage {
        editor.perform(pressKey(KeyEvent.KEYCODE_ENTER))
        label("Insert new line")

        return this
    }

    fun clearText(): EditorPage {
        editor.perform(ViewActions.clearText())
        label("Clear editor text")

        return this
    }

    fun insertText(text: String): EditorPage {
        editor.perform(typeText(text), ViewActions.closeSoftKeyboard())
        label("Inserted text")

        return this
    }

    fun replaceText(text: String): EditorPage {
        editor.perform(ViewActions.replaceText(text), ViewActions.closeSoftKeyboard())
        label("Replaced text")

        return this
    }

    fun focusedInsertText(text: String): EditorPage {
        editor.perform(typeTextIntoFocusedView(text), ViewActions.closeSoftKeyboard())
        label("Inserted text")

        return this
    }

    fun moveCursorLeftAsManyTimes(times: Int): EditorPage {
        for (time in 1..times) {
            editor.perform(pressKey(KeyEvent.KEYCODE_DPAD_LEFT))
        }
        label("Moved cursor left " + times + " times")

        return this
    }

    fun insertHTML(html: String): EditorPage {
        htmlEditor.perform(typeText(html), ViewActions.closeSoftKeyboard())
        label("Inserted HTML")

        return this
    }

    fun replaceHTML(html: String): EditorPage {
        htmlEditor.perform(ViewActions.replaceText(html), ViewActions.closeSoftKeyboard())
        label("Replaced HTML")

        return this
    }

    fun insertMedia() {
        openMediaToolbarButton.perform(betterScrollTo(), Actions.invokeClick())
        label("Inserting media")

        Thread.sleep(200)
        galleryButton.perform(Actions.invokeClick())
        label("Chose gallery")

        Thread.sleep(200)
        photoButton.perform(click())
        label("Chose photo")
    }

    fun undoChange(): EditorPage {
        undoButton.perform(Actions.invokeClick())
        label("Performed undo")

        return this
    }

    fun redoChange(): EditorPage {
        redoButton.perform(Actions.invokeClick())
        label("Performed redo")

        return this
    }

    fun makeHeader(style: HeadingStyle): EditorPage {
        headingButton.perform(betterScrollTo(), Actions.invokeClick())
        label("Choosing heading style")

        style.element.perform(click())
        label("Chose ${style.name}")

        return this
    }

    fun makeList(style: ListStyle): EditorPage {
        listButton.perform(betterScrollTo(), Actions.invokeClick())
        label("Choosing list style")

        style.element.perform(click())
        label("Chose ${style.name}")

        return this
    }

    fun toggleQuote(): EditorPage {
        quoteButton.perform(betterScrollTo(), Actions.invokeClick())
        label("Made quote")

        return this
    }

    fun checkQuote(assertion: Matcher<View>): EditorPage {
        quoteButton.check(matches(assertion))
        label("Checked quote state")

        return this
    }

    fun toggleBold(): EditorPage {
        boldButton.perform(betterScrollTo(), Actions.invokeClick())
        label("Made bold")

        return this
    }

    fun checkBold(assertion: Matcher<View>): EditorPage {
        boldButton.check(matches(assertion))
        label("Checked bold state")

        return this
    }

    fun toggleItalics(): EditorPage {
        italicsButton.perform(betterScrollTo(), Actions.invokeClick())
        label("Made italics")

        return this
    }

    fun checkItalics(assertion: Matcher<View>): EditorPage {
        italicsButton.check(matches(assertion))
        label("Checked italics state")

        return this
    }

    fun makeLink() {
        linkButton.perform(betterScrollTo(), Actions.invokeClick())
        label("Opened link dialogue")
    }

    fun checkLink(assertion: Matcher<View>): EditorPage {
        linkButton.check(matches(assertion))
        label("Checked link state")

        return this
    }

    fun toggleUnderline(): EditorPage {
        underlineButton.perform(betterScrollTo(), Actions.invokeClick())
        label("Made underline")

        return this
    }

    fun checkUnderline(assertion: Matcher<View>): EditorPage {
        underlineButton.check(matches(assertion))
        label("Checked underline state")

        return this
    }

    fun toggleStrikethrough(): EditorPage {
        strikethroughButton.perform(betterScrollTo(), Actions.invokeClick())
        label("Made strikethrough")

        return this
    }

    fun checkStrikethrough(assertion: Matcher<View>): EditorPage {
        strikethroughButton.check(matches(assertion))
        label("Checked strikethrough state")

        return this
    }

    fun addHorizontalRule(): EditorPage {
        horizontalRuleButton.perform(betterScrollTo(), Actions.invokeClick())
        label("Added horizontal rule")

        return this
    }

    fun addMoreRule(): EditorPage {
        moreRuleButton.perform(betterScrollTo(), Actions.invokeClick())
        label("Added more rule")

        return this
    }

    fun addPage(): EditorPage {
        pageButton.perform(betterScrollTo(), Actions.invokeClick())
        label("Added page")

        return this
    }

    fun toggleHtml(): EditorPage {
        htmlButton.perform(betterScrollTo(), Actions.invokeClick())
        label("Toggled HTML")

        return this
    }

    fun verify(expected: String): EditorPage {
        editor.check(matches(Matchers.withStrippedText(expected)))
        label("Verified expected editor contents")

        return this
    }

    fun verifyHTML(expected: String): EditorPage {
        htmlEditor.check(matches(Matchers.withStrippedText(expected)))
        label("Verified expected HTML editor contents")

        return this
    }

    fun verifyHTML(expected: Regex): EditorPage {
        htmlEditor.check(matches(Matchers.withRegex(expected)))
        label("Verified expected HTML editor contents")

        return this
    }

    fun verifyHTMLNoStripping(expected: String): EditorPage {
        htmlEditor.check(matches(withText(expected)))
        label("Verified expected HTML editor contents without stripping")

        return this
    }

    fun hasChanges(shouldHaveChanges : AztecText.EditorHasChanges): EditorPage {
        editor.check(matches(Matchers.hasContentChanges(shouldHaveChanges)))
        return this
    }

    fun hasChangesHTML(shouldHaveChanges : AztecText.EditorHasChanges): EditorPage {
        htmlEditor.check(matches(Matchers.hasContentChanges(shouldHaveChanges)))
        return this
    }

    fun copyToClipboard(): EditorPage {
        editor.perform(Actions.copyToClipboardAztec())
        label("Copy to Aztec clipboard")

        return this
    }

    fun copyRangeToClipboard(start: Int, end: Int): EditorPage {
        editor.perform(Actions.copyRangeToClipboardAztec(start, end))
        label("Copy text from index [$start] to [$end] to clipboard")

        return this
    }

    fun pasteFromClipboard(): EditorPage {
        editor.perform(Actions.pasteFromClipboardAztec())
        label("Paste from Aztec clipboard")

        return this
    }

    fun pasteRangeFromClipboard(start: Int, end: Int): EditorPage {
        editor.perform(Actions.pasteRangeFromClipboardAztec(start, end))
        label("Past from Aztec clipboard from range [$start] to [$end]")

        return this
    }

    fun setCursorPositionAtEnd(): EditorPage {
        editor.perform(Actions.setAztecCursorPositionEnd())
        label("Set Aztec cursor position at the end of text buffer")

        return this
    }

    fun threadSleep(millis: Long): EditorPage {
        Thread.sleep(millis)
        return this
    }

    enum class HeadingStyle(val element: DataInteraction) {
        DEFAULT(onData(hasToString("Default"))),
        ONE(onData(hasToString("Heading 1"))),
        TWO(onData(hasToString("Heading 2"))),
        THREE(onData(hasToString("Heading 3"))),
        FOUR(onData(hasToString("Heading 4"))),
        FIVE(onData(hasToString("Heading 5"))),
        SIX(onData(hasToString("Heading 6"))),
        PRESELECTOR(onData(hasToString("Preformat")))
    }

    enum class ListStyle(val element: DataInteraction) {
        UNORDERED(onData(hasToString("Unordered List"))),
        ORDERED(onData(hasToString("Ordered List")))
    }
}
