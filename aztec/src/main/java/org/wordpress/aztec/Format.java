package org.wordpress.aztec;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Onko on 8/19/2016.
 */

public class Format {

    private static interface IReplacement {
        String replace(String content);
    }

    private static String replaceAll(String content, String pattern, String replacement) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(content);
        return m.replaceAll(replacement);
    }

//    private static String replaceAll(String content, String pattern, IReplacement replacement) {
//        Pattern p = Pattern.compile(pattern);
//        Matcher m = p.matcher(content);
//        if (m.find()) {
//            return m.replaceAll(replacement.replace(content.substring(m.start(), m.end())));
//        }
//        else return content;
//    }

    private static String replace(String content, String pattern, String replacement) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(content);
        return m.replaceFirst(replacement);
    }

    public static String toHtml(String content) {

        if (content == null || TextUtils.isEmpty(content.trim())) {
            // Just whitespace, null, or undefined
            return "";
        }

        boolean preserve_linebreaks = false,
                preserve_br = false;

        Pattern p;
        Matcher m;
        StringBuffer sb;

        // Protect pre|script tags
        if (content.contains("<pre") || content.contains("<script")) {
            preserve_linebreaks = true;
            p = Pattern.compile("<(pre|script)[^>]*>[\\s\\S]+?</\\1>");
            m = p.matcher(content);
            sb = new StringBuffer();
            if (m.find()) {
                m.appendReplacement(sb, replaceAll(content.substring(m.start(), m.end()), "<br ?/?>(\\r\\n|\\n)?", "<wp-line-break>"));
                m.appendReplacement(sb, replaceAll(content.substring(m.start(), m.end()), "</?p( [^>]*)?>(\\r\\n|\\n)?", "<wp-line-break>"));
                m.appendReplacement(sb, replaceAll(content.substring(m.start(), m.end()), "\\r?\\n", "<wp-line-break>"));
            }
            m.appendTail(sb);
            content = sb.toString();
//            content = content.replace("<(pre|script)[^>]*>[\s\S]+?<\/\1>", function( a ) {
//                a = a.replace( /<br ?\/?>(\r\n|\n)?/g, '<wp-line-break>' );
//                a = a.replace( /<\/?p( [^>]*)?>(\r\n|\n)?/g, '<wp-line-break>' );
//                return a.replace( /\r?\n/g, '<wp-line-break>' );
//            });
        }

        // keep <br> tags inside captions and remove line breaks
        if (content.contains("[caption")) {
            preserve_br = true;
            p = Pattern.compile("\\[caption[\\s\\S]+?\\[/caption\\]");
            m = p.matcher(content);
            sb = new StringBuffer();
            if (m.find()) {
                String result = replaceAll(content.substring(m.start(), m.end()), "<br([^>]*)>", "<wp-temp-br$1>");
                m.appendReplacement(sb, replace(result, "[\\r\\n\\t]+", ""));
            }
            m.appendTail(sb);
            content = sb.toString();

//            content = content.replace( /\[caption[\s\S]+?\[\/caption\]/g, function( a ) {
//                return a.replace( /<br([^>]*)>/g, '<wp-temp-br$1>' ).replace( /[\r\n\t]+/, '' );
//            });
        }

        // Pretty it up for the source editor
        String blocklist = "blockquote|ul|ol|li|table|thead|tbody|tfoot|tr|th|td|div|h[1-6]|p|fieldset";
        content = replaceAll(content, "\\s*</(" + blocklist + ")>\\s*", "</$1>\n");
        content = replaceAll(content, "\\s*<((?:" + blocklist + ")(?: [^>]*)?)>", "\n<$1>");
//        content = content.replace( new RegExp( '\\s*</(' + blocklist1 + ')>\\s*', 'g' ), '</$1>\n' );
//        content = content.replace( new RegExp( '\\s*<((?:' + blocklist1 + ')(?: [^>]*)?)>', 'g' ), '\n<$1>' );



        // Mark </p> if it has any attributes.
//        content = content.replace( /(<p [^>]+>.*?)<\/p>/g, '$1</p#>' );
        content = replaceAll(content, "(<p [^>]+>.*?)</p>", "$1</p#>");

        // Separate <div> containing <p>
//        content = content.replace( /<div( [^>]*)?>\s*<p>/gi, '<div$1>\n\n' );
        content = replaceAll(content, "(?i)<div( [^>]*)?>\\s*<p>", "<div$1>\n\n");

        // Remove <p> and <br />
//        content = content.replace( /\s*<p>/gi, '' );
//        content = content.replace( /\s*<\/p>\s*/gi, '\n\n' );
//        content = content.replace( /\n[\s\u00a0]+\n/g, '\n\n' );
//        content = content.replace( /\s*<br ?\/?>\s*/gi, '\n' );
        content = replaceAll(content, "(?i)\\s*<p>", "");
        content = replaceAll(content, "(?i)\\s*</p>\\s*", "\n\n");
        content = replaceAll(content, "\\n[\\s\\u00a0]+\\n", "\n\n");
        content = replaceAll(content, "(?i)\\s*<br ?/?>\\s*", "\n");

        // Fix some block element newline issues
//        content = content.replace( /\s*<div/g, '\n<div' );
//        content = content.replace( /<\/div>\s*/g, '</div>\n' );
//        content = content.replace( /\s*\[caption([^\[]+)\[\/caption\]\s*/gi, '\n\n[caption$1[/caption]\n\n' );
//        content = content.replace( /caption\]\n\n+\[caption/g, 'caption]\n\n[caption' );
        content = replaceAll(content, "\\s*<div", "\n<div");
        content = replaceAll(content, "</div>\\s*", "</div>\n");
        content = replaceAll(content, "(?i)\\s*\\[caption([^\\[]+)\\[/caption\\]\\s*", "\n\n[caption$1[/caption]\n\n");
        content = replaceAll(content, "caption\\]\\n\\n+\\[caption", "caption]\n\n[caption");

        blocklist = "blockquote|ul|ol|li|table|thead|tbody|tfoot|tr|th|td|h[1-6]|pre|fieldset";
//        content = content.replace( new RegExp('\\s*<((?:' + blocklist2 + ')(?: [^>]*)?)\\s*>', 'g' ), '\n<$1>' );
//        content = content.replace( new RegExp('\\s*</(' + blocklist2 + ')>\\s*', 'g' ), '</$1>\n' );
//        content = content.replace( /<li([^>]*)>/g, '\t<li$1>' );
        content = replaceAll(content, "\\s*<((?:" + blocklist + ")(?: [^>]*)?)\\s*>", "\n<$1>");
        content = replaceAll(content, "<li([^>]*)>", "\t<li$1>");

        if (content.contains("<option")) {
            content = replaceAll(content, "\\s*<option", "\n<option");
            content = replaceAll(content, "\\s*</select>", "\n</select>");
//            content = content.replace( /\s*<option/g, '\n<option' );
//            content = content.replace( /\s*<\/select>/g, '\n</select>' );
        }

        if (content.contains("<hr")) {
            content = replaceAll(content, "\\s*<hr( [^>]*)?>\\s*", "\n\n<hr$1>\n\n");
//            content = content.replace( /\s*<hr( [^>]*)?>\s*/g, '\n\n<hr$1>\n\n' );
        }

        if (content.contains("<object")) {
            p = Pattern.compile("<object[\\s\\S]+?</object>");
            m = p.matcher(content);
            sb = new StringBuffer();
            if (m.find()) {
                m.appendReplacement(sb, replace(content.substring(m.start(), m.end()), "[\\r\\n]+", ""));
            }
            m.appendTail(sb);
            content = sb.toString();

//            content = content.replace( /<object[\s\S]+?<\/object>/g, function( a ) {
//                return a.replace( /[\r\n]+/g, '' );
//            });
        }

        // Unmark special paragraph closing tags
//        content = content.replace( /<\/p#>/g, '</p>\n' );
//        content = content.replace( /\s*(<p [^>]+>[\s\S]*?<\/p>)/g, '\n$1' );
        content = replaceAll(content, "</p#>", "</p>\n");
        content = replaceAll(content, "\\s*(<p [^>]+>[\\s\\S]*?</p>)", "\n$1");

        // Trim whitespace
//        content = content.replace( /^\s+/, '' );
//        content = content.replace( /[\s\u00a0]+$/, '' );
        content = replaceAll(content, "^\\s+", "");
        content = replaceAll(content, "[\\s\\u00a0]+$", "");

        // put back the line breaks in pre|script
        if (preserve_linebreaks) {
//            content = content.replace( /<wp-line-break>/g, '\n' );
            content = replaceAll(content, "<wp-line-break>", "\n");
        }

        // and the <br> tags in captions
        if ( preserve_br ) {
//            content = content.replace( /<wp-temp-br([^>]*)>/g, '<br$1>' );
            content = replaceAll(content, "<wp-temp-br([^>]*)>", "<br$1>");
        }

        return content;
    }

    public static String fromHtml(String html) {

        if (html == null || TextUtils.isEmpty(html.trim())) {
            // Just whitespace, null, or undefined
            return "";
        }

        boolean preserve_linebreaks = false,
                preserve_br = false;

        Pattern p;
        Matcher m;
        StringBuffer sb;

        String blocklist = "table|thead|tfoot|caption|col|colgroup|tbody|tr|td|th|div|dl|dd|dt|ul|ol|li|pre" +
                        "|form|map|area|blockquote|address|math|style|p|h[1-6]|hr|fieldset|legend|section" +
                        "|article|aside|hgroup|header|footer|nav|figure|details|menu|summary";


        if (html.contains("<object")) {
            p = Pattern.compile("<object[\\s\\S]+?</object>");
            m = p.matcher(html);
            sb = new StringBuffer();
            while (m.find()) {
                m.appendReplacement(sb, replaceAll(html.substring(m.start(), m.end()), "[\\r\\n]+", ""));
            }
            m.appendTail(sb);
            html = sb.toString();
        }
//        if (html.contains("<object")) {
//            html = html.replace( /<object[\s\S]+?<\/object>/g, function( a ) {
//                return a.replace( /[\r\n]+/g, '' );
//            });
//        }

        p = Pattern.compile("<[^<>]+>");
        m = p.matcher(html);
        sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, replaceAll(html.substring(m.start(), m.end()), "[\\r\\n]+", ""));
        }
        m.appendTail(sb);

//        html = replaceAll(html, "<[^<>]+>", new IReplacement() {
//            @Override
//            public String replace(String match) {
//                return replaceAll(match, "[\\r\\n]+", "");
//            }
//        });

        html = sb.toString();

//        html = html.replace( /<[^<>]+>/g, function( a ){
//            return a.replace( /[\r\n]+/g, ' ' );
//        });

        // Protect pre|script tags
        if (html.contains("<pre") || html.contains("<script")) {
            preserve_linebreaks = true;

            p = Pattern.compile("<(pre|script)[^>]*>[\\s\\S]+?</\\1>");
            m = p.matcher(html);
            sb = new StringBuffer();
            while (m.find()) {
                m.appendReplacement(sb, replaceAll(html.substring(m.start(), m.end()), "(\\r\\n|\\n)", "<wp-line-break>"));
            }
            m.appendTail(sb);
            html = sb.toString();

//            html = html.replace( /<(pre|script)[^>]*>[\s\S]+?<\/\1>/g, function( a ) {
//                return a.replace( /(\r\n|\n)/g, '<wp-line-break>' );
//            });
        }

        // keep <br> tags inside captions and convert line breaks
        if (html.contains("[caption' )")) {
            preserve_br = true;

            p = Pattern.compile("\\[caption[\\s\\S]+?\\[/caption\\]");
            m = p.matcher(html);
            sb = new StringBuffer();
            while (m.find()) {
                // keep existing <br>
                m.appendReplacement(sb, replaceAll(html.substring(m.start(), m.end()), "<br([^>]*)>", "<wp-temp-br$1>"));

                // no line breaks inside HTML tags
                Pattern p2 = Pattern.compile("<[a-zA-Z0-9]+( [^<>]+)?>");
                String content = html.substring(m.start(), m.end());
                Matcher m2 = p2.matcher(content);
                StringBuffer sb2 = new StringBuffer();
                while (m2.find()) {
                    m2.appendReplacement(sb2, replace(content.substring(m2.start(), m2.end()), "[\\r\\n\\t]+", " "));
                }
                m2.appendTail(sb2);
                m.appendReplacement(sb, sb2.toString());

                // convert remaining line breaks to <br>
                m.appendReplacement(sb, replaceAll(html.substring(m.start(), m.end()), "\\s*\\n\\s*", "<wp-temp-br />"));
            }
            m.appendTail(sb);
            html = sb.toString();

//            html = html.replace( /\[caption[\s\S]+?\[\/caption\]/g, function( a ) {
//                // keep existing <br>
//                a = a.replace( /<br([^>]*)>/g, '<wp-temp-br$1>' );
//                // no line breaks inside HTML tags
//                a = a.replace( /<[a-zA-Z0-9]+( [^<>]+)?>/g, function( b ) {
//                    return b.replace( /[\r\n\t]+/, ' ' );
//                });
//                // convert remaining line breaks to <br>
//                return a.replace( /\s*\n\s*/g, '<wp-temp-br />' );
//            });
        }

        html = html + "\n\n";

        html = replaceAll(html, "(?i)<br />\\s*<br />", "\n\n");
//        html = html.replace( /<br \/>\s*<br \/>/gi, '\n\n' );

        html = replaceAll(html, "(?i)(<(?:" + blocklist + ")(?: [^>]*)?>)", "\n$1");
//        html = html.replace( new RegExp( '(<(?:' + blocklist + ')(?: [^>]*)?>)', 'gi' ), '\n$1' );

        html = replaceAll(html, "(?i)(</(?:" + blocklist + ")>)", "$1\n\n");
//        html = html.replace( new RegExp( '(</(?:' + blocklist + ')>)', 'gi' ), '$1\n\n' );

        // hr is self closing block element
        html = replaceAll(html, "(?i)<hr( [^>]*)?>", "<hr$1>\n\n");
//        html = html.replace( /<hr( [^>]*)?>/gi, '<hr$1>\n\n' ); // hr is self closing block element

        // No <p> or <br> around <option>
        html = replaceAll(html, "(?i)\\s*<option", "<option");
//        html = html.replace( /\s*<option/gi, '<option' ); // No <p> or <br> around <option>

        html = replaceAll(html, "(?i)</option>\\s*", "</option>");
//        html = html.replace( /<\/option>\s*/gi, '</option>' );

        html = replaceAll(html, "\\r\\n|\\r", "\n");
//        html = html.replace( /\r\n|\r/g, '\n' );

        html = replaceAll(html, "\\n\\s*\\n+", "\n\n");
//        html = html.replace( /\n\s*\n+/g, '\n\n' );

        html = replaceAll(html, "([\\s\\S]+?)\\n\\n", "<p>$1</p>\n");
//        html = html.replace( /([\s\S]+?)\n\n/g, '<p>$1</p>\n' );

        html = replaceAll(html, "(?i)<p>\\s*?</p>", "");
//        html = html.replace( /<p>\s*?<\/p>/gi, '');

        html = replaceAll(html, "(?i)<p>\\\\s*(</?(?:" + blocklist + ")(?: [^>]*)?>)\\\\s*</p>", "$1");
//        html = html.replace( new RegExp( '<p>\\s*(</?(?:' + blocklist + ')(?: [^>]*)?>)\\s*</p>', 'gi' ), '$1' );

        html = replaceAll(html, "(?i)<p>(<li.+?)</p>", "$1");
//        html = html.replace( /<p>(<li.+?)<\/p>/gi, '$1');

        html = replaceAll(html, "(?i)<p>\\s*<blockquote([^>]*)>", "<blockquote$1><p>");
//        html = html.replace( /<p>\s*<blockquote([^>]*)>/gi, '<blockquote$1><p>');

        html = replaceAll(html, "(?i)</blockquote>\\s*</p>", "</p></blockquote>");
//        html = html.replace( /<\/blockquote>\s*<\/p>/gi, '</p></blockquote>');

        html = replaceAll(html, "(?i)<p>\\\\s*(</?(?:" + blocklist + ")(?: [^>]*)?>)", "$1");
//        html = html.replace( new RegExp( '<p>\\s*(</?(?:' + blocklist + ')(?: [^>]*)?>)', 'gi' ), '$1' );

        html = replaceAll(html, "(?i)(</?(?:" + blocklist + ")(?: [^>]*)?>)\\\\s*</p>", "$1");
//        html = html.replace( new RegExp( '(</?(?:' + blocklist + ')(?: [^>]*)?>)\\s*</p>', 'gi' ), '$1' );

        html = replaceAll(html, "(?i)\\s*\\n", "<br />\n");
//        html = html.replace( /\s*\n/gi, '<br />\n');

        html = replaceAll(html, "(?i)(</?(?:" + blocklist + ")[^>]*>)\\\\s*<br />", "$1");
//        html = html.replace( new RegExp( '(</?(?:' + blocklist + ')[^>]*>)\\s*<br />', 'gi' ), '$1' );

        html = replaceAll(html, "(?i)<br />(\\s*</?(?:p|li|div|dl|dd|dt|th|pre|td|ul|ol)>)", "$1");
//        html = html.replace( /<br \/>(\s*<\/?(?:p|li|div|dl|dd|dt|th|pre|td|ul|ol)>)/gi, '$1' );

        html = replaceAll(html, "(?i)(?:<p>|<br ?/?>)*\\s*\\[caption([^\\[]+)\\[/caption\\]\\s*(?:</p>|<br ?/?>)*", "[caption$1[/caption]");
//        html = html.replace( /(?:<p>|<br ?\/?>)*\s*\[caption([^\[]+)\[\/caption\]\s*(?:<\/p>|<br ?\/?>)*/gi, '[caption$1[/caption]' );

//        html = html.replace( /(<(?:div|th|td|form|fieldset|dd)[^>]*>)(.*?)<\/p>/g, function( a, b, c ) {
//            if ( c.match( /<p( [^>]*)?>/ ) ) {
//                return a;
//            }
//
//            return b + '<p>' + c + '</p>';
//        });

        // put back the line breaks in pre|script
        if (preserve_linebreaks) {
            html = replaceAll(html, "<wp-line-break>", "\n");
//            html = html.replace( /<wp-line-break>/g, '\n' );
        }

        if (preserve_br) {
            html = replaceAll(html, "<wp-temp-br([^>]*)>", "<br$1>");
//            html = html.replace( /<wp-temp-br([^>]*)>/g, '<br$1>' );
        }

        return html;
    }
}
