package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A tree that stores each strings on edges. A word is composed of the sum of the edges to a leaf. This has the advantage
 * that it may be used to locate similar words (i.e. suggestions for a mis-spelt word) however I didn't get round to
 * implementing it.
 */
public interface RadixTree {

    /**
     * Determines whether a tree contains a string.
     *
     * @param string The string to locate within this tree.
     * @return true iff this tree contains the given string; otherwise, false.
     */
    boolean contains(String string);

    /**
     * Determines whether a tree is a leaf (has no successors / trailing edges).
     *
     * @return true iff this tree is a leaf; otherwise, false.
     */
    boolean isLeaf();

    /**
     * Inserts a string into a tree.
     *
     * @param string The string to insert.
     */
    void insert(String string);

    /**
     * Gets the edges (string + successor node) of a tree.
     * @return The edges of this tree.
     */
    ArrayList<Edge> getEdges();

    /**
     * Used an interface to allow edge to 'decorate' (decorator pattern) its successor node.
     */
    class Node implements RadixTree {

        // This node's edges.
        private final ArrayList<Edge> edges;

        public Node(){
            this.edges = new ArrayList<>();
        }

        Node(Edge...edges){
            this(new ArrayList<>(Arrays.asList(edges)));
        }

        Node(ArrayList<Edge> edges){
            this.edges = edges;
        }

        @Override
        public void insert(String string) {

            /* Index of the edge whose value shares the longest common root with 'string'. */
            int index = -1;

            /* Length of the longest common root. */
            int rootLength = 0;

            // find the length of the longest common prefix and the index of the edge that contains it.
            for(int i = 0; i < edges.size(); i++){

                String value = edges.get(i).getValue(); int j = 0;

                while (j < string.length() && j < value.length() && string.charAt(j) == value.charAt(j)) j++;

                if (j > rootLength){
                    index = i; rootLength = j;
                }
            }

            if (rootLength == 0){

                /* No edge has a value that shares a root with 'string' therefore insert 'string' as a leaf. */
                edges.add(new Edge(string, new Node()));

            } else {

                /* The edge whose value shares the longest common root with 'string'. */
                final Edge leading = edges.get(index);

                if (rootLength == leading.getValue().length()) {

                    if (rootLength != string.length()) {
                        if (leading.isLeaf())
                            leading.insert("");

                        leading.insert(string.substring(rootLength));
                    }

                } else {

                    // replace the conflicting edge with a node, one edge to the original sub-tree and one to the rest
                    // of the inserted node.
                    edges.set(index,
                            new Edge(string.substring(0, rootLength),
                                    new Edge(string.substring(rootLength)),
                                    new Edge(leading.getValue().substring(rootLength), leading.getSuccessor())
                            )
                    );
                }
            }
        }

        @Override
        public boolean contains(String string) {

            /* Edge whose value is the longest prefix of the search string. */
            Edge bestEdge = null;

            /* Length of the edge's value that is the longest prefix of the search string. */
            int rootLength = 0;

            for (Edge leading : edges) {
                if (string.startsWith(leading.getValue())) {
                    if (Objects.equals(string, leading.getValue())) {
                        return leading.isLeaf() || leading.contains("");
                    }

                    if (leading.getValue().length() > rootLength) {
                        rootLength = leading.getValue().length();
                        bestEdge = leading;
                    }
                }
            }

            return bestEdge != null && bestEdge.contains(string.substring(bestEdge.getValue().length()));
        }

        @Override
        public boolean isLeaf() {
            return edges.isEmpty();
        }

        @Override
        public ArrayList<Edge> getEdges() {
            return edges;
        }

        @Override
        public String toString() {
            return isLeaf()
                    ? "Ø"
                    : String.format("{%s}", edges.stream().map(Object::toString).collect(Collectors.joining(", ")));
        }
    }

    class Edge implements RadixTree {

        private final RadixTree successor;
        private final String value;

        Edge(String value, RadixTree successor){
            this.successor = successor;
            this.value = value;
        }

        Edge(String value, Edge...edges){
            this.successor = new Node(edges);
            this.value = value;
        }

        String getValue(){
            return value;
        }

        RadixTree getSuccessor(){
            return successor;
        }

        @Override
        public void insert(String string) {
            successor.insert(string);
        }

        @Override
        public boolean contains(String string) {
            return successor.contains(string);
        }

        @Override
        public boolean isLeaf() {
            return successor.isLeaf();
        }

        @Override
        public ArrayList<Edge> getEdges() {
            return successor.getEdges();
        }

        @Override
        public String toString() {
            return String.format("%s → %s",
                    (Objects.equals(value, "")) ? "λ" : "\"" + value + "\"",
                    successor.toString()
            );
        }
    }
}