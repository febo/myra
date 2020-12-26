/*
 * BoundaryLaplaceSplit.java
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

package myra.classification.rule.unordered.attribute;

import static myra.classification.attribute.C45Split.PRECISION_10;
import static myra.datamining.Attribute.GREATER_THAN;
import static myra.datamining.Attribute.LESS_THAN_OR_EQUAL_TO;
import static myra.datamining.Dataset.RULE_COVERED;

import java.util.Arrays;

import myra.datamining.Dataset;
import myra.datamining.IntervalBuilder;
import myra.datamining.Attribute.Condition;
import myra.datamining.Dataset.Instance;

/**
 * This class creates discrete intervals based on the Laplace accuracy of the
 * intervals. This implementation only consider boundary threshold values
 * (values occuring between different class values).
 * 
 * @author Fernando Esteban Barril Otero
 * 
 * @see ClassAwareSplit
 */
public class BoundaryLaplaceSplit extends LaplaceSplit {
    /**
     * Returns attribute conditions representing the discrete intervals for the
     * specified attribute that have provide the higher Laplace accuracy.
     * 
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances flags.
     * @param attribute
     *            the index of the continuous attribute.
     * 
     * @return attribute conditions representing discrete intervals for the
     *         specified attribute.
     */
    @Override
    public Condition[] multiple(Dataset dataset,
                                Instance[] instances,
                                int attribute,
                                int target) {
        // (1) creates the pairing (value,class) for the uncovered examples

        Pair[] candidates = new Pair[dataset.size()];
        double[] frequency = new double[dataset.classLength()];

        int index = 0;
        double size = 0;

        for (int i = 0; i < dataset.size(); i++) {
            // the dynamc discretisation only considers the instances covered
            // by the current rule
            if (instances[i].flag == RULE_COVERED) {
                double v = dataset.value(i, attribute);

                if (!Double.isNaN(v)) {
                    Pair pair = new Pair();
                    pair.value = v;
                    pair.classValue = dataset.value(i, dataset.classIndex());
                    pair.weight = instances[i].weight;
                    candidates[index] = pair;

                    frequency[(int) pair.classValue] += pair.weight;
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

        // (2) determines the best threshold value

        final double minimum = IntervalBuilder.minimumCases(dataset, size);
        double accuracy = 0.0;

        // 0: lower interval condition
        // 1: upper interval condition
        Condition[] conditions = new Condition[2];

        for (int i = 0; i < conditions.length; i++) {
            conditions[i] = new Condition();
            conditions[i].attribute = attribute;
            conditions[i].relation = 0;
            conditions[i].frequency = new double[dataset.classLength()];
            conditions[i].length = 0;
        }

        double[] intervalFrequency = new double[dataset.classLength()];
        double intervalSize = 0;
        boolean[] evaluated = new boolean[candidates.length];

        for (int i = 1; i < candidates.length; i++) {
            double weight = candidates[i - 1].weight;

            intervalSize += weight;
            intervalFrequency[(int) candidates[i - 1].classValue] += weight;

            size -= weight;
            frequency[(int) candidates[i - 1].classValue] -= weight;

            if (candidates[i - 1].classValue != candidates[i].classValue) {
                if (candidates[i - 1].value == candidates[i].value) {
                    // skip backwards

                    double[] lowerFrequency = new double[dataset.classLength()];
                    System.arraycopy(intervalFrequency,
                                     0,
                                     lowerFrequency,
                                     0,
                                     lowerFrequency.length);
                    double lowerSize = intervalSize;

                    double[] upperFrequency = new double[dataset.classLength()];
                    System.arraycopy(frequency,
                                     0,
                                     upperFrequency,
                                     0,
                                     upperFrequency.length);
                    double upperSize = size;

                    int threshold = i;

                    while ((threshold > 1) && (candidates[threshold
                            - 1].value == candidates[threshold].value)) {
                        weight = candidates[threshold - 1].weight;

                        lowerSize -= weight;
                        lowerFrequency[(int) candidates[threshold
                                - 1].classValue] -= weight;

                        upperSize += weight;
                        upperFrequency[(int) candidates[threshold
                                - 1].classValue] += weight;

                        threshold--;
                    }

                    if (!evaluated[threshold - 1]
                            && (lowerSize + PRECISION_10 >= minimum)
                            && (upperSize + PRECISION_10 >= minimum)) {
                        evaluated[threshold - 1] = true;

                        accuracy = computeAccuracy(accuracy,
                                                   target,
                                                   dataset.classLength(),
                                                   threshold,
                                                   candidates,
                                                   conditions,
                                                   lowerFrequency,
                                                   lowerSize,
                                                   upperFrequency,
                                                   upperSize);
                    }

                    // skip forward

                    System.arraycopy(intervalFrequency,
                                     0,
                                     lowerFrequency,
                                     0,
                                     lowerFrequency.length);
                    lowerSize = intervalSize;

                    System.arraycopy(frequency,
                                     0,
                                     upperFrequency,
                                     0,
                                     upperFrequency.length);
                    upperSize = size;

                    threshold = i;

                    while ((threshold < candidates.length)
                            && (candidates[threshold
                                    - 1].value == candidates[threshold].value)) {
                        threshold++;
                        weight = candidates[threshold - 1].weight;

                        lowerSize += weight;
                        lowerFrequency[(int) candidates[threshold
                                - 1].classValue] += weight;

                        upperSize -= weight;
                        upperFrequency[(int) candidates[threshold
                                - 1].classValue] -= weight;
                    }

                    if (!evaluated[threshold - 1]
                            && (lowerSize + PRECISION_10 >= minimum)
                            && (upperSize + PRECISION_10 >= minimum)) {
                        evaluated[threshold - 1] = true;

                        accuracy = computeAccuracy(accuracy,
                                                   target,
                                                   dataset.classLength(),
                                                   threshold,
                                                   candidates,
                                                   conditions,
                                                   lowerFrequency,
                                                   lowerSize,
                                                   upperFrequency,
                                                   upperSize);
                    }
                }
                // the boundary point falls between two different values
                else if (!evaluated[i - 1]
                        && (intervalSize + PRECISION_10 >= minimum)
                        && (size + PRECISION_10 >= minimum)) {
                    evaluated[i - 1] = true;

                    accuracy = computeAccuracy(accuracy,
                                               target,
                                               dataset.classLength(),
                                               i,
                                               candidates,
                                               conditions,
                                               intervalFrequency,
                                               intervalSize,
                                               frequency,
                                               size);
                }
            }
        }

        if (conditions[0].relation == 0) {
            // a condition could not be created
            return null;
        }

        return conditions;
    }

    /**
     * Returns the accuracy of the candidate interval. This method computes the
     * Laplace accuracy of both lower and upper intervals and if it is better
     * the current best <code>accuracy</code>, it updates the
     * <code>conditions</code> array to store the new candidates.
     * 
     * @param accuracy
     *            the current best accuracy.
     * @param target
     *            the index of the target class value.
     * @param classLength
     *            the number of classes.
     * @param index
     *            the index of the threshold value.
     * @param candidates
     *            the candidate threshold values.
     * @param conditions
     *            the array of candidate conditions.
     * @param lowerFrequency
     *            the frequence of the lower interval.
     * @param lowerSize
     *            the size of the lower interval.
     * @param upperFrequency
     *            the frequency of the upper interval.
     * @param upperSize
     *            the size of the upper interval.
     * 
     * @return the accuracy of the candidate interval; if none of the intervals
     *         has a better accuracy than <code>accuracy</code>, it does not
     *         updated the <code>conditions</code> array and returns the same
     *         <code>accuracy</code> value.
     */
    private double computeAccuracy(double accuracy,
                                   int target,
                                   int classLength,
                                   int index,
                                   Pair[] candidates,
                                   Condition[] conditions,
                                   double[] lowerFrequency,
                                   double lowerSize,
                                   double[] upperFrequency,
                                   double upperSize) {
        double lower = (lowerFrequency[target] > 0)
                ? (lowerFrequency[target] + 1) / (lowerSize + classLength)
                : 0.0;

        double upper = (upperFrequency[target] > 0)
                ? (upperFrequency[target] + 1) / (upperSize + classLength)
                : 0.0;

        double intervalAccuracy = Math.max(lower, upper);

        if (((intervalAccuracy - accuracy) > 1e-15)
                || (lower > 0.0 && lower == accuracy
                        && lowerSize > conditions[0].length)
                || (upper > 0.0 && upper == accuracy
                        && upperSize > conditions[1].length)) {
            accuracy = intervalAccuracy;

            conditions[0].length = lowerSize;
            conditions[0].relation = LESS_THAN_OR_EQUAL_TO;
            conditions[0].entropy = lower;
            conditions[0].threshold[0] = candidates[index - 1].value;
            conditions[0].value[0] =
                    (candidates[index - 1].value + candidates[index].value)
                            / 2.0;
            System.arraycopy(lowerFrequency,
                             0,
                             conditions[0].frequency,
                             0,
                             classLength);

            conditions[1].length = upperSize;
            conditions[1].relation = GREATER_THAN;
            conditions[1].entropy = upper;
            conditions[1].threshold[0] = candidates[index - 1].value;
            conditions[1].value[0] =
                    (candidates[index - 1].value + candidates[index].value)
                            / 2.0;
            System.arraycopy(upperFrequency,
                             0,
                             conditions[1].frequency,
                             0,
                             classLength);
        }

        return accuracy;
    }
}