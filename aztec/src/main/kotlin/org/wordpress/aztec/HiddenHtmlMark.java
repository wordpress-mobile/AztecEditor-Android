package org.wordpress.aztec;

import android.text.TextPaint;
import android.text.style.CharacterStyle;

import org.xml.sax.Attributes;

public class HiddenHtmlMark extends CharacterStyle {

    private StringBuilder startTag;
    private StringBuilder endTag;
    private boolean isClosed;
    private boolean isParsed;
    private int level;

    public HiddenHtmlMark(String tag, StringBuilder attributes, int level) {
        this.level = level;
        this.startTag = new StringBuilder();
        this.startTag.append("<").append(tag).append(attributes).append(">");

        this.endTag = new StringBuilder();
        this.endTag.append("</").append(tag).append(">");

        isClosed = false;
        isParsed = false;
    }

    public StringBuilder getStartTag() {
        return startTag;
    }

    public StringBuilder getEndTag() {
        return endTag;
    }

    @Override
    public void updateDrawState(TextPaint textPaint) {

    }

    public boolean isClosed() {
        return isClosed;
    }

    public void close() {
        isClosed = true;
    }

    public boolean isParsed() {
        return isParsed;
    }

    public void parse() {
        isParsed = true;
    }

    public int getLevel() {
        return level;
    }

    private static class Hidden {
        Attributes attributes;

        public Hidden(Attributes attributes) {
            this.attributes = attributes;
        }
    }
}
