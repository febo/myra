/*
 * Laplace.java
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
 * The <code>Laplace</code> class represents a rule quality function based on
 * the Laplace accuracy.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class Laplace extends RuleFunction {
    @Override
    public double evaluate(Rule rule) {
	int[] frequency = rule.covered();
	int predicted = rule.getConsequent();
	int total = 0;

	for (int i = 0; i < frequency.length; i++) {
	    total += frequency[i];
	}

	return (frequency[predicted] + 1) / (double) (total + frequency.length);
    }
}