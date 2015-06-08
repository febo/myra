/*
 * Node.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2015 Fernando Esteban Barril Otero
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

package myra.tree;

/**
 * The <code>Node</code> class represents a node of a decision tree.
 * 
 * @author Fernando Esteban Barril Otero
 */
public abstract class Node {
    /**
     * The first level of the tree (the level of the root node).
     */
    public static final int INITIAL_LEVEL = 0;

    /**
     * The name of the node.
     */
    private String name;

    /**
     * The level in the tree where this node is placed (<code>0</code>
     * represents the first level of the tree).
     */
    private int level;

    /**
     * The class distribution of the instances reaching this node.
     */
    private double[] distribution;

    /**
     * The total number of instances reaching this node.
     */
    private double total;

    /**
     * Creates a new node.
     * 
     * @param level
     *            the level of the node.
     */
    public Node(String name, int level) {
	this.name = name;
	this.level = level;
    }

    /**
     * Returns the level of this node.
     * 
     * @return the level of this node.
     */
    public int getLevel() {
	return level;
    }

    /**
     * Sets the level of this node.
     * 
     * @param level
     *            the level to set.
     */
    public void setLevel(int level) {
	this.level = level;
    }

    /**
     * Returns the name of this node.
     * 
     * @return the name of this node.
     */
    public String getName() {
	return name;
    }

    /**
     * Sets the name of this node.
     * 
     * @param name
     *            the name to set.
     */
    public void setName(String name) {
	this.name = name;
    }

    /**
     * Returns the class distribution of the instances reaching this node.
     * 
     * @return the class distribution of the instances reaching this node.
     */
    public double[] getDistribution() {
	return distribution;
    }

    /**
     * Sets the class distribution of the instances reaching this node.
     * 
     * @param distribution
     *            the class distribution to set.
     */
    public void setDistribution(double[] distribution) {
	this.distribution = distribution;
	total = 0;

	for (int i = 0; i < distribution.length; i++) {
	    total += distribution[i];
	}
    }

    /**
     * Returns the total number of instances reaching this node.
     * 
     * @return the total number of instances reaching this node.
     */
    public double getTotal() {
	return total;
    }

    /**
     * Indicates if this node is a leaf node or not. If the node is a leaf node,
     * it is safe to call the {@link #getPrediction()} method.
     * 
     * @return <code>true</code> if this node is a leaf node; <code>false</code>
     *         otherwise.
     * 
     * @see #getPrediction()
     */
    public abstract boolean isLeaf();

    /**
     * Sorts the branches originating in this node (branches leading to a leaf
     * node will have lower indexes).
     */
    public abstract void sort();

    @Override
    public String toString() {
	return getName();
    }

    /**
     * Returns the leaf nodes of the (sub-)tree represented by this node.
     * 
     * @return the leaf nodes of the (sub-)tree represented by this node.
     */
    /*
     * public Collection<LeafNode> leaves() { LinkedList<Node> nodes = new
     * LinkedList<Node>(); nodes.add(this);
     * 
     * LinkedList<LeafNode> leaves = new LinkedList<LeafNode>();
     * 
     * while (!nodes.isEmpty()) { Node node = nodes.removeFirst();
     * 
     * if (node.isLeaf()) { leaves.add((LeafNode) node); } else { for (int i =
     * 0; i < node.branches.length; i++) {
     * nodes.addFirst(node.branches[i].getTo()); } } }
     * 
     * return leaves; }
     */

    /**
     * Returns the label predicted by the node.
     * 
     * @return the label predicted by the node.
     */
    // public abstract int getPrediction();

    /**
     * Sets the prediction of the node.
     * 
     * @param prediction
     *            the prediction to set.
     */
    // public abstract void setPrediction(int prediction);

    // @Override
    // public final boolean equals(Object o)
    // {
    // return super.equals(o);
    // }

    // @Override
    // public final int hashCode()
    // {
    // return super.hashCode();
    // }
}