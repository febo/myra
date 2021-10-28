/*
 * VarianceHeuristic.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2020 Fernando Esteban Barril Otero
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

package myra.classification.hierarchical;

import static myra.datamining.Attribute.Type.CONTINUOUS;
import static myra.datamining.Attribute.Type.NOMINAL;
import static myra.datamining.Dataset.RULE_COVERED;
import static myra.rule.Graph.START_INDEX;

import java.util.Arrays;

import myra.datamining.Attribute;
import myra.datamining.Attribute.Condition;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.datamining.IntervalBuilder;
import myra.rule.Graph;
import myra.rule.Graph.Entry;
import myra.rule.Heuristic;

/**
 * Heuristic information based on the distribution of class labels.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class VarianceHeuristic implements Heuristic {
    @Override
    public Entry[] compute(Graph graph, Dataset dataset, Instance[] instances) {
        return compute(graph, dataset, instances, new boolean[0]);
    }

    @Override
    public Entry[] compute(Graph graph,
                           Dataset dataset,
                           Instance[] instances,
                           boolean[] used) {
        double[] weight = new double[dataset.getHierarchy().size()];
        Attribute target = dataset.getTarget();

        for (int i = 0; i < target.values().length; i++) {
            weight[i] = dataset.getHierarchy().get(target.value(i)).getWeight();
        }

        Entry[] heuristic = Entry.initialise(new Entry[graph.size()]);

        if (graph.vertices()[START_INDEX].attribute == -1) {
            // we are dealing with a virtual starting vertex
            heuristic[START_INDEX].set(0, 0.0);
        }

        boolean[] available = new boolean[dataset.attributes().length];
        Arrays.fill(available, true);

        // determines the available attributes

        for (int i = 0; i < used.length; i++) {
            if (used[i]) {
                heuristic[i].set(0, 0.0);

                int index = graph.vertices()[i].attribute;

                if (index != -1) {
                    available[index] = false;
                }
            }
        }

        // computes the heuristic of the vertices representing available
        // attributes

        final double total = total(dataset, instances, weight);

        // if the variance of the set is 0, there is no point in adding more
        // terms to the rule; therefore, heuristic is set to 0 on all remaining
        // vertices
        if (total == 0) {
            for (int i = 0; i < available.length; i++) {
                if (available[i] && i != dataset.classIndex()) {
                    Attribute attribute = dataset.attributes()[i];

                    if (attribute.getType() == NOMINAL) {
                        for (int j = 0; j < attribute.size(); j++) {
                            int vertex = graph.indexOf(attribute.getIndex(), j);
                            heuristic[vertex].set(0, 0.0);
                        }
                    } else if (attribute.getType() == CONTINUOUS) {
                        int vertex = graph.indexOf(attribute.getIndex());
                        heuristic[vertex].set(0, 0.0);
                    }
                }
            }
        } else {
            for (int i = 0; i < available.length; i++) {
                if (available[i]) {
                    Attribute attribute = dataset.attributes()[i];

                    if (attribute.getType() == NOMINAL
                            && i != dataset.classIndex()) {
                        double[][] average = new double[attribute
                                .size()][dataset.getHierarchy().size()];
                        int[] counter = new int[attribute.size()];

                        // (1) computer the average values for the hierarchy
                        // values for each value of the attribute

                        for (int j = 0; j < dataset.size(); j++) {
                            if (instances[j].flag == RULE_COVERED) {
                                double v =
                                        dataset.value(j, attribute.getIndex());

                                if (v != Dataset.MISSING_VALUE_INDEX) {
                                    boolean[] active =
                                            dataset.label(i).active();

                                    for (int k = 0; k < active.length; k++) {
                                        average[(int) v][k] +=
                                                active[k] ? instances[i].weight
                                                        : 0;
                                    }

                                    counter[(int) v] += instances[i].weight;
                                }
                            }
                        }

                        for (int j = 0; j < average.length; j++) {
                            for (int k = 0; k < average[j].length; k++) {
                                average[j][k] /= counter[j];
                            }
                        }

                        // (2) calculates the variance of each term (attribute,
                        // value)

                        double[] variance = new double[attribute.size()];

                        for (int j = 0; j < dataset.size(); j++) {
                            if (instances[j].flag == RULE_COVERED) {
                                double v =
                                        dataset.value(j, attribute.getIndex());

                                if (v != Dataset.MISSING_VALUE_INDEX) {
                                    boolean[] active =
                                            dataset.label(i).active();
                                    double distance = 0.0;

                                    for (int k = 0; k < active.length; k++) {
                                        double value =
                                                (active[k] ? instances[j].weight
                                                        : 0)
                                                        - average[(int) v][k];
                                        distance += weight[k] * (value * value);
                                    }
                                    // the sqrt of the distance function cancels
                                    // out the sq of the variance
                                    variance[(int) v] += distance;
                                }
                            }
                        }

                        for (int j = 0; j < attribute.size(); j++) {
                            int vertex = graph.indexOf(attribute.getIndex(), j);

                            if (counter[j] == 0) {
                                heuristic[vertex].set(0, 0.0);
                            } else {
                                double value =
                                        (total - (variance[j] / counter[j]))
                                                / total;
                                // negative values indicate that the split has a
                                // worst variance than the original set;
                                // therefore we set the heuristic to 0 to avoid
                                // selecting the term
                                heuristic[vertex]
                                        .set(0, value > 0.0 ? value : 0.0);
                            }
                        }
                    }
                    // continuous attributes
                    else if (attribute.getType() == CONTINUOUS) {
                        Condition condition = IntervalBuilder.singleton()
                                .single(dataset,
                                        instances,
                                        attribute.getIndex());
                        int vertex = graph.indexOf(attribute.getIndex());

                        if (condition == null) {
                            heuristic[vertex].set(0, 0.0);
                        } else {
                            double value = (total - condition.entropy) / total;
                            // negative values indicate that the split has a
                            // worst variance than the original set; therefore
                            // we set the heuristic to 0 to avoid selecting
                            // the term
                            heuristic[vertex].set(0, value > 0.0 ? value : 0.0);
                        }
                    }
                }
            }
        }

        return heuristic;
    }

    @Override
    public Entry[] compute(Graph graph,
                           Dataset dataset,
                           Instance[] instances,
                           int target) {
        return compute(graph, dataset, instances, new boolean[0], target);
    }

    @Override
    public Entry[] compute(Graph graph,
                           Dataset dataset,
                           Instance[] instances,
                           boolean[] used,
                           int target) {
        return compute(graph, dataset, instances, used);
    }

    /**
     * Returns the total variance of the dataset. This value is used as the
     * highest (worst) variance value for the heuristic calculation.
     * 
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances flag.
     * 
     * @return the total variance of the dataset.
     */
    private double total(Dataset dataset,
                         Instance[] instances,
                         double[] weight) {
        double[] frequency = new double[weight.length];
        double size = 0.0;

        for (int i = 0; i < dataset.size(); i++) {
            if (instances[i].flag == RULE_COVERED) {
                boolean[] active = dataset.label(i).active();

                for (int j = 0; j < active.length; j++) {
                    frequency[j] += active[j] ? instances[i].weight : 0;
                }

                size += instances[i].weight;
            }
        }

        for (int i = 0; i < frequency.length; i++) {
            frequency[i] = frequency[i] / size;
        }

        double variance = 0.0;

        for (int i = 0; i < dataset.size(); i++) {
            if (instances[i].flag == RULE_COVERED) {
                boolean[] active = dataset.label(i).active();
                double distance = 0.0;

                for (int j = 0; j < active.length; j++) {
                    double value = (active[j] ? instances[i].weight : 0)
                            - frequency[j];
                    distance += weight[j] * (value * value);
                }
                // the sqrt of the distance function cancels out the sq of the
                // variance
                variance += distance;
            }
        }

        return variance / size;
    }
}