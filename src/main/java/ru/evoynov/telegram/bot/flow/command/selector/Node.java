package ru.evoynov.telegram.bot.flow.command.selector;

import java.util.ArrayList;
import java.util.List;

public class Node {

    private final Node parent;
    private final String id;
    private final String label;
    private final List<Node> children;

    public Node(Node parent, String id, String label) {
        this.parent = parent;
        this.id = id;
        this.label = label;
        this.children = new ArrayList<>();
    }

    public List<Node> getChildren() {
        return children;
    }

    public String getLabel() {
        return label;
    }

    public Node getParentNode() {
        return parent;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public boolean isNotLeaf() {
        return !isLeaf();
    }

    public boolean isRoot() {
        return parent == null;
    }

    public String getId() {
        return id;
    }
}
