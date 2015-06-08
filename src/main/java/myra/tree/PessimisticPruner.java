/*
 * PessimisticPruner.java
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
 * C4.5 error based pruner.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class PessimisticPruner extends AbstractPruner {
    /**
     * Recursively prunes a node of the tree based on the reduced-error pruning
     * procedure of the C4.5 algorithm. The statistical test applied assume that
     * the data set used in the pruning is the training data set.
     * 
     * @param dataset
     *            dataset the data set used during the pruning
     * @param tree
     *            the tree undergoing pruning.
     * @param node
     *            the node undergoing pruning.
     */
    @Override
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

	// the (sub)tree error
	double treeError = TreeStats.estimated(internal);

	// the error if the (sub)tree is replaced by a leaf node
	double leafError = TreeStats.estimated(distribution);

	// the error if the (sub)tree is replaced by its most used
	// branch
	double branchError = 0;
	int frequent = internal.frequentBranch();
	InternalNode subtree = null;

	if (!internal.children[frequent].isLeaf()) {
	    subtree = (InternalNode) internal.children[frequent];
	    subtree.setCoverage(internal.getCoverage());
	    subtree.setDistribution(distribution);

	    recalculate(dataset, subtree);
	    branchError = TreeStats.estimated(subtree);
	} else {
	    // if the frequent branch leads to a leaf node, we set the
	    // error to be equal to the leaf error; the subtree will
	    // be replaced by a leaf node if the leaf error is lower
	    // than the (sub-)tree error
	    branchError = leafError;
	}

	// checks if any of the above step leads to a better error rate

	Node substitute = null;

	if (leafError <= (treeError + 0.1)
		&& leafError <= (branchError + 0.1)) {
	    int prediction =
		    dataset.findMajority(internal.getCoverage(), RULE_COVERED);

	    substitute = new LeafNode(
				      dataset.attributes()[dataset.classIndex()]
					      .value(prediction),
				      internal.getLevel(),
				      prediction);
	    substitute.setDistribution(distribution);
	} else if (branchError <= (treeError + 0.1)) {
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