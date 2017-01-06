package org.wordpress.aztec;

/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.ParagraphStyle;
import android.text.style.TextAppearanceSpan;
import android.text.style.TypefaceSpan;

import org.ccil.cowan.tagsoup.HTMLSchema;
import org.ccil.cowan.tagsoup.Parser;
import org.wordpress.aztec.spans.AztecBlockSpan;
import org.wordpress.aztec.spans.AztecCodeSpan;
import org.wordpress.aztec.spans.AztecCommentSpan;
import org.wordpress.aztec.spans.AztecContentSpan;
import org.wordpress.aztec.spans.AztecCursorSpan;
import org.wordpress.aztec.spans.AztecListSpan;
import org.wordpress.aztec.spans.AztecRelativeSizeSpan;
import org.wordpress.aztec.spans.AztecStyleSpan;
import org.wordpress.aztec.spans.AztecSubscriptSpan;
import org.wordpress.aztec.spans.AztecSuperscriptSpan;
import org.wordpress.aztec.spans.AztecTypefaceSpan;
import org.wordpress.aztec.spans.AztecURLSpan;
import org.wordpress.aztec.spans.AztecUnderlineSpan;
import org.wordpress.aztec.spans.CommentSpan;
import org.wordpress.aztec.spans.FontSpan;
import org.wordpress.aztec.spans.ParagraphSpan;
import org.wordpress.aztec.spans.UnknownClickableSpan;
import org.wordpress.aztec.spans.UnknownHtmlSpan;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

import java.io.IOException;
import java.io.StringReader;

// This class was imported from AOSP and was modified to fit our needs, it's probably a good idea to keep it as a
// Java file.

/**
 * This class processes HTML strings into displayable styled text.
 * Not all HTML tags are supported.
 */
public class Html {
    /**
     * Retrieves images for HTML &lt;img&gt; tags.
     */
    public interface ImageGetter {
        /**
         * This method is called when the HTML parser encounters an
         * &lt;img&gt; tag.  The <code>source</code> argument is the
         * string from the "src" attribute; the return value should be
         * a Drawable representation of the image or <code>null</code>
         * for a generic replacement image.  Make sure you call
         * setBounds() on your Drawable if it doesn't already have
         * its bounds set.
         */
        void loadImage(String source, Html.ImageGetter.Callbacks callbacks);

        interface Callbacks {
            void onImageLoaded(Drawable drawable);
            void onImageLoadingFailed();
        }
    }

    /**
     * Is notified when HTML tags are encountered that the parser does
     * not know how to interpret.
     */
    public interface TagHandler {
        /**
         * This method will be called whenn the HTML parser encounters
         * a tag that it does not know how to interpret.
         */
        boolean handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader, Attributes attributes);
    }

    private Html() {
    }

    /**
     * Returns displayable styled text from the provided HTML string.
     * Any &lt;img&gt; tags in the HTML will display as a generic
     * replacement image which your program can then go through and
     * replace with real images.
     * <p/>
     * <p>This uses TagSoup to handle real HTML, including all of the brokenness found in the wild.
     */
    public static Spanned fromHtml(String source, Context context) {
        return fromHtml(source, null, context);
    }

    /**
     * Lazy initialization holder for HTML parser. This class will
     * a) be preloaded by the zygote, or b) not loaded until absolutely
     * necessary.
     */
    private static class HtmlParser {
        private static final HTMLSchema schema = new AztecHtmlSchema();
    }

    /**
     * Returns displayable styled text from the provided HTML string.
     * Any &lt;img&gt; tags in the HTML will use the specified ImageGetter
     * to request a representation of the image (use null if you don't
     * want this) and the specified TagHandler to handle unknown tags
     * (specify null if you don't want this).
     * <p/>
     * <p>This uses TagSoup to handle real HTML, including all of the brokenness found in the wild.
     */
    public static Spanned fromHtml(String source, TagHandler tagHandler, Context context) {
        Parser parser = new Parser();
        try {
            parser.setProperty(Parser.schemaProperty, HtmlParser.schema);
            parser.setFeature(Parser.rootBogonsFeature, false); //allows the unknown tags to exist without root element
        } catch (org.xml.sax.SAXNotRecognizedException e) {
            // Should not happen.
            throw new RuntimeException(e);
        } catch (org.xml.sax.SAXNotSupportedException e) {
            // Should not happen.
            throw new RuntimeException(e);
        }

        HtmlToSpannedConverter converter =
                new HtmlToSpannedConverter(source, tagHandler,
                        parser, context);
        return converter.convert();
    }

//  region Unused Code

    /**
     * Returns an HTML representation of the provided Spanned text. A best effort is
     * made to add HTML tags corresponding to spans. Also note that HTML metacharacters
     * (such as "&lt;" and "&amp;") within the input text are escaped.
     *
     * @param text input text to convert
     * @return string containing input converted to HTML
     */
//    public static String toHtml(Spanned text) {
//        StringBuilder out = new StringBuilder();
//        withinHtml(out, text);
//        return out.toString();
//    }

    /**
     * Returns an HTML escaped representation of the given plain text.
     */
//    public static String escapeHtml(CharSequence text) {
//        StringBuilder out = new StringBuilder();
//        withinStyle(out, text, 0, text.length());
//        return out.toString();
//    }

//    private static void withinHtml(StringBuilder out, Spanned text) {
//        int len = text.length();
//
//        int next;
//        for (int i = 0; i < text.length(); i = next) {
//            next = text.nextSpanTransition(i, len, ParagraphStyle.class);
//            ParagraphStyle[] style = text.getSpans(i, next, ParagraphStyle.class);
//            String elements = " ";
//            boolean needDiv = false;
//
//            for (int j = 0; j < style.length; j++) {
//                if (style[j] instanceof AlignmentSpan) {
//                    Layout.Alignment align =
//                            ((AlignmentSpan) style[j]).getAlignment();
//                    needDiv = true;
//                    if (align == Layout.Alignment.ALIGN_CENTER) {
//                        elements = "align=\"center\" " + elements;
//                    } else if (align == Layout.Alignment.ALIGN_OPPOSITE) {
//                        elements = "align=\"right\" " + elements;
//                    } else {
//                        elements = "align=\"left\" " + elements;
//                    }
//                }
//            }
//            if (needDiv) {
//                out.append("<div ").append(elements).append(">");
//            }
//
//            withinDiv(out, text, i, next);
//
//            if (needDiv) {
//                out.append("</div>");
//            }
//        }
//    }

//    private static void withinDiv(StringBuilder out, Spanned text,
//                                  int start, int end) {
//        int next;
//        for (int i = start; i < end; i = next) {
//            next = text.nextSpanTransition(i, end, QuoteSpan.class);
//            QuoteSpan[] quotes = text.getSpans(i, next, QuoteSpan.class);
//
//            for (QuoteSpan quote : quotes) {
//                out.append("<blockquote>");
//            }
//
//            withinBlockquote(out, text, i, next);
//
//            for (QuoteSpan quote : quotes) {
//                out.append("</blockquote>\n");
//            }
//        }
//    }

//    private static void withinBlockquote(StringBuilder out, Spanned text,
//                                         int start, int end) {
//        out.append(text);
//
//        int next;
//        for (int i = start; i < end; i = next) {
//            next = TextUtils.indexOf(text, '\n', i, end);
//            if (next < 0) {
//                next = end;
//            }
//
//            int nl = 0;
//
//            while (next < end && text.charAt(next) == '\n') {
//                nl++;
//                next++;
//            }
//
//            if (withinParagraph(out, text, i, next - nl, nl, next == end)) {
//                /* Paragraph should be closed */
//                out.append("</p>\n");
//                out.append(text);
//            }
//        }
//
//        out.append("</p>\n");
//    }

    /* Returns true if the caller should close and reopen the paragraph. */
//    private static boolean withinParagraph(StringBuilder out, Spanned text,
//                                           int start, int end, int nl,
//                                           boolean last) {
//        int next;
//        for (int i = start; i < end; i = next) {
//            next = text.nextSpanTransition(i, end, CharacterStyle.class);
//            CharacterStyle[] style = text.getSpans(i, next,
//                    CharacterStyle.class);
//
//            for (int j = 0; j < style.length; j++) {
//                if (style[j] instanceof AztecStyleSpan) {
//                    AztecStyleSpan styleSpan = (AztecStyleSpan)style[j];
//                    out.append("<");
//                    out.append(styleSpan.getStartTag());
//                    out.append(">");
//                }
//                if (style[j] instanceof TypefaceSpan) {
//                    String s = ((TypefaceSpan) style[j]).getFamily();
//
//                    if ("monospace".equals(s)) {
//                        out.append("<tt>");
//                    }
//                }
//                if (style[j] instanceof SuperscriptSpan) {
//                    out.append("<sup>");
//                }
//                if (style[j] instanceof SubscriptSpan) {
//                    out.append("<sub>");
//                }
//                if (style[j] instanceof UnderlineSpan) {
//                    out.append("<u>");
//                }
//                if (style[j] instanceof StrikethroughSpan) {
//                    out.append("<strike>");
//                }
//                if (style[j] instanceof URLSpan) {
//                    out.append("<a href=\"");
//                    out.append(((URLSpan) style[j]).getURL());
//                    out.append("\">");
//                }
//                if (style[j] instanceof ImageSpan) {
//                    out.append("<img src=\"");
//                    out.append(((ImageSpan) style[j]).getSource());
//                    out.append("\">");
//
//                    // Don't output the dummy character underlying the image.
//                    i = next;
//                }
//                if (style[j] instanceof AbsoluteSizeSpan) {
//                    out.append("<font size =\"");
//                    out.append(((AbsoluteSizeSpan) style[j]).getSize() / 6);
//                    out.append("\">");
//                }
//                if (style[j] instanceof ForegroundColorSpan) {
//                    out.append("<font color =\"#");
//                    String color = Integer.toHexString(((ForegroundColorSpan)
//                            style[j]).getForegroundColor() + 0x01000000);
//                    while (color.length() < 6) {
//                        color = "0" + color;
//                    }
//                    out.append(color);
//                    out.append("\">");
//                }
//            }
//
//            withinStyle(out, text, i, next);
//
//            for (int j = style.length - 1; j >= 0; j--) {
//                if (style[j] instanceof ForegroundColorSpan) {
//                    out.append("</font>");
//                }
//                if (style[j] instanceof AbsoluteSizeSpan) {
//                    out.append("</font>");
//                }
//                if (style[j] instanceof URLSpan) {
//                    out.append("</a>");
//                }
//                if (style[j] instanceof StrikethroughSpan) {
//                    out.append("</strike>");
//                }
//                if (style[j] instanceof UnderlineSpan) {
//                    out.append("</u>");
//                }
//                if (style[j] instanceof SubscriptSpan) {
//                    out.append("</sub>");
//                }
//                if (style[j] instanceof SuperscriptSpan) {
//                    out.append("</sup>");
//                }
//                if (style[j] instanceof TypefaceSpan) {
//                    String s = ((TypefaceSpan) style[j]).getFamily();
//
//                    if (s.equals("monospace")) {
//                        out.append("</tt>");
//                    }
//                }
//                if (style[j] instanceof AztecStyleSpan) {
//                    AztecStyleSpan styleSpan = (AztecStyleSpan)style[j];
//                    out.append("<");
//                    out.append(styleSpan.getEndTag());
//                    out.append("/>");
//                }
//            }
//        }
//
//        if (nl == 1) {
//            out.append("<br>\n");
//            return false;
//        } else {
//            for (int i = 2; i < nl; i++) {
//                out.append("<br>");
//            }
//            return !last;
//        }
//    }

//    private static void withinStyle(StringBuilder out, CharSequence text,
//                                    int start, int end) {
//        for (int i = start; i < end; i++) {
//            char c = text.charAt(i);
//
//            if (c == '<') {
//                out.append("&lt;");
//            } else if (c == '>') {
//                out.append("&gt;");
//            } else if (c == '&') {
//                out.append("&amp;");
//            } else if (c >= 0xD800 && c <= 0xDFFF) {
//                if (c < 0xDC00 && i + 1 < end) {
//                    char d = text.charAt(i + 1);
//                    if (d >= 0xDC00 && d <= 0xDFFF) {
//                        i++;
//                        int codepoint = 0x010000 | (int) c - 0xD800 << 10 | (int) d - 0xDC00;
//                        out.append("&#").append(codepoint).append(";");
//                    }
//                }
//            } else if (c > 0x7E || c < ' ') {
//                out.append("&#").append((int) c).append(";");
//            } else if (c == ' ') {
//                while (i + 1 < end && text.charAt(i + 1) == ' ') {
//                    out.append("&nbsp;");
//                    i++;
//                }
//
//                out.append(' ');
//            } else {
//                out.append(c);
//            }
//        }
//    }

//  endregion
    public static StringBuilder stringifyAttributes(Attributes attributes) {
        StringBuilder sb = new StringBuilder();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                // separate attributes by a space character
                sb.append(' ');
                sb.append(attributes.getLocalName(i)).append("=\"").append(attributes.getValue(i)).append('"');
            }
        }
        return sb;
    }

}

class HtmlToSpannedConverter implements ContentHandler, LexicalHandler {
    public int unknownTagLevel = 0;
    public Unknown unknown;

    private static final float[] HEADER_SIZES = {
            1.5f, 1.4f, 1.3f, 1.2f, 1.1f, 1f,
    };

    private String mSource;
    private XMLReader mReader;
    private SpannableStringBuilder spannableStringBuilder;
    private Html.TagHandler tagHandler;
    private Context context;

    public HtmlToSpannedConverter(
            String source, Html.TagHandler tagHandler,
            Parser parser, Context context) {
        mSource = source;
        spannableStringBuilder = new SpannableStringBuilder();
        this.tagHandler = tagHandler;
        mReader = parser;
        this.context = context;
    }

    public Spanned convert() {
        mReader.setContentHandler(this);
        try {
            mReader.setProperty(Parser.lexicalHandlerProperty, this);
            mReader.parse(new InputSource(new StringReader(mSource)));
        } catch (IOException e) {
            // We are reading from a string. There should not be IO problems.
            throw new RuntimeException(e);
        } catch (SAXException e) {
            // TagSoup doesn't throw parse exceptions.
            throw new RuntimeException(e);
        }

        // Fix flags and range for paragraph-type markup.
        Object[] obj = spannableStringBuilder.getSpans(0, spannableStringBuilder.length(), ParagraphStyle.class);
        for (int i = 0; i < obj.length; i++) {
            if (obj[i] instanceof UnknownHtmlSpan || obj[i] instanceof AztecBlockSpan) {
                continue;
            }
            int start = spannableStringBuilder.getSpanStart(obj[i]);
            int end = spannableStringBuilder.getSpanEnd(obj[i]);

            // If the last line of the range is blank, back off by one.
            if (end - 2 >= 0) {
                if (spannableStringBuilder.charAt(end - 1) == '\n' &&
                        spannableStringBuilder.charAt(end - 2) == '\n') {
                    end--;
                }
            }

            if (end == start) {
                spannableStringBuilder.removeSpan(obj[i]);
            } else {
                spannableStringBuilder.setSpan(obj[i], start, end, Spannable.SPAN_PARAGRAPH);
            }
        }

        return spannableStringBuilder;
    }

    private void handleStartTag(String tag, Attributes attributes) {
        if (unknownTagLevel != 0) {
            if (tag.equalsIgnoreCase("aztec_cursor")) {
                handleCursor(spannableStringBuilder);
                return;
            }
            // Swallow opening tag and attributes in current Unknown element
            unknown.rawHtml.append('<').append(tag).append(Html.stringifyAttributes(attributes)).append('>');
            unknownTagLevel += 1;
            return;
        }

        if (tag.equalsIgnoreCase("br")) {
            // We don't need to handle this. TagSoup will ensure that there's a </br> for each <br>
            // so we can safely emite the linebreaks when we handle the close tag.
        } else if (tag.equalsIgnoreCase("aztec_cursor")) {
            handleCursor(spannableStringBuilder);
        } else if (tag.equalsIgnoreCase("strong")) {
            start(spannableStringBuilder, new Bold(attributes));
        } else if (tag.equalsIgnoreCase("b")) {
            start(spannableStringBuilder, new Bold(attributes));
        } else if (tag.equalsIgnoreCase("em")) {
            start(spannableStringBuilder, new Italic(attributes));
        } else if (tag.equalsIgnoreCase("cite")) {
            start(spannableStringBuilder, new Italic(attributes));
        } else if (tag.equalsIgnoreCase("dfn")) {
            start(spannableStringBuilder, new Italic(attributes));
        } else if (tag.equalsIgnoreCase("i")) {
            start(spannableStringBuilder, new Italic(attributes));
        } else if (tag.equalsIgnoreCase("big")) {
            start(spannableStringBuilder, new Big(attributes));
        } else if (tag.equalsIgnoreCase("small")) {
            start(spannableStringBuilder, new Small(attributes));
        } else if (tag.equalsIgnoreCase("font")) {
            start(spannableStringBuilder, new Font(attributes));
        } else if (tag.equalsIgnoreCase("tt")) {
            start(spannableStringBuilder, new Monospace(attributes));
        } else if (tag.equalsIgnoreCase("a")) {
            start(spannableStringBuilder, new Href(attributes));
        } else if (tag.equalsIgnoreCase("u")) {
            start(spannableStringBuilder, new Underline(attributes));
        } else if (tag.equalsIgnoreCase("sup")) {
            start(spannableStringBuilder, new Super(attributes));
        } else if (tag.equalsIgnoreCase("sub")) {
            start(spannableStringBuilder, new Sub(attributes));
        } else if (tag.equalsIgnoreCase("code")) {
            start(spannableStringBuilder, new Code(attributes));
        } else if (tag.equalsIgnoreCase("img")) {
            startImg(spannableStringBuilder, attributes, context);
        } else {
            if (tagHandler != null) {
                boolean tagHandled = tagHandler.handleTag(true, tag, spannableStringBuilder, mReader, attributes);
                if (tagHandled) {
                    return;
                }
            }

            if (!UnknownHtmlSpan.Companion.getKNOWN_TAGS().contains(tag.toLowerCase())) {
                // Initialize a new "Unknown" node
                if (unknownTagLevel == 0) {
                    unknownTagLevel = 1;
                    unknown = new Unknown();
                    unknown.rawHtml = new StringBuilder();
                    unknown.rawHtml.append('<').append(tag).append(Html.stringifyAttributes(attributes)).append('>');
                    start(spannableStringBuilder, unknown);
                }
            }
        }
    }

    private void handleEndTag(String tag) {
        // Unknown tag previously detected
        if (unknownTagLevel != 0) {
            if (tag.equalsIgnoreCase("aztec_cursor")) {
                return; //already handled at start tag
            }
            // Swallow closing tag in current Unknown element
            unknown.rawHtml.append("</").append(tag).append(">");
            unknownTagLevel -= 1;
            if (unknownTagLevel == 0) {
                // Time to wrap up our unknown tag in a Span
                spannableStringBuilder.append("\uFFFC"); // placeholder character
                endUnknown(spannableStringBuilder, unknown.rawHtml, context);
            }
            return;
        }

        if (tag.equalsIgnoreCase("br")) {
            handleBr(spannableStringBuilder);
        } else if (tag.equalsIgnoreCase("strong")) {
            end(spannableStringBuilder, TextFormat.FORMAT_BOLD);
        } else if (tag.equalsIgnoreCase("b")) {
            end(spannableStringBuilder, TextFormat.FORMAT_BOLD);
        } else if (tag.equalsIgnoreCase("em")) {
            end(spannableStringBuilder, TextFormat.FORMAT_ITALIC);
        } else if (tag.equalsIgnoreCase("cite")) {
            end(spannableStringBuilder, TextFormat.FORMAT_ITALIC);
        } else if (tag.equalsIgnoreCase("dfn")) {
            end(spannableStringBuilder, TextFormat.FORMAT_ITALIC);
        } else if (tag.equalsIgnoreCase("i")) {
            end(spannableStringBuilder, TextFormat.FORMAT_ITALIC);
        } else if (tag.equalsIgnoreCase("big")) {
            end(spannableStringBuilder, TextFormat.FORMAT_BIG);
        } else if (tag.equalsIgnoreCase("small")) {
            end(spannableStringBuilder, TextFormat.FORMAT_SMALL);
        } else if (tag.equalsIgnoreCase("font")) {
            endFont(spannableStringBuilder);
        } else if (tag.equalsIgnoreCase("tt")) {
            end(spannableStringBuilder, TextFormat.FORMAT_MONOSPACE);
        } else if (tag.equalsIgnoreCase("a")) {
            end(spannableStringBuilder, TextFormat.FORMAT_LINK);
        } else if (tag.equalsIgnoreCase("u")) {
            end(spannableStringBuilder, TextFormat.FORMAT_UNDERLINED);
        } else if (tag.equalsIgnoreCase("sup")) {
            end(spannableStringBuilder, TextFormat.FORMAT_SUPERSCRIPT);
        } else if (tag.equalsIgnoreCase("sub")) {
            end(spannableStringBuilder, TextFormat.FORMAT_SUBSCRIPT);
        } else if (tag.equalsIgnoreCase("code")) {
            end(spannableStringBuilder, TextFormat.FORMAT_CODE);
        } else if (tagHandler != null) {
            tagHandler.handleTag(false, tag, spannableStringBuilder, mReader, null);
        }
    }

    private static void handleCursor(SpannableStringBuilder text) {
        int start = text.length();

        Object[] unknownSpans = text.getSpans(start, start, Unknown.class);

        if (unknownSpans.length > 0) {
            start = text.getSpanStart(unknownSpans[0]);
        }

        text.setSpan(new AztecCursorSpan(), start, start, Spanned.SPAN_MARK_MARK);
    }

    private static void handleBr(SpannableStringBuilder text) {
        text.append("\n");
    }

    private static Object getLast(Spanned text, Class kind) {
        /*
         * This knows that the last returned object from getSpans()
         * will be the most recently added.
         */
        Object[] objs = text.getSpans(0, text.length(), kind);

        if (objs.length == 0) {
            return null;
        } else {
            return objs[objs.length - 1];
        }
    }

    private static void start(SpannableStringBuilder text, Object mark) {
        int len = text.length();
        text.setSpan(mark, len, len, Spannable.SPAN_MARK_MARK);
    }

    private static void end(SpannableStringBuilder text, TextFormat textFormat) {
        int len = text.length();
        AttributedMarker marker;
        AztecContentSpan newSpan = null;

        switch (textFormat) {
            case FORMAT_BOLD:
                marker = (AttributedMarker) getLast(text, Bold.class);
                if (marker != null) {
                    newSpan = new AztecStyleSpan(Typeface.BOLD, Html.stringifyAttributes(marker.attributes).toString());
                }
                break;
            case FORMAT_ITALIC:
                marker = (AttributedMarker) getLast(text, Italic.class);
                if (marker != null) {
                    newSpan = new AztecStyleSpan(Typeface.ITALIC, Html.stringifyAttributes(marker.attributes).toString());
                }
                break;
            case FORMAT_UNDERLINED:
                marker = (AttributedMarker) getLast(text, Underline.class);
                if (marker != null) {
                    newSpan = new AztecUnderlineSpan(Html.stringifyAttributes(marker.attributes).toString());
                }
                break;
            case FORMAT_LINK:
                marker = (AttributedMarker) getLast(text, Href.class);
                if (marker != null) {
                    newSpan = new AztecURLSpan(marker.attributes.getValue("href"), Html.stringifyAttributes(marker.attributes).toString());
                }
                break;
            case FORMAT_BIG:
                marker = (AttributedMarker) getLast(text, Big.class);
                if (marker != null) {
                    newSpan = new AztecRelativeSizeSpan("big", 1.25f, Html.stringifyAttributes(marker.attributes).toString());
                }
                break;
            case FORMAT_SMALL:
                marker = (AttributedMarker) getLast(text, Small.class);
                if (marker != null) {
                    newSpan = new AztecRelativeSizeSpan("small", 0.8f, Html.stringifyAttributes(marker.attributes).toString());
                }
                break;
            case FORMAT_SUPERSCRIPT:
                marker = (AttributedMarker) getLast(text, Super.class);
                if (marker != null) {
                    newSpan = new AztecSuperscriptSpan(Html.stringifyAttributes(marker.attributes).toString());
                }
                break;
            case FORMAT_SUBSCRIPT:
                marker = (AttributedMarker) getLast(text, Sub.class);
                if (marker != null) {
                    newSpan = new AztecSubscriptSpan(Html.stringifyAttributes(marker.attributes).toString());
                }
                break;
            case FORMAT_MONOSPACE:
                marker = (AttributedMarker) getLast(text, Monospace.class);
                if (marker != null) {
                    newSpan = new AztecTypefaceSpan("tt", "monospace", Html.stringifyAttributes(marker.attributes).toString());
                }
                break;
            case FORMAT_FONT:
                marker = (AttributedMarker) getLast(text, Font.class);
                if (marker != null) {
                    newSpan = new FontSpan(Html.stringifyAttributes(marker.attributes).toString());
                }
                break;
            case FORMAT_CODE:
                marker = (AttributedMarker) getLast(text, Code.class);
                if (marker != null) {
                    newSpan = new AztecCodeSpan(Html.stringifyAttributes(marker.attributes).toString());
                }
                break;
            case FORMAT_PARAGRAPH:
                marker = (AttributedMarker) getLast(text, Paragraph.class);
                if (marker != null) {
                    newSpan = new ParagraphSpan(Html.stringifyAttributes(marker.attributes).toString());
                }
                break;
            default:
                throw new IllegalArgumentException("Style not supported");
        }

        int where = text.getSpanStart(marker);
        text.removeSpan(marker);

        if (where != len && newSpan != null) {
            text.setSpan(newSpan, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private static void startImg(final SpannableStringBuilder text,
                                 Attributes attributes, final Context context) {
        final String src = attributes.getValue("", "src");
        final int start = text.length();

        // TODO: we should some placeholder drawable while loading imges
        Drawable loadingDrawable = ContextCompat.getDrawable(context, android.R.drawable.progress_indeterminate_horizontal);
        loadingDrawable.setBounds(0, 0, loadingDrawable.getIntrinsicWidth(), loadingDrawable.getIntrinsicHeight());
        final ImageSpan imageSpan = new ImageSpan(loadingDrawable, src);

        text.append("\uFFFC");
        text.setSpan(imageSpan, start, text.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static void endFont(SpannableStringBuilder text) {
        int len = text.length();
        Font font = (Font) getLast(text, Font.class);
        int where = text.getSpanStart(font);

        end(text, TextFormat.FORMAT_FONT);

        if (font != null && where != len) {

            String color = font.attributes.getValue("color");

            if (!TextUtils.isEmpty(color)) {
                if (color.startsWith("@")) {
                    Resources res = Resources.getSystem();
                    String name = color.substring(1);
                    int colorRes = res.getIdentifier(name, "color", "android");
                    if (colorRes != 0) {
                        ColorStateList colors = res.getColorStateList(colorRes);
                        text.setSpan(new TextAppearanceSpan(null, 0, 0, colors, null),
                                where, len,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                } else {
                    try {
                        int c = Color.parseColor(color);
                        if (c != -1) {
                            text.setSpan(new ForegroundColorSpan(c | 0xFF000000),
                                    where, len,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    } catch (Exception e) {
                        // unknown color
                    }
                }
            }

            String face = font.attributes.getValue("face");

            if (face != null) {
                text.setSpan(new TypefaceSpan(face), where, len,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static void endUnknown(SpannableStringBuilder text, StringBuilder rawHtml, Context context) {
        int len = text.length();
        Object obj = getLast(text, Unknown.class);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        // TODO: this is temp fix
        if (where > 0 && text.getSpans(where - 1, where - 1, AztecListSpan.class).length > 0) {
            where -= 1;
        }

        if (where != len) {
            // TODO: Replace this dummy drawable with something else
            UnknownHtmlSpan unknownHtmlSpan = new UnknownHtmlSpan(rawHtml, context, android.R.drawable.ic_menu_help);
            text.setSpan(unknownHtmlSpan, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            UnknownClickableSpan unknownClickableSpan = new UnknownClickableSpan(unknownHtmlSpan);
            text.setSpan(unknownClickableSpan, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        handleStartTag(localName, attributes);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        handleEndTag(localName);
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        // If unknown tag, then swallow everything
        if (unknownTagLevel != 0) {
            for (int i = 0; i < length; i++) {
                unknown.rawHtml.append(ch[i + start]);
            }
            return;
        }

        StringBuilder sb = new StringBuilder();

        /*
         * Ignore whitespace that immediately follows other whitespace;
         * newlines count as spaces.
         */

        for (int i = 0; i < length; i++) {
            char c = ch[i + start];

            if (c == ' ' || c == '\n') {
                char pred;
                int len = sb.length();

                if (len == 0) {
                    len = spannableStringBuilder.length();

                    if (len == 0) {
                        pred = '\n';
                    } else {
                        pred = spannableStringBuilder.charAt(len - 1);
                    }
                } else {
                    pred = sb.charAt(len - 1);
                }

                if (pred != ' ' && pred != '\n') {
                    sb.append(' ');
                }
            } else {
                sb.append(c);
            }
        }

        spannableStringBuilder.append(sb);
    }

    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
    }

    public void processingInstruction(String target, String data) throws SAXException {
    }

    public void skippedEntity(String name) throws SAXException {
    }

    @Override
    public void startDTD(String s, String s1, String s2) throws SAXException {

    }

    @Override
    public void endDTD() throws SAXException {

    }

    @Override
    public void startEntity(String s) throws SAXException {

    }

    @Override
    public void endEntity(String s) throws SAXException {

    }

    @Override
    public void startCDATA() throws SAXException {

    }

    @Override
    public void endCDATA() throws SAXException {

    }

    @Override
    public void comment(char[] chars, int start, int length) throws SAXException {
        if (unknownTagLevel != 0) {
            unknown.rawHtml.append("<!--");
            for (int i = 0; i < length; i++) {
                unknown.rawHtml.append(chars[i + start]);
            }
            unknown.rawHtml.append("-->");
            return;
        }

        String comment = new String(chars, start, length);
        int spanStart = spannableStringBuilder.length();
        spannableStringBuilder.append(comment);

        if (comment.equalsIgnoreCase(AztecCommentSpan.Comment.MORE.getHtml())) {
            spannableStringBuilder.setSpan(
                    new AztecCommentSpan(
                            context,
                            context.getResources().getDrawable(R.drawable.img_more)
                    ),
                    spanStart,
                    spannableStringBuilder.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        } else if (comment.equalsIgnoreCase(AztecCommentSpan.Comment.PAGE.getHtml())) {
            spannableStringBuilder.setSpan(
                    new AztecCommentSpan(
                            context,
                            context.getResources().getDrawable(R.drawable.img_page)
                    ),
                    spanStart,
                    spannableStringBuilder.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        } else {
            spannableStringBuilder.setSpan(
                    new CommentSpan(),
                    spanStart,
                    spannableStringBuilder.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }

    private static class Unknown {
        public StringBuilder rawHtml;
    }

    private static class AttributedMarker {
        Attributes attributes;
    }

    private static class Bold extends AttributedMarker {
        Bold(Attributes attributes) {
            this.attributes = attributes;
        }
    }

    private static class Italic extends AttributedMarker {
        Italic(Attributes attributes) {
            this.attributes = attributes;
        }
    }

    private static class Underline extends AttributedMarker {
        Underline(Attributes attributes) {
            this.attributes = attributes;
        }
    }

    private static class Big extends AttributedMarker {
        Big(Attributes attributes) {
            this.attributes = attributes;
        }
    }

    private static class Small extends AttributedMarker {
        Small(Attributes attributes) {
            this.attributes = attributes;
        }
    }

    private static class Monospace extends AttributedMarker {
        Monospace(Attributes attributes) {
            this.attributes = attributes;
        }
    }

    private static class Super extends AttributedMarker {
        Super(Attributes attributes) {
            this.attributes = attributes;
        }
    }

    private static class Sub extends AttributedMarker {
        Sub(Attributes attributes) {
            this.attributes = attributes;
        }
    }

    private static class Font extends AttributedMarker {
        Font(Attributes attributes) {
            this.attributes = attributes;
        }
    }

    private static class Code extends AttributedMarker {
        Code(Attributes attributes) {
            this.attributes = attributes;
        }
    }

    private static class Href extends AttributedMarker {
        Href(Attributes attributes) {
            this.attributes = attributes;
        }
    }

    private static class Paragraph extends AttributedMarker {
        Paragraph(Attributes attributes) {
            this.attributes = attributes;
        }
    }
}