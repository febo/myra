/*
 * TopDownListPruner.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2017 Fernando Esteban Barril Otero
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
import static myra.datamining.Dataset.NOT_COVERED;
import static myra.datamining.IntervalBuilder.MINIMUM_CASES;
import static myra.rule.Assignator.ASSIGNATOR;
import static myra.rule.ListMeasure.DEFAULT_MEASURE;

import myra.Cost;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.rule.Rule.Term;

/**
 * A list pruner that starts by pruning the first (top) rule of the list.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class TopDownListPruner implements ListPruner {
    @Override
    public void prune(Dataset dataset, RuleList list) {
        ListMeasure measure = CONFIG.get(DEFAULT_MEASURE);
        // quality of the original list of rules
        Cost best = measure.evaluate(dataset, list);

        for (int i = 0; i < list.rules.length; i++) {
            while (list.rules[i].isEnabled() && !list.rules[i].isEmpty()) {
                // removes the last term of the rule
                Term last = list.rules[i].pop();

                if (list.rules[i].isEmpty()) {
                    list.rules[i].setEnabled(false);
                }

                // updated rule coverage
                update(dataset, list, i);
                // evaluates the list after modifying the rule
                Cost current = measure.evaluate(dataset, list);

                if (current.compareTo(best) >= 0) {
                    // updates the current best quality and continues to
                    // refine the rule
                    best = current;
                } else {
                    list.rules[i].push(last);
                    list.rules[i].setEnabled(true);
                    // if the quality decreases, reverts the change
                    update(dataset, list, i);

                    break;
                }
            }
        }

        list.compact();
    }

    /**
     * Updates the rule coverage of the list.
     * 
     * @param dataset
     *            the current dataset.
     * @param list
     *            the list of rules.
     * @param index
     *            the index of the current rule being updated.
     */
    private void update(Dataset dataset, RuleList list, int index) {
        Instance[] instances = Instance.newArray(dataset.size());
        Instance.markAll(instances, NOT_COVERED);

        for (int i = 0; i < list.rules.length; i++) {
            // we consider rules below the current one even if they are
            // disabled since their coverage might change
            if (list.rules[i].isEnabled() || i > index) {
                // updates the rule coverage
                int coverage = list.rules[i].apply(dataset, instances);

                if (coverage >= CONFIG.get(MINIMUM_CASES)) {
                    CONFIG.get(ASSIGNATOR)
                            .assign(dataset, list.rules[i], instances);
                    Dataset.markCovered(instances);
                    // in case this rule was disabled
                    list.rules[i].setEnabled(true);
                } else if (!list.rules[i].isEmpty()) {
                    list.rules[i].setEnabled(false);
                    // sanity check, we should not need to disable any
                    // rule above the current one
                    if (i <= index) {
                        throw new IllegalStateException("Invalid rule coverage during update: "
                                + "current rule " + index + ", disabled rule "
                                + i);
                    }
                }
            }
        }
    }
}