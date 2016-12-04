package ru.ms.processors;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.stream.Collectors;

/**
 * Created by sergey on 28.11.16.
 */
public class DominatorTest {

    public static class Node implements Comparable<Node> {
        private final int idx;

        public Node(int idx) {
            this.idx = idx;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "idx=" + idx +
                    '}';
        }

        @Override
        public int compareTo(Node node) {
            return node.idx - this.idx;
        }
    }

    public static class Edge extends DefaultEdge {
        public Object getSource()
        {
            return super.getSource();
        }

        /**
         * Retrieves the target of this edge. This is protected, for use by subclasses only (e.g. for
         * implementing toString).
         *
         * @return target of this edge
         */
        public Object getTarget()
        {
            return super.getTarget();
        }
    }

    public static void main(String[] args) {
        DirectedGraph graph = new DefaultDirectedGraph(Edge.class);

        Node n[] = new Node[11];
        for (int i = 1; i < n.length; ++i) {
            n[i] = new Node(i);
            graph.addVertex(n[i]);
        }

        graph.addEdge(n[1], n[2]);
        graph.addEdge(n[1], n[3]);
        graph.addEdge(n[2], n[3]);
        graph.addEdge(n[3], n[4]);
        graph.addEdge(n[4], n[5]);
        graph.addEdge(n[4], n[6]);
        graph.addEdge(n[4], n[3]);
        graph.addEdge(n[5], n[7]);
        graph.addEdge(n[6], n[7]);
        graph.addEdge(n[7], n[8]);
        graph.addEdge(n[7], n[4]);
        graph.addEdge(n[8], n[9]);
        graph.addEdge(n[8], n[10]);
        graph.addEdge(n[8], n[3]);
        graph.addEdge(n[9], n[1]);
        graph.addEdge(n[10], n[7]);

        DominatorTree dom = new DominatorTree(
                (x) -> graph.incomingEdgesOf(x).stream().map( (e) -> ((Edge)e).getSource() ).collect(Collectors.toSet()),
                (x) -> graph.outgoingEdgesOf(x).stream().map( (e) -> ((Edge)e).getTarget() ).collect(Collectors.toSet()),
                n[1]
        );

        dom.process((x) -> true);
    }
}
