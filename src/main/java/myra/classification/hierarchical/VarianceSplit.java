/*
 * VarianceSplit.java
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

package myra.classification.hierarchical;

import static myra.classification.attribute.C45Split.DELTA;
import static myra.classification.attribute.C45Split.PRECISION_10;
import static myra.classification.attribute.C45Split.PRECISION_15;
import static myra.datamining.Attribute.GREATER_THAN;
import static myra.datamining.Attribute.LESS_THAN_OR_EQUAL_TO;
import static myra.datamining.Dataset.RULE_COVERED;

import java.util.Arrays;

import myra.datamining.Attribute;
import myra.datamining.Attribute.Condition;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.datamining.Hierarchy;
import myra.datamining.IntervalBuilder;

/**
 * This class creates discrete intervals based on variance of the class labels.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class VarianceSplit extends IntervalBuilder {
    @Override
    public Condition[] multiple(Dataset dataset,
                                Instance[] instances,
                                int attribute) {
        // (1) determines the weight of the labels

        Hierarchy hierarchy = dataset.getHierarchy();

        if (hierarchy == null) {
            throw new IllegalArgumentException("Hierarchical problem expected.");
        }

        double[] weight = new double[hierarchy.size()];
        Attribute target = dataset.getTarget();

        for (int i = 0; i < target.values().length; i++) {
            weight[i] = hierarchy.get(target.value(i)).getWeight();
        }

        // (2) creates the candidate threshold values

        Pair[] candidates = new Pair[dataset.size()];
        double[] frequency = new double[target.size()];

        int index = 0;
        double size = 0;

        for (int i = 0; i < dataset.size(); i++) {
            // the dynamic discretisation only considers the instances covered
            // by the current rule
            if (instances[i].flag == RULE_COVERED) {
                double v = dataset.value(i, attribute);

                if (!Double.isNaN(v)) {
                    Pair pair = new Pair();
                    pair.value = v;
                    pair.weight = instances[i].weight;
                    pair.active = dataset.label(i).active();

                    candidates[index] = pair;

                    for (int j = 0; j < pair.active.length; j++) {
                        if (pair.active[j]) {
                            frequency[j] += pair.weight;
                        }
                    }

                    size += pair.weight;
                    index++;
                }
            }
        }

        if (index == 0) {
            // there are no candidate threshold values
            return null;
        }

        candidates = Arrays.copyOf(candidates, index);
        Arrays.sort(candidates);

        // (3)

        Condition[] conditions =
                create(candidates,
                       0,
                       candidates.length,
                       frequency,
                       weight,
                       size,
                       IntervalBuilder.minimumCases(dataset, size));

        if (conditions == null) {
            // no interval was created
            return null;
        } else {
            for (Condition c : conditions) {
                c.attribute = attribute;
            }

            return conditions;
        }
    }

    @Override
    public Condition single(Dataset dataset,
                            Instance[] instances,
                            int attribute) {
        Condition[] conditions = multiple(dataset, instances, attribute);
        Condition best = null;

        if (conditions != null && conditions.length > 0) {
            for (Condition c : conditions) {
                if ((best == null) || (c.entropy < best.entropy)
                        || (c.entropy == best.entropy
                                && c.length > best.length)) {
                    best = c;
                }
            }
        }

        return best;
    }

    /**
     * Returns an attribute condition representing the best interval according
     * to the variance of the class labels.
     * 
     * @param candidates
     *            the array of candidate values.
     * @param start
     *            the start index on the candidates array.
     * @param end
     *            the end index on the candidates array.
     * @param frequency
     *            the class values frequency.
     * @param weight
     *            the class label weight (used in the variance calculation).
     * @param size
     *            the weighted size of the interval.
     * @param minimum
     *            the minimum interval size allowed.
     * 
     * @return an attribute condition representing the best interval according
     *         to the variance of the class labels.
     */
    protected Condition[] create(Pair[] candidates,
                                 int start,
                                 int end,
                                 double[] frequency,
                                 double[] weight,
                                 double size,
                                 double minimum) {
        // calculates the variance of the distribution

        double variance =
                variance(candidates, start, end, frequency, weight, size);

        // determines the best threshold value

        double gain = 0.0;

        // 0: lower interval condition
        // 1: upper interval condition
        Condition[] conditions = new Condition[2];

        for (int i = 0; i < conditions.length; i++) {
            conditions[i] = new Condition();
            conditions[i].relation = 0;
            conditions[i].frequency = new double[frequency.length];
        }

        final double total = size;
        double[] intervalFrequency = new double[frequency.length];
        double intervalSize = 0;
        int tries = 0;

        for (int i = (start + 1); i < end; i++) {
            double w = candidates[i - 1].weight;

            // adjusts the intervals' statistics

            intervalSize += w;
            size -= w;

            boolean[] active = candidates[i - 1].active;

            for (int j = 0; j < active.length; j++) {
                double value = candidates[i - 1].active[j] ? w : 0;
                intervalFrequency[j] += value;
                frequency[j] -= value;
            }

            if (candidates[i - 1].value + DELTA < candidates[i].value) {
                if ((intervalSize + PRECISION_10 >= minimum)
                        && (size + PRECISION_10 >= minimum)) {
                    tries++;

                    // compute the variance of the intervals

                    double lowerVariance = variance(candidates,
                                                    start,
                                                    i,
                                                    intervalFrequency,
                                                    weight,
                                                    intervalSize);

                    double upperVariance = variance(candidates,
                                                    i,
                                                    end,
                                                    frequency,
                                                    weight,
                                                    size);

                    // determines the gain of the split

                    double intervalGain =
                            variance - ((intervalSize / total) * lowerVariance)
                                    - ((size / total) * upperVariance);

                    if ((intervalGain - gain) > PRECISION_15
                            || ((intervalGain == gain) && (lowerVariance == 0.0
                                    || upperVariance == 0.0))) {
                        gain = intervalGain;

                        conditions[0].length = intervalSize;
                        conditions[0].relation = LESS_THAN_OR_EQUAL_TO;
                        conditions[0].entropy = lowerVariance;
                        conditions[0].index = i - 1;
                        conditions[0].threshold[0] = candidates[i - 1].value;
                        conditions[0].value[0] =
                                (candidates[i - 1].value + candidates[i].value)
                                        / 2.0;

                        conditions[1].length = size;
                        conditions[1].relation = GREATER_THAN;
                        conditions[1].entropy = upperVariance;
                        conditions[1].index = i - 1;
                        conditions[1].threshold[0] = candidates[i - 1].value;
                        conditions[1].value[0] =
                                (candidates[i - 1].value + candidates[i].value)
                                        / 2.0;
                    }
                }
            }
        }

        if (conditions[0].relation == 0) {
            // a condition could not be created
            return null;
        }

        conditions[0].tries = tries;
        conditions[1].tries = tries;

        return conditions;
    }

    /**
     * Returns the variance of the values between <code>start</code> and
     * <code>end</code>.
     * 
     * @param candidates
     *            the array of candidate values.
     * @param start
     *            the start index on the candidates array.
     * @param end
     *            the end index on the candidates array.
     * @param frequency
     *            the class values frequency.
     * @param weight
     *            the class label weight (used in the variance calculation).
     * @param size
     *            the weighted size of the interval.
     * 
     * @return the variance of the values between <code>start</code> and
     *         <code>end</code>.
     */
    private double variance(Pair[] candidates,
                            int start,
                            int end,
                            double[] frequency,
                            double[] weight,
                            double size) {
        double[] average = new double[frequency.length];

        for (int i = 0; i < frequency.length; i++) {
            average[i] = frequency[i] / size;
        }

        double variance = 0.0;

        for (int i = start; i < end; i++) {
            double distance = 0.0;

            for (int j = 0; j < candidates[i].active.length; j++) {
                double value =
                        (candidates[i].active[j] ? candidates[i].weight : 0)
                                - average[j];
                distance = weight[j] * (value * value);
            }

            variance += Math.sqrt(distance);
        }

        return variance / size;
    }

    /**
     * This class represents a (value,labels) pair.
     */
    public static class Pair extends IntervalBuilder.Pair {
        /**
         * Active labels information.
         */
        public boolean[] active;
    }
}