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
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Spanned;

import org.wordpress.aztec.plugins.IAztecPlugin;
import org.wordpress.aztec.plugins.html2visual.IHtmlPreprocessor;
import org.xml.sax.Attributes;

import java.util.List;

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

    public interface MediaCallback {
        void mediaLoadingStarted();
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

        throw new UnsupportedOperationException("fromHtml not supported in fork");
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

