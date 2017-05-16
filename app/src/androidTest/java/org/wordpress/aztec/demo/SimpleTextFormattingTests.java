package org.wordpress.aztec.demo;

import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SimpleTextFormattingTests {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testSimpleBoldFormatting() {
        // Switch to HTML view
        ViewInteraction htmlButton = onView(withId(R.id.format_bar_button_html));
        htmlButton.perform(click());

        // Type text with bold tags
        ViewInteraction htmlViewEditText = onView(withId(R.id.source));
        htmlViewEditText.perform(typeText("<b>hello world</b>"));

        // Switch back to visual view
        htmlButton.perform(click());

        // Assert that bold button is enabled
        ViewInteraction boldButton = onView(withId(R.id.format_bar_button_bold));
        boldButton.check(matches(isEnabled()));
    }
}
