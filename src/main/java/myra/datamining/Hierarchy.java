/*
 * Hierarchy.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2019 Fernando Esteban Barril Otero
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package myra.datamining;

import static myra.Config.CONFIG;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import myra.Config.ConfigKey;

/**
 * This class represents a class hierarchy in hierarchical classification
 * problems.
 * 
 * @author Fernando Esteban Barril Otero
 * 
 * @since 5.0
 */
public class Hierarchy {
    /**
     * The config key for the class labels to be ignored.
     */
    public final static ConfigKey<String> IGNORE_LIST = new ConfigKey<>();

    /**
     * The config key for the array of class labels to be ignored.
     */
    public final static ConfigKey<boolean[]> IGNORE = new ConfigKey<>();

    /**
     * The config key to indicate single-label hierarchical problems.
     */
    public final static ConfigKey<Boolean> SINGLE_LABEL = new ConfigKey<>();

    /**
     * Relationship separator between nodes of the hierarchy.
     */
    public final static String DELIMITER = "/";

    /**
     * Label separator in multi-label hierarchical problems.
     */
    public final static String SEPARATOR = "@";

    /**
     * The config key for the label weight.
     */
    public static final ConfigKey<Double> WEIGHT = new ConfigKey<Double>();

    /**
     * The default label weight value.
     */
    public static final double DEFAULT_WEIGHT = 0.75;

    /**
     * The set of nodes (improves the performance).
     */
    private Map<String, Node> nodes = new HashMap<String, Node>();

    /**
     * Root node of the hierarchy.
     */
    private Node root;

    /**
     * Class label distribution.
     */
    private double[] distribution;

    /**
     * Adds a label to the hierarchy.
     * 
     * @param label
     *            the label to add.
     */
    public void add(String label) {
        if (nodes.containsKey(label)) {
            throw new IllegalArgumentException("Duplicated node label: "
                    + label);
        }

        nodes.put(label, new Node(label));
    }

    /**
     * Creates a parent-child relationship between the specified node labels.
     * 
     * @param parent
     *            the parent label.
     * @param child
     *            the child label.
     */
    public void link(String parent, String child) {
        Node p = get(parent);
        Node c = get(child);

        if (p == null) {
            throw new IllegalArgumentException("Parent node not found: "
                    + parent);
        } else if (c == null) {
            throw new IllegalArgumentException("Child node not found: "
                    + child);
        }

        c.getParents().add(p);
        p.getChildren().add(c);
    }

    /**
     * Returns <code>true</code> if the hierarchy contains no labels.
     * 
     * @return <code>true</code> if the hierarchy contains no labels.
     */
    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    /**
     * Returns the node with the specified label.
     * 
     * @param label
     *            the node label.
     * 
     * @return the node with the specified label; <code>null</code> if the label
     *         is not found.
     */
    public Node get(String label) {
        return nodes.get(label);
    }

    /**
     * Computes the weights of all nodes of the hierarchy.
     */
    public void weigh() {
        HashSet<String> weighted = new HashSet<String>(nodes.size(), 1.0f);
        LinkedList<Node> evaluation = new LinkedList<Node>();

        if (!isEmpty()) {
            double w = CONFIG.get(WEIGHT);
            root.setWeight(w);
            weighted.add(root.getLabel());

            for (Node node : root.getChildren()) {
                node.setWeight(w);
                weighted.add(node.getLabel());
                evaluation.addAll(node.getChildren());
            }

            while (!evaluation.isEmpty()) {
                boolean computed = true;
                double sum = 0.0;
                Node node = evaluation.removeFirst();

                if (!weighted.contains(node.getLabel())) {
                    for (Node parent : node.getParents()) {
                        if (weighted.contains(parent.getLabel())) {
                            sum += parent.getWeight();
                        } else {
                            evaluation.addLast(node);
                            computed = false;
                            break;
                        }
                    }

                    if (computed) {
                        node.setWeight(w * (sum / node.getParents().size()));
                        weighted.add(node.getLabel());
                        evaluation.addAll(node.getChildren());
                    }
                }
            }
        }
    }

    /**
     * Validates the hierarchy and sets the weight of each node. Note that if
     * the root node has a single child, it will be removed and the child node
     * promoted to be the root of the hierarchy.
     * 
     * @param labels
     *            set of labels present in the hierarchy.
     */
    public void validate(Set<String> labels) {
        for (Node node : nodes.values()) {
            if (node.getParents().isEmpty()) {
                if (root == null) {
                    root = node;
                } else {
                    throw new IllegalStateException("Invalid hierarchy structure: root="
                            + root + "," + node);
                }
            }
        }

        if (root.getChildren().size() == 1) {
            nodes.remove(root.getLabel());
            labels.remove(root.getLabel());

            root = root.getChildren().iterator().next();
            root.getParents().clear();
        }

        distribution = new double[nodes.size()];

        // once the structure of the hierarchy is validated, computes
        // the weights of the nodes (see Vens et al., "Decision Trees
        // for Hierarchical Multi-label Classification",
        // Machine Learning (2008) 73: 185–214)

        weigh();

        // determines the depth of each node of the class hierarchy
        // if a node has multiple parents, the depth is the smallest
        // value among the parents

        root.depth = 0;
        LinkedList<Node> toVisit = new LinkedList<>(root.getChildren());

        while (!toVisit.isEmpty()) {
            Node node = toVisit.removeFirst();

            for (Node parent : node.getParents()) {
                if (node.depth == -1 || (parent.depth + 1) < node.depth) {
                    node.depth = parent.depth + 1;
                }
            }

            toVisit.addAll(node.getChildren());
        }
    }

    /**
     * Increaments the class label distribution.
     * 
     * @param active
     *            the active class labels to increment.
     */
    public void increment(boolean[] active) {
        if (active.length != distribution.length) {
            throw new IllegalArgumentException("Invalid length: "
                    + active.length + ", expected " + distribution.length);
        }

        for (int i = 0; i < active.length; i++) {
            if (active[i]) {
                distribution[i] += 1.0;
            }
        }
    }

    /**
     * Returns the class label distribution.
     * 
     * @return the class label distribution.
     */
    public double[] distribution() {
        return distribution;
    }

    /**
     * Returns the number of nodes in the hierarchy.
     * 
     * @return the number of nodes in the hierarchy.
     */
    public int size() {
        return nodes.size();
    }

    /**
     * Return the root node of the hierarchy.
     * 
     * @return the root node of the hierarchy.
     */
    public Node root() {
        return root;
    }

    /**
     * This class represents a node of the hierarchy.
     */
    public class Node implements Comparable<Node> {
        /**
         * References to the parent nodes.
         */
        private Collection<Node> parents;

        /**
         * References of the children nodes.
         */
        private Collection<Node> children;

        /**
         * The node label.
         */
        private String label;

        /**
         * The hierarchical node weight.
         */
        private double weight;

        /**
         * The depth of the node in the class hierarchy.
         */
        private int depth = -1;

        /**
         * Default constructor.
         * 
         * @param label
         *            the label of the node.
         */
        public Node(String label) {
            this.label = label;
            parents = new LinkedList<Node>();
            children = new LinkedList<Node>();
        }

        /**
         * Returns the depth of the node in the class hierarchy.
         * 
         * @return the depth of the node in the class hierarchy.
         */
        public int getDepth() {
            if (depth == -1) {
                throw new IllegalStateException("Depth not calculated: "
                        + depth);
            }

            return depth;
        }

        /**
         * Returns <code>true</code> if this node represents a leaf node.
         * 
         * @return <code>true</code> if this node represents a leaf node;
         *         otherwise <code>false</code>.
         */
        public boolean isLeaf() {
            return children.isEmpty();
        }

        /**
         * Returns <code>true</code> if this node represents the root node of
         * the hierarchy.
         * 
         * @return <code>true</code> if this node represents the root node of
         *         the hierarchy; otherwise <code>false</code>.
         */
        public boolean isRoot() {
            return parents.isEmpty();
        }

        /**
         * Sets the weight of the node.
         * 
         * @param weight
         *            the weight to set.
         */
        void setWeight(double weight) {
            this.weight = weight;
        }

        /**
         * Returns the weight of the node.
         * 
         * @return the weight of the node.
         */
        public double getWeight() {
            return weight;
        }

        /**
         * Returns <code>true</code> if the specified node label is an ancestor
         * of this node.
         * 
         * @param label
         *            the node label.
         * 
         * @return <code>true</code> if the specified node label is an ancestor
         *         of this node; otherwise returns <code>false</code>.
         */
        public boolean isAncestor(String label) {
            HashSet<String> labels = new HashSet<String>();
            LinkedList<Node> parentList = new LinkedList<Node>(parents);

            while (!parentList.isEmpty()) {
                Node n = parentList.removeFirst();
                labels.add(n.getLabel());
                parentList.addAll(n.getParents());
            }

            return labels.contains(label);
        }

        /**
         * Returns <code>true</code> if the specified node label is a descendant
         * of this node.
         * 
         * @param label
         *            the node label.
         * 
         * @return <code>true</code> if the specified node label is a descendant
         *         of this node; otherwise returns <code>false</code>.
         */
        public boolean isDescendant(String label) {
            HashSet<String> labels = new HashSet<String>();
            LinkedList<Node> childList = new LinkedList<Node>(children);

            while (!childList.isEmpty()) {
                Node n = childList.removeFirst();
                labels.add(n.getLabel());
                childList.addAll(n.getChildren());
            }

            return labels.contains(label);
        }

        /**
         * Returns the label of the node.
         * 
         * @return the label of the node.
         */
        public String getLabel() {
            return label;
        }

        /**
         * Returns the parent nodes of this node. An empty collection is
         * returned when this node represents the root of the hierarchy.
         * 
         * @return the parent nodes of this node.
         */
        public Collection<Node> getParents() {
            return parents;
        }

        /**
         * Returns the child nodes of this node. An empty collection is returned
         * when this node represents a leaf node.
         * 
         * @return the child nodes of this node.
         */
        public Collection<Node> getChildren() {
            return children;
        }

        /**
         * Returns the child node given its label.
         * 
         * @param label
         *            the label representing the node.
         * 
         * @return the child node given its label; <code>null</code> if a child
         *         node is not found.
         */
        public Node getChild(String label) {
            for (Node child : children) {
                if (child.getLabel().equals(label)) {
                    return child;
                }
            }

            return null;
        }

        /**
         * Returns the ancestor nodes of this node.
         * 
         * @return the ancestor nodes of this node.
         */
        public Collection<Node> getAncestors() {
            Set<Node> ancestors = new LinkedHashSet<Node>();
            LinkedList<Node> parents = new LinkedList<Node>(getParents());

            while (!parents.isEmpty()) {
                Node node = parents.removeFirst();
                ancestors.add(node);
                parents.addAll(node.getParents());
            }

            return ancestors;
        }

        /**
         * Returns the descendant nodes of this node.
         * 
         * @return the descendant nodes of this node.
         */
        public Collection<Node> getDescendants() {
            Set<Node> descendants = new LinkedHashSet<Node>();
            LinkedList<Node> children = new LinkedList<Node>(getChildren());

            while (!children.isEmpty()) {
                Node node = children.removeFirst();
                descendants.add(node);
                children.addAll(node.getChildren());
            }

            return descendants;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Node) {
                Node node = (Node) o;
                return label.equals(node.label);
            }

            return false;
        }

        @Override
        public int hashCode() {
            return label.hashCode();
        }

        @Override
        public String toString() {
            return label;
        }

        @Override
        public int compareTo(Node o) {
            return label.compareTo(o.label);
        }
    }
}