/*
 * Pruner.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2021 Fernando Esteban Barril Otero
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

package myra.classification.rule.hierarchical;

import static myra.Config.CONFIG;
import static myra.datamining.Dataset.COVERED;
import static myra.datamining.Dataset.NOT_COVERED;
import static myra.datamining.Dataset.RULE_COVERED;
import static myra.datamining.IntervalBuilder.MINIMUM_CASES;
import static myra.rule.Assignator.ASSIGNATOR;

import java.util.ArrayList;

import myra.Cost;
import myra.datamining.Dataset;
import myra.datamining.Attribute.Condition;
import myra.datamining.Dataset.Instance;
import myra.rule.Assignator;
import myra.rule.Rule;
import myra.rule.Rule.Term;
import myra.rule.RuleFunction;

/**
 * A pruner for hierarchical classification rules.
 * 
 * @since 5.0
 */
public class Pruner extends myra.rule.Pruner {

    @Override
    public int prune(Dataset dataset,
                     Rule rule,
                     Instance[] instances,
                     RuleFunction function) {

        // (1) determines the covered instances of each term

        Term[] terms = rule.terms();
        Coverage[] coverage = new Coverage[terms.length];
        int start = 0;

        while (start < coverage.length) {
            for (int i = 0; i < dataset.size(); i++) {
                // only considers instances not covered
                if (instances[i].flag != COVERED) {
                    for (int j = start; j < coverage.length; j++) {
                        if (terms[j].isEnabeld()) {
                            Condition condition = terms[j].condition();
                            double v = dataset.value(i, condition.attribute);

                            if (condition.satisfies(v)) {
                                coverage[j].covered.add(i);
                            } else {
                                break;
                            }
                        }
                    }
                }
            }

            // checks that the first term of the rule cover the minimum
            // number of cases, otherwise disables it and repeat the
            // coverage of the rule
            if (coverage[start].total() < CONFIG.get(MINIMUM_CASES)) {
                terms[start].setEnabeld(false);
                start++;
                // reset coverage for all terms
                for (int j = 0; j < coverage.length; j++) {
                    coverage[j] = new Coverage();
                }
            } else {
                // when the rule covers the minimum number of cases, stop the
                // coverage test
                break;
            }
        }

        // (2) prunes the rule

        Assignator assignator = CONFIG.get(ASSIGNATOR);
        int selected = -1;
        Cost best = null;

        for (int i = start; i < coverage.length; i++) {
            if (coverage[i].total() >= CONFIG.get(MINIMUM_CASES)) {
                // reset the covered instances
                Instance.mark(instances, RULE_COVERED, NOT_COVERED);

                for (int index : coverage[i].covered) {
                    instances[index].flag = RULE_COVERED;
                }

                assignator.assign(dataset, rule, instances);

                // evaluate the rule
                Cost pruned = function.evaluate(dataset, rule, instances);

                if (best == null || pruned.compareTo(best) >= 0) {
                    best = pruned;
                    selected = i;
                }
            } else {
                // stops the procedure, since any remaining term will not
                // cover the minimum number
                break;
            }

        }

        // (3) disables and removes unused terms

        for (int i = selected + 1; i < terms.length; i++) {
            terms[i].setEnabeld(false);
        }

        rule.setQuality(best);
        rule.compact();
        rule.apply(dataset, instances);

        return assignator.assign(dataset, rule, instances);
    }

    /**
     * Class to store the coverage information of a term.
     * 
     * @author Fernando Esteban Barril Otero
     */
    private static class Coverage {
        /**
         * Covered instances information.
         */
        ArrayList<Integer> covered = new ArrayList<>();

        /**
         * Returns the total number of covered examples.
         * 
         * @return the total number of covered examples.
         */
        int total() {
            return covered.size();
        }
    }
}