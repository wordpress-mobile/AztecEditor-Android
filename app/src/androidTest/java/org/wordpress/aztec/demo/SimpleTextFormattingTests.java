package org.wordpress.aztec.demo;

import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.wordpress.aztec.demo.TestUtils.enterHTML;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SimpleTextFormattingTests {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testSimpleBoldFormatting() {
        ViewInteraction boldButton = onView(withId(R.id.format_bar_button_bold));
        boldButton.check(matches(isNotChecked()));

        enterHTML("<b>hello world</b>");

        boldButton.check(matches(isChecked()));
    }
}
