package ru.ms.processors;

import java.util.Set;

/**
 * Created by sergey on 04.12.16.
 */
public class AltExtractor<Node> {
    private final DominatorTree.DomTreeNode<Node> dominators;
    private final Node startPoint;

    public AltExtractor(DominatorTree.DomTreeNode<Node> dominators_) {
        dominators = dominators_;
        startPoint = dominators.bb;
    }

    public void process() {

    }

    public Set<Set<Node>> getAlts() {
        return null;
    }
}
