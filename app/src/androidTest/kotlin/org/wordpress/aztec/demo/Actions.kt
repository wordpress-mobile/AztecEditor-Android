package org.wordpress.aztec.demo

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
                Press.FINGER)
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
}
