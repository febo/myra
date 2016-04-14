/*
 * LeafNode.java
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

package myra.classification.tree;

import myra.classification.Label;

/**
 * This class represents a leaf node of the decision tree. Leaf nodes are
 * responsible for class predictions.
 * 
 * @author Fernando Esteban Barril Otero
 */
public final class LeafNode extends Node {
    /**
     * The label predicted by the leaf node.
     */
    private Label prediction;

    /**
     * Default constructor.
     * 
     * @param name
     *            the name of the node.
     * @param level
     *            the level of the node.
     * @param prediction
     *            the prediction of the node.
     */
    public LeafNode(String name, int level, Label prediction) {
	super(name, level);
	this.prediction = prediction;
    }

    @Override
    public boolean isLeaf() {
	return true;
    }

    /**
     * Returns the label predicted by the node.
     * 
     * @return the label predicted by the node.
     */
    public Label getPrediction() {
	return prediction;
    }

    /**
     * Sets the prediction of the node.
     * 
     * @param prediction
     *            the prediction to set.
     */
    public void setPrediction(Label prediction) {
	this.prediction = prediction;
    }

    @Override
    public void sort() {
    }
}