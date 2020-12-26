/*
 * RuleList.java
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

package myra.rule;

import static myra.datamining.Dataset.NOT_COVERED;

import java.lang.reflect.Array;
import java.util.Arrays;

import myra.AbstractWeighable;
import myra.Config;
import myra.Cost;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.datamining.Model;
import myra.datamining.Prediction;

/**
 * This class represents a list of classification rules. When classifying a new
 * example, the rules are applying in a sequential order. Then, the first rule
 * to cover the example is used to make a prediction.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class RuleList extends AbstractWeighable<RuleList> implements Model {
    /**
     * The quality of the rule list.
     */
    protected Cost quality;

    /**
     * The list of rules.
     */
    protected Rule[] rules;

    /**
     * The iteration in which the list was created.
     */
    private int iteration;

    /**
     * Default constructor.
     */
    public RuleList() {
        rules = (Rule[]) Array.newInstance(Config.CONFIG.get(Rule.DEFAULT_RULE),
                                           0);
    }

    /**
     * Adds a rule to the list.
     * 
     * @param rule
     *            the rule to add.
     */
    public void add(Rule rule) {
        rules = Arrays.copyOf(rules, rules.length + 1);
        rules[rules.length - 1] = rule;
    }

    /**
     * Returns the list of rules.
     * 
     * @return the list of rules.
     */
    public final Rule[] rules() {
        return rules;
    }

    /**
     * Returns the number of rules.
     * 
     * @return the number of rules.
     */
    public final int size() {
        return rules.length;
    }

    /**
     * Returns <code>true</code> if the list of rules contains a empty rule
     * (default rule).
     * 
     * @return <code>true</code> if the list of rules contains a empty rule;
     *         <code>false</code> otherwise.
     */
    public boolean hasDefault() {
        for (int i = 0; i < rules.length; i++) {
            if (rules[i].terms().length == 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the default (empty antecedent) rule.
     * 
     * @return the default rule; <code>null</code> if the list does not have a
     *         default rule.
     */
    public Rule defaultRule() {
        for (int i = 0; i < rules.length; i++) {
            if (rules[i].terms().length == 0) {
                return rules[i];
            }
        }

        return null;
    }

    /**
     * Returns the quality of the rule list.
     * 
     * @return the quality of the rule list.
     */
    public Cost getQuality() {
        return quality;
    }

    /**
     * Sets the quality of the rule list.
     * 
     * @param quality
     *            the quality to set.
     */
    public void setQuality(Cost quality) {
        this.quality = quality;
    }

    /**
     * Returns the iteration in which the list was created.
     * 
     * @return the iteration in which the list was created.
     */
    public int getIteration() {
        return iteration;
    }

    /**
     * Sets the iteration in which the list was created.
     * 
     * @param iteration
     *            the iteration number.
     */
    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    /**
     * Applies the rule list to the specified dataset. This method will update
     * the coverage of each rule.
     * 
     * @param dataset
     *            the current dataset.
     *
     * @see Rule#apply(Dataset, Dataset.Instance[])
     */
    public void apply(Dataset dataset) {
        Instance[] instances = Instance.newArray(dataset.size());
        Instance.markAll(instances, NOT_COVERED);

        for (int i = 0; i < rules.length; i++) {
            if (rules[i].isEnabled()) {
                rules[i].apply(dataset, instances);
                Dataset.markCovered(instances);
            }
        }
    }

    /**
     * Removes rules that are not enabled from the rule list.
     */
    public void compact() {
        int position = 0;

        for (Rule rule : rules) {
            if (rule.isEnabled()) {
                position++;
            }
        }

        if (position != size()) {
            Rule[] compact = (Rule[]) Array
                    .newInstance(Config.CONFIG.get(Rule.DEFAULT_RULE),
                                 position);
            position = 0;

            for (int i = 0; i < rules.length; i++) {
                if (rules[i].isEnabled()) {
                    compact[position] = rules[i];
                    position++;
                }
            }

            rules = compact;
        }
    }

    /**
     * Returns the predicted class value of the specified instance.
     * 
     * @param dataset
     *            the current dataset.
     * @param instance
     *            the instance index.
     * 
     * @return the predicted class value of the specified instance.
     */
    public Prediction predict(Dataset dataset, int instance) {
        for (int i = 0; i < rules.length; i++) {
            if (rules[i].isEnabled() && rules[i].covers(dataset, instance)) {
                return rules[i].getConsequent();
            }
        }

        throw new IllegalArgumentException("Could not classify instance: "
                + instance);
    }

    /**
     * Returns the string representation of the rule list.
     * 
     * @param dataset
     *            the current dataset.
     * 
     * @return the string representation of the rule list.
     */
    public String toString(Dataset dataset) {
        StringBuffer buffer = new StringBuffer();
        double terms = 0.0;

        for (int i = 0; i < rules.length; i++) {
            if (i > 0) {
                buffer.append("\n");
            }

            buffer.append(rules[i].toString(dataset));
            terms += rules[i].size();
        }

        buffer.append(String.format("%n%nNumber of rules: %d%n", rules.length));
        buffer.append(String.format("Total number of terms: %.0f%n", terms));

        double mean = terms / rules.length;
        buffer.append(String.format("Average number of terms: %.2f%n", mean));

        if (quality != null) {
            buffer.append(String.format("List quality: %f%n", quality.raw()));
            buffer.append(String.format("List iteration: %d", iteration));
        }

        return buffer.toString();
    }

    @Override
    public String export(Dataset dataset) {
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < rules.length; i++) {
            if (i > 0) {
                buffer.append("\n");
            }

            buffer.append(rules[i].toString(dataset));
        }

        return buffer.toString();
    }

    @Override
    public int compareTo(RuleList o) {
        // (1) compare the quality

        int c = (quality == null ? 0 : quality.compareTo(o.quality));

        if (c == 0) {
            // (2) compare the number of rules

            c = Double.compare(o.size(), size());

            if (c == 0) {
                int total1 = 0;

                for (Rule rule : rules) {
                    total1 += rule.size();
                }

                int total2 = 0;

                for (Rule rule : o.rules) {
                    total2 += rule.size();
                }

                // (3) compare the total number of terms

                return Double.compare(total2, total1);
            }
        }

        return c;
    }
}