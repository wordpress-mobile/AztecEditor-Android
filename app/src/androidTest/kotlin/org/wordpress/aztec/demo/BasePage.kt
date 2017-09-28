package org.wordpress.aztec.demo

import android.support.test.espresso.ViewAction
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.action.GeneralLocation
import android.support.test.espresso.action.Press
import android.support.test.espresso.action.Tap
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed

/**
 * Created by matisseh on 9/6/17.
 */

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
