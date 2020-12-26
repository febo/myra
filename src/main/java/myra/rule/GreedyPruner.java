/*
 * GreedyPruner.java
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
import static myra.rule.Assignator.ASSIGNATOR;

import myra.Cost;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.rule.Rule.Term;

/**
 * This class represents a greedy prune procedure, which removes
 * one-term-at-a-time term of the rule until the rule quality decreases.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class GreedyPruner extends Pruner {
    @Override
    public int prune(Dataset dataset,
                     Rule rule,
                     Instance[] instances,
                     RuleFunction function) {
        Assignator assignator = CONFIG.get(ASSIGNATOR);
        assignator.assign(dataset, rule, instances);

        Cost best = function.evaluate(dataset, rule, instances);

        while (rule.size() > 1) {
            Term[] terms = rule.terms();
            int irrelevant = -1;

            for (int i = 0; i < terms.length; i++) {
                terms[i].setEnabeld(false);

                rule.apply(dataset, instances);
                assignator.assign(dataset, rule, instances);

                Cost current = function.evaluate(dataset, rule, instances);

                if (current.compareTo(best) >= 0) {
                    best = current;
                    irrelevant = i;
                }

                terms[i].setEnabeld(true);
            }

            if (irrelevant == -1) {
                // we did not find any improvements
                break;
            } else {
                // permanently remove the term
                terms[irrelevant].setEnabeld(false);
                rule.compact();
            }
        }

        rule.apply(dataset, instances);
        return assignator.assign(dataset, rule, instances);
    }
}