/*
 * TreeStats.java
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
import myra.util.Stats;

/**
 * This class represents the C4.5's statistical procedures.
 * 
 * @author Fernando Esteban Barril Otero
 */
public final class TreeStats {
    /**
     * Returns the estimated error of the subtree represented by the specified
     * node.
     * 
     * @param node
     *            the node reference.
     * 
     * @return the predicted error of the subtree represented by the specified
     *         node.
     */
    public static final double estimated(Node node) {
	if (node.isLeaf()) {
	    return estimated(node.getDistribution(),
			     ((LeafNode) node).getPrediction());
	} else {
	    double error = 0;

	    for (Node child : ((InternalNode) node).children) {
		error += estimated(child);
	    }

	    return error;
	}
    }

    /**
     * Returns the estimated error of the distribution.
     * 
     * @param distribution
     *            the distribution of examples.
     * 
     * @return the predicted error of the distribution.
     */
    public static final double estimated(double[] distribution) {
	int majority = 0;

	for (int i = 1; i < distribution.length; i++) {
	    if (distribution[majority] < distribution[i]) {
		majority = i;
	    }
	}

	return estimated(distribution, new Label(majority));
    }

    /**
     * Returns the estimated error of the distribution given the specified class
     * prediction.
     * 
     * @param distribution
     *            the distribution of examples.
     * @param prediction
     *            the class prediction index.
     * 
     * @return the predicted error of the distribution.
     */
    public static final double estimated(double[] distribution,
					 Label prediction) {
	double error = 0;
	double total = 0;

	for (int i = 0; i < distribution.length; i++) {
	    if (i != prediction.value()) {
		error += distribution[i];
	    }

	    total += distribution[i];
	}

	return error + Stats.errors(total, error);
    }

    /**
     * Returns the error of the distribution.
     * 
     * @param distribution
     *            the distribution of examples.
     * 
     * @return the error of the distribution.
     */
    public static final double error(double[] distribution) {
	int majority = 0;

	for (int i = 1; i < distribution.length; i++) {
	    if (distribution[majority] < distribution[i]) {
		majority = i;
	    }
	}

	return error(distribution, new Label(majority));
    }

    /**
     * Returns the estimated error of the distribution given the specified class
     * prediction.
     * 
     * @param distribution
     *            the distribution of examples.
     * @param prediction
     *            the class prediction index.
     * 
     * @return the predicted error of the distribution.
     */
    public static final double error(double[] distribution, Label prediction) {
	double error = 0;

	for (int i = 0; i < distribution.length; i++) {
	    if (i != prediction.value()) {
		error += distribution[i];
	    }
	}

	return error;
    }

    /**
     * Returns the error of the subtree represented by the specified node.
     * 
     * @param node
     *            the node reference.
     * 
     * @return the error of the subtree represented by the specified node.
     */
    public static final double error(Node node) {
	if (node.isLeaf()) {
	    return error(node.getDistribution(),
			 ((LeafNode) node).getPrediction());
	} else {
	    double error = 0;

	    for (Node child : ((InternalNode) node).children) {
		error += error(child);
	    }

	    return error;
	}
    }

    /**
     * Returns the total of the distribution.
     * 
     * @param distribution
     *            the distribution of examples.
     * 
     * @return the total of the distribution.
     */
    public static final double total(double[] distribution) {
	double total = 0;

	for (int i = 0; i < distribution.length; i++) {
	    total += distribution[i];
	}

	return total;
    }
}