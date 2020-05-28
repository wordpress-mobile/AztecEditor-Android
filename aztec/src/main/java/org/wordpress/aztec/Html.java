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
import org.wordpress.aztec.plugins.IAztecPlugin;
import org.wordpress.aztec.plugins.html2visual.IHtmlCommentHandler;
import org.wordpress.aztec.plugins.html2visual.IHtmlContentHandler;
import org.wordpress.aztec.plugins.html2visual.IHtmlPreprocessor;
import org.wordpress.aztec.plugins.html2visual.IHtmlTextHandler;
import org.wordpress.aztec.spans.AztecCodeSpan;
import org.wordpress.aztec.spans.AztecCursorSpan;
import org.wordpress.aztec.spans.AztecMediaSpan;
import org.wordpress.aztec.spans.AztecRelativeSizeBigSpan;
import org.wordpress.aztec.spans.AztecRelativeSizeSmallSpan;
import org.wordpress.aztec.spans.AztecStyleBoldSpan;
import org.wordpress.aztec.spans.AztecStyleCiteSpan;
import org.wordpress.aztec.spans.AztecStyleItalicSpan;
import org.wordpress.aztec.spans.AztecStyleEmphasisSpan;
import org.wordpress.aztec.spans.AztecStyleStrongSpan;
import org.wordpress.aztec.spans.AztecSubscriptSpan;
import org.wordpress.aztec.spans.AztecSuperscriptSpan;
import org.wordpress.aztec.spans.AztecTypefaceMonospaceSpan;
import org.wordpress.aztec.spans.AztecURLSpan;
import org.wordpress.aztec.spans.AztecUnderlineSpan;
import org.wordpress.aztec.spans.CommentSpan;
import org.wordpress.aztec.spans.FontSpan;
import org.wordpress.aztec.spans.IAztecInlineSpan;
import org.wordpress.aztec.spans.IAztecParagraphStyle;
import org.wordpress.aztec.spans.UnknownClickableSpan;
import org.wordpress.aztec.spans.UnknownHtmlSpan;
import org.wordpress.aztec.util.CleaningUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

import static org.wordpress.aztec.util.ExtensionsKt.getLast;

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

        void loadImage(String source, Html.ImageGetter.Callbacks callbacks, int maxWidth, int minWidth);

        interface Callbacks {
            void onImageFailed();

            void onImageLoaded(Drawable drawable);

            void onImageLoading(Drawable drawable);
        }
    }

    public interface VideoThumbnailGetter {
        void loadVideoThumbnail(String source, Html.VideoThumbnailGetter.Callbacks callbacks, int maxWidth);

        void loadVideoThumbnail(String source, Html.VideoThumbnailGetter.Callbacks callbacks, int maxWidth, int minWidth);

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
    public static Spanned fromHtml(String source,
                                   Context context,
                                   List<IAztecPlugin> plugins,
                                   List<String> ignoredTags) {
        return fromHtml(source, null, context, plugins, ignoredTags, true);
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
                                   Context context, List<IAztecPlugin> plugins,
                                   List<String> ignoredTags, boolean shouldIgnoreWhitespace) {

        Parser parser = new Parser();
        try {
            parser.setProperty(Parser.schemaProperty, HtmlParser.schema);
            parser.setFeature(Parser.rootBogonsFeature, false); // allows the unknown tags to exist without root element
        } catch (org.xml.sax.SAXNotRecognizedException e) {
            // Should not happen.
            throw new RuntimeException(e);
        } catch (org.xml.sax.SAXNotSupportedException e) {
            // Should not happen.
            throw new RuntimeException(e);
        }

        source = CleaningUtils.cleanNestedBoldTags(source);

        source = preprocessSource(source, plugins);

        HtmlToSpannedConverter converter =
                new HtmlToSpannedConverter(source,
                        tagHandler,
                        parser,
                        context,
                        plugins,
                        ignoredTags,
                        shouldIgnoreWhitespace);

        return converter.convert();
    }

    private static String preprocessSource(String source, List<IAztecPlugin> plugins) {
        for (IAztecPlugin plugin : plugins) {
            if (plugin instanceof IHtmlPreprocessor) {
                source = ((IHtmlPreprocessor)plugin).beforeHtmlProcessed(source);
            }
        }
        return source;
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

class HtmlToSpannedConverter implements org.xml.sax.ContentHandler, LexicalHandler {
    private int nestingLevel = 0;

    private int contentHandlerLevel = 0;
    private IHtmlContentHandler contentHandlerPlugin;
    private ContentHandler content;
    private boolean insidePreTag = false;
    private boolean insideCodeTag = false;

    private String source;
    private List<IAztecPlugin> plugins;
    private XMLReader reader;
    private SpannableStringBuilder spannableStringBuilder;
    private Html.TagHandler tagHandler;
    private Context context;
    private List<String> ignoredTags;
    private boolean shouldIgnoreWhitespace;

    public HtmlToSpannedConverter(
            String source, Html.TagHandler tagHandler,
            Parser parser,
            Context context, List<IAztecPlugin> plugins,
            List<String> ignoredTags, boolean shouldIgnoreWhitespace) {
        this.source = source;
        this.plugins = plugins;
        this.spannableStringBuilder = new SpannableStringBuilder();
        this.tagHandler = tagHandler;
        this.reader = parser;
        this.context = context;
        this.ignoredTags = ignoredTags;
        this.shouldIgnoreWhitespace = shouldIgnoreWhitespace;
    }

    public Spanned convert() {
        reader.setContentHandler(this);
        try {
            reader.setProperty(Parser.lexicalHandlerProperty, this);
            reader.parse(new InputSource(new StringReader(source)));
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
            if (paragraph instanceof UnknownHtmlSpan || paragraph instanceof IAztecParagraphStyle || paragraph instanceof AztecMediaSpan) {
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
        if (handleContentStart(tag, attributes)) {
            return; // content was handled
        }

        if (tagHandler != null) {
            if (tag.equalsIgnoreCase("pre")) {
                insidePreTag = true;
            }

            if (tagHandler.handleTag(true, tag, spannableStringBuilder,
                    context, attributes, nestingLevel)) {
                return; // tag was handled
            }
        }

        // noinspection StatementWithEmptyBody
        if (tag.equalsIgnoreCase("br")) {
            // We don't need to handle this. TagSoup will ensure that there's a </br> for each <br>
            // so we can safely emite the linebreaks when we handle the close tag.
        } else if (tag.equalsIgnoreCase("aztec_cursor")) {
            handleCursor(spannableStringBuilder);
        } else if (tag.equalsIgnoreCase("strong")) {
            start(spannableStringBuilder, AztecTextFormat.FORMAT_STRONG, attributes);
        } else if (tag.equalsIgnoreCase("b")) {
            start(spannableStringBuilder, AztecTextFormat.FORMAT_BOLD, attributes);
        } else if (tag.equalsIgnoreCase("em")) {
            start(spannableStringBuilder, AztecTextFormat.FORMAT_EMPHASIS, attributes);
        } else if (tag.equalsIgnoreCase("cite")) {
            start(spannableStringBuilder, AztecTextFormat.FORMAT_CITE, attributes);
        } else if (tag.equalsIgnoreCase("dfn")) {
            start(spannableStringBuilder, AztecTextFormat.FORMAT_ITALIC, attributes);
        } else if (tag.equalsIgnoreCase("i")) {
            start(spannableStringBuilder, AztecTextFormat.FORMAT_ITALIC, attributes);
        } else if (tag.equalsIgnoreCase("big")) {
            start(spannableStringBuilder, AztecTextFormat.FORMAT_BIG, attributes);
        } else if (tag.equalsIgnoreCase("small")) {
            start(spannableStringBuilder, AztecTextFormat.FORMAT_SMALL, attributes);
        } else if (tag.equalsIgnoreCase("font")) {
            start(spannableStringBuilder, AztecTextFormat.FORMAT_FONT, attributes);
        } else if (tag.equalsIgnoreCase("tt")) {
            start(spannableStringBuilder, AztecTextFormat.FORMAT_MONOSPACE, attributes);
        } else if (tag.equalsIgnoreCase("a")) {
            start(spannableStringBuilder, AztecTextFormat.FORMAT_LINK, attributes);
        } else if (tag.equalsIgnoreCase("u")) {
            start(spannableStringBuilder, AztecTextFormat.FORMAT_UNDERLINE, attributes);
        } else if (tag.equalsIgnoreCase("sup")) {
            start(spannableStringBuilder, AztecTextFormat.FORMAT_SUPERSCRIPT, attributes);
        } else if (tag.equalsIgnoreCase("sub")) {
            start(spannableStringBuilder, AztecTextFormat.FORMAT_SUBSCRIPT, attributes);
        } else if (tag.equalsIgnoreCase("code")) {
            insideCodeTag = true;
            start(spannableStringBuilder, AztecTextFormat.FORMAT_CODE, attributes);
        } else if (!UnknownHtmlSpan.Companion.getKNOWN_TAGS().contains(tag.toLowerCase())) {
            // Initialize a new "Unknown" node
            if (contentHandlerLevel == 0) {
                startHandlingContent(tag, attributes);
            }
        }
    }

    private boolean handleContentStart(String tag, Attributes attributes) {
        if (contentHandlerLevel != 0) {
            if (tag.equalsIgnoreCase("aztec_cursor")) {
                handleCursor(spannableStringBuilder);
                return true;
            }

            // Swallow opening tag and attributes in current Unknown element
            content.rawHtml.append('<').append(tag).append(Html.stringifyAttributes(attributes)).append('>');
            contentHandlerLevel += 1;
            return true;
        } else {
            for (IAztecPlugin plugin : plugins) {
                if (plugin instanceof IHtmlContentHandler) {
                    IHtmlContentHandler contentHandler = ((IHtmlContentHandler)plugin);
                    if (contentHandler.canHandleTag(tag.toLowerCase())) {
                        contentHandlerPlugin = contentHandler;
                        startHandlingContent(tag, attributes);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void startHandlingContent(String tag, Attributes attributes) {
        contentHandlerLevel = 1;
        content = new ContentHandler();
        content.rawHtml = new StringBuilder();
        content.rawHtml.append('<').append(tag).append(Html.stringifyAttributes(attributes)).append('>');

        spannableStringBuilder.setSpan(content, spannableStringBuilder.length(),
                spannableStringBuilder.length(), Spannable.SPAN_MARK_MARK);
    }

    private void handleEndTag(String tag, int nestingLevel) {
        if (handleContentEnd(tag, nestingLevel)) {
            return;
        }

        if (tagHandler != null) {
            if (tag.equalsIgnoreCase("pre")) {
                insidePreTag = false;
            }

            if (tagHandler.handleTag(false, tag, spannableStringBuilder, context,
                    new AztecAttributes(), nestingLevel)) {
                return; // tag was handled
            }
        }

        if (tag.equalsIgnoreCase("br")) {
            handleBr(spannableStringBuilder);
        } else if (tag.equalsIgnoreCase("strong")) {
            end(spannableStringBuilder, AztecTextFormat.FORMAT_STRONG);
        } else if (tag.equalsIgnoreCase("b")) {
            end(spannableStringBuilder, AztecTextFormat.FORMAT_BOLD);
        } else if (tag.equalsIgnoreCase("em")) {
            end(spannableStringBuilder, AztecTextFormat.FORMAT_EMPHASIS);
        } else if (tag.equalsIgnoreCase("cite")) {
            end(spannableStringBuilder, AztecTextFormat.FORMAT_CITE);
        } else if (tag.equalsIgnoreCase("dfn")) {
            end(spannableStringBuilder, AztecTextFormat.FORMAT_ITALIC);
        } else if (tag.equalsIgnoreCase("i")) {
            end(spannableStringBuilder, AztecTextFormat.FORMAT_ITALIC);
        } else if (tag.equalsIgnoreCase("big")) {
            end(spannableStringBuilder, AztecTextFormat.FORMAT_BIG);
        } else if (tag.equalsIgnoreCase("small")) {
            end(spannableStringBuilder, AztecTextFormat.FORMAT_SMALL);
        } else if (tag.equalsIgnoreCase("font")) {
            endFont(spannableStringBuilder);
        } else if (tag.equalsIgnoreCase("tt")) {
            end(spannableStringBuilder, AztecTextFormat.FORMAT_MONOSPACE);
        } else if (tag.equalsIgnoreCase("a")) {
            end(spannableStringBuilder, AztecTextFormat.FORMAT_LINK);
        } else if (tag.equalsIgnoreCase("u")) {
            end(spannableStringBuilder, AztecTextFormat.FORMAT_UNDERLINE);
        } else if (tag.equalsIgnoreCase("sup")) {
            end(spannableStringBuilder, AztecTextFormat.FORMAT_SUPERSCRIPT);
        } else if (tag.equalsIgnoreCase("sub")) {
            end(spannableStringBuilder, AztecTextFormat.FORMAT_SUBSCRIPT);
        } else if (tag.equalsIgnoreCase("code")) {
            insideCodeTag = false;
            end(spannableStringBuilder, AztecTextFormat.FORMAT_CODE);
        }
    }

    private boolean handleContentEnd(String tag, int nestingLevel) {
        // Content handler tag previously detected
        if (contentHandlerLevel != 0) {
            if (tag.equalsIgnoreCase("aztec_cursor")) {
                return true;
            } else if (tag.equalsIgnoreCase("br")) {
                contentHandlerLevel -= 1;
                return true;
            }
            contentHandlerLevel -= 1;

            // Unknown/handled content, swallow closing tag in current Unknown element
            content.rawHtml.append("</").append(tag).append(">");

            if (contentHandlerPlugin == null && contentHandlerLevel == 0) {
                // Time to wrap up our content handler tag in a Span
                spannableStringBuilder.append(Constants.INSTANCE.getIMG_CHAR()); // placeholder character
                endContentHandler(spannableStringBuilder, nestingLevel, content, context);
            } else if (contentHandlerLevel == 0) {
                // Content is handled by a plugin
                endPluginContentHandler(spannableStringBuilder, nestingLevel, content);
            }
            return true;
        }
        return false;
    }

    private static void handleCursor(SpannableStringBuilder text) {
        int start = text.length();

        Object[] unknownSpans = text.getSpans(start, start, ContentHandler.class);

        if (unknownSpans.length > 0) {
            start = text.getSpanStart(unknownSpans[0]);
        }

        text.setSpan(new AztecCursorSpan(), start, start, Spanned.SPAN_MARK_MARK);
    }

    private static void handleBr(SpannableStringBuilder text) {
        text.append("\n");
    }


    private static void start(SpannableStringBuilder text, AztecTextFormat textFormat, Attributes attrs) {
        final AztecAttributes attributes = new AztecAttributes(attrs);
        IAztecInlineSpan newSpan;

        switch (textFormat) {
            case FORMAT_BOLD:
                newSpan = new AztecStyleBoldSpan(attributes);
                break;
            case FORMAT_STRONG:
                newSpan = new AztecStyleStrongSpan(attributes);
                break;
            case FORMAT_ITALIC:
                newSpan = new AztecStyleItalicSpan(attributes);
                break;
            case FORMAT_EMPHASIS:
                newSpan = new AztecStyleEmphasisSpan(attributes);
                break;
            case FORMAT_CITE:
                newSpan = new AztecStyleCiteSpan(attributes);
                break;
            case FORMAT_UNDERLINE:
                newSpan = new AztecUnderlineSpan(false, attributes);
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

    private static void end(SpannableStringBuilder text, AztecTextFormat textFormat) {
        IAztecInlineSpan span;

        switch (textFormat) {
            case FORMAT_BOLD:
                span = (AztecStyleBoldSpan) getLast(text, AztecStyleBoldSpan.class);
                break;
            case FORMAT_STRONG:
                span = (AztecStyleStrongSpan) getLast(text, AztecStyleStrongSpan.class);
                break;
            case FORMAT_ITALIC:
                span = (AztecStyleItalicSpan) getLast(text, AztecStyleItalicSpan.class);
                break;
            case FORMAT_EMPHASIS:
                span = (AztecStyleEmphasisSpan) getLast(text, AztecStyleEmphasisSpan.class);
                break;
            case FORMAT_CITE:
                span = (AztecStyleCiteSpan) getLast(text, AztecStyleCiteSpan.class);
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

        // Process HTML style attribute
        span.applyInlineStyleAttributes(text, where, text.length());
    }

    private static void endFont(SpannableStringBuilder text) {
        int len = text.length();
        FontSpan font = (FontSpan) getLast(text, FontSpan.class);
        int where = text.getSpanStart(font);

        end(text, AztecTextFormat.FORMAT_FONT);

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

    private void endPluginContentHandler(SpannableStringBuilder text, int nestingLevel, ContentHandler unknown) {
        text.removeSpan(unknown);

        contentHandlerPlugin.handleContent(unknown.rawHtml.toString(), text, nestingLevel);
        contentHandlerPlugin = null;
    }

    private void endContentHandler(SpannableStringBuilder text, int nestingLevel, ContentHandler unknown, Context context) {
        int len = text.length();
        int where = text.getSpanStart(unknown);

        text.removeSpan(unknown);

        if (where != len) {
            // TODO: Replace this dummy drawable with something else
            UnknownHtmlSpan unknownHtmlSpan = new UnknownHtmlSpan(nestingLevel, unknown.rawHtml, context, android.R.drawable.ic_menu_help);
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
        if (!ignoredTags.contains(localName)) {
            nestingLevel++;

            handleStartTag(localName, attributes, nestingLevel);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (!ignoredTags.contains(localName)) {
            handleEndTag(localName, nestingLevel);

            nestingLevel--;
        }
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        // If unknown tag, then swallow everything
        if (contentHandlerLevel != 0) {
            for (int i = 0; i < length; i++) {
                content.rawHtml.append(ch[i + start]);
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

                if (c == ' ' && !shouldIgnoreWhitespace) {
                    sb.append(c);
                    continue;
                }

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

        processTextHandlerPlugins(sb);

        spannableStringBuilder.append(sb);
    }

    private void processTextHandlerPlugins(StringBuilder sb) {
        if (plugins != null) {
            for (IAztecPlugin plugin : plugins) {
                if (plugin instanceof IHtmlTextHandler) {
                    IHtmlTextHandler textPlugin = (IHtmlTextHandler)plugin;
                    Pattern pattern = Pattern.compile(textPlugin.getPattern());
                    Matcher matcher = pattern.matcher(sb.toString());

                    while (matcher.find()) {
                        boolean textHandled = textPlugin.onHtmlTextMatch(matcher.group(), spannableStringBuilder, nestingLevel);
                        if (textHandled) {
                            sb.delete(matcher.start(), matcher.end());
                            matcher = pattern.matcher(sb.toString());
                        }
                    }
                }
            }
        }
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
        if (contentHandlerLevel != 0) {
            content.rawHtml.append("<!--");
            for (int i = 0; i < length; i++) {
                content.rawHtml.append(chars[i + start]);
            }
            content.rawHtml.append("-->");
            return;
        }

        String comment = new String(chars, start, length);
        int spanStart = spannableStringBuilder.length();

        boolean wasCommentHandled = processCommentHandlerPlugins(comment);

        if (!wasCommentHandled) {
            spannableStringBuilder.append(comment);
            spannableStringBuilder.setSpan(
                    new CommentSpan(comment),
                    spanStart,
                    spannableStringBuilder.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }

    private boolean processCommentHandlerPlugins(String comment) {
        boolean wasCommentHandled = false;
        if (plugins != null) {
            for (IAztecPlugin plugin : plugins) {
                if (plugin instanceof IHtmlCommentHandler) {
                    wasCommentHandled = ((IHtmlCommentHandler) plugin).handleComment(comment, spannableStringBuilder,
                            nestingLevel, new Function1<Integer, Unit>() {
                        @Override
                        public Unit invoke(Integer newNesting) {
                            nestingLevel = newNesting;
                            return Unit.INSTANCE;
                        }
                    });
                    if (wasCommentHandled) {
                        break;
                    }
                }
            }
        }
        return wasCommentHandled;
    }

    private static class ContentHandler {
        public StringBuilder rawHtml;
    }
}
