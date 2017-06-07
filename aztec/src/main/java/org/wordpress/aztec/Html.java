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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ParagraphStyle;
import android.text.style.TextAppearanceSpan;
import android.text.style.TypefaceSpan;

import org.ccil.cowan.tagsoup.HTMLSchema;
import org.ccil.cowan.tagsoup.Parser;
import org.wordpress.aztec.AztecText.OnImageTappedListener;
import org.wordpress.aztec.AztecText.OnVideoTappedListener;
import org.wordpress.aztec.spans.AztecBlockSpan;
import org.wordpress.aztec.spans.AztecCodeSpan;
import org.wordpress.aztec.spans.AztecCommentSpan;
import org.wordpress.aztec.spans.AztecCursorSpan;
import org.wordpress.aztec.spans.AztecInlineSpan;
import org.wordpress.aztec.spans.AztecMediaSpan;
import org.wordpress.aztec.spans.AztecRelativeSizeBigSpan;
import org.wordpress.aztec.spans.AztecRelativeSizeSmallSpan;
import org.wordpress.aztec.spans.AztecStyleBoldSpan;
import org.wordpress.aztec.spans.AztecStyleItalicSpan;
import org.wordpress.aztec.spans.AztecSubscriptSpan;
import org.wordpress.aztec.spans.AztecSuperscriptSpan;
import org.wordpress.aztec.spans.AztecTypefaceMonospaceSpan;
import org.wordpress.aztec.spans.AztecURLSpan;
import org.wordpress.aztec.spans.AztecUnderlineSpan;
import org.wordpress.aztec.spans.CommentSpan;
import org.wordpress.aztec.spans.FontSpan;
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
        void loadImage(String source, Html.ImageGetter.Callbacks callbacks, int maxWidth);

        interface Callbacks {
            void onImageFailed();

            void onImageLoaded(Drawable drawable);

            void onImageLoading(Drawable drawable);
        }
    }

    public interface VideoThumbnailGetter {

        void loadVideoThumbnail(String source, Html.VideoThumbnailGetter.Callbacks callbacks, int maxWidth);

        interface Callbacks {
            void onThumbnailFailed();

            void onThumbnailLoaded(Drawable drawable);

            void onThumbnailLoading(Drawable drawable);
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
        boolean handleTag(boolean opening, String tag, Editable output,
                          Context context, Attributes attributes, int nestingLevel);
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
    public static Spanned fromHtml(String source, OnImageTappedListener onImageTappedListener, OnVideoTappedListener onVideoTappedListener,
                                   UnknownHtmlSpan.OnUnknownHtmlClickListener onUnknownHtmlClickListener, Context context) {
        return fromHtml(source, null, onUnknownHtmlClickListener, context);
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
    public static Spanned fromHtml(String source, TagHandler tagHandler,
                                   UnknownHtmlSpan.OnUnknownHtmlClickListener onUnknownHtmlClickListener,
                                   Context context) {

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
                new HtmlToSpannedConverter(source, tagHandler, parser, onUnknownHtmlClickListener, context);

        return converter.convert();
    }

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
    private int nestingLevel = 0;

    public int unknownTagLevel = 0;
    public Unknown unknown;
    private boolean insidePreTag = false;
    private boolean insideCodeTag = false;

    private String mSource;
    private UnknownHtmlSpan.OnUnknownHtmlClickListener onUnknownHtmlClickListener;
    private XMLReader mReader;
    private SpannableStringBuilder spannableStringBuilder;
    private Html.TagHandler tagHandler;
    private Context context;

    public HtmlToSpannedConverter(
            String source, Html.TagHandler tagHandler,
            Parser parser, UnknownHtmlSpan.OnUnknownHtmlClickListener onUnknownHtmlClickListener, Context context) {
        mSource = source;
        spannableStringBuilder = new SpannableStringBuilder();
        this.tagHandler = tagHandler;
        mReader = parser;
        this.context = context;
        this.onUnknownHtmlClickListener = onUnknownHtmlClickListener;
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
        Object[] paragraphs = spannableStringBuilder.getSpans(0, spannableStringBuilder.length(), ParagraphStyle.class);
        for (Object paragraph : paragraphs) {
            if (paragraph instanceof UnknownHtmlSpan || paragraph instanceof AztecBlockSpan || paragraph instanceof AztecMediaSpan) {
                continue;
            }
            int start = spannableStringBuilder.getSpanStart(paragraph);
            int end = spannableStringBuilder.getSpanEnd(paragraph);

            // If the last line of the range is blank, back off by one.
            if (end - 2 >= 0) {
                if (spannableStringBuilder.charAt(end - 1) == '\n' &&
                        spannableStringBuilder.charAt(end - 2) == '\n') {
                    end--;
                }
            }

            if (end == start) {
                spannableStringBuilder.removeSpan(paragraph);
            } else {
                spannableStringBuilder.setSpan(paragraph, start, end, Spannable.SPAN_PARAGRAPH);
            }
        }

        return spannableStringBuilder;
    }

    private void handleStartTag(String tag, Attributes attributes, int nestingLevel) {
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
            start(spannableStringBuilder, TextFormat.FORMAT_BOLD, attributes);
        } else if (tag.equalsIgnoreCase("b")) {
            start(spannableStringBuilder, TextFormat.FORMAT_BOLD, attributes);
        } else if (tag.equalsIgnoreCase("em")) {
            start(spannableStringBuilder, TextFormat.FORMAT_ITALIC, attributes);
        } else if (tag.equalsIgnoreCase("cite")) {
            start(spannableStringBuilder, TextFormat.FORMAT_ITALIC, attributes);
        } else if (tag.equalsIgnoreCase("dfn")) {
            start(spannableStringBuilder, TextFormat.FORMAT_ITALIC, attributes);
        } else if (tag.equalsIgnoreCase("i")) {
            start(spannableStringBuilder, TextFormat.FORMAT_ITALIC, attributes);
        } else if (tag.equalsIgnoreCase("big")) {
            start(spannableStringBuilder, TextFormat.FORMAT_BIG, attributes);
        } else if (tag.equalsIgnoreCase("small")) {
            start(spannableStringBuilder, TextFormat.FORMAT_SMALL, attributes);
        } else if (tag.equalsIgnoreCase("font")) {
            start(spannableStringBuilder, TextFormat.FORMAT_FONT, attributes);
        } else if (tag.equalsIgnoreCase("tt")) {
            start(spannableStringBuilder, TextFormat.FORMAT_MONOSPACE, attributes);
        } else if (tag.equalsIgnoreCase("a")) {
            start(spannableStringBuilder, TextFormat.FORMAT_LINK, attributes);
        } else if (tag.equalsIgnoreCase("u")) {
            start(spannableStringBuilder, TextFormat.FORMAT_UNDERLINE, attributes);
        } else if (tag.equalsIgnoreCase("sup")) {
            start(spannableStringBuilder, TextFormat.FORMAT_SUPERSCRIPT, attributes);
        } else if (tag.equalsIgnoreCase("sub")) {
            start(spannableStringBuilder, TextFormat.FORMAT_SUBSCRIPT, attributes);
        } else if (tag.equalsIgnoreCase("code")) {
            insideCodeTag = true;
            start(spannableStringBuilder, TextFormat.FORMAT_CODE, attributes);
        } else {
            if (tagHandler != null) {
                if (tag.equalsIgnoreCase("pre")) {
                    insidePreTag = true;
                }

                boolean tagHandled = tagHandler.handleTag(true, tag, spannableStringBuilder,
                        context, attributes, nestingLevel);

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
                    spannableStringBuilder.setSpan(unknown, spannableStringBuilder.length(),
                            spannableStringBuilder.length(), Spannable.SPAN_MARK_MARK);
                }
            }
        }
    }

    private void handleEndTag(String tag, int nestingLevel) {
        // Unknown tag previously detected
        if (unknownTagLevel != 0) {
            if (tag.equalsIgnoreCase("aztec_cursor")) {
                return; //already handled at start tag
            } else if (tag.equalsIgnoreCase("br")) {
                unknownTagLevel -= 1;
                return; //already handled at start tag
            }
            // Swallow closing tag in current Unknown element
            unknown.rawHtml.append("</").append(tag).append(">");
            unknownTagLevel -= 1;
            if (unknownTagLevel == 0) {
                // Time to wrap up our unknown tag in a Span
                spannableStringBuilder.append("\uFFFC"); // placeholder character
                endUnknown(spannableStringBuilder, nestingLevel, unknown.rawHtml, context, onUnknownHtmlClickListener);
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
            end(spannableStringBuilder, TextFormat.FORMAT_UNDERLINE);
        } else if (tag.equalsIgnoreCase("sup")) {
            end(spannableStringBuilder, TextFormat.FORMAT_SUPERSCRIPT);
        } else if (tag.equalsIgnoreCase("sub")) {
            end(spannableStringBuilder, TextFormat.FORMAT_SUBSCRIPT);
        } else if (tag.equalsIgnoreCase("code")) {
            insideCodeTag = false;
            end(spannableStringBuilder, TextFormat.FORMAT_CODE);
        } else if (tagHandler != null) {
            if (tag.equalsIgnoreCase("pre")) {
                insidePreTag = false;
            }
            tagHandler.handleTag(false, tag, spannableStringBuilder, context,
                    new AztecAttributes(), nestingLevel);
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

    private static void start(SpannableStringBuilder text, TextFormat textFormat, Attributes attrs) {
        final AztecAttributes attributes = new AztecAttributes(attrs);
        AztecInlineSpan newSpan;

        switch (textFormat) {
            case FORMAT_BOLD:
                newSpan = new AztecStyleBoldSpan(attributes);
                break;
            case FORMAT_ITALIC:
                newSpan = new AztecStyleItalicSpan(attributes);
                break;
            case FORMAT_UNDERLINE:
                newSpan = new AztecUnderlineSpan(attributes);
                break;
            case FORMAT_LINK:
                String url = attributes.hasAttribute("href") ? attributes.getValue("href") : "";
                newSpan = new AztecURLSpan(url, attributes);
                break;
            case FORMAT_BIG:
                newSpan = new AztecRelativeSizeBigSpan(attributes);
                break;
            case FORMAT_SMALL:
                newSpan = new AztecRelativeSizeSmallSpan(attributes);
                break;
            case FORMAT_SUPERSCRIPT:
                newSpan = new AztecSuperscriptSpan(attributes);
                break;
            case FORMAT_SUBSCRIPT:
                newSpan = new AztecSubscriptSpan(attributes);
                break;
            case FORMAT_MONOSPACE:
                newSpan = new AztecTypefaceMonospaceSpan(attributes);
                break;
            case FORMAT_FONT:
                newSpan = new FontSpan(attributes);
                break;
            case FORMAT_CODE:
                newSpan = new AztecCodeSpan(attributes);
                break;
            default:
                throw new IllegalArgumentException("Style not supported");
        }

        int len = text.length();
        text.setSpan(newSpan, len, len, Spannable.SPAN_MARK_MARK);
    }

    private static void end(SpannableStringBuilder text, TextFormat textFormat) {
        AztecInlineSpan span;

        switch (textFormat) {
            case FORMAT_BOLD:
                span = (AztecStyleBoldSpan) getLast(text, AztecStyleBoldSpan.class);
                break;
            case FORMAT_ITALIC:
                span = (AztecStyleItalicSpan) getLast(text, AztecStyleItalicSpan.class);
                break;
            case FORMAT_UNDERLINE:
                span = (AztecUnderlineSpan) getLast(text, AztecUnderlineSpan.class);
                break;
            case FORMAT_LINK:
                span = (AztecURLSpan) getLast(text, AztecURLSpan.class);
                break;
            case FORMAT_BIG:
                span = (AztecRelativeSizeBigSpan) getLast(text, AztecRelativeSizeBigSpan.class);
                break;
            case FORMAT_SMALL:
                span = (AztecRelativeSizeSmallSpan) getLast(text, AztecRelativeSizeSmallSpan.class);
                break;
            case FORMAT_SUPERSCRIPT:
                span = (AztecSuperscriptSpan) getLast(text, AztecSuperscriptSpan.class);
                break;
            case FORMAT_SUBSCRIPT:
                span = (AztecSubscriptSpan) getLast(text, AztecSubscriptSpan.class);
                break;
            case FORMAT_MONOSPACE:
                span = (AztecTypefaceMonospaceSpan) getLast(text, AztecTypefaceMonospaceSpan.class);
                break;
            case FORMAT_FONT:
                span = (FontSpan) getLast(text, FontSpan.class);
                break;
            case FORMAT_CODE:
                span = (AztecCodeSpan) getLast(text, AztecCodeSpan.class);
                break;
            default:
                throw new IllegalArgumentException("Style not supported");
        }

        int where = text.getSpanStart(span);
        text.setSpan(span, where, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static void endFont(SpannableStringBuilder text) {
        int len = text.length();
        FontSpan font = (FontSpan) getLast(text, FontSpan.class);
        int where = text.getSpanStart(font);

        end(text, TextFormat.FORMAT_FONT);

        if (font != null && where != len) {

            String color = font.getAttributes().getValue("color");

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

            String face = font.getAttributes().getValue("face");

            if (face != null) {
                text.setSpan(new TypefaceSpan(face), where, len,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static void endUnknown(SpannableStringBuilder text, int nestingLevel, StringBuilder rawHtml, Context context,
                                   UnknownHtmlSpan.OnUnknownHtmlClickListener onUnknownHtmlClickListener) {
        int len = text.length();
        Object obj = getLast(text, Unknown.class);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        if (where != len) {
            // TODO: Replace this dummy drawable with something else
            UnknownHtmlSpan unknownHtmlSpan = new UnknownHtmlSpan(nestingLevel, rawHtml, context, android.R.drawable.ic_menu_help, onUnknownHtmlClickListener);
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
        nestingLevel++;

        handleStartTag(localName, attributes, nestingLevel);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        handleEndTag(localName, nestingLevel);

        nestingLevel--;
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
         * Ignore whitespace that immediately follows other whitespace, unless in pre or comment tags;
         * newlines count as spaces.
         */

        for (int i = 0; i < length; i++) {
            char c = ch[i + start];

            if (!insidePreTag && !insideCodeTag && c == ' ' || c == '\n') {
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

        if (comment.equalsIgnoreCase(AztecCommentSpan.Comment.MORE.getHtml())) {
            spannableStringBuilder.append(Constants.INSTANCE.getMAGIC_CHAR());
            spannableStringBuilder.setSpan(
                    new AztecCommentSpan(
                            comment,
                            context,
                            context.getResources().getDrawable(R.drawable.img_more),
                            nestingLevel
                    ),
                    spanStart,
                    spannableStringBuilder.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        } else if (comment.equalsIgnoreCase(AztecCommentSpan.Comment.PAGE.getHtml())) {
            spannableStringBuilder.append(Constants.INSTANCE.getMAGIC_CHAR());
            spannableStringBuilder.setSpan(
                    new AztecCommentSpan(
                            comment,
                            context,
                            context.getResources().getDrawable(R.drawable.img_page),
                            nestingLevel
                    ),
                    spanStart,
                    spannableStringBuilder.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        } else {
            spannableStringBuilder.append(comment);
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
}
