/*
 * Pruner.java
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
import static myra.rule.RuleFunction.DEFAULT_FUNCTION;

import myra.Config.ConfigKey;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;

/**
 * The <code>Pruner</code> should be implemented by classes that are responsible
 * of pruning (removing) irrelavent terms from the antecedent of a rule.
 * 
 * @author Fernando Esteban Barril Otero
 */
public abstract class Pruner {
    /**
     * The config key for the default pruning instance.
     */
    public final static ConfigKey<Pruner> DEFAULT_PRUNER =
            new ConfigKey<Pruner>();

    /**
     * Removes irrelevant terms from the antecedent of a rule. This method uses
     * the default rule evaluation function specified by
     * {@link RuleFunction#DEFAULT_FUNCTION}.
     * 
     * @param dataset
     *            the current dataset.
     * @param rule
     *            the rule to be pruned.
     * @param instances
     *            the instaces flag array.
     * 
     * @return the number of uncovered instances remaining.
     */
    public int prune(Dataset dataset, Rule rule, Instance[] instances) {
        return prune(dataset, rule, instances, CONFIG.get(DEFAULT_FUNCTION));
    }

    /**
     * Removes irrelevant terms from the antecedent of a rule.
     * 
     * @param dataset
     *            the current dataset.
     * @param rule
     *            the rule to be pruned.
     * @param instances
     *            the instaces flag array.
     * @param function
     *            the rule evaluation function.
     * 
     * @return the number of uncovered instances remaining.
     */
    public abstract int prune(Dataset dataset,
                              Rule rule,
                              Instance[] instances,
                              RuleFunction function);

    /**
     * A "no-pruner" procedure - i.e., it does not modify the rule.
     */
    public static class None extends Pruner {
        @Override
        public int prune(Dataset dataset,
                         Rule rule,
                         Instance[] instances,
                         RuleFunction function) {
            Assignator assignator = CONFIG.get(ASSIGNATOR);
            return assignator.assign(dataset, rule, instances);
        }
    }
}