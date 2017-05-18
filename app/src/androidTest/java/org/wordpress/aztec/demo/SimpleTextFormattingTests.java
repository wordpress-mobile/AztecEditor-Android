package org.wordpress.aztec.demo;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.wordpress.aztec.demo.TestUtils.aztecText;
import static org.wordpress.aztec.demo.TestUtils.boldButton;
import static org.wordpress.aztec.demo.TestUtils.formattedText;
import static org.wordpress.aztec.demo.TestUtils.headingButton;
import static org.wordpress.aztec.demo.TestUtils.headingFiveSelector;
import static org.wordpress.aztec.demo.TestUtils.headingFourSelector;
import static org.wordpress.aztec.demo.TestUtils.headingOneSelector;
import static org.wordpress.aztec.demo.TestUtils.headingSixSelector;
import static org.wordpress.aztec.demo.TestUtils.headingThreeSelector;
import static org.wordpress.aztec.demo.TestUtils.headingTwoSelector;
import static org.wordpress.aztec.demo.TestUtils.italicButton;
import static org.wordpress.aztec.demo.TestUtils.linkButton;
import static org.wordpress.aztec.demo.TestUtils.linkOKButton;
import static org.wordpress.aztec.demo.TestUtils.linkTextField;
import static org.wordpress.aztec.demo.TestUtils.linkURLField;
import static org.wordpress.aztec.demo.TestUtils.linkURLText;
import static org.wordpress.aztec.demo.TestUtils.moreButton;
import static org.wordpress.aztec.demo.TestUtils.orderedListButton;
import static org.wordpress.aztec.demo.TestUtils.pageButton;
import static org.wordpress.aztec.demo.TestUtils.preSelector;
import static org.wordpress.aztec.demo.TestUtils.quoteButton;
import static org.wordpress.aztec.demo.TestUtils.sourceText;
import static org.wordpress.aztec.demo.TestUtils.strikethroughButton;
import static org.wordpress.aztec.demo.TestUtils.toggleHTMLView;
import static org.wordpress.aztec.demo.TestUtils.underlineButton;
import static org.wordpress.aztec.demo.TestUtils.unformattedText;
import static org.wordpress.aztec.demo.TestUtils.unorderedListButton;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SimpleTextFormattingTests {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testSimpleBoldFormatting() {
        // Enter text in visual editor with formatting
        aztecText.perform(typeText(unformattedText));
        boldButton.perform(click());
        aztecText.perform(typeText(formattedText));

        // Check that HTML formatting tags were correctly added
        toggleHTMLView();
        sourceText.check(matches(withText(unformattedText + "<b>" + formattedText + "</b>")));
    }

    @Test
    public void testSimpleItalicFormatting() {
        // Enter text in visual editor with formatting
        aztecText.perform(typeText(unformattedText));
        italicButton.perform(click());
        aztecText.perform(typeText(formattedText));

        // Check that HTML formatting tags were correctly added
        toggleHTMLView();
        sourceText.check(matches(withText(unformattedText + "<i>" + formattedText + "</i>")));
    }

    @Test
    public void testSimpleStrikethroughFormatting() {
        // Enter text in visual editor with formatting
        aztecText.perform(typeText(unformattedText));
        strikethroughButton.perform(click());
        aztecText.perform(typeText(formattedText));

        // Check that HTML formatting tags were correctly added
        toggleHTMLView();
        sourceText.check(matches(withText(unformattedText + "<del>" + formattedText + "</del>")));
    }

    @Test
    public void testSimpleUnderlineFormatting() {
        // Enter text in visual editor with formatting
        aztecText.perform(typeText(unformattedText));
        underlineButton.perform(click());
        aztecText.perform(typeText(formattedText));

        // Check that HTML formatting tags were correctly added
        toggleHTMLView();
        sourceText.check(matches(withText(unformattedText + "<u>" + formattedText + "</u>")));
    }

    @Test
    public void testSimpleQuoteFormatting() {
        // Enter text in visual editor with formatting
        aztecText.perform(typeText(unformattedText + "\n"));
        quoteButton.perform(click());
        aztecText.perform(typeText(formattedText));

        // Check that HTML formatting tags were correctly added
        toggleHTMLView();
        sourceText.check(matches(withText(unformattedText + "\n<blockquote>" + formattedText + "</blockquote>")));
    }

    @Test
    public void testSimpleUnorderedListFormatting() {
        // Enter text in visual editor with formatting
        aztecText.perform(typeText(unformattedText + "\n"));
        unorderedListButton.perform(scrollTo(), click());
        aztecText.perform(typeText(formattedText));

        // Check that HTML formatting tags were correctly added
        toggleHTMLView();
        sourceText.check(matches(withText(unformattedText + "\n<ul>\n\t<li>" + formattedText + "</li>\n</ul>")));
    }

    @Test
    public void testSimpleOrderedListFormatting() {
        // Enter text in visual editor with formatting
        aztecText.perform(typeText(unformattedText + "\n"));
        orderedListButton.perform(scrollTo(), click());
        aztecText.perform(typeText(formattedText));

        // Check that HTML formatting tags were correctly added
        toggleHTMLView();
        sourceText.check(matches(withText(unformattedText + "\n<ol>\n\t<li>" + formattedText + "</li>\n</ol>")));
    }

    @Test
    public void testSimpleLinkFormatting() {
        // Enter text in visual editor with formatting
        aztecText.perform(typeText(unformattedText));
        linkButton.perform(scrollTo(), click());
        linkURLField.perform(typeTextIntoFocusedView(linkURLText));
        linkTextField.perform(typeText(formattedText));
        linkOKButton.perform(click());

        // Check that HTML formatting tags were correctly added
        toggleHTMLView();
        sourceText.check(matches(withText(unformattedText + "<a href='" + linkURLText + "'>" + formattedText + "</a>")));
    }

    @Test
    public void testSimpleMoreTagFormatting() {
        // Enter text in visual editor more tag in between
        aztecText.perform(typeText(unformattedText));
        moreButton.perform(scrollTo(), click());
        aztecText.perform(typeTextIntoFocusedView(unformattedText));

        // Check that more tag was correctly added
        toggleHTMLView();
        sourceText.check(matches(withText(unformattedText + "\n\n<!--more-->\n\n" + unformattedText)));
    }

    @Test
    public void testSimplePageBreakFormatting() {
        // Enter text in visual editor with page break in between
        aztecText.perform(typeText(unformattedText));
        pageButton.perform(scrollTo(), click());
        aztecText.perform(typeTextIntoFocusedView(unformattedText));

        // Check that page break was correctly added
        toggleHTMLView();
        sourceText.check(matches(withText(unformattedText + "\n\n<!--pagebreak-->\n\n" + unformattedText)));
    }

    @Test
    public void testSimpleHeadingOneFormatting() {
        // Enter text in visual editor with formatting
        aztecText.perform(typeText(unformattedText + "\n"));
        headingButton.perform(click());
        headingOneSelector.perform(click());
        aztecText.perform(typeTextIntoFocusedView(formattedText));

        // Check that HTML formatting tags were correctly added
        toggleHTMLView();
        sourceText.check(matches(withText(unformattedText + "\n<h1>" + formattedText + "</h1>")));
    }

    @Test
    public void testSimpleHeadingTwoFormatting() {
        // Enter text in visual editor with formatting
        aztecText.perform(typeText(unformattedText + "\n"));
        headingButton.perform(click());
        headingTwoSelector.perform(click());
        aztecText.perform(typeTextIntoFocusedView(formattedText));

        // Check that HTML formatting tags were correctly added
        toggleHTMLView();
        sourceText.check(matches(withText(unformattedText + "\n<h2>" + formattedText + "</h2>")));
    }

    @Test
    public void testSimpleHeadingThreeFormatting() {
        // Enter text in visual editor with formatting
        aztecText.perform(typeText(unformattedText + "\n"));
        headingButton.perform(click());
        headingThreeSelector.perform(click());
        aztecText.perform(typeTextIntoFocusedView(formattedText));

        // Check that HTML formatting tags were correctly added
        toggleHTMLView();
        sourceText.check(matches(withText(unformattedText + "\n<h3>" + formattedText + "</h3>")));
    }

    @Test
    public void testSimpleHeadingFourFormatting() {
        // Enter text in visual editor with formatting
        aztecText.perform(typeText(unformattedText + "\n"));
        headingButton.perform(click());
        headingFourSelector.perform(click());
        aztecText.perform(typeTextIntoFocusedView(formattedText));

        // Check that HTML formatting tags were correctly added
        toggleHTMLView();
        sourceText.check(matches(withText(unformattedText + "\n<h4>" + formattedText + "</h4>")));
    }

    @Test
    public void testSimpleHeadingFiveFormatting() {
        // Enter text in visual editor with formatting
        aztecText.perform(typeText(unformattedText + "\n"));
        headingButton.perform(click());
        headingFiveSelector.perform(click());
        aztecText.perform(typeTextIntoFocusedView(formattedText));

        // Check that HTML formatting tags were correctly added
        toggleHTMLView();
        sourceText.check(matches(withText(unformattedText + "\n<h5>" + formattedText + "</h5>")));
    }

    @Test
    public void testSimpleHeadingSixFormatting() {
        // Enter text in visual editor with formatting
        aztecText.perform(typeText(unformattedText + "\n"));
        headingButton.perform(click());
        headingSixSelector.perform(click());
        aztecText.perform(typeTextIntoFocusedView(formattedText));

        // Check that HTML formatting tags were correctly added
        toggleHTMLView();
        sourceText.check(matches(withText(unformattedText + "\n<h6>" + formattedText + "</h6>")));
    }

    @Test
    public void testSimplePreformattedTextFormatting() {
        // Enter text in visual editor with formatting
        aztecText.perform(typeText(unformattedText + "\n"));
        headingButton.perform(click());
        preSelector.perform(click());
        aztecText.perform(typeTextIntoFocusedView(formattedText));

        // Check that HTML formatting tags were correctly added
        toggleHTMLView();
        sourceText.check(matches(withText(unformattedText + "\n<pre>" + formattedText + "</pre>")));
    }
}