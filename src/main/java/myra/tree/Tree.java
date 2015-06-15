/*
 * Tree.java
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

import static myra.Attribute.EQUAL_TO;
import static myra.Attribute.GREATER_THAN;
import static myra.Attribute.IN_RANGE;
import static myra.Attribute.LESS_THAN_OR_EQUAL_TO;
import static myra.Attribute.Type.CONTINUOUS;

import java.util.LinkedList;

import myra.Attribute;
import myra.Attribute.Condition;
import myra.Dataset;
import myra.Model;

/**
 * This class represents a decision tree.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class Tree implements Model, Comparable<Tree> {
    /**
     * The root node of the tree.
     */
    private Node root;

    /**
     * The quality of the tree.
     */
    private double quality;

    /**
     * The iteration that created the tree.
     */
    private int iteration;

    /**
     * Creates a new <code>Tree</code> object.
     * 
     * @param root
     *            the root node of the tree.
     */
    public Tree(Node root) {
	this.root = root;
	quality = 0.0;
    }

    /**
     * Returns the quality of the tree.
     * 
     * @return the quality of the tree.
     */
    public double getQuality() {
	return quality;
    }

    /**
     * Sets the quality of the tree.
     * 
     * @param quality
     *            the quality value to set.
     */
    public void setQuality(double quality) {
	this.quality = quality;
    }

    /**
     * Returns the iteration that created the tree.
     * 
     * @return the iteration that created the tree.
     */
    public int getIteration() {
	return iteration;
    }

    /**
     * Sets the iteration that created the tree.
     * 
     * @param iteration
     *            the iteration to set.
     */
    public void setIteration(int iteration) {
	this.iteration = iteration;
    }

    /**
     * Returns the root node of the tree.
     * 
     * @return the root node of the tree.
     */
    public Node getRoot() {
	return root;
    }

    /**
     * Sets the root node of the tree.
     * 
     * @param root
     *            the node to set.
     */
    public void setRoot(Node root) {
	this.root = root;
    }

    /**
     * Returns the number of internal nodes in the tree.
     * 
     * @return the number of internal nodes in the tree.
     */
    public int internal() {
	int count = 0;
	LinkedList<Node> toVisit = new LinkedList<Node>();
	toVisit.add(root);

	while (!toVisit.isEmpty()) {
	    Node node = toVisit.removeFirst();

	    if (!node.isLeaf()) {
		Node[] children = ((InternalNode) node).children;

		for (int i = 0; i < children.length; i++) {
		    toVisit.add(children[i]);
		}

		count++;
	    }
	}

	return count;
    }

    /**
     * Returns the number of nodes in the tree.
     * 
     * @return the number of nodes in the tree.
     */
    public int size() {
	int count = 0;
	LinkedList<Node> toVisit = new LinkedList<Node>();
	toVisit.add(root);

	while (!toVisit.isEmpty()) {
	    Node node = toVisit.removeFirst();
	    count++;

	    if (!node.isLeaf()) {
		Node[] children = ((InternalNode) node).children;

		for (int i = 0; i < children.length; i++) {
		    toVisit.add(children[i]);
		}
	    }
	}

	return count;
    }

    /**
     * Determines the class value prediction for the specified instance by
     * following the tree in a top-down fashion.
     * 
     * @param dataset
     *            the current dataset.
     * @param instance
     *            the index of the instance.
     * @param node
     *            the node to visit.
     * @param weight
     *            the weight of the instance.
     * @param probabilities
     *            the class probabilities vector.
     */
    protected void prediction(Dataset dataset,
			      int instance,
			      Node node,
			      double weight,
			      double[] probabilities) {
	if (node.isLeaf()) {
	    if (node.getTotal() > 0) {
		double[] distribution = node.getDistribution();
		double total = node.getTotal();

		for (int i = 0; i < distribution.length; i++) {
		    if (distribution[i] > 0) {
			probabilities[i] += weight * (distribution[i] / total);
		    }
		}
	    } else {
		probabilities[((LeafNode) node).getPrediction()] += weight;
	    }

	    return;
	}

	InternalNode internal = (InternalNode) node;
	Attribute attribute = dataset.attributes()[internal.attribute()];
	double value = dataset.value(instance, attribute.getIndex());

	if (dataset.isMissing(attribute, value)) {
	    for (int i = 0; i < internal.children.length; i++) {
		prediction(dataset,
			   instance,
			   internal.children[i],
			   weight * (internal.children[i].getTotal()
				   / internal.getTotal()),
			   probabilities);
	    }
	} else {
	    for (int i = 0; i < internal.conditions.length; i++) {
		if (internal.conditions[i].satisfies(value)) {
		    prediction(dataset,
			       instance,
			       internal.children[i],
			       weight,
			       probabilities);
		    break;
		}
	    }
	}
    }

    /**
     * Returns the predicted class for the specified instance.
     * 
     * @param dataset
     *            the current dataset.
     * @param instance
     *            the index of the instance.
     * 
     * @return the predicted class for the specified instance.
     */
    public int classify(Dataset dataset, int instance) {
	double[] probabilities = new double[dataset.classLength()];

	prediction(dataset, instance, root, 1.0, probabilities);

	int highest = 0;

	for (int i = 1; i < probabilities.length; i++) {
	    if (probabilities[i] > probabilities[highest]) {
		highest = i;
	    }
	}

	return highest;
    }

    /**
     * Returns the string representation of the tree.
     * 
     * @param dataset
     *            the current dataset.
     * 
     * @return the string representation of the tree.
     */
    public String toString(Dataset dataset) {
	StringBuffer buffer = new StringBuffer();
	buffer.append(toString(dataset, root, ""));

	int size = size();

	buffer.append(String.format("%nTotal number of nodes: %d%n", size));
	buffer.append(String.format("Number of leaf nodes: %d",
				    (size - internal())));

	if (!Double.isNaN(quality)) {
	    buffer.append(String.format("%nTree quality: %f%n", quality));
	    buffer.append(String.format("Tree iteration: %d%n", iteration));
	}

	return buffer.toString();
    }

    /**
     * Substitutes continuous attributes' threshold values with values that
     * occur in the dataset.
     * 
     * @param dataset
     *            the current dataset.
     */
    public void fixThresholds(Dataset dataset) {
	LinkedList<InternalNode> nodes = new LinkedList<InternalNode>();

	if (!root.isLeaf()) {
	    nodes.add((InternalNode) root);
	}

	while (!nodes.isEmpty()) {
	    InternalNode node = nodes.removeFirst();

	    for (int i = 0; i < node.children.length; i++) {
		Condition c = node.conditions[i];

		if (c != null && dataset.attributes()[c.attribute]
			.getType() == CONTINUOUS) {
		    // if a condition was created, we substitute the threshold
		    // values with values that occur in the dataset (this is to
		    // avoid having threshold values that don't represent values
		    // from the dataset)

		    for (int j = 0; j < dataset.size(); j++) {
			double v = dataset.value(j, c.attribute);

			for (int k = 0; k < c.value.length; k++) {
			    if (v <= c.value[k] && v > c.threshold[k]) {
				c.threshold[k] = v;
			    }
			}
		    }

		    // at the end of this procedure, the threshold ad value
		    // should be the same
		    for (int k = 0; k < c.value.length; k++) {
			c.value[k] = c.threshold[k];
		    }
		}

		if (!node.children[i].isLeaf()) {
		    nodes.add((InternalNode) node.children[i]);
		}
	    }
	}
    }

    /**
     * Returns the string representation of the tree.
     * 
     * @param graph
     *            the constructions graph.
     * @param dataset
     *            the current dataset.
     * @param node
     *            the node representing the root of the (sub-)tree.
     * @param indent
     *            the current level identation.
     * 
     * @return the string representation of the tree.
     */
    private String toString(Dataset dataset, Node node, String indent) {
	StringBuffer buffer = new StringBuffer();

	if (!node.isLeaf()) {
	    InternalNode internal = (InternalNode) node;
	    if (node != root) {
		buffer.append(System.lineSeparator());
	    }

	    String name = node.getName();

	    for (int i = 0; i < internal.conditions.length; i++) {
		buffer.append(indent);
		Condition condition = internal.conditions[i];

		switch (condition.relation) {
		case LESS_THAN_OR_EQUAL_TO:
		    buffer.append(String
			    .format("%s <= %s",
				    name,
				    Double.toString(condition.value[0])));
		    break;

		case GREATER_THAN:
		    buffer.append(String
			    .format("%s > %s",
				    name,
				    Double.toString(condition.value[0])));
		    break;

		case IN_RANGE:
		    buffer.append(String
			    .format("%s < %s <= %s",
				    Double.toString(condition.value[0]),
				    name,
				    Double.toString(condition.value[1])));
		    break;

		case EQUAL_TO:
		    buffer.append(String
			    .format("%s = %s",
				    name,
				    dataset.attributes()[condition.attribute]
					    .value((int) condition.value[0])));
		    break;
		}

		buffer.append(": ");

		String next = indent + "|    ";
		buffer.append(toString(dataset, internal.children[i], next));
	    }
	} else {
	    buffer.append(node.getName());

	    double[] distribution = node.getDistribution();
	    int prediction = ((LeafNode) node).getPrediction();
	    double errors = 0.0;

	    for (int i = 0; i < distribution.length; i++) {
		if (i != prediction) {
		    errors += distribution[i];
		}
	    }

	    buffer.append(" (");
	    buffer.append(String.format("%.1f",
					distribution[prediction] + errors));

	    if (errors > 0.0) {
		buffer.append("/");
		buffer.append(String.format("%.1f", errors));
	    }

	    buffer.append(")");
	    buffer.append(System.lineSeparator());
	}

	return buffer.toString();
    }

    @Override
    public int compareTo(Tree o) {
	// compare the quality
	int c = Double.compare(quality, o.quality);

	if (c == 0) {
	    // compare the number of rules
	    c = Double.compare(o.size(), size());
	}

	return c;
    }
}