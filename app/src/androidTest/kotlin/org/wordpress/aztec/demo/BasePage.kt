package org.wordpress.aztec.demo

import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed

abstract class BasePage {

    abstract val trait: ViewInteraction

    constructor() {
        waitForPage()
        label("On page ${this.javaClass.simpleName}")
    }

    fun label(label: String) {
        BaseTest.label(label)
    }

    fun waitForPage() {
        trait.check(matches(isDisplayed()))
    }

    // Better scrolling action for last toolbar item (<90% of item displayed)
    internal fun betterScrollTo(): ViewAction {
        return ViewActions.actionWithAssertions(BetterScrollToAction())
    }

    // Better click action for last toolbar item (<90% of item displayed)
    internal fun betterClick(): ViewAction {
        return BetterClickAction(Tap.SINGLE, GeneralLocation.CENTER, Press.FINGER)
    }
}
