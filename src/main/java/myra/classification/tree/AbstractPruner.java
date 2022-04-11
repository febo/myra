/*
 * AbstractPruner.java
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

import static myra.datamining.Dataset.NOT_COVERED;
import static myra.datamining.Dataset.RULE_COVERED;

import myra.Config.ConfigKey;
import myra.classification.Label;
import myra.datamining.Attribute;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;

/**
 * Base class for tree pruner procedures.
 * 
 * @author Fernando Esteban Barril Otero
 */
public abstract class AbstractPruner {
    /**
     * The config key to indicate if the default pruner to use.
     */
    public static final ConfigKey<AbstractPruner> DEFAULT_PRUNER =
            new ConfigKey<>();

    /**
     * Prunes the tree.
     * 
     * @param dataset
     *            the data set used during the pruning.
     * @param tree
     *            the tree to be pruned.
     *
     * @return the pruned tree.
     */
    public Tree prune(Dataset dataset, Tree tree) {
        if (!tree.getRoot().isLeaf()) {
            prune(dataset, tree, (InternalNode) tree.getRoot(), null, -1);
        }

        return tree;
    }

    /**
     * Recursively prunes a node of the tree.
     * 
     * @param dataset
     *            dataset the data set used during the pruning
     * @param tree
     *            the tree undergoing pruning.
     * @param node
     *            the node undergoing pruning.
     * @param parent
     *            the parent node.
     * @param index
     *            the index of <code>node</code> in the parent's array.
     */
    protected abstract void prune(Dataset dataset,
                                  Tree tree,
                                  InternalNode node,
                                  InternalNode parent,
                                  final int index);

    /**
     * Recalculates the coverage of the specified subtree.
     * 
     * @param dataset
     *            the current dataset.
     * @param subtree
     *            the root node of the subtree.
     */
    protected void recalculate(Dataset dataset, InternalNode subtree) {
        Instance[] instances = subtree.getCoverage();
        Instance[][] split = new Instance[subtree.conditions.length][];

        double[] count = new double[subtree.conditions.length];
        double[][] distribution =
                new double[split.length][dataset.classLength()];

        for (int i = 0; i < split.length; i++) {
            int index = subtree.conditions[i].attribute;
            Attribute attribute = dataset.attributes()[index];

            split[i] = Instance.copyOf(instances);
            double total = 0.0;

            // determines the subset of instances that satisfy the branch
            // condition. this only consideres the previously covered
            // instances, since the further we go donw the tree, the less
            // instances will be covered

            for (int j = 0; j < dataset.size(); j++) {
                if (instances[j].flag == RULE_COVERED) {
                    double v = dataset.value(j, index);

                    if (subtree.conditions[i].satisfies(v)) {
                        count[i] += instances[j].weight;
                        total += instances[j].weight;

                        distribution[i][(int) dataset
                                .value(j, dataset.classIndex())] +=
                                        instances[j].weight;
                    } else if (!dataset.isMissing(attribute, v)) {
                        split[i][j].flag = NOT_COVERED;
                        total += instances[j].weight;
                    }
                }
            }

            double missing = 0;

            for (int j = 0; j < dataset.size(); j++) {
                if (instances[j].flag == RULE_COVERED) {
                    double v = dataset.value(j, attribute.getIndex());

                    if (dataset.isMissing(attribute, v)) {
                        double weight = instances[j].weight
                                * (Double.isNaN(count[i] / total) ? 0.0
                                        : (count[i] / total));
                        split[i][j].weight = weight;
                        missing += weight;

                        distribution[i][(int) dataset
                                .value(j, dataset.classIndex())] += weight;
                    }
                }
            }

            count[i] += missing;
        }

        for (int i = 0; i < subtree.children.length; i++) {
            subtree.children[i].setDistribution(distribution[i]);

            if (!subtree.children[i].isLeaf()) {
                InternalNode next = (InternalNode) subtree.children[i];
                next.setCoverage(split[i]);

                recalculate(dataset, next);
            } else {
                LeafNode leaf = (LeafNode) subtree.children[i];

                if (count[i] == 0) {
                    leaf.setPrediction(new Label(dataset.getTarget(),
                                                 dataset.findMajority(instances,
                                                                      RULE_COVERED)));
                } else {
                    leaf.setPrediction(new Label(dataset.getTarget(),
                                                 dataset.findMajority(split[i],
                                                                      RULE_COVERED)));
                }

                leaf.setName(dataset.attributes()[dataset.classIndex()]
                        .value(leaf.getPrediction().value()));
            }
        }
    }

    /**
     * A <i>no pruner</i> procedure.
     * 
     * @author Fernando Esteban Barril Otero
     */
    public static class None extends AbstractPruner {
        @Override
        protected void prune(Dataset dataset,
                             Tree tree,
                             InternalNode node,
                             InternalNode parent,
                             int index) {
            // this is a 'no' pruner operator
        }
    }
}