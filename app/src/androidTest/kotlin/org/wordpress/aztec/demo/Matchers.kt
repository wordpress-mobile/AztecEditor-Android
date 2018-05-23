package org.wordpress.aztec.demo

import android.view.View
import android.widget.EditText
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.source.Format
import org.wordpress.aztec.source.SourceViewEditText

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
                    val expectedHtml = Format.removeSourceEditorFormatting(expected, false)
                    val actualHtml = Format.removeSourceEditorFormatting(view.text.toString(), false)
                    return actualHtml == expectedHtml
                }

                return false
            }
        }
    }

    fun hasContentChanges(shouldHaveChanges: AztecText.EditorHasChanges): TypeSafeMatcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("User has made changes to the post: $shouldHaveChanges")
            }

            public override fun matchesSafely(view: View): Boolean {
                if (view is SourceViewEditText) {
                    return view.hasChanges() == shouldHaveChanges
                }
                if (view is AztecText) {
                    return view.hasChanges() == shouldHaveChanges
                }
                return false
            }
        }
    }
}
