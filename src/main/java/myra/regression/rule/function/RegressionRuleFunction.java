/*
 * RegressionRuleFunction.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2016 Fernando Esteban Barril Otero
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

package myra.regression.rule.function;

import myra.Cost;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.regression.rule.RegressionRule;
import myra.rule.Rule;
import myra.rule.RuleFunction;

/**
 * Base class for all regression rule quality functions.
 * 
 * @since 4.5
 * 
 * @author Fernando Esteban Barril Otero
 */
public abstract class RegressionRuleFunction extends RuleFunction {
    @Override
    public final Cost evaluate(Dataset dataset,
			       Rule rule,
			       Instance[] instances) {
	return evaluate(dataset, (RegressionRule) rule, instances);
    }

    /**
     * Evaluates the specified regression rule.
     * 
     * @param dataset
     *            the current dataset.
     * @param rule
     *            the rule to be evaluated.
     * @param instances
     *            the instaces flag array.
     * 
     * @return the cost of the rule.
     */
    public abstract Cost evaluate(Dataset dataset,
				  RegressionRule rule,
				  Instance[] instances);
}