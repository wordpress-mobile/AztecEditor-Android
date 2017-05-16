package org.wordpress.aztec.demo;

import android.support.test.espresso.ViewInteraction;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Test utilities to be used with instrumentation tests.
 */
public class TestUtils {
    public static void toggleHTMLView() {
        ViewInteraction htmlButton = onView(withId(R.id.format_bar_button_html));
        htmlButton.perform(click());
    }

    public static void enterHTML(String text) {
        toggleHTMLView();

        ViewInteraction htmlViewEditText = onView(withId(R.id.source));
        htmlViewEditText.perform(typeText(text));

        toggleHTMLView();
    }
}
