package ru.ms.processors;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by sergey on 28.11.16.
 */
public class DominatorTree<Node> {
    private final TreeMap<Node, CopyOnWriteArrayList<Node>> allDominators = new TreeMap<>();
    private final Function<Node, CopyOnWriteArrayList<Node>> predcessorsFunc;
    private final Function<Node, CopyOnWriteArrayList<Node>> successorsFunc;
    private final Node startPoint;

    public DominatorTree(
            Function<Node, CopyOnWriteArrayList<Node>> predcessors_,
            Function<Node, CopyOnWriteArrayList<Node>> successors_,
            Node startPoint_)
    {
        predcessorsFunc = predcessors_;
        successorsFunc = successors_;
        startPoint = startPoint_;
    }

    private static <T> CopyOnWriteArrayList intersect(CopyOnWriteArrayList<T>[] sets) {
        if (sets.length == 0) return new CopyOnWriteArrayList();

        return sets[0].stream().filter(
                (T t) -> {
                    for (int i = 1; i < sets.length; ++i) {
                        if (!sets[i].contains(t)) {
                            return false;
                        }
                    }
                    return true;
                }).collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    public boolean isSuperset(CopyOnWriteArrayList from, CopyOnWriteArrayList to) {
        return from.containsAll(to) && from.size() > to.size();
    }

    public void process(Predicate<Node> isInScope) {
        Set<Node> visited = new HashSet<>();
        Queue<Node> toVisit = new ArrayDeque<>();

        Node n = startPoint;

        while (n != null) {
            if (!visited.contains(n) ) {
                visited.add(n);

                if (isInScope.test(n)) {
                    System.out.println("--------------------");
                    System.out.println(n);
                    CopyOnWriteArrayList<Node> predcess = predcessorsFunc.apply(n);
                    if (predcess.size() == 0) {
                        CopyOnWriteArrayList<Node> dominatorsPerNode = allDominators.get(n);
                        if (dominatorsPerNode == null) {
                            dominatorsPerNode = new CopyOnWriteArrayList<Node>();
                            allDominators.put(n, dominatorsPerNode);
                        }
                        dominatorsPerNode.add(n);

                        List doms = dominatorsPerNode
                                .stream()
                                .map(Object::toString)
                                .collect(Collectors.toList());
                        System.out.println("\t" + String.join("\n\t", doms));
                    } else {
                        CopyOnWriteArrayList<Node>[] dominatorsPerPredcessors =
                                (CopyOnWriteArrayList<Node>[]) predcess
                                        .stream()
                                        .map(allDominators::get)
                                        .filter((x) -> x != null)
                                        .toArray(CopyOnWriteArrayList[]::new);

                        CopyOnWriteArrayList<Node> dominators = intersect(dominatorsPerPredcessors);
                        dominators.add(n);
                        allDominators.put(n, dominators);

                        List doms = dominators.stream().map((x) -> x.toString()).collect(Collectors.toList());
                        System.out.println("\t" + String.join("\n\t", doms));
                    }

                    CopyOnWriteArrayList<Node> successors = successorsFunc.apply(n);
                    toVisit.addAll(successors);
                }
            }
            n = toVisit.poll();
        }

        System.out.println("Processed: " + visited.size() + " nodes");
    }

    public CopyOnWriteArrayList<Node> getDominators(Node bb) {
        return allDominators.get(bb);
    }

    public Set calculateBackEdges(Set edges, Function<Object, Node> fromFunc, Function<Object, Node> toFunc) {
        Set<Object> result = new HashSet<>();

        for (Object edge : edges) {
            Node from = fromFunc.apply(edge);
            Node to = toFunc.apply(edge);

            CopyOnWriteArrayList<Node> fromDom = getDominators(from);
            CopyOnWriteArrayList<Node> toDom = getDominators(to);

            if (fromDom != null && toDom != null) {
                if (isSuperset(fromDom, toDom)) {
                    result.add(edge);
                }
            }
        }

        return result;
    }

    public static class DomTreeNode <Node> {
        public final List<DomTreeNode<Node>> children = new ArrayList<>();
        public DomTreeNode<Node> parent;

        public final Node bb;
        public int totalChildrenCount;

        public DomTreeNode(
                Node bb)
        {
            this.bb = bb;
        }
    }

    public DomTreeNode<Node>
    buildDomTree(
            Set<Node> nodes)
    {
        Map<Integer, List<CopyOnWriteArrayList<Node>>> dominatorsByNest =
                allDominators
                        .values()
                        .stream()
                        .collect(
                                Collectors
                                        .groupingBy(
                                                CopyOnWriteArrayList::size
                                        )
                        );

        TreeMap<Integer, LinkedHashSet<DomTreeNode<Node>>> tree = new TreeMap<>();
        DomTreeNode<Node> root = null;

        for (Integer nest : dominatorsByNest.keySet()) {
            for (CopyOnWriteArrayList<Node> doms : dominatorsByNest.get(nest)) {
                DomTreeNode<Node> treeNode = new DomTreeNode<>(doms.get(doms.size() - 1));

                if (nest > 1) {
                    Set<DomTreeNode<Node>> possibleParents = tree.get(nest - 1);
                    Node parentNode = doms.get(doms.size() - 2);
                    for (DomTreeNode<Node> possibleParent : possibleParents) {
                        if (possibleParent.bb == parentNode) {
                            possibleParent.children.add(treeNode);
                            treeNode.parent = possibleParent;
                            break;
                        }
                    }
                } else {
                    root = treeNode;
                }

                LinkedHashSet<DomTreeNode<Node>> nestSet =
                        tree.get(nest);
                if (nestSet == null) {
                    nestSet = new LinkedHashSet<>();
                    tree.put(nest, nestSet);
                }
                nestSet.add(treeNode);

            }
        }

        calculateTotalChildred(root);

        try {
            try (PrintWriter pw = new PrintWriter(new FileOutputStream("dominators.dot"))) {
                pw.println("digraph G {");
                observeDomTree(root, pw);
                pw.println("}");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return root;
    }

    public static <Node> int calculateTotalChildred(DomTreeNode<Node> node) {
        int total = 0;
        for (DomTreeNode<Node> child : node.children) {
            total += calculateTotalChildred(child);
        }
        node.totalChildrenCount = total + 1;

        return node.totalChildrenCount;
    }

    private static <Node> void observeDomTree(DomTreeNode<Node> node, PrintWriter pw) {
        pw.println("\"" + node.bb.hashCode() + "\" [");
        pw.println("\tlabel=\"" + node.bb.toString() + " \\n Children: " + node.totalChildrenCount +  "\"");
        pw.println("];");
        for (DomTreeNode<Node> child : node.children) {
            pw.println("\"" + node.bb.hashCode() + "\" -> \"" +child.bb.hashCode() + "\";" );
            observeDomTree(child, pw);
        }
    }
}
