/*
 * C45Split.java
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

package myra.classification.attribute;

import static myra.datamining.Attribute.GREATER_THAN;
import static myra.datamining.Attribute.LESS_THAN_OR_EQUAL_TO;

import myra.datamining.Attribute.Condition;

/**
 * This class creates discrete intervals based on the entropy measure, similar
 * to the C4.5 algorithm. The interval returned is the one that has the lowest
 * entropy and, at the same time, provides the higher entropy gain in relation
 * to the distribution of values.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class C45Split extends AbstractEntropySplit {
    /*
     * These are constanst used in the original Quinlan C4.5 implementation.
     */

    /**
     * C4.5 epsilon.
     */
    public static final double EPSILON = 1E-3;

    /**
     * C4.5 correction.
     */
    public static final double DELTA = 1E-5;

    /**
     * Numeric precision adjustment <code>E-10</code>.
     */
    public static final double PRECISION_10 = 1E-10;

    /**
     * Numeric precision adjustment <code>E-15</code>.
     */
    public static final double PRECISION_15 = 1E-15;

    /**
     * Returns attribute conditions representing the discrete intervals for the
     * specified attribute that have provide the higher entropy gain in relation
     * to the distribution of values.
     * 
     * @param candidates
     *            the array of candidate values.
     * @param start
     *            the start index on the candidates array.
     * @param end
     *            the end index on the candidates array.
     * @param frequency
     *            the class values frequency.
     * @param size
     *            the weighted size of the interval.
     * @param minimum
     *            the minimum interval size allowed.
     * 
     * @return attribute conditions representing discrete intervals for the
     *         specified attribute.
     */
    @Override
    protected Condition[] create(Pair[] candidates,
                                 int start,
                                 int end,
                                 double[] frequency,
                                 double size,
                                 double minimum) {
        // calculates the entropy of the distribution

        double entropy = 0.0;

        for (int i = 0; i < frequency.length; i++) {
            if (frequency[i] > 0) {
                double p = frequency[i] / size;
                entropy -= (p * (Math.log(p) / Math.log(2.0)));
            }
        }

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

        double[] intervalFrequency = new double[frequency.length];
        double intervalSize = 0;
        double total = size;
        int tries = 0;

        for (int i = (start + 1); i < end; i++) {
            double weight = candidates[i - 1].weight;

            intervalSize += weight;
            intervalFrequency[(int) candidates[i - 1].classValue] += weight;

            size -= weight;
            frequency[(int) candidates[i - 1].classValue] -= weight;

            if (candidates[i - 1].value + DELTA < candidates[i].value) {
                if ((intervalSize + PRECISION_10 >= minimum)
                        && (size + PRECISION_10 >= minimum)) {
                    tries++;

                    // compute the entropy of the intervals

                    double lowerEntropy = 0.0;
                    double upperEntropy = 0.0;
                    int lowerDiversity = 0;
                    int upperDiversity = 0;

                    for (int j = 0; j < frequency.length; j++) {
                        if (frequency[j] > 0) {
                            double p = frequency[j] / size;
                            upperEntropy -= (p * log2(p));
                            upperDiversity++;
                        }

                        if (intervalFrequency[j] > 0) {
                            double p = intervalFrequency[j] / intervalSize;
                            lowerEntropy -= (p * log2(p));
                            lowerDiversity++;
                        }
                    }

                    // determines the gain of the split

                    double intervalGain =
                            entropy - ((intervalSize / total) * lowerEntropy)
                                    - ((size / total) * upperEntropy);

                    if ((intervalGain - gain) > PRECISION_15) {
                        gain = intervalGain;

                        conditions[0].length = intervalSize;
                        conditions[0].relation = LESS_THAN_OR_EQUAL_TO;
                        conditions[0].entropy = lowerEntropy;
                        conditions[0].diversity = lowerDiversity;
                        conditions[0].index = i - 1;
                        conditions[0].threshold[0] = candidates[i - 1].value;
                        conditions[0].value[0] =
                                (candidates[i - 1].value + candidates[i].value)
                                        / 2.0;
                        // copies the distribution frequency
                        System.arraycopy(intervalFrequency,
                                         0,
                                         conditions[0].frequency,
                                         0,
                                         intervalFrequency.length);

                        conditions[1].length = size;
                        conditions[1].relation = GREATER_THAN;
                        conditions[1].entropy = upperEntropy;
                        conditions[1].diversity = upperDiversity;
                        conditions[1].index = i - 1;
                        conditions[1].threshold[0] = candidates[i - 1].value;
                        conditions[1].value[0] =
                                (candidates[i - 1].value + candidates[i].value)
                                        / 2.0;
                        // copies the distribution frequency
                        System.arraycopy(frequency,
                                         0,
                                         conditions[1].frequency,
                                         0,
                                         frequency.length);
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
}