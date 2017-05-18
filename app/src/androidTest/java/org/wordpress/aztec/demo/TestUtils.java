package org.wordpress.aztec.demo;

import android.support.test.espresso.ViewInteraction;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

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
    public static ViewInteraction sourceText = onView(allOf(withId(R.id.source), isDisplayed()));

    // Format Toolbar Buttons
    public static ViewInteraction boldButton = onView(withId(R.id.format_bar_button_bold));
    public static ViewInteraction headingButton = onView(withId(R.id.format_bar_button_heading));
    public static ViewInteraction italicButton = onView(withId(R.id.format_bar_button_italic));
    public static ViewInteraction linkButton = onView(withId(R.id.format_bar_button_link));
    public static ViewInteraction moreButton = onView(withId(R.id.format_bar_button_more));
    public static ViewInteraction orderedListButton = onView(withId(R.id.format_bar_button_ol));
    public static ViewInteraction pageButton = onView(withId(R.id.format_bar_button_page));
    public static ViewInteraction quoteButton = onView(withId(R.id.format_bar_button_quote));
    public static ViewInteraction strikethroughButton = onView(withId(R.id.format_bar_button_strikethrough));
    public static ViewInteraction underlineButton = onView(withId(R.id.format_bar_button_underline));
    public static ViewInteraction unorderedListButton = onView(withId(R.id.format_bar_button_ul));

    // Heading/Paragraph Format Selectors
    public static ViewInteraction headingOneSelector = onView(allOf(withId(android.R.id.title), withText("Heading 1")));
    public static ViewInteraction headingTwoSelector = onView(withText("Heading 2"));
    public static ViewInteraction headingThreeSelector = onView(withText("Heading 3"));
    public static ViewInteraction headingFourSelector = onView(withText("Heading 4"));
    public static ViewInteraction headingFiveSelector = onView(withText("Heading 5"));
    public static ViewInteraction headingSixSelector = onView(withText("Heading 6"));
    public static ViewInteraction preSelector = onView(withText("Heading 6"));

    // Link Modal
    public static ViewInteraction linkOKButton = onView(withId(android.R.id.button1));
    public static ViewInteraction linkTextField = onView(withId(R.id.linkText));
    public static ViewInteraction linkURLField = onView(withId(R.id.linkURL));

    public static void toggleHTMLView() {
        ViewInteraction htmlButton = onView(withId(R.id.format_bar_button_html));
        htmlButton.perform(click());
    }
}
