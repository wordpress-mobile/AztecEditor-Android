package org.wordpress.aztec.demo

import android.view.View
import android.widget.EditText
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

object Matchers {
    fun withRegex(expected: Regex): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("EditText matches $expected")
            }

            public override fun matchesSafely(view: View): Boolean {
                if (view is EditText) {
                    val regex = Regex(">\\s+<")
                    val strippedText = view.text.toString().replace(regex, "><")
                    return strippedText.matches(expected)
                }

                return false
            }
        }
    }

    fun withStrippedText(expected: String): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("EditText matches $expected")
            }

            public override fun matchesSafely(view: View): Boolean {
                if (view is EditText) {
                    val regex = Regex(">\\s+<")
                    val strippedText = view.text.toString().replace(regex, "><")
                    return strippedText.equals(expected)
                }

                return false
            }
        }
    }
}
