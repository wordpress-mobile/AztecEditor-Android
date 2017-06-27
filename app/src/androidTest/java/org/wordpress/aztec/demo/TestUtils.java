package org.wordpress.aztec.demo;

import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.support.test.espresso.action.ViewActions;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasToString;

/**
 * Test utilities to be used with instrumentation tests.
 */
public class TestUtils {

    // Strings
    static String unformattedText = "hello";
    static String formattedText = "world";
    static String linkURLText = "https://github.com/wordpress-mobile/AztecEditor-Android";

    // Editor Views
    static ViewInteraction aztecText = onView(withId(R.id.aztec));
    static ViewInteraction sourceText = onView(allOf(withId(R.id.source), isDisplayed()));

    // Format Toolbar Buttons
    static ViewInteraction boldButton = onView(withId(R.id.format_bar_button_bold));
    static ViewInteraction headingButton = onView(withId(R.id.format_bar_button_heading));
    static ViewInteraction italicButton = onView(withId(R.id.format_bar_button_italic));
    static ViewInteraction linkButton = onView(withId(R.id.format_bar_button_link));
    static ViewInteraction moreButton = onView(withId(R.id.format_bar_button_more));
    static ViewInteraction pageButton = onView(withId(R.id.format_bar_button_page));
    static ViewInteraction quoteButton = onView(withId(R.id.format_bar_button_quote));
    static ViewInteraction strikethroughButton = onView(withId(R.id.format_bar_button_strikethrough));
    static ViewInteraction underlineButton = onView(withId(R.id.format_bar_button_underline));
    static ViewInteraction listButton = onView(withId(R.id.format_bar_button_list));

    // List Selectors
    static DataInteraction listUnorderedSelector = onData(hasToString("Unordered List"));
    static DataInteraction listOrderedSelector = onData(hasToString("Ordered List"));

    // Heading/Paragraph Format Selectors
    static DataInteraction headingOneSelector = onData(hasToString("Heading 1"));
    static DataInteraction headingTwoSelector = onData(hasToString("Heading 2"));
    static DataInteraction headingThreeSelector = onData(hasToString("Heading 3"));
    static DataInteraction headingFourSelector = onData(hasToString("Heading 4"));
    static DataInteraction headingFiveSelector = onData(hasToString("Heading 5"));
    static DataInteraction headingSixSelector = onData(hasToString("Heading 6"));
    static DataInteraction preSelector = onData(hasToString("Preformat"));

    // Link Modal
    static ViewInteraction linkOKButton = onView(withId(android.R.id.button1));
    static ViewInteraction linkTextField = onView(withId(R.id.linkText));
    static ViewInteraction linkURLField = onView(withId(R.id.linkURL));

    // Switch to HTML view
    static void toggleHTMLView() {
        ViewInteraction htmlButton = onView(withId(R.id.format_bar_button_html));
        htmlButton.perform(betterScrollTo(), betterClick());
    }

    // Better scrolling action for last toolbar item (<90% of item displayed)
    static ViewAction betterScrollTo() {
        return ViewActions.actionWithAssertions(new BetterScrollToAction());
    }

    // Better click action for last toolbar item (<90% of item displayed)
    static ViewAction betterClick() {
        return new BetterClickAction(Tap.SINGLE, GeneralLocation.CENTER, Press.FINGER);
    }
}
