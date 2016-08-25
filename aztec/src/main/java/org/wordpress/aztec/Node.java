package org.wordpress.aztec;

import java.util.ArrayList;
import java.util.List;

public class Node {

    String text;
    String tag;
    Node parent;
    List<Node> children;

    public Node() {
        children = new ArrayList<>();
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
