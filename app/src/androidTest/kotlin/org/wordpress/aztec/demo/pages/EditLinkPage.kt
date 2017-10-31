package org.wordpress.aztec.demo.pages

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.replaceText
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import org.wordpress.aztec.demo.BasePage
import org.wordpress.aztec.demo.R

class EditLinkPage : BasePage() {

    private var urlField: ViewInteraction
    private var nameField: ViewInteraction
    private var okButton: ViewInteraction
    private var cancelButton: ViewInteraction
    private var removeButton: ViewInteraction

    override val trait: ViewInteraction
        get() = onView(withText("Insert link"))

    init {
        urlField = onView(withId(R.id.linkURL))
        nameField = onView(withId(R.id.linkText))
        okButton = onView(withId(android.R.id.button1))
        cancelButton = onView(withId(android.R.id.button2))
        removeButton = onView(withId(android.R.id.button3))
    }

    fun updateURL(url: String): EditLinkPage {
        urlField.perform(replaceText(url), ViewActions.closeSoftKeyboard())
        label("Entered url")

        return this
    }

    fun verifyURL(expected: String): EditLinkPage {
        urlField.check(ViewAssertions.matches(withText(expected)))
        label("Verified expected URL contents")

        return this
    }

    fun updateName(name: String): EditLinkPage {
        nameField.perform(replaceText(name), ViewActions.closeSoftKeyboard())
        label("Entered name")

        return this
    }

    fun verifyName(expected: String): EditLinkPage {
        nameField.check(ViewAssertions.matches(withText(expected)))
        label("Verified expected name contents")

        return this
    }

    fun ok() {
        okButton.perform(click())
        label("Inserted link")
    }

    fun remove() {
        removeButton.perform(click())
        label("Removed link")
    }

    fun cancel() {
        cancelButton.perform(click())
        label("Canceled")
    }
}
