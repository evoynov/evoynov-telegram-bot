package ru.evoynov.telegram.bot.flow.command.selector;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Triple;
import ru.evoynov.telegram.bot.flow.command.Command;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SelectorState {

    @Getter
    private final String selectorName;
    private final String baseHeader;
    private final List<String> breadCrumbs = new ArrayList<>();
    private Node currentNode;
    private final Node startNode;
    @Getter
    private final int itemsPerPage;
    private int currentPage = 0;

    @Setter
    @Getter
    private String selectedId;
    @Getter
    private final Command commandForSelection;

    public SelectorState(
            String baseHeader,
            List<Triple<String, String, Object>> treeDescription,
            int itemsPerPage,
            Command commandForSelection,
            String selectorName
    ) {
        this.baseHeader = baseHeader;
        this.commandForSelection = commandForSelection;
        this.currentNode = new Node(null, null, null);
        this.startNode = this.currentNode;
        this.itemsPerPage = itemsPerPage;
        this.selectorName = selectorName;

        fillTree(currentNode, treeDescription);
    }

    private void fillTree(Node parent, List<Triple<String, String, Object>> tree) {
        for (var entry : tree.stream().sorted((Comparator.comparing(Triple::getMiddle))).collect(Collectors.toList())) {
            var node = new Node(parent, entry.getLeft(), entry.getMiddle());
            if (entry.getRight() instanceof List) {
                var children = (List<Triple<String, String, Object>>) entry.getRight();
                fillTree(node, children);
            }
            parent.getChildren().add(node);
        }
    }

    public boolean hasPreviousPage() {
        return currentPage > 0;
    }

    public int getPagesCount() {
        return (int) Math.round(Math.ceil(currentNode.getChildren().size() / (.0d + itemsPerPage)));
    }

    public int getItemsCount() {
        return currentNode.getChildren().size();
    }

    public boolean hasNextPage() {
        return currentPage < (getPagesCount() - 1);
    }

    public boolean isRoot() {
        return currentNode.isRoot();
    }

    public String getHeader() {
        return (
                baseHeader +
                        (breadCrumbs.isEmpty() ? "" : " / " + String.join(" / ", breadCrumbs)) +
                        ((getPagesCount() > 1) ? ". Стр. " + (currentPage + 1) + " из " + getPagesCount() : "") +
                        ". Всего: " +
                        getItemsCount()
        );
    }

    public List<Triple<String, String, Boolean>> getCurrentPageItems() {
        return currentNode
                .getChildren()
                .stream()
                .skip((long) currentPage * getItemsPerPage())
                .limit(getItemsPerPage())
                .map(n -> Triple.of(n.getId(), n.getLabel(), n.isLeaf()))
                .collect(Collectors.toList());
    }

    public void resetState() {
        currentNode = startNode;
        currentPage = 0;
    }

    public void toNextPage() {
        if (!hasNextPage()) {
            resetState();
            return;
        }

        currentPage++;
    }

    public void toPreviousPage() {
        if (!hasPreviousPage()) {
            resetState();
            return;
        }

        currentPage--;
    }

    public boolean hasChildren(String id) {
        var item = currentNode.getChildren().stream().filter(t -> t.getId().equals(id)).findAny();
        return item.map(Node::isNotLeaf).orElse(false);
    }

    public void toChild(String id) {
        var item = currentNode.getChildren().stream().filter(t -> t.getId().equals(id)).findAny();
        if (item.isPresent()) {
            currentNode = item.get();
            currentPage = 0;
        } else {
            resetState();
        }
    }

    public void toParent() {
        if (currentNode.isRoot()) {
            resetState();
        } else {
            int pos = 0;
            for (; pos < currentNode.getParentNode().getChildren().size(); pos++) {
                if (currentNode.getId().equals(currentNode.getParentNode().getChildren().get(pos).getId())) {
                    break;
                }
            }
            currentPage = pos / getItemsPerPage();
            currentNode = currentNode.getParentNode();
        }
    }

}
