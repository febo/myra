/*
 * SensitivitySpecificity.java
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

package myra.rule.function;

import myra.rule.Rule;
import myra.rule.RuleFunction;

/**
 * The <code>SensitivitySpecificity</code> class represents a rule quality
 * function based on the Sensitivity and Specificity measure. The quality of a
 * rule is equivalent to <i>Sensitivity</i> x <i>Specificity</i>.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class SensitivitySpecificity extends RuleFunction {
    @Override
    public double evaluate(Rule rule) {
	BinaryConfusionMatrix m = fill(rule);

	double value = (m.TP / (m.TP + m.FN)) * (m.TN / (m.TN + m.FP));

	return Double.isNaN(value) ? 0.0 : value;
    }
}