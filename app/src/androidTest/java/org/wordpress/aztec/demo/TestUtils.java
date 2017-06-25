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
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.hasToString;

/**
 * Test utilities to be used with instrumentation tests.
 */
public class TestUtils {

    // Strings
    public static String unformattedText = "hello";
    public static String formattedText = "world";
    public static String linkURLText = "https://github.com/wordpress-mobile/AztecEditor-Android";

    // Editor Views
    public static ViewInteraction aztecText = onView(withId(R.id.aztec));
    public static ViewInteraction sourceText = onView(withId(R.id.source));

    // Format Toolbar Buttons
    public static ViewInteraction boldButton = onView(withId(R.id.format_bar_button_bold));
    public static ViewInteraction headingButton = onView(withId(R.id.format_bar_button_heading));
    public static ViewInteraction italicButton = onView(withId(R.id.format_bar_button_italic));
    public static ViewInteraction linkButton = onView(withId(R.id.format_bar_button_link));
    public static ViewInteraction moreButton = onView(withId(R.id.format_bar_button_more));
    public static ViewInteraction pageButton = onView(withId(R.id.format_bar_button_page));
    public static ViewInteraction quoteButton = onView(withId(R.id.format_bar_button_quote));
    public static ViewInteraction strikethroughButton = onView(withId(R.id.format_bar_button_strikethrough));
    public static ViewInteraction underlineButton = onView(withId(R.id.format_bar_button_underline));
    public static ViewInteraction listButton = onView(withId(R.id.format_bar_button_list));

    // List Selectors
    public static DataInteraction listUnorderedSelector = onData(hasToString("Unordered List"));
    public static DataInteraction listOrderedSelector = onData(hasToString("Ordered List"));

    // Heading/Paragraph Format Selectors
    public static DataInteraction headingOneSelector = onData(hasToString("Heading 1"));
    public static DataInteraction headingTwoSelector = onData(hasToString("Heading 2"));
    public static DataInteraction headingThreeSelector = onData(hasToString("Heading 3"));
    public static DataInteraction headingFourSelector = onData(hasToString("Heading 4"));
    public static DataInteraction headingFiveSelector = onData(hasToString("Heading 5"));
    public static DataInteraction headingSixSelector = onData(hasToString("Heading 6"));
    public static DataInteraction preSelector = onData(hasToString("Preformat"));

    // Link Modal
    public static ViewInteraction linkOKButton = onView(withId(android.R.id.button1));
    public static ViewInteraction linkTextField = onView(withId(R.id.linkText));
    public static ViewInteraction linkURLField = onView(withId(R.id.linkURL));

    // Switch to HTML view
    public static void toggleHTMLView() {
        ViewInteraction htmlButton = onView(withId(R.id.format_bar_button_html));
        htmlButton.perform(betterScrollTo(),betterClick());
    }

    // Better scrolling action for last toolbar item (<90% of item displayed)
    public static ViewAction betterScrollTo() {
        return ViewActions.actionWithAssertions(new BetterScrollToAction());
    }

    // Better click action for last toolbar item (<90% of item displayed)
    public static ViewAction betterClick() {
        return new BetterClickAction(Tap.SINGLE, GeneralLocation.CENTER, Press.FINGER);
    }
}
