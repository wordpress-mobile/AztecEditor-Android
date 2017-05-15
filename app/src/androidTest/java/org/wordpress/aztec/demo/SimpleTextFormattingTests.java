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
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SimpleTextFormattingTests {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testSimpleBoldFormatting() {
        // Focus on visual editor
        ViewInteraction aztecText = onView(withId(R.id.aztec));
        aztecText.perform(click());

        // Enable bold formatting
        ViewInteraction boldButton = onView(withId(R.id.format_bar_button_bold));
        boldButton.perform(click());

        // Type bold text
        aztecText.perform(typeText("hello world"), closeSoftKeyboard());

        // Switch to HTML view
        ViewInteraction htmlButton = onView(withId(R.id.format_bar_button_html));
        htmlButton.perform(click());

        // Assert that text has bold tags
        ViewInteraction sourceText = onView(allOf(withId(R.id.source), isDisplayed()));
        sourceText.check(matches(withText("<b>hello world</b>")));
    }
}
