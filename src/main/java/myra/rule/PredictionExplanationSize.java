/*
 * PredictionExplanationSize.java
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

import static myra.Config.CONFIG;
import static myra.classification.rule.unordered.ConflictResolution.FREQUENT_CLASS;
import static myra.rule.RuleSet.CONFLICT_RESOLUTION;

import myra.Cost;
import myra.Cost.Minimise;
import myra.classification.rule.ClassificationRule;
import myra.datamining.Dataset;

/**
 * This class calculates the prediction-explanation size of a list of rules.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class PredictionExplanationSize implements ListMeasure {

    @Override
    public Cost evaluate(Dataset dataset, RuleList list) {
        if (list instanceof RuleSet) {
            return average(dataset, (RuleSet) list);
        }

        return average(dataset, list);
    }

    /**
     * Returns the average number of terms of the predictions for the specified
     * <code>RuleList</code> object.
     * 
     * @param dataset
     *            the dataset to be classified.
     * @param list
     *            the list of rules.
     * 
     * @return the average number of terms of the predictions.
     */
    private Cost average(Dataset dataset, RuleList list) {
        double terms = 0.0;

        for (int i = 0; i < dataset.size(); i++) {
            boolean found = false;

            for (Rule rule : list.rules()) {
                terms += rule.size();

                if (rule.covers(dataset, i)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new RuntimeException("Example not covered.");
            }
        }

        return new Minimise(terms / dataset.size());
    }

    /**
     * Returns the average number of terms of the predictions for the specified
     * <code>UnorderedRuleList</code> object.
     * 
     * @param dataset
     *            the dataset to be classified.
     * @param set
     *            the set of rules (unordered list).
     * 
     * @return the average number of terms of the predictions.
     */
    private Cost average(Dataset dataset, RuleSet set) {
        double terms = 0.0;

        if (CONFIG.get(CONFLICT_RESOLUTION) == FREQUENT_CLASS) {
            for (int i = 0; i < dataset.size(); i++) {
                boolean found = false;

                for (Rule rule : set.rules()) {
                    if (!rule.isEmpty() && rule.covers(dataset, i)) {
                        terms += rule.size();
                        found = true;
                    }
                }

                if (!found && set.defaultRule() == null) {
                    throw new RuntimeException("Example not covered.");
                }
            }
        } else {
            for (int i = 0; i < dataset.size(); i++) {
                double best = Double.MIN_VALUE;
                double length = 0.0;

                for (ClassificationRule rule : (ClassificationRule[]) set
                        .rules()) {
                    if (!rule.isEmpty() && rule.covers(dataset, i)) {
                        int[] frequency = rule.covered();
                        int total = 0;

                        for (int j = 0; j < frequency.length; j++) {
                            total += frequency[j];
                        }

                        double laplace =
                                (frequency[rule.getConsequent().value()] + 1)
                                        / (double) (total
                                                + dataset.classLength());

                        if (laplace > best) {
                            best = laplace;
                            length = rule.size();
                        }
                    }
                }

                terms += length;
            }
        }

        return new Minimise(terms / dataset.size());
    }
}