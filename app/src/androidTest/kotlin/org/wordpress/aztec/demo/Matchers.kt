package org.wordpress.aztec.demo

import android.view.View
import android.widget.EditText
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

object Matchers {
    fun withRegex(regex: Regex): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("EditText matches $regex")
            }

            public override fun matchesSafely(view: View): Boolean {
                if (view is EditText) {
                    return view.text.toString().matches(regex)
                }

                return false
            }
        }
    }
}
