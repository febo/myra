/*
 * SiftRules.java
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

package myra.classification.rule;

import static myra.datamining.Attribute.Type.CONTINUOUS;
import static myra.datamining.Attribute.Type.NOMINAL;
import static myra.datamining.Dataset.MISSING_VALUE_INDEX;
import static myra.datamining.Dataset.RULE_COVERED;

import myra.datamining.Attribute;
import myra.datamining.Dataset;
import myra.datamining.IntervalBuilder;
import myra.datamining.Attribute.Condition;
import myra.datamining.Dataset.Instance;
import myra.rule.Rule;
import myra.rule.RuleList;

/**
 * Class responsible to build an ordered list of rules from a collection of
 * rules. The order of the rules is determined by a procedure inspired by
 * C4.5rules algorithm.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class SiftRules {
    /**
     * The code length (bits) of each attribute.
     */
    private double[] attributeBits;

    /**
     * The code length (bits) of all attributes.
     */
    private double totalBits;

    /**
     * Returns a rule list given a set of rules.
     * 
     * @param dataset
     *            the current dataset.
     * @param rules
     *            the rules to organise as a list of rules.
     * 
     * @return a list of rules.
     */
    public RuleList create(Dataset dataset, Rule[] rules) {
        prepare(dataset);

        return null;
    }

    /**
     * Reads the dataset metadata.
     * 
     * @param dataset
     *            the current dataset.
     */
    private void prepare(Dataset dataset) {
        Attribute[] attributes = dataset.attributes();
        attributeBits = new double[attributes.length];
        double total = 0;
        // intances array used for the discretisation process
        Instance[] instances = Instance.newArray(dataset.size());
        Instance.markAll(instances, RULE_COVERED);

        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i].getType() == NOMINAL) {
                double[] frequency = new double[attributes[i].values().length];

                for (int j = 0; j < dataset.size(); j++) {
                    double v = dataset.value(j, i);

                    if (v != MISSING_VALUE_INDEX) {
                        frequency[(int) v]++;
                    }
                }

                double sum = 0;

                for (int j = 0; j < frequency.length; j++) {
                    if (frequency[j] > 0) {
                        sum += (frequency[j] / dataset.size())
                                * (log2(dataset.size()) - log2(frequency[j]));
                    }
                }

                attributeBits[i] = sum;
            } else if (attributes[i].getType() == CONTINUOUS) {
                Condition[] conditions = IntervalBuilder.singleton()
                        .multiple(dataset, instances, i);

                if (conditions == null) {
                    // no conditions could be created
                    attributeBits[i] = 0;
                } else {
                    // we only want the number of candidate thresholds
                    double size = conditions[0].tries;

                    attributeBits[i] = 1 + log2(size) / 2;
                }
            } else {
                // sanity check
                throw new RuntimeException("Invalid attribute type: "
                        + attributes[i].getType());
            }

            // accumulates the total
            total += attributeBits[i];
        }

        setTotalBits(0);

        for (int i = 0; i < attributes.length; i++) {
            if (attributeBits[i] > 0) {
                double p = attributeBits[i] / total;
                setTotalBits(getTotalBits() - p * log2(p));
            }
        }
    }

    /**
     * Returns the base 2 logarithm of the specified number.
     * 
     * @param number
     *            a double number.
     * 
     * @return the base 2 logarithm of the specified number.
     */
    private double log2(double number) {
        return Math.log(number) / Math.log(2.0);
    }

    /**
     * Returns the total number of bits.
     * 
     * @return the total number of bits.
     */
    public double getTotalBits() {
        return totalBits;
    }

    /**
     * Sets the total number of bits.
     * 
     * @param totalBits
     *            the value to set.
     */
    public void setTotalBits(double totalBits) {
        this.totalBits = totalBits;
    }
}