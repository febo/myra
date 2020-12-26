/*
 * AbstractEntropySplit.java
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

import static myra.datamining.Dataset.RULE_COVERED;

import java.util.Arrays;

import myra.datamining.Dataset;
import myra.datamining.IntervalBuilder;
import myra.datamining.Attribute.Condition;
import myra.datamining.Dataset.Instance;

/**
 * Base class for entropy-based <code>IntervalBuilder</code> implementations.
 * 
 * @author Fernando Esteban Barril Otero
 */
public abstract class AbstractEntropySplit extends IntervalBuilder {
    /**
     * Returns an array of conditions representing discrete intervals.
     * 
     * @param candidates
     *            the candidate values.
     * @param start
     *            the start index of the values to consider.
     * @param end
     *            the end index of the values to consider.
     * @param frequency
     *            the class distribution of the values.
     * @param size
     *            the weigthed size of the candidate values.
     * @param minimum
     *            the minimum size of an interval.
     * 
     * @return an array of conditions representing discrete intervals.
     */
    protected abstract Condition[] create(Pair[] candidates,
                                          int start,
                                          int end,
                                          double[] frequency,
                                          double size,
                                          double minimum);

    @Override
    public Condition[] multiple(Dataset dataset,
                                Instance[] instances,
                                int attribute) {
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

        Condition[] conditions =
                create(candidates,
                       0,
                       candidates.length,
                       frequency,
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

    /**
     * Returns a single discrete interval for the specified attribute. This
     * method uses the conditions created by
     * {@link #multiple(Dataset, Dataset.Instance[], int)} to choose the best
     * interval. The interval returned is the interval with the lowest entropy,
     * or when there are more than two intervals with the same entropy value,
     * the one that has more instances.
     * 
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances flags.
     * @param attribute
     *            the index of the continuous attribute.
     * 
     * @return the attribute condition representing a single discrete interval
     *         for the specified attribute.
     */
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
     * Returns the <i>base 2</i> logarithm of a <code>double</code> value.
     * 
     * @param value
     *            a value.
     * 
     * @return the <i>base 2</i> logarithm of <code>value</code>.
     */
    protected double log2(double value) {
        return Math.log(value) / Math.log(2.0);
    }
}