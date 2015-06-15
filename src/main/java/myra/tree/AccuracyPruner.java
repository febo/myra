/*
 * AccuracyPruner.java
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

import static myra.Dataset.RULE_COVERED;

import myra.Dataset;

/**
 * Accuracy based pruner.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class AccuracyPruner extends AbstractPruner {
    /**
     * Recursively prunes a node of the tree based on the predictive accuracy.
     */
    protected void prune(Dataset dataset,
			 Tree tree,
			 InternalNode node,
			 InternalNode parent,
			 final int index) {
	for (int i = 0; i < node.children.length; i++) {
	    if (!node.children[i].isLeaf()) {
		prune(dataset, tree, (InternalNode) node.children[i], node, i);
	    }
	}

	InternalNode internal = (InternalNode) node;
	double[] distribution = internal.getDistribution();
	final double total = TreeStats.total(distribution);

	// the (sub)tree accuracy
	double treeAcc = (total - TreeStats.error(internal)) / total;

	// the accuracy if the (sub)tree is replaced by a leaf node
	double leafAcc = (total - TreeStats.error(distribution)) / total;

	// the accuracy if the (sub)tree is replaced by its most used branch
	double branchAcc = 0;
	int frequent = internal.frequentBranch();
	InternalNode subtree = null;

	if (!internal.children[frequent].isLeaf()) {
	    subtree = (InternalNode) internal.children[frequent];
	    subtree.setCoverage(internal.getCoverage());
	    subtree.setDistribution(distribution);

	    recalculate(dataset, subtree);
	    branchAcc = (total - TreeStats.error(subtree)) / total;
	} else {
	    // if the frequent branch leads to a leaf node, we set the
	    // accuracy to be equal to the leaf accuracy; the subtree will
	    // be replaced by a leaf node if the leaf accuracy is greater
	    // than the (sub-)tree accuracy
	    branchAcc = leafAcc;
	}

	// checks if any of the above step leads to a better accuracy

	Node substitute = null;

	if (leafAcc >= treeAcc && leafAcc >= branchAcc) {
	    int prediction =
		    dataset.findMajority(internal.getCoverage(), RULE_COVERED);

	    substitute = new LeafNode(
				      dataset.attributes()[dataset.classIndex()]
					      .value(prediction),
				      internal.getLevel(),
				      prediction);
	    substitute.setDistribution(distribution);
	} else if (branchAcc >= treeAcc) {
	    substitute = subtree;
	}

	if (substitute != null) {
	    if (parent == null) {
		// we must be dealing with the root of the tree
		tree.setRoot(substitute);
	    } else {
		parent.children[index] = substitute;
		substitute.setLevel(internal.getLevel());

		if (!substitute.isLeaf()) {
		    // the distribution has changes, so we need to prune the
		    // (sub-)tree again
		    prune(dataset,
			  tree,
			  (InternalNode) substitute,
			  parent,
			  index);
		}
	    }
	} else if (!internal.children[frequent].isLeaf()) {
	    recalculate(dataset, internal);
	}
    }
}