/*
 * GreedyBuilder.java
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

import static myra.Config.CONFIG;
import static myra.classification.attribute.C45Split.EPSILON;
import static myra.datamining.Dataset.NOT_COVERED;
import static myra.datamining.Dataset.RULE_COVERED;
import static myra.datamining.IntervalBuilder.MINIMUM_CASES;

import java.util.Arrays;

import myra.classification.Label;
import myra.datamining.Attribute;
import myra.datamining.Attribute.Condition;
import myra.datamining.Attribute.Type;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;

/**
 * This class represents a greedy decision tree builder similar to the C4.5
 * algorithm.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class TreeBuilder {
    /**
     * The initial level of the tree (the level of the root node).
     */
    public static final int INITIAL_LEVEL = 0;

    /**
     * Creates a decision tree.
     * 
     * @param graph
     *            the construction graph.
     * @param heuristic
     *            the heuristic values of the vertices of the graph.
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances information.
     * 
     * @return a decision tree.
     */
    public Tree build(Graph graph,
                      double[] heuristic,
                      Dataset dataset,
                      Instance[] instances) {
        boolean[] used = new boolean[graph.size()];
        Arrays.fill(used, false);

        return new Tree(follow(graph,
                               heuristic,
                               dataset,
                               instances,
                               used,
                               INITIAL_LEVEL,
                               null,
                               -1));
    }

    /**
     * Recusively creates a (sub-)tree.
     * 
     * @param graph
     *            the construction graph.
     * @param heuristic
     *            the heuristic values of the vertices of the graph.
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances information.
     * @param used
     *            array indicating the used attributes.
     * @param level
     *            the current level in the tree.
     * @param parent
     *            the parent node.
     * @param index
     *            the index of the branch condition being followed.
     * 
     * @return the node representing the root of the (sub-)tree.
     */
    protected Node follow(Graph graph,
                          double[] heuristic,
                          Dataset dataset,
                          Instance[] instances,
                          boolean[] used,
                          int level,
                          InternalNode parent,
                          final int index) {
        Attribute attribute = null;
        Condition[] conditions = null;

        // if all instances belong to the same class or we cannot select an
        // attribute to visit, we add a leaf node

        if ((diversity(dataset, instances) == 1)
                || ((attribute = select(graph,
                                        heuristic,
                                        dataset,
                                        used,
                                        instances,
                                        parent,
                                        index)) == null)
                || ((conditions = InternalNode.branch(graph,
                                                      dataset,
                                                      attribute,
                                                      instances)) == null)) {
            Label prediction =
                    new Label(dataset.getTarget(),
                              dataset.findMajority(instances, RULE_COVERED));

            LeafNode leaf =
                    new LeafNode(dataset.attributes()[dataset.classIndex()]
                            .value(prediction.value()), level, prediction);

            double[] distribution = new double[dataset.classLength()];

            for (int i = 0; i < dataset.size(); i++) {
                if (instances[i].flag == RULE_COVERED) {
                    distribution[(int) dataset
                            .value(i, dataset.classIndex())] +=
                                    instances[i].weight;
                }
            }

            leaf.setDistribution(distribution);

            return leaf;
        }

        InternalNode internal = new InternalNode(attribute, level, conditions);
        internal.setCoverage(instances);

        double[] overall = new double[dataset.classLength()];

        for (int i = 0; i < dataset.size(); i++) {
            if (instances[i].flag == RULE_COVERED) {
                overall[(int) dataset.value(i, dataset.classIndex())] +=
                        instances[i].weight;
            }
        }

        internal.setDistribution(overall);

        double[] count = new double[internal.conditions.length];
        Instance[][] split = new Instance[count.length][];
        // the frequency of class values of each branch
        double[][] distribution =
                new double[count.length][dataset.classLength()];

        for (int i = 0; i < count.length; i++) {
            split[i] = Instance.copyOf(instances);
            double total = 0.0;

            // determines the subset of instances that satisfy the branch
            // condition. this only considers the previously covered
            // instances, since the further we go donw the tree, the less
            // instances will be covered

            for (int j = 0; j < dataset.size(); j++) {
                if (instances[j].flag == RULE_COVERED) {
                    double v = dataset.value(j, attribute.getIndex());

                    if (internal.conditions[i].satisfies(v)) {
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
                        double weight =
                                instances[j].weight * (count[i] / total);
                        split[i][j].weight = weight;
                        missing += weight;

                        distribution[i][(int) dataset
                                .value(j, dataset.classIndex())] += weight;
                    }
                }
            }

            count[i] += missing;
        }

        int valid = 0;

        for (int i = 0; i < count.length; i++) {
            if (count[i] >= CONFIG.get(MINIMUM_CASES)) {
                valid++;
            }
        }

        // if at least 2 of the subsets contain the minimum number of cases
        // we continue growing the tree, otherwise we add a leaf node

        if (valid >= 2) {
            for (int i = 0; i < count.length; i++) {
                if (count[i] == 0) {
                    // add a leaf node predicting the mojority class of
                    // the parent node

                    Label prediction =
                            new Label(dataset.getTarget(),
                                      dataset.findMajority(instances,
                                                           RULE_COVERED));

                    internal.children[i] =
                            new LeafNode(dataset.attributes()[dataset
                                    .classIndex()].value(prediction.value()),
                                         level + 1,
                                         prediction);
                } else if (count[i] < (CONFIG.get(MINIMUM_CASES) * 2)) {
                    // add a leaf node predicting the majority class of
                    // the covered examples

                    Label prediction =
                            new Label(dataset.getTarget(),
                                      dataset.findMajority(split[i],
                                                           RULE_COVERED));

                    internal.children[i] =
                            new LeafNode(dataset.attributes()[dataset
                                    .classIndex()].value(prediction.value()),
                                         level + 1,
                                         prediction);
                } else {
                    // grows the sub-tree recursively

                    boolean[] expanded = used.clone();

                    if (attribute.getType() == Type.NOMINAL) {
                        expanded[attribute.getIndex()] = true;
                    }

                    internal.children[i] = follow(graph,
                                                  heuristic,
                                                  dataset,
                                                  split[i],
                                                  expanded,
                                                  level + 1,
                                                  internal,
                                                  i);

                    // checks if we are better off adding a leaf node

                    Label majority =
                            new Label(dataset.getTarget(),
                                      dataset.findMajority(split[i],
                                                           RULE_COVERED));

                    if (TreeStats.error(internal.children[i]) >= (count[i]
                            - distribution[i][majority.value()] - EPSILON)) {
                        internal.children[i] =
                                new LeafNode(dataset.attributes()[dataset
                                        .classIndex()].value(majority.value()),
                                             level + 1,
                                             majority);
                    }
                }

                internal.children[i].setDistribution(distribution[i]);
            }

            return internal;
        } else {
            // tries to select another attribute

            boolean[] expanded = used.clone();
            expanded[attribute.getIndex()] = true;

            return follow(graph,
                          heuristic,
                          dataset,
                          instances,
                          expanded,
                          level,
                          parent,
                          index);
        }
    }

    /**
     * Returns an attribute (vertex for the construction graph) to vist.
     * 
     * @param graph
     *            the construction graph.
     * @param heuristic
     *            the heuristic value of the vertices of the graph.
     * @param dataset
     *            the current dataset.
     * @param used
     *            the array of already used attributes.
     * @param instances
     *            indicates the available instances.
     * @param parent
     *            the reference to the parent node; <code>null</code> if this is
     *            the root node.
     * @param index
     *            the index of the branch condition being followed.
     * 
     * @return an attribute (vertex for the construction graph) to vist.
     */
    protected Attribute select(Graph graph,
                               double[] heuristic,
                               Dataset dataset,
                               boolean[] used,
                               Instance[] instances,
                               InternalNode parent,
                               final int index) {
        GainRatioHeuristic method = new GainRatioHeuristic();
        double ratio[] = method.compute(dataset, instances, used);

        double best = 0.0;
        int selected = -1;

        for (int i = 0; i < ratio.length; i++) {
            if (!used[i] && ratio[i] > best) {
                best = ratio[i];
                selected = i;
            }
        }

        return (selected == -1) ? null : dataset.attributes()[selected];
    }

    /**
     * Returns the number of different class values of the covered instances.
     * 
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the array of covered instances.
     * 
     * @return the number of different class values of the covered instances.
     */
    protected int diversity(Dataset dataset, Instance[] instances) {
        int[] frequency = new int[dataset.classLength()];

        for (int i = 0; i < dataset.size(); i++) {
            if (instances[i].flag == RULE_COVERED) {
                frequency[(int) dataset.value(i, dataset.classIndex())]++;
            }
        }

        int diversity = 0;

        for (int i = 0; i < frequency.length; i++) {
            if (frequency[i] > 0) {
                diversity++;
            }
        }

        return diversity;
    }
}