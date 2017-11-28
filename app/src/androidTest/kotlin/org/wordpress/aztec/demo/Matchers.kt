package org.wordpress.aztec.demo

import android.view.View
import android.widget.EditText
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.wordpress.aztec.source.Format

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

    fun withText(expected: String): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("EditText matches $expected")
            }

            public override fun matchesSafely(view: View): Boolean {
                if (view is EditText) {
                    val expectedHtml = Format.removeSourceEditorFormatting(expected, false)
                    val actualHtml = Format.removeSourceEditorFormatting(view.text.toString(), false)
                    return actualHtml == expectedHtml
                }

                return false
            }
        }
    }
}
